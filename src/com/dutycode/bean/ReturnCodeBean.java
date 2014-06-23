package com.dutycode.bean;
/**
 * 这里放置返回代码
 * @author michael
 *
 */
public class ReturnCodeBean {

	/**
	 * 原密码不正确
	 */
	public final static int ERROR_OLD_PSW_ERROR = 0;
	
	/**
	 * 用户名为空
	 */
	public final static int ERROR_EMPTY_USERNAME_OR_USERJID = -1;
	
	/**
	 * 返回值为真
	 */
	public final static int RETURN_TRUE = 1;
	
	/**
	 * 返回值为假
	 */
	public final static int RETURN_FALSE = 2;
	
	/**
	 * 接收文件
	 */
	public final static int RECIVE_FILE = 3;
	
	/**
	 * 拒绝接收文件
	 */
	public final static int REJICT_FILE = 4;
	
	/**
	 * 文件传输请求
	 */
	public final static int FILE_REQUEST = 5;
	
	/**
	 * 开始接收文件
	 */
	public final static int START_RECIVE_FILE = 6;
	
	/**
	 * 接收文件完成
	 */
	public final static int COMPLITE_RECIVE_FILE = 7;
	/**
	 * 接收文件错误
	 */
	public final static int ERROR_RECIVE_FILE = 8;
	
}
