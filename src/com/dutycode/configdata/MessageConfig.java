package com.dutycode.configdata;

/**
 * 聊天消息的配置项
 * @author michael
 *
 */
public class MessageConfig {
	/**
	 * 消息的最大记录
	 */
	public static int MESSAGE_MAX_LENGTH = 10 ;
	
	/**
	 * 消息存放位置相对路径
	 */
	public static String MESSAGE_LOG_PATH = "chatchat/messagelog/";
	
	/**
	 * 消息存放位置绝对路径（加上内存卡的位置）
	 */
	public static String MESSAGE_LOG_FINAL_PATH = Fileconfig.sdrootpath + MESSAGE_LOG_PATH;
	
}
