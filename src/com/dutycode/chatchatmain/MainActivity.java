package com.dutycode.chatchatmain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dutycode.bean.ReturnCodeBean;
import com.dutycode.service.ChatService;
import com.dutycode.service.ClientConServer;
import com.dutycode.service.FileTransferOperateService;
import com.dutycode.service.NotificationService;
import com.dutycode.service.UserOperateService;

public class MainActivity extends Activity {
	public static String userloginname;

	// 分组信息
	private List<Object> groupArr;

	private List<Object> childArr_S;// 中间变量，用于转换List为List<List<Object>>
	// 组员信息
	private List<List<Object>> childArr;

	private boolean isExit; // 标示是否退出程序

	private boolean isConnectOK = true;// 标示当前与服务器的连接状态,默认当前为已连接

	// ExpandListView控件，用户存放用户列表
	private ExpandableListView ex_listview_friendlist;
	private EditText changePswNewPsw;
	private EditText changePswNewPswRepet;

	// 头部信息栏控件
	private TextView titleUserName;
	private ImageButton titlePresenceStatusIcon;
	private TextView titlePresenceStatusText;

	// 状态栏提示管理器
	private NotificationManager notificationmanger;

	// 整体消息监听
	private Thread totalMessageListnerThread;
	// 服务器连接监听线程
	private Thread conncetToServerListenerThread;

	private ClientConServer clintconnserver;
	private UserOperateService useroperateservice;
	private FileTransferOperateService filetransferservice;

	private String newPsw;
	private String newPswRepet;

	// 用户状态
	private Presence.Mode userStatusMode = Presence.Mode.available;// 默认初始化为在线

	private Context context = MainActivity.this;

