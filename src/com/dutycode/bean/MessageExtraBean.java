package com.dutycode.bean;

/**
 * 消息类的扩展类，提供未读消息记录功能
 * @author michael
 *
 */
public class MessageExtraBean extends MessageBean {

	/**
	 * 未读消息计数
	 */
	private int unReadMessageCount;

	
	public int getUnReadMessageCount() {
		return unReadMessageCount;
	}

	public void setUnReadMessageCount(int unReadMessageCount) {
		this.unReadMessageCount = unReadMessageCount;
	}
	
	
	
}
