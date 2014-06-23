package com.dutycode.chatchatmain;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.dutycode.service.ChatService;
/**
 * 历史消息查看界面Activity
 * 
 * @author michael
 *
 */
public class HistoryMessageActivity extends Activity {

	private DatePicker datepickerMessagedate;
	private Button btnSearchMessage;
	private TextView textviewMessageLog;
	
	private String chatTo;
	private ChatService chatservice;
	
	private Thread mSearchMessageThread;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_message);
		
		Bundle bundle = getIntent().getExtras();
		chatTo = bundle.getString("chatTo");
		
		datepickerMessagedate = (DatePicker)findViewById(R.id.datepicker_messagedate);
		btnSearchMessage = (Button)findViewById(R.id.btn_search_messagelog);
		textviewMessageLog = (TextView)findViewById(R.id.textview_messagelog);
		
		chatservice = new ChatService();
		
		//添加点击事件，查询消息记录
		btnSearchMessage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				mSearchMessageThread = new Thread(searchRunnable);
				mSearchMessageThread.start();
				
			}
		});

	}
	
	Runnable searchRunnable = new Runnable(){

		@Override
		public void run() {
			
			int mounth = datepickerMessagedate.getMonth() + 1;
			String searchMessageTime = datepickerMessagedate.getYear() + "-" 
					+ (mounth < 10 ? "0"+mounth:mounth)  + "-" 
					+ datepickerMessagedate.getDayOfMonth();
			
			System.out.println("===Date :" + searchMessageTime);
			String messageLog = chatservice.getHistoryMessageLog(searchMessageTime, chatTo);
			
			Message message = Message.obtain();
			message.obj = messageLog;
			
			messageHandler.sendMessage(message);
		}
		
	};
	
	//消息处理句柄，查询之后将更新主线程UI
	Handler messageHandler = new Handler(Looper.getMainLooper()){

		@Override
		public void handleMessage(Message msg) {
			String messageLog = msg.obj.toString();
			textviewMessageLog.setText(messageLog);
			textviewMessageLog.setMovementMethod(ScrollingMovementMethod.getInstance());
		}
		
	};

}
