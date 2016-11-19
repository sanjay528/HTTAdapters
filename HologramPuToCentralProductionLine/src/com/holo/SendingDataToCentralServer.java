package com.holo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;


public class SendingDataToCentralServer {

	static Logger logger = Logger.getLogger("");

	private String puUrl;
	private String puUserName;
	private String password;
	private HttpClient httpClient = new DefaultHttpClient();
	private static String tenantIdentifier;
	//private String encodedToken;
	private long timeStamp;
	private static PropertiesConfiguration prop;

	public SendingDataToCentralServer(final String puUrl,final String puUserName,final String password) {

		this.puUrl = puUrl;
		this.puUserName = puUserName;
		this.password = password;
		logger.info("SendingDaCentralServer calling "+puUrl);
		try {
			DataSending(puUrl);
			logger.info("Sending url central....."+puUrl);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String DataSending(String curl) throws IOException, JSONException {
		
		logger.info("........data sedding() calling....."+curl);
		tenantIdentifier = prop.getString("tenantIdentfier");
		HttpGet post = new HttpGet(curl);
		post.addHeader("Content-Type", "application/json");
		post.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
		
		HttpResponse response = httpClient.execute(post);
		
		System.out.println("Responce Here is. ......"+response);
		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output = "", output1 = "";
		logger.info(".............Data sedding calling output ....."+output);
		while ((output = br.readLine()) != null) {
			output1 = output1 + output;
		}
		logger.info("Output From Server:" + output1);
		
		JSONObject jsonOutPut=new JSONObject(output1);
		
		if(!jsonOutPut.getBoolean("success")){
			logger.error("Error StatusCode:" + jsonOutPut + ", Sending Data: "+ curl);
			return PuConstants.FAILURE + ", Reason:" + jsonOutPut.getString("message");
		}

		br.close();
		return output1;
	}

	
}



