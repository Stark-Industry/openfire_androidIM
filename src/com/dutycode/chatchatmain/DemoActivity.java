package com.dutycode.chatchatmain;

import org.jivesoftware.smack.XMPPException;

import com.dutycode.service.UserOperateService;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
/**
 * 测试Demo，测试一些功能
 * @author michael
 *
 */
public class DemoActivity extends Activity{

	private Button btnSearchUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_activity);
		
		btnSearchUser = (Button)findViewById(R.id.btnSearchTest);
		btnSearchUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				try {
					new UserOperateService().searchUser("a");
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
	}

	
}
