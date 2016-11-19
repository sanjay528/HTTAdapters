package com.holo;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class HoloThreadedQueueAdapter {
	
	
	public static void main(String[] args) {

		try {
			Queue<ProcessRequestData> queue = new ConcurrentLinkedQueue<ProcessRequestData>();
			PropertiesConfiguration prop = new PropertiesConfiguration("PuIntegrator.ini");
			System.out.println("Adapter Started.......");
			
			String logPath=prop.getString("LogFilePath");
			File filelocation = new File(logPath);			
			if(!filelocation.isDirectory()){
				filelocation.mkdirs();
			}	
			
			Logger logger = Logger.getRootLogger();
			FileAppender appender = (FileAppender)logger.getAppender("fileAppender");
			appender.setFile(logPath+"/PuIntegrator.log");
			appender.activateOptions();
		
			PuProducer p = new PuProducer(queue,prop);
			PuConsumer c = new PuConsumer(queue,prop);
			
			Thread t1 = new Thread(p);
			Thread t2 = new Thread(c);

			t1.start();
			t2.start();
			
		} catch (ConfigurationException e) {
			System.out.println("(ConfigurationException) Properties file loading error.... : " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
		}
		

	}
}
