package com.dutycode.chatchatmain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dutycode.service.ClientConServer;
import com.dutycode.service.UserOperateService;
import com.dutycode.tool.Tools;
/**
 * 注册用户Activity
 * @author michael
 *
 */
public class RegActivity extends Activity {

	private EditText editUsername;
	private EditText editPassword;
	private EditText editPasswordRepeat;
	private EditText editServerIp;
	
	private Button btnRegNewUser;
	private Button btnReturnToLogin;
	
	private LinearLayout progressLinner ;
	
	private String serverIp ;
	private String userName;
	private String userPassword;
	private String userPasswordRepet;
	
	private Context context = RegActivity.this;
	
	private UserOperateService userOperateService;
	
	private  final Timer returnLogintimer = new Timer();
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reg_newuser);
		
		//初始化控件
		editUsername = (EditText)findViewById(R.id.new_username);
		editPassword = (EditText)findViewById(R.id.new_password);
		editPasswordRepeat = (EditText)findViewById(R.id.new_password_repet);
		editServerIp = (EditText)findViewById(R.id.new_serverip);
		
		btnRegNewUser = (Button)findViewById(R.id.btn_regform_regnewuser);
		btnReturnToLogin = (Button)findViewById(R.id.btn_noreg_return_login);
		
		progressLinner = (LinearLayout)findViewById(R.id.reg_status_layout);
		
//		Bundle bundle = getIntent().getExtras();
//		
//		if (bundle.getBoolean("isIpExists")){
//			//之前有输入IP，这里不需要再输入，将IP输入框设置为不可见
//			editServerIp.setVisibility(View.GONE);
//			serverIp = bundle.getString("serverIp");
//		}
		
		btnRegNewUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progressLinner.setVisibility(View.VISIBLE);
				//对表单数据进行初始化
				userName = editUsername.getText().toString().trim();
				userPassword = editPassword.getText().toString().trim();
				userPasswordRepet = editPasswordRepeat.getText().toString().trim();
				serverIp = editServerIp.getText().toString().trim();
				/*
				 * 这里涉及到了一点java初始值的问题：
				 * java虚拟机会指定默认值
				 * 1.数值类型=0，如：int
				 * 2.boolean类型=false
				 * 3.字符类型=空字符，如：char
				 * 4.字符串类型=null，如：String
				 * 5.对象类型=null，如：Object
				 * 
				 * */
//				if (null == serverIp || "".equals(serverIp)){
//					serverIp = editServerIp.getText().toString().trim();
//				}
				//对得到的数据进行判断，如果为空，给出提示
				if ("".equals(userName) || "".equals(userPassword) 
						|| "".equals(userPasswordRepet) || "".equals(serverIp)){
					progressLinner.setVisibility(View.GONE);
					Toast.makeText(context, context.getResources().getString(R.string.reg_error_empty_form_message),
						Toast.LENGTH_LONG).show();
				}else if (!Tools.isCorrectIp(serverIp)){
					progressLinner.setVisibility(View.GONE);
					Toast.makeText(context, context.getResources().getString(R.string.login_error_serverip),
							Toast.LENGTH_LONG).show();
				}else if (!userPassword.equals(userPasswordRepet)){
					progressLinner.setVisibility(View.GONE);
					Toast.makeText(context, context.getResources().getString(R.string.reg_error_psw_not_same),
							Toast.LENGTH_LONG).show();
				}else {
					//调用注册线程
					new Thread(regRunnable).start();
				}
				
			}
		});
		
		btnReturnToLogin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				new Thread(returnToLoginRunnable).start();
				
			}
		});
		
	}
	
	
	/**
	 * 注册线程
	 */
	Runnable regRunnable = new Runnable(){

		@Override
		public void run() {
			android.os.Message msg = android.os.Message.obtain();
			//初始化ClientConService，用于注册connection
			try {
				new ClientConServer(serverIp, 5222);
				Map<String,String> attributes = new HashMap<String, String>();
				attributes.put("date", sdf.format(new Date()));
				
				//初始化userOperateService
				userOperateService = new UserOperateService();
				if (userOperateService.regAccount(userName, userPassword, attributes)){
					msg.obj = true;
				}else {
					msg.obj = false;
				}
			} catch (XMPPException e) {
				msg.obj = false;
				e.printStackTrace();
			}
			
			regHandler.sendMessage(msg);
		}
		
	};
	
	/**
	 * 返回登陆界面
	 */
	Runnable returnToLoginRunnable = new Runnable() {
		
		@Override
		public void run() {
			
			Intent intent = new Intent(RegActivity.this, LoginActivity.class);
			//销毁当前Activity
			RegActivity.this.finish();
			startActivity(intent);
			
		}
	};
	
	/**
	 * 注册结果Handler，更新UI提示
	 */
	Handler regHandler = new Handler (){

		@Override
		public void handleMessage(Message msg) {
			boolean isRegOK = (Boolean)msg.obj;
			if (isRegOK){
				Toast.makeText(context, context.getResources().getString(R.string.reg_success),
						Toast.LENGTH_SHORT).show();
				
				//启动定时器，两秒后返回登陆界面
				returnLogintimer.schedule(returnToLoginTask, 5000);
				
				
			}else {
				progressLinner.setVisibility(View.GONE);
				Toast.makeText(context, context.getResources().getString(R.string.reg_fail),
						Toast.LENGTH_SHORT).show();
			}
		}
		
	};
	
	/**
	 * 返回主界面定时任务
	 */
	TimerTask returnToLoginTask = new TimerTask() {
		
		@Override
		public void run() {
			//启动登陆界面
			Intent intent = new Intent(RegActivity.this, LoginActivity.class);
			//销毁当前Activity
			RegActivity.this.finish();
			startActivity(intent);
		}
	};
	
}