	private Map<String, Object> chatThreadMap = new HashMap<String, Object>();
	private ExpandListViewFriendListAdapter expandlistviewfriendlistadapter = new ExpandListViewFriendListAdapter();
	private NotificationService notificationservice = new NotificationService();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.expandlistview_friendlist);

		// //在这里添加处理用户状态显示的代码
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.top_title_bar_layout);
		// //获取指定layout中的控件Id，以方便实例化
		// final LayoutInflater factory =
		// LayoutInflater.from(MainActivity.this);
		// final View titleView =
		// factory.inflate(R.layout.top_title_bar_layout,null);
		//

		// 初始化控件
		titleUserName = (TextView) findViewById(R.id.top_title_username);
		titlePresenceStatusIcon = (ImageButton) findViewById(R.id.top_title_presence_status_ico);
		titlePresenceStatusText = (TextView) findViewById(R.id.top_title_status);

		// 显示用户状态
		presenceStatusOnTitleBarHandler.sendEmptyMessage(0);

		// 获取用户列表，放置到list中
		Map<String, List<Object>> map = new ClientConServer().getUserList();
		groupArr = new ArrayList<Object>();
		childArr_S = new ArrayList<Object>();
		childArr = new ArrayList<List<Object>>();
		groupArr = map.get("groupName");
		childArr_S = map.get("groupMember");

		// 将list对象转换成List<List<Object>>，用于分组查询子节点
		for (int i = 0; i < childArr_S.size(); i++) {
			List<Object> list = (List<Object>) childArr_S.get(i);
			childArr.add(list);
		}

		useroperateservice = new UserOperateService();
		filetransferservice = new FileTransferOperateService();

		ex_listview_friendlist = (ExpandableListView) findViewById(R.id.expandableListView_FriendList);
		ex_listview_friendlist.setAdapter(expandlistviewfriendlistadapter);
		expandlistviewfriendlistadapter.notifyDataSetChanged();

		// 注册notificationmanger
		notificationmanger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// 添加点击用户事件。点击用户进入到发送消息界面
		ex_listview_friendlist
				.setOnChildClickListener(new OnChildClickListener() {

					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						if (isConnectOK) {
							String username = childArr.get(groupPosition)
									.get(childPosition).toString();
							String userJID = new ClientConServer()
									.getUserJIDByName(username);

							// 用于传递参数到下一个Activity
							Bundle bundle = new Bundle();
							bundle.putString("ChatTo", userJID);

							// 检测是否已经存在ChatThread
							if (chatThreadMap.containsKey(userJID)) {
								bundle.putString("ChatThreadId", chatThreadMap
										.get(userJID).toString());
							}

							Intent intent = new Intent(MainActivity.this,
									ChatActivity.class);
							intent.putExtras(bundle);
							startActivity(intent);
						} else {
							Toast.makeText(
									MainActivity.this,
									MainActivity.this
											.getString(R.string.lose_connect_with_server),
									Toast.LENGTH_SHORT).show();
						}

						return false;
					}
				});

		// 启动服务器状态连接监听线程
		conncetToServerListenerThread = new Thread(connectToServeListerRunable);
		conncetToServerListenerThread.start();

		// 启动监听消息线程
		totalMessageListnerThread = new Thread(totalMessageListenerRunnable);
		totalMessageListnerThread.start();

		// 文件监听线程
		new Thread(fileTansferListenRunnable).start();

	}

	// 创建菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 1,
				context.getResources().getString(R.string.menu_change_password))
				.setIcon(R.drawable.menu_password);
		menu.add(Menu.NONE, Menu.FIRST + 2, 2,
				context.getResources().getString(R.string.menu_change_status))
				.setIcon(R.drawable.menu_settings);
		menu.add(
				Menu.NONE,
				Menu.FIRST + 3,
				3,
				context.getResources().getString(
						R.string.menu_search_and_add_user)).setIcon(
				R.drawable.menu_search_user);

		menu.add(Menu.NONE, Menu.FIRST + 6, 6,
				context.getResources().getString(R.string.menu_exit_program))
				.setIcon(R.drawable.menu_logout);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 处理菜单点击事件
		switch (item.getItemId()) {
		case Menu.FIRST + 1:
			// 修改密码
			changePasswordHandler.sendEmptyMessage(0);
			break;
		case Menu.FIRST + 2:
			// TODO 修改登陆状态
			changeStatusHandler.sendEmptyMessage(0);
			break;
		case Menu.FIRST + 3:
			Intent searchIntent = new Intent(MainActivity.this,
					SearchUserActivity.class);
			startActivity(searchIntent);
			break;

		case Menu.FIRST + 6:
			// 退出程序
			notificationmanger.cancelAll();
			// 返回主界面
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			System.exit(0);
			break;
		}

		return false;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	/**
	 * 退出程序方法
	 */
	private void exit() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(
					getApplicationContext(),
					MainActivity.this.getResources().getString(
							R.string.exit_program_tip), Toast.LENGTH_SHORT)
					.show();
			mHandler.sendEmptyMessageDelayed(0, 2000);
		} else {

			// 将登陆状态改为退出登陆
			new ClientConServer().logoff();
			// 将状态栏的消息删除掉
			notificationmanger.cancelAll();
			// 返回主界面
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			System.exit(0);
		}
	}

	/**
	 * 处理退出消息，如果2000ms之后没有再次点击返回，将isExit置为false
	 */
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			isExit = false;
		}

	};

	/**
	 * 服务器连接状态Handler
	 */
	Handler cononcetToServerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			boolean isConnected = (Boolean) msg.obj;
			isConnectOK = isConnected;
		}

	};

	/**
	 * 未读消息Handler
	 */
	Handler unReadMessageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 得到传递来的消息，更新状态栏消息给出提示
			// 消息来源，消息发送者
			Map<String, Object> map = (Map<String, Object>) msg.obj;

			// 更新状态栏，给出提示
			notificationservice.setNotification(map, MainActivity.this,
					notificationmanger);

			// 已经创建了chatThread
			if (!chatThreadMap.containsKey(map.get("chatThreadId").toString())) {
				chatThreadMap.put(map.get("chatTo").toString(),
						map.get("chatThreadId").toString());
			}
		}
	};

	/**
	 * 当前用户状态Handler
	 */
	Handler presenceStatusOnTitleBarHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// //TODO 在这里添加处理用户状态显示的代码
			// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
			// R.layout.top_title_bar_layout);
			// //初始化控件
			// titleUserName = (TextView)findViewById(R.id.top_title_username);
			// titlePresenceStatusIcon =
			// (ImageButton)findViewById(R.id.top_title_presence_status_ico);
			// titlePresenceStatusText =
			// (TextView)findViewById(R.id.top_title_status);

			int imgUserPresenceStatus;
			String statusText = "";
			switch (userStatusMode) {
			case available:
				statusText = context.getResources().getString(
						R.string.change_status_available);
				imgUserPresenceStatus = R.drawable.available;
				break;
			case away:
				statusText = context.getResources().getString(
						R.string.change_status_away);
				imgUserPresenceStatus = R.drawable.away;
				break;
			case chat:
				statusText = context.getResources().getString(
						R.string.change_status_chat);
				imgUserPresenceStatus = R.drawable.chat;
				break;
			case dnd:
				statusText = context.getResources().getString(
						R.string.change_status_dnd);
				imgUserPresenceStatus = R.drawable.dnd;
				break;
			case xa:
				statusText = context.getResources().getString(
						R.string.change_status_xa);
				imgUserPresenceStatus = R.drawable.xa;
				break;
			default:
				statusText = context.getResources().getString(
						R.string.change_status_available);
				imgUserPresenceStatus = R.drawable.available;
				break;
			}

			// 设置界面
			titleUserName.setText(MainActivity.userloginname);
			titlePresenceStatusIcon.setImageResource(imgUserPresenceStatus);
			titlePresenceStatusText.setText(statusText);

			titlePresenceStatusIcon.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// 处理点击事件
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						changeStatusHandler.sendEmptyMessage(0);
					}
					return false;
				}
			});

		}

	};
	/**
	 * 服务器连接状态监听器
	 */
	Runnable connectToServeListerRunable = new Runnable() {

		@Override
		public void run() {
			clintconnserver = new ClientConServer(cononcetToServerHandler);
			clintconnserver.listeningConnectToServer();
		}

	};
	/**
	 * 整体的消息监听
	 */
	Runnable totalMessageListenerRunnable = new Runnable() {

		@Override
		public void run() {

			/* 添加消息监听 */
			new ChatService(MainActivity.this, unReadMessageHandler)
					.listenningMessage();

		}

	};

	/**
	 * 修改密码线程
	 */
	Runnable changePasswordRunnable = new Runnable() {

		@Override
		public void run() {
			android.os.Message msg = android.os.Message.obtain();

			if (useroperateservice.changePassword(newPsw)) {
				msg.obj = true;
			} else {
				msg.obj = false;
			}
			// s 发送更新UI线程Handler
			changePswResHandler.sendMessage(msg);
		}

	};

	/**
	 * 修改密码结果Handler
	 */
	Handler changePswResHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			boolean isChangeOk = (Boolean) msg.obj;
			if (isChangeOk) {
				Toast.makeText(
						context,
						context.getResources().getString(
								R.string.change_psw_success),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						context,
						context.getResources().getString(
								R.string.change_psw_fail), Toast.LENGTH_SHORT)
						.show();
			}
		}

	};
	/**
	 * 处理更改密码UI线程数据
	 */
	Handler changePasswordHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			LayoutInflater factory = LayoutInflater.from(MainActivity.this);
			// 获得自定义对话框
			View view = factory.inflate(R.layout.change_psw_layout, null);

			// 在弹出的对话框中初始化控件的方法
			changePswNewPsw = (EditText) view
					.findViewById(R.id.change_psw_newpsw);
			changePswNewPswRepet = (EditText) view
					.findViewById(R.id.change_psw_newpsw_rept);

			AlertDialog changePswDialog = new AlertDialog.Builder(
					MainActivity.this)
					.setIcon(android.R.drawable.btn_star)
					.setTitle(
							context.getResources().getString(
									R.string.change_psw_title))
					.setView(view)
					.setPositiveButton(
							context.getResources().getString(
									R.string.btn_sure_change_psw),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {

									// 初始化控件
									// changePswNewPsw =
									// (EditText)findViewById(R.id.change_psw_newpsw);
									// changePswNewPswRepet =
									// (EditText)findViewById(R.id.change_psw_newpsw_rept);
									newPsw = changePswNewPsw.getText()
											.toString().trim();
									newPswRepet = changePswNewPswRepet
											.getText().toString().trim();
									if ("".equals(newPsw)
											|| "".equals(newPswRepet)) {
										Toast.makeText(
												context,
												context.getResources()
														.getString(
																R.string.change_psw_error_empty_tip),
												Toast.LENGTH_SHORT).show();
									} else if (!newPsw.equals(newPswRepet)) {
										Toast.makeText(
												context,
												context.getResources()
														.getString(
																R.string.change_psw_error_not_the_same_tip),
												Toast.LENGTH_SHORT).show();
									} else {
										// 启动修改密码线程
										new Thread(changePasswordRunnable)
												.start();
									}
								}
							})
					.setNegativeButton(
							context.getResources().getString(
									R.string.btn_no_change_psw), null).create();
			changePswDialog.show();

		}

	};

	/**
	 * 修改用户状态Handler，
	 */
	Handler changeStatusHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			/*
			 * 状态：分别为： 在线 available 离开 away 可以聊天 chat 请勿打扰 dnd 不在电脑旁 xa
			 */
			String[] presenceMode = {
					context.getResources().getString(
							R.string.change_status_available),
					context.getResources().getString(
							R.string.change_status_away),
					context.getResources().getString(
							R.string.change_status_chat),
					context.getResources()
							.getString(R.string.change_status_dnd),
					context.getResources().getString(R.string.change_status_xa) };
			// 包含多个选项的对话框
			AlertDialog dialog = new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.btn_star)
					.setTitle(
							context.getResources().getString(
									R.string.change_status_title))
					.setItems(presenceMode, onStatusSelectClick).create();
			dialog.show();

		}

	};

	/**
	 * 弹出的状态栏中的点击监听事件
	 */
	OnClickListener onStatusSelectClick = new OnClickListener() {

		@Override
		public void onClick(DialogInterface _dialog, int _which) {
			switch (_which) {
			case 0:
				// TODO 在线
				userStatusMode = Presence.Mode.available;
				break;
			case 1:
				// TODO 离开
				userStatusMode = Presence.Mode.away;
				break;
			case 2:
				// TODO chat
				userStatusMode = Presence.Mode.chat;
				break;
			case 3:
				// TODO dnd
				userStatusMode = Presence.Mode.dnd;
				break;
			case 4:
				// TODO xa
				userStatusMode = Presence.Mode.xa;
				break;
			default:
				userStatusMode = Presence.Mode.available;
			}

			new Thread(changeStatusModeRunnable).start();

		}
	};

	/**
	 * 修改状态线程，
	 */
	Runnable changeStatusModeRunnable = new Runnable() {

		@Override
		public void run() {

			clintconnserver.setMode(userStatusMode);
			// 重新加载新的用户状态
			presenceStatusOnTitleBarHandler.sendEmptyMessage(0);
		}

	};

	/**
	 * 文件传输监听
	 */
	Runnable fileTansferListenRunnable = new Runnable() {

		@Override
		public void run() {

			filetransferservice.listenFileTransfer(fileReciveConfirmHandler);
		}

	};

	Handler fileReciveConfirmHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			ProgressDialog mypDialog = new ProgressDialog(context);
			mypDialog = new ProgressDialog(context);
			final FileTransferRequest requestMode = (FileTransferRequest) msg.obj;
			AlertDialog dialog = new AlertDialog.Builder(context)
					.setTitle(
							context.getResources().getString(
									R.string.file_recive_confirm_title))
					.setMessage(
							context.getResources()
									.getString(
											R.string.file_recive_confirm_message_content_front)
									+ requestMode.getRequestor()
									+ context
											.getResources()
											.getString(
													R.string.file_recive_confirm_message_content_midle)
									+ requestMode.getFileName()
									+ context
											.getResources()
											.getString(
													R.string.file_recive_confirm_message_content_end)
									+ (requestMode.getFileSize()/1024)
									+ "KB")
					.setPositiveButton(
							context.getResources().getString(
									R.string.file_recive_confirm_btn_ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									new Thread(new Runnable() {

										@Override
										public void run() {
											android.os.Message msg = android.os.Message
													.obtain();
											msg.obj = ReturnCodeBean.START_RECIVE_FILE;
											reciveFileProgressHandler
													.sendMessage(msg);
											// 接收文件
											filetransferservice.saveReciveFile(
													true, requestMode,
													reciveFileProgressHandler);
										}

									}).start();

								}
							})
					.setNegativeButton(
							context.getResources().getString(
									R.string.file_recive_confirm_btn_cancel),
							null).create();
			dialog.show();

		}

	};

	Handler reciveFileProgressHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			int message = (Integer) msg.obj;
			String messageConent = "";
			switch (message) {
			case ReturnCodeBean.START_RECIVE_FILE:
				messageConent = "开始接收文件";
				break;
			case ReturnCodeBean.COMPLITE_RECIVE_FILE:
				messageConent = "完成接收文件";
				break;
			case ReturnCodeBean.ERROR_RECIVE_FILE:
				messageConent = "接收文件错误";
				break;
			}

			if (message == ReturnCodeBean.START_RECIVE_FILE
					|| message == ReturnCodeBean.ERROR_RECIVE_FILE) {
				Toast.makeText(context, messageConent, Toast.LENGTH_SHORT)
						.show();
			}

			if (message == ReturnCodeBean.COMPLITE_RECIVE_FILE) {
				Toast.makeText(
						context,
						context.getResources().getString(
								R.string.file_send_success), Toast.LENGTH_SHORT)
						.show();

			}
		}

	};

	private class ExpandListViewFriendListAdapter extends
			BaseExpandableListAdapter {

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return childArr.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int arg0, int arg1) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView text = null;
			if (convertView != null) {
				text = (TextView) convertView;
				text.setText(childArr.get(groupPosition).get(childPosition)
						.toString());
			} else {
				text = createChildView(childArr.get(groupPosition)
						.get(childPosition).toString());
			}

			Drawable img_online, img_offline;

			Resources res = getResources();

			img_online = res.getDrawable(R.drawable.online);
			img_offline = res.getDrawable(R.drawable.offline);

			// 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示

			img_online.setBounds(0, 0, img_online.getMinimumWidth(),
					img_online.getMinimumHeight());
			img_offline.setBounds(0, 0, img_offline.getMinimumWidth(),
					img_offline.getMinimumHeight());

			// 判断是否在线
			if (new ClientConServer().isSomeOneOnline(childArr
					.get(groupPosition).get(childPosition).toString())) {

				text.setCompoundDrawables(img_online, null, null, null);
			} else {
				text.setCompoundDrawables(img_offline, null, null, null);
			}
			return text;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return childArr.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groupArr.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return groupArr.size();
		}

		@Override
		public long getGroupId(int arg0) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView text = null;
			if (convertView != null) {
				text = (TextView) convertView;
				text.setText(groupArr.get(groupPosition).toString());
			} else {
				text = createGroupView(groupArr.get(groupPosition).toString());
			}
			return text;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			// 返回true保证子列表能够被点击，若返回false，则不能被点击
			return true;
		}

		/**
		 * 子列表视图
		 * 
		 * @param _content
		 *            子列表单元名,这里为用户的账号名称
		 * @return
		 */
		private TextView createChildView(String content) {
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 80);
			TextView text = new TextView(MainActivity.this);
			text.setLayoutParams(layoutParams);
			// text.setWidth(LayoutParams.MATCH_PARENT);
			text.setBackgroundResource(R.drawable.chat_list_child_list);
			text.setGravity(Gravity.TOP | Gravity.LEFT);
			text.setPadding(40, 0, 0, 0);
			text.setTextSize(20);
			text.setText(content);
			return text;
		}

		/**
		 * 组视图
		 * 
		 * @param content
		 * @return
		 */
		private TextView createGroupView(String content) {
			AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 60);
			TextView text = new TextView(MainActivity.this);
			text.setLayoutParams(layoutParams);
			text.setBackgroundResource(R.drawable.chat_list_group_list);
			// text.setWidth(LayoutParams.MATCH_PARENT);
			text.setGravity(Gravity.TOP | Gravity.LEFT);
			text.setPadding(50, 0, 0, 5);
			text.setTextSize(20);
			text.setText(content);
			return text;
		}

	}

}
