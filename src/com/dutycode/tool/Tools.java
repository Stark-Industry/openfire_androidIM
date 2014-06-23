package com.dutycode.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用的工具类
 * 包括： IP地址格式判断
 * @author michael
 *
 */
public class Tools {

	/**
	 * 判断IP地址是否合法
	 * 
	 * @param _ip IP地址
	 * @return IP合法返回true，不合法返回false
	 */
	public static boolean isCorrectIp(String _ip){
		Pattern ipPattern = Pattern.compile("([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}");
		
		Matcher ipMatcher = ipPattern.matcher(_ip);
		
		if (ipMatcher.find()){
			return true;
		}else {
			return false;
		}
		
	}
		
}
