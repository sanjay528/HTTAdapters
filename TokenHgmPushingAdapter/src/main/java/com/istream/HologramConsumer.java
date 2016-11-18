package com.istream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONObject;

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

public class HologramConsumer implements Runnable {
	
	private static final Logger logger = Logger.getLogger(HologramConsumer.class);
	
	public static JSONObject  lineHgJson = new JSONObject();
	
	private static CSVReader csvReader;
	private static Long threadSleepTime;
	private static String processedFilePath;
	private String unProcessedFilePath;
	private static String tenantIdentifier;
	//private static String encodePassword;
	private static String baseUrl;
	private static HttpPost post;
	private Queue<File> sharedQueue;
	private HttpClient postClient;
	private CsvFileWriter csvFileWriter;

	private int  lineIdle;

	private String expires_in;
	private String token_type;
	private String refresh_token;
	private String access_token;
	private static int counter;


	public HologramConsumer(Queue<File> queue, PropertiesConfiguration prop,HttpClient httpClient) throws ClientProtocolException, IOException, JSONException {
		this.sharedQueue =queue;
		postClient = httpClient;
		threadSleepTime = prop.getLong("threadSleepPeriod");
		processedFilePath = prop.getString("processedFilePath");
		unProcessedFilePath = prop.getString("unProcessedFilePath");
		tenantIdentifier = prop.getString("tenantIdentfier");
		//String credentials = prop.getString("username")+":"+prop.getString("password");
//		encodePassword = new String(Base64.encodeBase64(credentials.getBytes()));
	    initiateAuthentication();
		post = setRequiredPostUrl(prop.getString("lineDetailApi").trim());
		baseUrl = prop.getString("lineDetailApi").trim();
		csvFileWriter = new CsvFileWriter(prop);
		
	}
	
	private void initiateAuthentication() throws ClientProtocolException, IOException, JSONException {
		
		//initialPost:
			String url ="https://localhost:8442/hologramtt/api/oauth/token?username=htt&password=htt&"+
				"grant_type=password&client_id=jr/ynUAbal7yKg79BVq30BKbPByEHSWmt9THWtPmXKQ=&client_secret=p9t1mzNzQx6pNiPfPNXJQfrP6NCjpEUB4QKY7nkuwmg=";
			HttpPost post = new HttpPost(url);
			post.addHeader("Content-Type", "application/json");
			post.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
			HttpResponse response = postClient.execute(post);
			org.json.JSONObject resultJson = readResponseContent(response);
			expires_in = resultJson.getString("expires_in");
			token_type = resultJson.getString("token_type");
			refresh_token = resultJson.getString("refresh_token");
			access_token = resultJson.getString("access_token");
			logger.info("sanju: "+resultJson.getString("expires_in"));
		    class refreshToken extends TimerTask {
		        public void run() {
		        	String url ="https://localhost:8442/hologramtt/api/oauth/token?refresh_token="+refresh_token+
		    			"grant_type=refresh_token&client_id=jr/ynUAbal7yKg79BVq30BKbPByEHSWmt9THWtPmXKQ=&client_secret=p9t1mzNzQx6pNiPfPNXJQfrP6NCjpEUB4QKY7nkuwmg=";
		        	HttpPost post = new HttpPost(url);
					post.addHeader("Content-Type", "application/json");
					post.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
					try {
						HttpResponse response = postClient.execute(post);
					org.json.JSONObject resultJson = readResponseContent(response);
		        	expires_in = resultJson.getString("expires_in");
					token_type = resultJson.getString("token_type");
					refresh_token = resultJson.getString("refresh_token");
					access_token = resultJson.getString("access_token");
					} catch (IOException | JSONException e) {
						e.printStackTrace();
					} 
		        }
		    }
		    Timer timer = new Timer();
		    timer.schedule(new refreshToken(), Long.valueOf(expires_in)*1000, Long.valueOf(expires_in)*1000); 
		}	
	private HttpPost setRequiredPostUrl(final String postUrl) {

		HttpPost postRequest = new HttpPost(postUrl);
		//postRequest.addHeader("Authorization", "Basic " + encodePassword);
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
		postRequest.addHeader("Authorization",token_type+' '+access_token);
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
			logger.info(lineHgJson.toString());
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