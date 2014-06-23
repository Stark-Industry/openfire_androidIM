package com.dutycode.service;

import java.io.File;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.os.Handler;

import com.dutycode.bean.ReturnCodeBean;
import com.dutycode.configdata.Fileconfig;
import com.dutycode.tool.AndroidTools;

/**
 * 文件传输
 * 
 * @author michael
 * 
 */
public class FileTransferOperateService {

	private XMPPConnection conncetion = (XMPPConnection) ClientConServer.connection;


	//初始值不接受文件
	private int isAcceptFile = 0;
	
	private Handler fileTransferHandler;


	public FileTransferOperateService() {
//		/*
//		 * 下面这段内容是必须的但是我不清楚为什么要这么做
//		 */
////		conncetion = (XMPPConnection) ClientConServer.connection;
//		
//		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
//				.getInstanceFor(conncetion);
//
//		if (sdm == null)
//			sdm = new ServiceDiscoveryManager(conncetion);
//
//		sdm.addFeature("http://jabber.org/protocol/disco#info");
//		sdm.addFeature("jabber:iq:privacy");
		

	}

	/**
	 * 发送文件
	 * 
	 * @param _userJID
	 * @param _filePath
	 *            文件路径
	 * @param _fileDescription
	 *            文件描述
	 */
	public OutgoingFileTransfer sendFile(String _userJID, String _filePath,
			String _fileDescription) {

		 /*
		 * 下面这段内容是必须的但是我不清楚为什么要这么做
		 * */
		 ServiceDiscoveryManager sdm = ServiceDiscoveryManager
		 .getInstanceFor(conncetion);
		
		 if (sdm == null)
		 sdm = new ServiceDiscoveryManager(conncetion);
		
		 sdm.addFeature("http://jabber.org/protocol/disco#info");
		 sdm.addFeature("jabber:iq:privacy");

		// Create the file transfer manager
		FileTransferManager manager = new FileTransferManager(conncetion);
		FileTransferNegotiator.setServiceEnabled(conncetion, true);

		String fileTo = conncetion.getRoster().getPresence(_userJID).getFrom();
		OutgoingFileTransfer outfiletransfer = manager
				.createOutgoingFileTransfer(fileTo);

		try {
			outfiletransfer.sendFile(new File(_filePath), _fileDescription);
		} catch (XMPPException e) {
			e.printStackTrace();
		}

		return outfiletransfer;
	}

	/**
	 * 接收文件传输，将文件保存到chatchat目录下的recivefile目录下方
	 * 
	 * @param _accept
	 *            是否接收文件
	 */
	public void reciveFile(final boolean _accept) {
		if (_accept){
			isAcceptFile = ReturnCodeBean.RECIVE_FILE;
		}else {
			isAcceptFile = ReturnCodeBean.REJICT_FILE;
		}
	}
	/**
	 * 这里仅监听文件传输请求，监听到之后是否
	 */
	public void listenFileTransfer(Handler _handler ){
		fileTransferHandler = _handler;
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager
				 .getInstanceFor(conncetion);
				
		 if (sdm == null)
		 sdm = new ServiceDiscoveryManager(conncetion);
		
		 sdm.addFeature("http://jabber.org/protocol/disco#info");
		 sdm.addFeature("jabber:iq:privacy");
		 
		FileTransferManager filemanger = new FileTransferManager(
				conncetion);
		FileTransferNegotiator.setServiceEnabled(conncetion, true);
		filemanger.addFileTransferListener(filetransferListenr);
	}
	
	FileTransferListener filetransferListenr = new FileTransferListener() {
		
		@Override
		public void fileTransferRequest(FileTransferRequest _request) {
			android.os.Message msg = android.os.Message.obtain();
			//给线程发送消息，处理是否要显示接收文件
			msg.obj = _request;
//			msg.obj = ReturnCodeBean.FILE_REQUEST;
			fileTransferHandler.sendMessage(msg);
			
//			if (isAcceptFile == ReturnCodeBean.RECIVE_FILE) {
//				IncomingFileTransfer transfer = _request.accept();
//				try {
//					String fileName = _request.getFileName()
//							+ _request.getMimeType();
//					String fileSavePath = Fileconfig.RECIVE_FILE_FOLDER_PATH
//							+ fileName;
//					AndroidTools
//							.createFoldersOnSD(Fileconfig.RECIVE_FILE_FOLDER_PATH);
//
//					transfer.recieveFile(new File(fileSavePath));
//				} catch (XMPPException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}else if (isAcceptFile == ReturnCodeBean.REJICT_FILE){
//				_request.reject();
//			}
		}
	};
	
	
	public void saveReciveFile(boolean _isRecive, FileTransferRequest _request, final Handler _handler){
		android.os.Message msg = android.os.Message.obtain();
		if (_isRecive) {
			IncomingFileTransfer transfer = _request.accept();
			try {
				String fileName = _request.getFileName();
				String fileSavePath = Fileconfig.RECIVE_FILE_FOLDER_PATH
						+ fileName;
				String finalFileSavePath = Fileconfig.sdrootpath + fileSavePath;
//				AndroidTools
//						.createFoldersOnSD(Fileconfig.RECIVE_FILE_FOLDER_PATH);
				AndroidTools.createFileOnSD(Fileconfig.RECIVE_FILE_FOLDER_PATH, fileName);
				transfer.recieveFile(new File(finalFileSavePath));
				
				while (!transfer.isDone()){
					System.out.println("Status:" + transfer.getStatus());
					System.out.println("Progress:" + transfer.getProgress());
					Thread.sleep(1000);
				}
				if (transfer.isDone()){
					msg.obj = ReturnCodeBean.COMPLITE_RECIVE_FILE;
					_handler.sendMessage(msg);
				}
			} catch (XMPPException e) {
				msg.obj = ReturnCodeBean.ERROR_RECIVE_FILE;
				_handler.sendMessage(msg);
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			_request.reject();
		}
		
		
	}
}
