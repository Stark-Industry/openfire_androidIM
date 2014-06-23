package com.dutycode.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.dutycode.bean.MessageBean;
import com.dutycode.chatchatmain.ChatActivity;
import com.dutycode.chatchatmain.MainActivity;
import com.dutycode.chatchatmain.R;
import com.dutycode.configdata.Fileconfig;
import com.dutycode.configdata.MessageConfig;
import com.dutycode.tool.AndroidTools;


/**
 * 聊天服务类，处理聊天信息
 * @author michael
 *
 */
public class ChatService {

	XMPPConnection connection = (XMPPConnection) ClientConServer.connection;
	
	/*用户保存用户的聊天记录*/
	private List<MessageBean> messageList = new ArrayList<MessageBean>();
	
	/*用户放置聊天信息*/
	private List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
	//消息监听器
	private NewMessageListener messagelistener = new NewMessageListener();
	//未读消息监听器
	private MessageUnReadListenner unreadmessagelistener = new MessageUnReadListenner();
	//消息Handler，用于和主线程UI进行交互，刷新UI数据
	private Handler messageListenHandler;
	
	//全局消息句柄，用于Notification，刷新UI
	private Handler totalMessageListenHandler;
	
	//chatmanger用于处理当前的聊天
	private ChatManager chatmanger = connection.getChatManager();
	
	private Chat chat ;
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
	
	SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy-MM-dd");
	
	
	public ChatService(){}
	
	public ChatService(Handler _handler){
		this.messageListenHandler = _handler;
	}
	
	public ChatService(Context _context, Handler _handler ){
		this.totalMessageListenHandler = _handler;
		Log.i("messageInfo","Activity From:" + _context.getPackageName());
	}
	
	/**
	 * 构造方法
	 * @param _handler
	 * @param _userJID 用户的JID，这里用作聊天线程
	 */
	public ChatService(Handler _handler, String _userJID){
		this.messageListenHandler = _handler;
		
		if (chat == null){
			chat = chatmanger.createChat(_userJID, messagelistener);
		}else {
			chat.addMessageListener(messagelistener);
		}
		
			
	}
	
	
	public ChatService(Handler _handler, String _userJID, String _chatThreadID){
		this.messageListenHandler = _handler;
		if(chatmanger.getThreadChat(_chatThreadID) != null){
			chat = chatmanger.getThreadChat(_chatThreadID);
			chat.addMessageListener(messagelistener);
		}else {
			
			chat = chatmanger.createChat(_userJID, messagelistener);
		}
		
		
		
		
	}
	
