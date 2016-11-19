package com.holo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Queue;

import javax.management.RuntimeErrorException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class PuProducer implements Runnable {
	
	private final static Logger logger = Logger.getLogger(PuProducer.class);

	private int no,recordsCount;
	private String encodedPassword;
	PropertiesConfiguration prop;
	BufferedReader br = null;
	private Queue<ProcessRequestData> messageQueue;
	private static HttpGet getRequest;
	private static byte[] encoded;
	private static String tenantIdentifier;
	private static HttpResponse response;
	private static HttpClient httpClient;
	private int wait;


	public static HttpClient wrapClient(HttpClient base) {

		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@SuppressWarnings("unused")
				public void checkClientTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				@SuppressWarnings("unused")
				public void checkServerTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
					// TODO Auto-generated method stub

				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			return null;
		}
	}

	public PuProducer(Queue<ProcessRequestData> messageQueue,PropertiesConfiguration prop) {
		
		// 1. Here intialize connection object for connecting to the RESTful
		// service
		// 2. Connect to RESTful service
		this.messageQueue = messageQueue;
		this.prop = prop;
		httpClient = new DefaultHttpClient();
		httpClient = wrapClient(httpClient);
		String username = prop.getString("username");
		String password = prop.getString("password");
		wait = prop.getInt("ThreadSleep_period");
		encodedPassword = username.trim() + ":" + password.trim();
		tenantIdentifier = prop.getString("tenantIdentfier");
		
		getRequest = new HttpGet(prop.getString("PuServerUrl"));
		
		//logger.info("call url is (PuServerQuery) "+prop.getString("PuServerQuery"));
		
		encoded = Base64.encodeBase64(encodedPassword.getBytes());
		getRequest.setHeader("Authorization", "Basic " + new String(encoded));
		getRequest.setHeader("Content-Type", "application/json");
		getRequest.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
		readDataFromRestfulService();
	}

	@Override
	public void run() {
		while (true) {
			produce();	   
			try {
				Thread.sleep(wait);
			} catch (InterruptedException ex) {
				logger.error("thread is Interrupted for the : " + ex.getCause().getLocalizedMessage());
			}
		}
	}

	/**
	 * Make a RESTful call to fetch the list of messages and add to the message
	 * queue for processing by the consumer thread.
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	private void produce() {
		try {
			logger.info("Produce() class calling ...");
		    synchronized (messageQueue) {
			   if (messageQueue.isEmpty()) {
				readDataFromRestfulService();
				messageQueue.notifyAll();
			   
				}else{
					logger.info(" records are Processing .... ");
					messageQueue.notifyAll();
					Thread.sleep(wait);
				}
			 } 

		} catch (InterruptedException e) {
			logger.error("thread is Interrupted for the : " + e.getCause().getLocalizedMessage());
		}

	}

	/**
	 * Change the Message.java class accordingly as per the JSON string of the
	 * respective RESTful API Read the JSON data from the RESTful API.
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * 
	 */
	private void readDataFromRestfulService() {

		
		try {
			//logger.info("#######################readDataFromRestfulService() call ################################");
            //no=1;
			response = httpClient.execute(getRequest);
			if (response.getStatusLine().getStatusCode() == 401) {
				logger.error("Authentication Failed : HTTP error code is: "+ response.getStatusLine().getStatusCode());
				httpClient.getConnectionManager().shutdown();	
				throw new AuthenticationException("AuthenticationException :  Target  system server username (or) password you entered is incorrect . check in the PuIntegrator.ini file");		
			}else if(response.getStatusLine().getStatusCode() == 404){
				logger.error("Resource Not Found Exception : HTTP error code is: " + response.getStatusLine().getStatusCode());
				httpClient.getConnectionManager().shutdown();
				throw new RuntimeErrorException(null, "Resource NotFound Exception :  Target  system server  'Local System' url error.");			
			}else if(response.getStatusLine().getStatusCode() != 200){
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				String errorOutput,error = "";
				BufferedReader br1 = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				while((errorOutput = br1.readLine()) != null){
					error = errorOutput;
				}
				br1.close();
				throw new Exception(error);			
			}
			
			logger.info("Sucessfully connected Target Syastem.......");
			
			br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

			String output;
			while ((output = br.readLine()) != null) {
				logger.info("Output is"+output);
				
				List<ProcessRequestData> processRequestDatas = new Gson().fromJson(output, new TypeToken<List<ProcessRequestData>>() {}.getType());
				//(List<CmsProcessRequestData>) gsonConverter.fromJson(output, new TypeToken<List<CmsProcessRequestData>>() {}.getType());
				int length = processRequestDatas.size();
				if (length > 0) {
					for (ProcessRequestData processRequestData : processRequestDatas) {
						logger.info(no +") requestDate=" + processRequestData.getRequestDate() + " , requestMessage = "+processRequestData.getRequestMessage());
						messageQueue.offer(processRequestData);
						no = no + 1;
					}
				}
			}
			br.close();
		  
		} catch (ClientProtocolException e) {
			logger.error("ClientProtocolException : " + e.getCause().getLocalizedMessage());
		} catch (IOException e) {
			logger.error("IOException : " + e.getCause() + ". verify the Local System running or not");		
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e1) {
					logger.error("thread is Interrupted for the : " + e1.getCause().getLocalizedMessage());
				}
		} catch (IllegalStateException e) {
			logger.error("IllegalStateException: " + e.getCause().getLocalizedMessage());
			
		} catch (AuthenticationException e) {						
			
			
			logger.info("##################################################################################");
			logger.info("");
			logger.error("AuthenticationException: " + e.getLocalizedMessage());
			logger.info("");
			logger.info("##################################################################################");
			logger.info("");
			logger.info("Stoping Adapter...");
			System.exit(0);
			
		} catch (RuntimeErrorException e) {
			
			logger.info("##################################################################################");
			logger.info("");
			logger.error("RuntimeErrorException: " + e.getLocalizedMessage());
			logger.info("");
			logger.info("##################################################################################");
			logger.info("");
			logger.info("Stoping Adapter...");
			System.exit(0);
			
		} catch (Exception e) {					
			logger.error("Exception: " + e.getLocalizedMessage());			
		}

	}

}
