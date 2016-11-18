package com.istream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;

import au.com.bytecode.opencsv.CSVReader;
import net.sf.json.JSONObject;

public class HologramConsumer implements Runnable {
	
	private static final Logger logger = Logger.getLogger(HologramConsumer.class);
	
	public static JSONObject  lineHgJson = new JSONObject();
	
	private static CSVReader csvReader;
	private static Long threadSleepTime;
	private static String processedFilePath;
	private String unProcessedFilePath;
	private static String tenantIdentifier;
	private static String encodePassword;
	private static String baseUrl;
	private static HttpPost post;
	
	
	private Queue<File> sharedQueue;
	private HttpClient postClient;
	private CsvFileWriter csvFileWriter;

	private int  lineIdle;
	private static int counter;


	public HologramConsumer(Queue<File> queue, PropertiesConfiguration prop,HttpClient httpClient) {
		this.sharedQueue =queue;
		postClient = httpClient;
		threadSleepTime = prop.getLong("threadSleepPeriod");
		processedFilePath = prop.getString("processedFilePath");
		unProcessedFilePath = prop.getString("unProcessedFilePath");
		tenantIdentifier = prop.getString("tenantIdentfier");
		String credentials = prop.getString("username")+":"+prop.getString("password");
		encodePassword = new String(Base64.encodeBase64(credentials.getBytes()));
		post = setRequiredPostUrl(prop.getString("lineDetailApi").trim());
		baseUrl = prop.getString("lineDetailApi").trim();
		csvFileWriter = new CsvFileWriter(prop);
		
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
			logger.info("Consumer() calling ...");
			try {
				synchronized (sharedQueue) {
					consume();
					Thread.sleep(threadSleepTime);
				}
			} catch (InterruptedException ex) {
				logger.error("thread is Interrupted for the : " + ex.getCause().getLocalizedMessage());
			}
		}
	}

	private void consume() {
		
		try {
			if(sharedQueue.isEmpty()){
				counter = counter+2;
				logger.info("Idle count:" +counter);
				if(counter == 150){
					csvFileWriter.writeToCSV(null,3L);
					lineIdle = counter+lineIdle;
					counter =0;
				}
			}
				while (!sharedQueue.isEmpty()) {
					counter =0;
					for (File processFile : sharedQueue) {
						logger.info(processFile.getName());
						sharedQueue.poll();
						processRequest(processFile);
					}
					sharedQueue.notifyAll();
				}
			
		} catch (Exception e) {
			logger.error("thread is Interrupted for the : " + e.getCause().getLocalizedMessage());
		}

	}
	
	private void processRequest(final File processFile) {

		StringBuilder hologramsData = new StringBuilder();
		String barcodeNumber = null; 
		Long lineNumber = null;
		String[] currentLineData;
		int i = 0;
		try {
			csvReader = new CSVReader(new FileReader(processFile));

			while ((currentLineData = csvReader.readNext()) != null) {
				if (i == 0) {
					lineNumber = Long.valueOf(currentLineData[0]);
					logger.info("Line Number -- " +lineNumber);
				} else if (i == 1) {
					barcodeNumber = currentLineData[0];
					logger.info("BarCode Number -- "+barcodeNumber);
				} else {
					hologramsData.append(currentLineData[0] + ",");
				}
				i++;
			}
			hologramsData.deleteCharAt(hologramsData.length() - 1);
			lineHgJson.put("hologramNumber", hologramsData.toString());
			lineHgJson.put("barcodeNumber", barcodeNumber);
			lineHgJson.put("status", 0);
			csvReader.close();
			logger.info(lineHgJson);
			post.setURI(new URI(baseUrl+ "/" + lineNumber));
			post.setEntity(new StringEntity(lineHgJson.toString()));
			HttpResponse response = postClient.execute(post);
			org.json.JSONObject resultJson = readResponseContent(response);
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("HTTP error code : " + response.getStatusLine().getStatusCode() + "Error Message :"+ resultJson.getString("defaultUserMessage"));
				throw new Exception(String.valueOf(response.getStatusLine().getStatusCode()));
		     } else {
		    	if(resultJson.has("changes")){
		    		processFile.renameTo(new File(unProcessedFilePath+"/"+processFile.getName())); 
		    		logger.error(resultJson.getJSONObject("changes").getString("pReturnMessage"));
		    	}else{
		    		logger.info(resultJson);
		    		csvFileWriter.writeToCSV(lineHgJson.toString(),resultJson.getLong("resourceId"));
		    		processFile.renameTo(new File(processedFilePath+"/"+processFile.getName()));
					logger.info("ALL Records Inserted Successfully And File Moved -- "+processFile.getName() +"-- " + sharedQueue.size());
		    	}
		     }
			lineHgJson.clear();
		} catch (FileNotFoundException e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public org.json.JSONObject readResponseContent(HttpResponse response) throws IOException, JSONException {
		return new org.json.JSONObject(EntityUtils.toString(response.getEntity()));
    }

}