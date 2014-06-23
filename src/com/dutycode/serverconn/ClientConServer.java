package com.dutycode.serverconn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.util.Log;

/**
 * 连接openfire服务器类
 * 负责连接Openfire
 * @author Michael.Zhang
 * time: 2013-3-22 下午3:32:36
 * 
 */
public class ClientConServer {
	

	private Context context;
	
	boolean isOnline = false;//标志，用来标示用户是否在线，初始为不在线
	
	private static XMPPConnection connection ; //声明XMPPconnection对象，将在login方法中进行初始化
	
	/**
	 * 构造方法
	 * @param _context
	 */
	public ClientConServer(Context _context){
		this.context = _context;
	}

	public ClientConServer(){}
	
	
	/**
	 * 获取连接
	 * @param _serverIp 服务器IP地址
	 * @param _serverPort 服务器端口
	 * @return
	 */
	private XMPPConnection getConnection(String _serverIp, int _serverPort){
		ConnectionConfiguration config = new ConnectionConfiguration(_serverIp, _serverPort);
		/* 是否启用安全验证 */
		config.setSASLAuthenticationEnabled(false);
		/*是否启用调试模式*/
//		config.setDebuggerEnabled(true);
		
		/*创建Connection链接*/
		XMPPConnection connection = new XMPPConnection(config);
		return connection;
	}
	
	/**
	 * 登陆
	 * @param _username 用户名
	 * @param _password 密码
	 * @param _serverIp 服务器IP地址
	 * @param _serverPort 服务器端口号 
	 * @return
	 */
	public boolean login(String _username, String _password, String _serverIp, int _serverPort){
		
		//初始化connection对象
		connection = getConnection(_serverIp, _serverPort);
		
		try{
			connection.connect();
			connection.login(_username, _password);
			
			return true;
		}catch (XMPPException e){
			e.printStackTrace();
		}
		
		return false;
		
	}
	

	
	/**
	 * 退出登陆，即断开与服务器的链接
	 * @return 退出结果
	 */
	public boolean logoff(){
		
		if (connection.isConnected()){
			connection.disconnect();
			return true;
		}else {
			return false;//用户没有登录，无须断开
		}
		
	}
	
	/**
	 * 得到好友列表
	 * @return 好友列表
	 */
	public Map<String,List<Object>> getUserList(){
		
		Roster roster = connection.getRoster();
		Collection<RosterGroup> entriesGroup = roster.getGroups(); 
		
		
		Map<String,List<Object>>  map = new HashMap<String,List<Object>>();
		List<Object> listGroup = new ArrayList<Object>();
		List<Object> listGroupMember = new ArrayList<Object>();
		for(RosterGroup group: entriesGroup){  
            Collection<RosterEntry> entries = group.getEntries();  
            Log.i("---", group.getName());
            listGroup.add(group.getName());
            List<Object> groupMemb = new ArrayList<Object>();
            for (RosterEntry entry : entries) {
            	groupMemb.add(entry.getName());
//                Presence presence = roster.getPresence(entry.getUser());   
//                System.out.println("===P:"+ presence.getPacketID() + " " + presence.getStatus());
                //Log.i("---", "user: "+entry.getUser());   
//                Log.i("---", "name: "+entry.getName() + "  ----  " + presence.isAvailable());
                //Log.i("---", "tyep: "+entry.getType());   
                //Log.i("---", "status: "+entry.getStatus());   
                Log.i("---", "groups: "+entry.getGroups());   
            }
            listGroupMember.add(groupMemb);
        }
		
		map.put("groupName", listGroup);
		map.put("groupMember", listGroupMember);
		
		return map;
	}
	
	/**
	 * 查询某用户是否在线
	 * @param _username
	 * @return 用户在线，返回true， 不在线，返回false
	 */
	public boolean isSomeOneOnline(String _username){
		Roster roster = connection.getRoster();
//		Presence presence = roster.getPresence(_username);
		
		roster.addRosterListener(new RosterListener() {
			
			@Override
			public void presenceChanged(Presence presence) {
				isOnline = presence.isAvailable();
				
				System.out.println("====" + isOnline);
			}
			
			@Override
			public void entriesUpdated(Collection<String> arg0) {
				
			}
			
			@Override
			public void entriesDeleted(Collection<String> arg0) {
				
			}
			
			@Override
			public void entriesAdded(Collection<String> arg0) {
				
			}
		});
		Presence presence = roster.getPresence(_username);
		isOnline = presence.isAvailable();
		System.out.println("FinalOn : " + isOnline);
		return isOnline;
	}
	
	/**
	 * 得到用户在线状态，包括以下几种：
	 * 	1 ： available ：Available (the default) 在线
	 * 	2： away Away. 离开
	 * 	3： chat Chat，可以聊天
	 * 	4： dnd ：Do not disturb，请勿打扰
	 * 	5： xa ： Away for an extended period of time.暂时离开
	 * @param _username
	 * @return <b> available </b> 在线
	 * 	<b> away</b>  离开
	 * 	<b> chat </b> 可以聊天
	 * 	<b> dnd </b> 请勿打扰
	 * 	<b> xa </b>暂时离开
	 */
	public Presence.Mode getMode(String _username){
		Roster roster = connection.getRoster();
		Presence presence = roster.getPresence(_username);
		
		return presence.getMode();
	}
	
	 
}
