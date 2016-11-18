package com.shellAdapter;

import java.io.File;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class ShellAdapterMain {

	static Logger logger = Logger.getLogger("");
	private static PropertiesConfiguration prop;
	public static void main(String[] args) {
		
		try {
			prop = new PropertiesConfiguration("shellproperties.txt");
			
			System.out.println("Adapter Started...");
			
			String logPath=prop.getString("LogFilePath");
			File filelocation = new File(logPath);			
			if(!filelocation.isDirectory()){
				filelocation.mkdirs();
			}
			Logger logger = Logger.getRootLogger();
			FileAppender appender = (FileAppender)logger.getAppender("fileAppender");
			appender.setFile(logPath+"/hologramshellscript.log");
			appender.activateOptions();
			HologramShellProducer p = new HologramShellProducer(prop);
			Thread t1 = new Thread(p);
			t1.start();
			
		} catch (ConfigurationException e) {
			
			e.printStackTrace();
		}
	

	}

}
