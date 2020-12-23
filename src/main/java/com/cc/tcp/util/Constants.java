package com.cc.tcp.util;

import java.io.File;

public class Constants {


	public static String PROPERTY_PATH = "config" + File.separator;


	/**
	 * HTTP服务地址
	 */
	public static String HTTP_SERVICE_URL = PropertyManager.getPropertyManager("sys").getProperty("HTTP_SERVICE_URL");

	/**
	 * 开启线程端口
	 */
	public static String SOCKET_PORT = PropertyManager.getPropertyManager("sys").getProperty("SOCKET_PORT");

	/**
	 * 线程池大小
	 */
	public static String THREAD_NUM = PropertyManager.getPropertyManager("sys").getProperty("THREAD_NUM");

	/**
	 * socket队列大小
	 */
	public static String SOCKET_NUM = PropertyManager.getPropertyManager("sys").getProperty("SOCKET_NUM");
}
