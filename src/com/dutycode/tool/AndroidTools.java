package com.dutycode.tool;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.content.Context;

import com.dutycode.configdata.Fileconfig;

/**
 * 关于android的一些工具类<br/>
 * 包括：<br/>
 * 	1.SD卡检测 {@link #isHasSD()}　<br/>
 *  2.检测SD卡上文件是否存在 {@link #isFileExists(String)}<br/>
 *  3.在SD卡上创建文件 {@link #createFileOnSD(String, String)} <br/>
 *  4.删掉SD卡上文件 {@link #deleteFileOnSD(String)}<br/>
 * @author michael
 * @version 1.0
 */
public class AndroidTools {

	/**
	 * 检测是否存在SD卡，或者外部存储
	 * @return 存在，返回true，不存在，返回false
	 */
	public static boolean isHasSD(){
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			return true;
		else 
			return false;
	}
	/**
	 * 检测SD上某文件是否存在
	 * 注意：需要首先判断是否存在SD卡，参见{@link #isHasSD()}
	 * @param _filepath 文件路径
	 * @return
	 */
	public static boolean isFileExists(String _filepath){
		File file = new File(Fileconfig.sdrootpath + _filepath);
		if (file.exists())
			return true;
		else 
			return false;
	}
	
	/**
	 * 在SD卡上创建文件
	 * @param _filepath 文件名称
	 * @param _folder 文件夹名称
	 */
	public static void createFileOnSD(String _folder, String _filepath){
		File file = new File(Fileconfig.sdrootpath + _folder + _filepath);
		File fileFolder = new File(Fileconfig.sdrootpath + _folder);
		
		if (!fileFolder.exists())
			fileFolder.mkdirs();
		//这里不做文件是否存在的判断
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 在SD卡上创建文件夹
	 * @param _folders
	 */
	public static void createFoldersOnSD(String _folders){
		File filefoder = new File(Fileconfig.sdrootpath + _folders);
		if (!filefoder.exists()){
			filefoder.mkdirs();
		}
	}
	
	/**
	 * 删除SD卡上文件
	 * @param _filepath 文件路径
	 */
	public static void deleteFileOnSD(String _filepath){
		File file = new File(Fileconfig.sdrootpath + _filepath);
		
		//不需要判断文件是否存在
		file.delete();
		
	}
	
}