	/**
	 * 发送消息（简单消息，不包括附加内容）
	 * @param _userJID 消息接收人的账号
	 * @param _message 发送的消息内容
	 */
	public void sendMessage(String _userJID, String _message){
		
		MessageBean messageBean = new MessageBean();
		
		try {
			chat.sendMessage(_message);
			
			//记录消息内容，保存到消息的List中
			messageBean.setMessageBody(_message);
			messageBean.setMessageFrom(MainActivity.userloginname + "  ");
			messageBean.setMessageTime(sdf.format(new Date()));
			
			logMessage(messageBean);
			
		}catch(XMPPException e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 监听指定用户JID的消息，适用于单独的聊天
	 * @param _userJID 用户的JID，这里同时为聊天的ThreadID
	 * @warning 方法过时
	 */
	@Deprecated
	public void listenningMessage(String _userJID){

		String chatThread = _userJID;

		if (chatmanger.getThreadChat(chatThread) != null){
			chat = chatmanger.getThreadChat(chatThread);
			chat.addMessageListener(messagelistener);
		}else {
			chat = chatmanger.createChat(_userJID, chatThread, messagelistener);
		}
		
		
	}
	
	/**
	 * 监听所有的消息
	 */
	public void listenningMessage(){
		chatmanger.addChatListener(new ChatManagerListener() {
			
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				if (!createdLocally){
					chat.addMessageListener(unreadmessagelistener);
				}
				
				
				
				
				
			}
		});
	}
	
	/**
	 * 记录消息
	 * @param _message 消息内容
	 */
	private void logMessage(MessageBean _message){
		if (messageList.size() == MessageConfig.MESSAGE_MAX_LENGTH){
			//messageList到达最大长度,将内容保存到文件中
			saveMessage(messageList);
			Log.i("listSizeIn:", "" + messageList.size());
			//将messageList置为空
			messageList.removeAll(messageList);
			messageList.add(_message);
			Log.i("listSizeIn2:", "" + messageList.size());
		}else {
			messageList.add(_message);
			Log.i("listsize", "" + messageList.size());
		}
		
	}
	
	/**
	 * 将消息保存到内存卡文件当中
	 * @param _messageList 
	 */
	private void saveMessage(List<MessageBean> _messageList){
		
		//聊天的记录文件名
		String messageLogName = sdfdate.format(new Date()) + ".chatchatfile";
		//聊天记录文件所在位置的文件夹，这里格式是这样的:chatchat/messagelog/UserName/：其中UserName为当前登录用户名
		String userchatfullname = chat.getParticipant();
		String userchatname = userchatfullname.contains("/")?userchatfullname.substring(0,userchatfullname.indexOf("/")):userchatfullname;
		String messageFolder = MessageConfig.MESSAGE_LOG_PATH + MainActivity.userloginname +"/"
								+userchatname +"/";
		//聊天记录完整路径
		String messageFinalLogPath = Fileconfig.sdrootpath + messageFolder + messageLogName;
		
		//检测是否存在SD卡，如果不存在,不能保存聊天记录
		if (AndroidTools.isHasSD()){
			//存在SD卡，执行存储任务
			if (!AndroidTools.isFileExists(messageFinalLogPath)){
				//文件不存在，创建文件，并添加内容
				AndroidTools.createFileOnSD(messageFolder, messageLogName);
			}
			//保存消息记录
			saveMessageToFile(messageList, messageFinalLogPath);
		}
		
	}
	
	
	/**
	 * 文件保存方法，将消息保存到文件当中
	 * @param _messageList 消息列表
	 * @param _filepath 文件路径
	 */
	private void saveMessageToFile(List<MessageBean> _messageList, String _filepath){
		StringBuffer messagesb = new StringBuffer();
		
		
		messagesb.append("---MessageSaved at ：" + sdf.format(new Date()) + "---\n");
		//将消息内容转换成String类型用于保存到文件中
		for (int i = 0; i < _messageList.size(); i++){
			messagesb.append(messageList.get(i).getMessageFrom() + "\n");
			messagesb.append(messageList.get(i).getMessageTime() + "\n");
			messagesb.append(messageList.get(i).getMessageBody() + "\n");
			messagesb.append("\n");
		}
		try {
			FileOutputStream fout = new FileOutputStream(_filepath,true);
			byte[] bytes = messagesb.toString().getBytes();
			
			fout.write(bytes);
			fout.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * 仅用于在点击返回按钮的时候进行检查是否需要存文件
	 */
	public void onlySaveMessageFileOnExit(){
		if (messageList.size() < MessageConfig.MESSAGE_MAX_LENGTH){
			
			//聊天的记录文件名
			String messageLogName = sdfdate.format(new Date())  + ".chatchatfile";
			//聊天记录文件所在位置的文件夹，这里格式是这样的:chatchat/messagelog/UserName/：其中UserName为当前登录用户名
			String messageFolder = MessageConfig.MESSAGE_LOG_PATH + MainActivity.userloginname +"/" + chat.getThreadID() + "/";
			//聊天记录完整路径
			String messageFinalLogPath = Fileconfig.sdrootpath + messageFolder + messageLogName;
			
			saveMessageToFile(messageList, messageFinalLogPath);
		}
	}

	/**
	 * 得到聊天内容的List
	 * @return 聊天内容List对象
	 */
	public List<Map<String,Object>> getMessageList(){
		
		Map<String,Object> messagemap = new HashMap<String, Object>();
		
		int listsize = messageList.size();
		if (listsize>0){
			messagemap.put("messageBody", messageList.get(listsize-1).getMessageBody());
			messagemap.put("messageFrom", messageList.get(listsize-1).getMessageFrom() + "  ");
			messagemap.put("messageTime", messageList.get(listsize-1).getMessageTime());
			
			list.add(messagemap);
		}
		return list;
	}

	
	/**
	 * 将数据发送给主线程，用于更新UI
	 */
	private void setAdapterList(){
		
		/*更新UI主线程，刷新聊天列表*/
		
		android.os.Message message = android.os.Message.obtain();
		message.obj = this.getMessageList();
		messageListenHandler.sendMessage(message);
		
	}
	
	
	/**
	 * 查询某天的聊天记录
	 * @param _messageDate 日期
	 * @param _chatWith 与谁聊天，其实是聊天记录的文件夹的名字
	 * @return 聊天记录
	 */
	public String getHistoryMessageLog(String _messageDate,String _chatWith){
		
		String messagelog = "";
		StringBuffer tempStr = new StringBuffer();
		//消息存放的文件夹
		String messageFolder = MessageConfig.MESSAGE_LOG_PATH + MainActivity.userloginname + "/" + _chatWith + "/";
		//消息的名称
		String messageName = _messageDate + ".chatchatfile";
		
		String finalMessagePath = Fileconfig.sdrootpath + messageFolder + messageName;
		//检测是否存在文件
		if (AndroidTools.isHasSD()){
			if (AndroidTools.isFileExists(messageFolder + messageName)){
				//文件存在，查询消息记录，读取文件
				try {
					FileInputStream fin = new FileInputStream(finalMessagePath);
					
					byte[] buffer = new byte[1024];
					int c;
					while ((c = fin.read(buffer)) != -1){
						tempStr.append(EncodingUtils.getString(buffer, "UTF-8"));
					}
					fin.close();
					messagelog = tempStr.toString();
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}else {
				//不存在文件，即没有聊天记录，返回相应提示
				messagelog = "";//这里设置为空，应该是要设置为从string.xml中读取的，但是没有找到办法
			}
		}
		return messagelog;
	}
	
	/**
	 * 监听消息(内部类)
	 * @author michael
	 *
	 */
	class NewMessageListener implements MessageListener{
		
		public NewMessageListener(){}
		
		//消息bean
		
		@Override
		public void processMessage(Chat chat, Message message) {
			
			
			chat.removeMessageListener(unreadmessagelistener);
			MessageBean messageBean = new MessageBean();
			messageBean.setMessageBody(message.getBody());
			messageBean.setMessageFrom(message.getFrom());
			messageBean.setMessageTime("("+sdf.format(new Date())+")");
			
			logMessage(messageBean);
			setAdapterList();
		}
	}
	
	
	/**
	 * 全局消息监听器
	 * @author michael
	 *
	 */
	class MessageUnReadListenner implements MessageListener{

		@Override
		public void processMessage(Chat chat, Message msg) {
			
			/*更新UI主线程，刷新聊天列表*/			
			android.os.Message message = android.os.Message.obtain();
			Map<String,Object> map = new HashMap<String, Object>();
			String chatTo = chat.getParticipant().substring(0, chat.getParticipant().indexOf("/"));
			map.put("chatTo", chatTo);
			map.put("chatThreadId", chat.getThreadID());
			message.obj = map;

			//TODO 如果这里能得到当前的聊天线程，然后与未读消息线程进行比对，在进行下面的UI主线程交互，就好啦
			//但是问题在于。由于这个监听器是在MainActivity中实例化的，这时候，chat还没有初始化，所以通过chat
			//得不到聊天线程
			totalMessageListenHandler.sendMessage(message);
			
		}
		
	}

}



