package com.dutycode.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;

import com.dutycode.bean.UserBean;

/**
 * 用户操作服务类
 * 包括:<br/>
 * 注册用户<br/>
 * 添加好友<br/>
 * 修改用户密码<br/>
 * @author michael
 *
 */
public class UserOperateService {
	//这里不需要关注connection是否初始化，交由注册时点击按钮时初始化
	private XMPPConnection conncetion = (XMPPConnection) ClientConServer.connection;
	
	private AccountManager accountmanger = conncetion.getAccountManager();
	
	private Roster roster = conncetion.getRoster();
	
	/**
	 * 注册新用户
	 * @param _username 用户名
	 * @param _password 密码
	 * @param attributes 附加值，比如邮箱等
	 * @return 注册是否成功
	 */
	public boolean regAccount(String _username, String _password, Map<String,String> attributes){
		boolean regmsg = false;//注册消息返回信息，用于显示给用户的提示
		
		//这里有点疑惑，这里使用AccountManger中的createAccount方法和使用Registration的区别是什么
		try {
			accountmanger.createAccount(_username, _password, attributes);
			regmsg = true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
		return regmsg;
	}
	
	/**
	 * 修改密码
	 * @param _newpassword 新密码
	 * @return 修改成功 true， 失败false
	 */
	public boolean changePassword(String _newpassword){
		boolean isChangeOK = false;
		try {
			accountmanger.changePassword(_newpassword);
			isChangeOK = true;
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isChangeOK;
	}
	
	/**
	 * 设置好友请求方式<br/>
	 * 1、全部允许 accept_all<br/>
	 * 2、手动处理 manual<br/>
	 * 3、全部拒绝 reject_all<br/>
	 * @param _mode
	 */
	public void setSubScriptionMode(Roster.SubscriptionMode _mode){
		
		roster.setSubscriptionMode(_mode);
	}
	
	/**
	 * 查询用户信息
	 * @param _username 用户名
	 * @return
	 * @throws XMPPException 
	 */
	public List<UserBean> searchUser(String _username) throws XMPPException{
		
		List<UserBean> searchRes = new ArrayList<UserBean>();
		UserSearchManager userSearchManger = new UserSearchManager(conncetion);
		
		Form searchForm = userSearchManger.getSearchForm("search." + conncetion.getServiceName());
		
		Form answerForm = searchForm.createAnswerForm();

		answerForm.setAnswer("Username", true);  
        answerForm.setAnswer("search", _username);  
		
		ReportedData resData = userSearchManger.getSearchResults(answerForm, "search." + conncetion.getServiceName());
		
		Iterator<Row> it = resData.getRows();
	
		Row row = null;
		UserBean user = null;
		while (it.hasNext()){
			user = new UserBean();
			row = it.next();
			user.setUserName(row.getValues("Username").next().toString());
			user.setUserJID(row.getValues("jid").next().toString());
			user.setEmail(row.getValues("Email").next().toString());
			user.setName(row.getValues("Name").next().toString());
			searchRes.add(user);
		}
		return searchRes;
	}
	
	/**
	 * 添加分组
	 * @param _groupname 分组名称
	 * @return 创建成功返回true，失败返回false
	 */
	public boolean addGroup(String _groupname){
		try{
			roster.createGroup(_groupname);
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	
	}
	
	/**
	 * 获取所有分组
	 * @return 分组列表
	 */
	public List<RosterGroup> getAllGroup(){
		List<RosterGroup> grouplist = new ArrayList<RosterGroup>();  
        Collection<RosterGroup> rosterGroup = roster.getGroups();  
        Iterator<RosterGroup> i = rosterGroup.iterator();  
        while (i.hasNext()) {
            grouplist.add(i.next());  
        }  
        return grouplist;  

	}
	/**
	 * 得到所有分组的名字
	 * @return
	 */
	public List<Object> getAllGroupName(){
		List<RosterGroup> grouplist = this.getAllGroup();
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < grouplist.size(); i++){
			list.add(grouplist.get(i).getName());
		}
		return list;
	}
	
	/**
	 * 添加好友，默认添加到好友列表中
	 * @param _friendJIDname like a@michael-pc
	 * @param _friendNickName nickname
	 * @return
	 */
	public boolean addNewFriend(String _friendJIDname, String _friendNickName){
		try {
			roster.createEntry(_friendJIDname, _friendNickName, null);
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 添加好友到组中
	 * @param _friendJIDname
	 * @param _friendNickName
	 * @param _groupName
	 * @return
	 */
	public boolean addNewFriend(String _friendJIDname, String _friendNickName, String _groupName){
		try {
			roster.createEntry("v@michael-pc", "v", new String[] {"好友"});
//			roster.createEntry(_friendJIDname, _friendNickName, new String[] {_groupName});
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 删除好友
	 * @param _userName 用户名 eg: a@michae-pc
	 * @return 删除结果 成功返回true，失败返回false
	 */
	public boolean deleteFriend(String _userName){
		
		try {
			
			RosterEntry rosterEntity = roster.getEntry(_userName);
			roster.removeEntry(rosterEntity);
			return true;
		}catch(XMPPException e){
			e.printStackTrace();
			return false;
		}
	}
	
	
	

}
