package com.dutycode.configdata;

import java.io.File;

/**
 * 文件相关配置变量
 * 
 * @author michael
 * 
 */
public class Fileconfig {

	/**
	 * 保存密码的xml文件名称，存储在SD上
	 */
	public final static String xmlinfoname = "userinfo.xml";
	/**
	 * 保存的xml文件所在文件夹
	 */
	public final static String xmlfolderpath = "chatchat/";

	public final static String xmlinfopath = xmlfolderpath + xmlinfoname;
	/**
	 * sd卡根路径
	 */
	public final static String sdrootpath = android.os.Environment
			.getExternalStorageDirectory() + File.separator;
	
	/**
	 * 收到的文件保存的位置
	 */
	public final static String REVICE_FOLDER = "recivefile/";
	/**
	 * 收到的文件保存的绝对位置
	 */
	public final static String RECIVE_FILE_FOLDER_PATH =  xmlfolderpath + REVICE_FOLDER;
}
