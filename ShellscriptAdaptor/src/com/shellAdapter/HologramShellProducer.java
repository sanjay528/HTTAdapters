package com.shellAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class HologramShellProducer implements Runnable {

	static Logger logger = Logger.getLogger("");
	private long wait;
	private String srcdir;
	private String scriptName;
	private String remotedir;
	private File oldfile;
	private String destdir;

	public HologramShellProducer(PropertiesConfiguration prop) {

		srcdir = prop.getString("srcdir");
		remotedir = prop.getString("remotedir");
		scriptName = prop.getString("filename");
		destdir = prop.getString("destdir");
		wait = prop.getInt("threradSleep");

	}

	@Override
	public void run() {

		try {
			logger.info("Produce() class calling ...");
			File folder = new File(srcdir);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					callShellscript(listOfFiles[i].getName());
				}
			}
			Thread.sleep(wait);

		} catch (InterruptedException e) {
			logger.error("thread is Interrupted for the : "
					+ e.getCause().getLocalizedMessage());
		}

	}

	private void callShellscript(String name) {

		/**
		 * Need to check folder for empty files need to discuss with sir
		 * 
		 **/
		List<String> command = new ArrayList<String>();
		command.add("expect");
		command.add(scriptName);
		command.add(remotedir);
		command.add(srcdir);
		command.add(name);
		ProcessBuilder builder = new ProcessBuilder(command);
		try {
			logger.info(builder);
			Process process = builder.start();
			int exitval = process.waitFor();
			if (exitval != 0) {
				BufferedReader is = new BufferedReader(new InputStreamReader(
						process.getInputStream()));
				while (is.readLine() != null) {
					System.out.println(is.readLine());
				}
			}else{
				oldfile = new File(srcdir + "/" + name);
				File newFile = new File(destdir + "/" + name);
				boolean bolean = oldfile.renameTo(newFile);
				if (bolean == true) {
					logger.info("ALL Record Inserted Successfully And File name changed to :" + newFile);
				} else {
					logger.info("record inserted but file renamed fail but records processed");

				}
			}
		} catch (IOException e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
