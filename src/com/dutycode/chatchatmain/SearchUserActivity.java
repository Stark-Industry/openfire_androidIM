package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dutycode.bean.ReturnCodeBean;
import com.dutycode.bean.UserBean;
import com.dutycode.service.UserOperateService;

/**
 * 查询用户界面<br/>
 * 包括：<br/>
 * 1、查询用户<br/>
 * 2、添加用户为好友<br/>
 * 
 * @author michael
 * 
 */
public class SearchUserActivity extends Activity {

	private EditText edittextSearchUserName;
	private Button btnSearchUser;
	private ListView listviewUserList;

	// 弹出对话框中的控件
	private TextView textviewAddUserName;
	private Spinner spinerAddUserGroupList;

	private String searchUsername;
	private String addUserJID;
	private String addUserName;

	private UserOperateService useroperateservice;

	private final Context context = SearchUserActivity.this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_user);

		// 初始化控件
		edittextSearchUserName = (EditText) findViewById(R.id.search_user_username);
		btnSearchUser = (Button) findViewById(R.id.btn_search_user);
		listviewUserList = (ListView) findViewById(R.id.listview_search_user_list);

		// 初始化业务类
		useroperateservice = new UserOperateService();
		// 点击查找按钮
		btnSearchUser.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				searchUsername = edittextSearchUserName.getText().toString()
						.trim();

				if ("".equals(searchUsername)) {
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.search_user_error_empty_username),
							Toast.LENGTH_SHORT).show();
				} else {
					new Thread(searchUserRunnable).start();
				}
			}
		});

	}

	/**
	 * 搜索用户线程
	 */
	Runnable searchUserRunnable = new Runnable() {

		@Override
		public void run() {
			List<UserBean> userList = new ArrayList<UserBean>();
			android.os.Message msg = android.os.Message.obtain();
			try {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

				userList = useroperateservice.searchUser(searchUsername);
				// 将数据处理成Map类型
				for (int i = 0; i < userList.size(); i++) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("username", userList.get(i).getUserName());
					map.put("userjid", userList.get(i).getUserJID());
					map.put("useremail", userList.get(i).getEmail());
					list.add(map);

				}
				msg.obj = list;
			} catch (XMPPException e) {
				e.printStackTrace();
				msg.obj = null;
			}
			// 更新主线程UI
			userSearchResHandler.sendMessage(msg);
		}

	};

	/**
	 * 用户搜索结果处理Handler
	 */
	Handler userSearchResHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (null == msg) {
				Toast.makeText(
						context,
						context.getString(R.string.search_user_server_error_tip),
						Toast.LENGTH_SHORT).show();
			} else {
				List<Map<String, Object>> userlist = (List<Map<String, Object>>) msg.obj;
				// 将用户信息渲染到ListView

				SimpleAdapter adapter = new SimpleAdapter(context, userlist,
						R.layout.search_user_listview_layout, new String[] {
								"username", "userjid", "useremail" },
						new int[] { R.id.search_user_res_username,
								R.id.search_user_res_userejid,
								R.id.search_user_res_useremail });

				listviewUserList.setAdapter(adapter);

				listviewUserList
						.setOnItemLongClickListener(new OnItemLongClickListener() {

							@Override
							public boolean onItemLongClick(
									AdapterView<?> parent, View view,
									int position, long id) {
								// TODO 出现的用户列表，长按点击的事件，进行添加好友操作
								Map<String, Object> map = (Map<String, Object>) listviewUserList
										.getItemAtPosition(position);
								android.os.Message msgto = android.os.Message
										.obtain();
								msgto.obj = map;
								addUserHandler.sendMessage(msgto);
								return false;
							}

						});
			}
		}

	};

	/**
	 * 添加好友界面，包括查询
	 */
	Handler addUserHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Map<String, Object> map = (Map<String, Object>) msg.obj;
			addUserJID = map.get("userjid").toString();
			addUserName = map.get("username").toString();
			LayoutInflater factory = LayoutInflater
					.from(SearchUserActivity.this);
			// 获取自定义的对话框
			View view = factory.inflate(R.layout.add_user_dialog_layout, null);

			textviewAddUserName = (TextView) view
					.findViewById(R.id.textview_add_user_name);
			spinerAddUserGroupList = (Spinner) view
					.findViewById(R.id.search_add_user_spiner_grouplist);

			textviewAddUserName.setText(addUserName);

			// 获取分组信息，将其放置到Spinner中
			List<Object> grouplist = useroperateservice.getAllGroupName();

			ArrayAdapter<Object> arrayadapter = new ArrayAdapter<Object>(
					context, android.R.layout.simple_spinner_item, grouplist);
			arrayadapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			spinerAddUserGroupList.setAdapter(arrayadapter);

			AlertDialog addUserDialog = new AlertDialog.Builder(
					SearchUserActivity.this)
					.setIcon(R.drawable.chatchat)
					.setTitle(R.string.search_user_add_user_title)
					.setView(view)
					.setPositiveButton(
							context.getResources().getString(
									R.string.search_user_add_user_btn_add),
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// 添加点击事件
									
									//启动添加好友线程
									new Thread(addUserRunnable).start();
									
								}
							})
					.setNegativeButton(
							context.getResources().getString(
									R.string.search_user_add_user_btn_cancel),
							null).create();
			addUserDialog.show();

		}

	};
	
	/**
	 * 添加好友线程
	 */
	Runnable addUserRunnable = new Runnable(){

		@Override
		public void run() {
			
			android.os.Message msg = android.os.Message.obtain();
			
			String groupName = spinerAddUserGroupList.getSelectedItem().toString().trim();
			if (null == addUserJID || null == addUserName ||
					"".equals(addUserJID) || "".equals(addUserName)){
				msg.obj = ReturnCodeBean.ERROR_EMPTY_USERNAME_OR_USERJID;
			}else if("".equals(groupName)){
				if (useroperateservice.addNewFriend(addUserJID, addUserName)){
					msg.obj = ReturnCodeBean.RETURN_TRUE;
				}else {
					msg.obj = ReturnCodeBean.RETURN_FALSE;
				}
			}else {
				if (useroperateservice.addNewFriend(addUserJID, addUserName, groupName)){
					msg.obj = ReturnCodeBean.RETURN_TRUE;
				}else {
					msg.obj = ReturnCodeBean.RETURN_FALSE;
				}
			}
			
			addUserResHandler.sendMessage(msg);
			
		}
		
	};
	/**
	 * 添加好友结果Handler
	 */
	Handler addUserResHandler = new Handler (){

		@Override
		public void handleMessage(Message msg) {
			int msgcode = (Integer)msg.obj;
			if (msgcode == ReturnCodeBean.RETURN_TRUE){
				Toast.makeText(context, context.getResources().getString(R.string.search_user_add_user_succes),
						Toast.LENGTH_SHORT).show();
			}else if (msgcode == ReturnCodeBean.RETURN_FALSE){
				Toast.makeText(context, context.getResources().getString(R.string.search_user_add_user_fail),
						Toast.LENGTH_SHORT).show();
			}else if (msgcode == ReturnCodeBean.ERROR_EMPTY_USERNAME_OR_USERJID){
				Toast.makeText(context, context.getResources().getString(R.string.search_user_add_user_error_friendname_is_empty),
						Toast.LENGTH_SHORT).show();
			}
		}
		
	};

}
