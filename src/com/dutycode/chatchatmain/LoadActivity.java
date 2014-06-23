package com.dutycode.chatchatmain;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

/**
 * 页面启动时页面
 * 
 * @author michael
 * 
 */
public class LoadActivity extends Activity {

	private final int SPLISH_DISPLAY_LENGTH = 3000; // 延迟3秒启动登陆界面

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_load);
		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// 启动三秒后进度到登陆界面
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent startLoginIntent = new Intent(LoadActivity.this,
						LoginActivity.class);
				LoadActivity.this.startActivity(startLoginIntent);
				LoadActivity.this.finish();
			}
		}, SPLISH_DISPLAY_LENGTH);

	}

}
