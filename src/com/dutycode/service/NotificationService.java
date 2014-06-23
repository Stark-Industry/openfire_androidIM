package com.dutycode.service;

import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.dutycode.chatchatmain.ChatActivity;
import com.dutycode.chatchatmain.R;

/**
 * 消息提示Service，显示在状态栏上
 * @author michael
 * @version 1.0
 *
 */
public class NotificationService {

	/**
	 * 设置状态栏提示
	 * @param _messageMap 消息来源以及chat的线程ID
	 * @param _context 上下文关系
	 * @param _notificationmanger NotificationManger
	 */
	public void setNotification(Map<String,Object> _messageMap, Context _context, NotificationManager _notificationmanger) {
		/*
		 * 创建新的Intent，作为单击Notification留言条时， 会运行的Activity
		 */

		String messageFrom = _messageMap.get("chatTo").toString();
		String chatThreadId = _messageMap.get("chatThreadId").toString();
		Bundle bundle = new Bundle();
		bundle.putString("ChatTo", messageFrom);
		bundle.putString("ChatThreadId",chatThreadId);
		
		Intent notifyIntent = new Intent(_context, ChatActivity.class);

		notifyIntent.putExtras(bundle);
		
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		/* 创建PendingIntent作为设置递延运行的Activity */
		PendingIntent appIntent = PendingIntent.getActivity(_context,
				0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		/* 创建Notication，并设置相关参数 */
		Notification myNoti = new Notification();

		/* 设置statusbar显示的icon */
		 myNoti.icon=R.drawable.chatchat;

		/* 设置statusbar显示的文字信息 */
		myNoti.tickerText = messageFrom;

		/* 设置notification发生时同时发出默认声音 */
		myNoti.defaults = Notification.DEFAULT_SOUND;

		/*设置点击后消失*/
		myNoti.flags = Notification.FLAG_AUTO_CANCEL;
		
		/* 设置Notification留言条的参数 */
		myNoti.setLatestEventInfo(_context, "您有未读消息", "消息来自：" +messageFrom,
				appIntent);

		/* 送出Notification */
		_notificationmanger.notify(0, myNoti);
		
	}
}
