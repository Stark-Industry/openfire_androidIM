package com.dutycode.tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.dutycode.bean.XmlUserInfoBean;
import com.dutycode.configdata.UserXmlParseConst;

/**
 * xml读写相关类
 * @author michael
 *
 */
public class XMLHelper {

	/**
	 * 创建XML文件，仅创建只有一个父节点的xml
	 * @param _datamap 放置xml的节点值和节点名称
	 * @param _xmlpath xml的存放位置
	 */
	public static void createXML(Map<String,Object> _datamap, String _xmlpath){
		StringWriter xmlWriter = new StringWriter();  
		try {
			
			//创建XmlSerializer,有两种方式   
            XmlPullParserFactory pullFactory;  
            //使用工厂类XmlPullParserFactory
            pullFactory = XmlPullParserFactory.newInstance();  
            XmlSerializer xmlSerializer = pullFactory.newSerializer();  
            xmlSerializer.setOutput(xmlWriter);  
            
            //开始写xml
            xmlSerializer.startDocument("UTF-8", true);  //<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
            
            //获取_datamap的key值
            Set<String> keyset = _datamap.keySet();
            
            //遍历key值，进行xml节点生成
            //格式类似于：<title>Vanuatu</title>
            xmlSerializer.startTag("", "data");  
            
            for (String keyname:keyset){
            	xmlSerializer.startTag("", keyname);  
                xmlSerializer.text(_datamap.get(keyname).toString());  
                xmlSerializer.endTag("", keyname);  
            }
            xmlSerializer.endTag("", "data");  
            //结束写xml
            xmlSerializer.endDocument();  
            

            //将xml写入到文件
            OutputStream outputstream = new FileOutputStream(_xmlpath);
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outputstream); 
            outStreamWriter.write(xmlWriter.toString());
            outStreamWriter.close();
            outputstream.close();

		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取指定路径下的xml，将读取内容放置到map中
	 * @return 返回xml内节点集合
	 */
	public static Map<String,Object> readAllXML(String _xmlpath){
		
		return null;
	}
	
	/**
	 * 采用Android SAX 解析XML
	 * @param _nodename 节点名称
	 * @param _xmlpath xml文件路径
	 * @return 节点内容
	 */
	public static String readXMLByNodeName(String _nodename,String _xmlpath){
		
		UserInfoHandler userinfohandler = new UserInfoHandler();
		try {
			InputStream in = new FileInputStream(_xmlpath);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parse = factory.newSAXParser();
			XMLReader xmlreader = parse.getXMLReader();
			xmlreader.setContentHandler(userinfohandler);
			xmlreader.parse(new InputSource(in));
			
			XmlUserInfoBean userinfo = userinfohandler.getUserinfobean();;
			
			if (_nodename.equals(UserXmlParseConst.USERNAME))
				return userinfo.getUsername();
			if (_nodename.equals(UserXmlParseConst.PASSWORD))
				return userinfo.getPassword();
			if (_nodename.equals(UserXmlParseConst.SERVER_IP))
				return userinfo.getServerip();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//如果参数传递错误，返回null
		return null;
	}
}


/**
 * 用户信息Handler
 * @author michael
 *
 */
class UserInfoHandler extends DefaultHandler{

	private XmlUserInfoBean userinfobean;
	private String preTag;//记录解析时上一个节点名称
	
	
	public XmlUserInfoBean getUserinfobean() {
		return userinfobean;
	}

	public void setUserinfobean(XmlUserInfoBean userinfobean) {
		this.userinfobean = userinfobean;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String result = new String(ch, start, length); 
		if (preTag.equals(UserXmlParseConst.USERNAME)){
			userinfobean.setUsername(result);
		}
		if (preTag.equals(UserXmlParseConst.PASSWORD)){
			userinfobean.setPassword(result);
		}
		if (preTag.equals(UserXmlParseConst.SERVER_IP)){
			userinfobean.setServerip(result);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (qName.equals("data")){
			preTag = null;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		userinfobean = new XmlUserInfoBean();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		preTag = qName;
	}
	
}
