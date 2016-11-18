package com.htt.central;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class HologramCentralConsumer implements Runnable {
	
	static Socket requestSocket = null;
	String message;
	private HttpClient client;
	private String hologram_PATH_BEFORE_PROCESS;
	private String hologram_PATH_After_PROCESS;
	private File oldfile;
	private String result;
	private static HttpPost post;
	private static String tenantIdentifier;
	public static int adapterSleepPeriod;
	private static String encodePassword;
	static Logger logger = Logger.getLogger("");
	private static CSVReader reader;
	private static  String orderUrl;
	
	
	public HologramCentralConsumer(Queue<HologramProcessRequestData> queue, PropertiesConfiguration prop, HttpClient httpClient) {
		
		client = httpClient;
		//this.queue =queue;
		tenantIdentifier = prop.getString("tenantIdentfier");
		adapterSleepPeriod = prop.getInt("adapterSleepPeriod");
		hologram_PATH_BEFORE_PROCESS =prop.getString("hologram_PATH_BEFORE_PROCESS");
		hologram_PATH_After_PROCESS =prop.getString("hologram_PATH_After_PROCESS");
		tenantIdentifier = prop.getString("tenantIdentfier");
		String credentials = prop.getString("username")+":"+prop.getString("password");
		encodePassword = new String(Base64.encodeBase64(credentials.getBytes()));
		post = setRequiredPostUrl(prop.getString("dataposturl").trim());
		orderUrl = prop.getString("dataposturl").trim();
		
	}
	private HttpPost setRequiredPostUrl(final String postUrl) {
		
	    HttpPost postRequest = new HttpPost(postUrl);
		postRequest.addHeader("Authorization", "Basic " + encodePassword);
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
		return postRequest;
	}


	@Override
	public void run() {
		
		while (true) {
			logger.info("Consumer() class calling ...");
			try {
				consume();
				Thread.sleep(adapterSleepPeriod);
			} catch (InterruptedException ex) {
				logger.error("thread is Interrupted for the : " + ex.getCause().getLocalizedMessage());
			} catch (Exception e) {
				logger.error("thread is Interrupted for the : " + e.getCause().getLocalizedMessage());
				e.printStackTrace();
			}
				
		}
		
	}

	private void consume() {
		File folder = new File(hologram_PATH_BEFORE_PROCESS);
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles != null){
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					try {
							processRequest(listOfFiles[i].getName());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		}else{
			logger.info("There is no file to process");
		}
	}
	
	/**
	 * 
	 * for Processing scan file and rename
	 * 
	 */
	private void processRequest(String fileName) {
		try {
			StringBuilder sb = new StringBuilder();
			reader = new CSVReader(new FileReader(hologram_PATH_BEFORE_PROCESS + "/" + fileName));

			String[] currentLineData;
			while ((currentLineData = reader.readNext()) != null) {
				sb.append(currentLineData[0]);
				String string = sb.substring(1, sb.length()).toString();

				post.setURI(new URI(orderUrl + "/" + 3));
				post.setEntity(new StringEntity("{\"" + string + "\",\"" + currentLineData[1] + "\",\"" + currentLineData[2]));

				HttpResponse response = client.execute(post);
				result = EntityUtils.toString(response.getEntity());
				org.json.JSONObject obj = new org.json.JSONObject(result);
				if (response.getStatusLine().getStatusCode() != 200) {
					//org.json.JSONObject obj = new org.json.JSONObject(result);
					logger.error("HTTP error code : " + response.getStatusLine().getStatusCode() + "Error Message :");
					throw new Exception(String.valueOf(response.getStatusLine().getStatusCode()));

				} else {
					String returnMessage = obj.getJSONObject("changes").getString("pReturnMessage");
					
					System.out.println(returnMessage.length());
					if (returnMessage == null || returnMessage.isEmpty()) {

						logger.info("##################### record processed:###################  ");
						logger.info(" Hologram data inserted for " + currentLineData[1]);

					} else {
						logger.info(returnMessage);
					}
					
					
				}
				sb.setLength(0);
			}

			reader.close();
			oldfile = new File(hologram_PATH_BEFORE_PROCESS + "/" + fileName);

			File newFile = new File(hologram_PATH_After_PROCESS + "/" + fileName);

			boolean bolean = oldfile.renameTo(newFile);
			if (bolean == true) {
				logger.info("ALL Record Inserted Successfully And File name changed to :" + newFile);
			} else {
				logger.info("record inserted but file renamed fail but records processed");

			}

		} catch (FileNotFoundException e) {
			logger.info(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.info(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (URISyntaxException e) {
			logger.info(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

}
