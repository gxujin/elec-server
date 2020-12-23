package com.cc.tcp.util;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * property管理工具
 * 用于获取property配置信息
 * PropertyManager.getPropertyManager(String name).getProperty(String propName)
 * @author chenjb
 *
 */

public class PropertyManager {

	private static HashMap mgrs = new HashMap();
	private Properties props = null;

	public static synchronized PropertyManager getPropertyManager(String name) {
		Object item = mgrs.get(name);
		if (item == null) {
			return new PropertyManager(name);
		}
		return ((PropertyManager)item);
	}

	public static synchronized PropertyManager getInstance(String name) {
		Object item = mgrs.get(name);
		if (item == null) {
			return new PropertyManager(name);
		}
		return ((PropertyManager)item);
	}

	private PropertyManager(String name) {
		this.props = new Properties();
		try {
			this.props.load(getPropertiesStream(name));
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public String getProperty(String propName)
	{
		return this.props.getProperty(propName);
	}

	public InputStream getPropertiesStream(String name) throws FileNotFoundException {
//		使用项目的根目录(相对于物理路径)
		String filename = System.getProperty("user.dir") + File.separator + Constants.PROPERTY_PATH +name+".properties".replace('/', File.separatorChar);
//		使用classes的路径(相对于环境的路径)
		//String filename = PropertyManager.class.getClassLoader().getResource("").getPath()+name+".properties";
		InputStream in = new FileInputStream(filename);
		return in;
	}


	public static void main(String[] args) {
		//System.out.println(System.getProperty("user.dir") + File.separator + Constants.PROPERTY_PATH);
		//System.out.println(PropertyManager.getPropertyManager("system").getProperty("REMOTE_URL"));
	}
}
