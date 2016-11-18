package com.printersend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public class HologramPrinterConsumer implements Runnable {

	private long adapterSleepPeriod;
	private String barcodefolder;
	private Socket socket;
	static Logger logger = Logger.getLogger("");
	private BufferedReader reader;

	public HologramPrinterConsumer(PropertiesConfiguration prop) {

		this.adapterSleepPeriod = prop.getLong("adapterwaitPeriod");
		this.barcodefolder = prop.getString("barcodefolder");
	
	}

	@Override
	public void run() {

		while (true) {
			logger.info("Consumer() class calling ...");
			try {
				consume();
			}  catch (Exception e) {
				logger.error("thread is Interrupted for the : " + e.getCause().getLocalizedMessage());
				e.printStackTrace();
			}
				
		}
		
			
	}

	private void consume() {
		

		try{
			File directory = new File(barcodefolder);
			File[] fList = directory.listFiles();
			if(fList.length !=0){
			Arrays.sort(fList);
			for (final File file : fList) {
				if(file.exists()){
				InputStream in = new FileInputStream(file);
				reader = new BufferedReader(new InputStreamReader(in));
				StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					out.append(line + "\n");
				}

				String host = "192.168.0.206";
				int port = 9100;
				InetAddress address = InetAddress.getByName(host);
				socket = new Socket(address, port);
				String sendMessage = out.toString() + "\n";
				// Send the message to the server
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);
				logger.info(sendMessage);
				bw.write(sendMessage);
				bw.flush();
				file.delete();
				socket.close();
			}else{
				Thread.sleep(adapterSleepPeriod);
				run();
			}
			}
		}else{
			logger.info(" Consumer is waiting for files");
			Thread.sleep(adapterSleepPeriod);
			run();
		}
		
		} catch (Exception e) {
			try {
			logger.info(e.getLocalizedMessage());
				Thread.sleep(adapterSleepPeriod);
				run();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
	
	}

}
