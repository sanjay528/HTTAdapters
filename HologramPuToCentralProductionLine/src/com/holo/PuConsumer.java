package com.holo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Queue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

public class PuConsumer implements Runnable {

	private final static Logger logger = Logger.getLogger(PuConsumer.class);
	
	private Queue<ProcessRequestData> queue;
	static Socket requestSocket = null;
	private static PropertiesConfiguration prop;
	private String message;
	private static HttpPost post;
	private static HttpPut put;
	private static byte[] encoded;
	private static String tenantIdentifier;
	private static HttpClient httpClient;
	private PuProcessCommandImpl processCommand;

	public static int wait;

	
	public PuConsumer(Queue<ProcessRequestData> queue,PropertiesConfiguration properties) {
		try{
			
			this.queue = queue;
			this.prop=properties;
			httpClient = new DefaultHttpClient();
			httpClient = wrapClient(httpClient);
			processCommand = new PuProcessCommandImpl(prop,httpClient);
			System.out.println("check calling or ont "+processCommand);
			
		} catch(Exception e){
			logger.error("Pu comsumer Exception : " + e.getMessage());
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e1) {
				logger.error("thread is Interrupted for the : " + e1.getCause().getLocalizedMessage());
			}
		}
	
	}

	@Override
	public void run() {

		while (true) {
			logger.info("Consumer() class calling ...");
			try {
				synchronized (queue) {
					consume();
					queue.wait();
				}
			} catch (InterruptedException ex) {
				logger.error("thread is Interrupted for the : " + ex.getCause().getLocalizedMessage());
			}
		}
	}

	private void consume() {
		
		try {
			
				while (!queue.isEmpty()) {
					for (ProcessRequestData processRequestData : queue) {
						logger.info("inside for loop consume()"+queue.size());
						queue.poll();
						logger.info("inside for loop consume()"+queue.size());
						logger.info("after pull  consume)()"+processCommand);
						processCommand.processRequest(processRequestData);
					}
					queue.notifyAll();
				}
			
		} catch (Exception e) {
			logger.error("thread is Interrupted for the : " + e.getCause().getLocalizedMessage());
		}

	}
	
public PuConsumer(final PropertiesConfiguration properties,final HttpClient wrapClient) {
		
		prop = properties;
		httpClient = wrapClient;
		tenantIdentifier = prop.getString("tenantIdentfier");
		encoded = Base64.encodeBase64((prop.getString("centralUserName").trim() + ":" + prop.getString("centralPassword").trim()).getBytes());
		
	}


	/*public static void sendResponse(final Long id,final String status, final String responceData) {
		
		logger.info("##..enter into responceUpdateProcessRequest()...... #");
		try {
			
			logger.info("##..enter into responceUpdateProcessRequest()....Try Block .. #");
			
			put = new HttpPut(prop.getString("PuServerUrl").trim()+"/"+id);
			
			logger.info(" put url is .."+prop.getString("PuServerUrl").trim()+"/"+id);
			
			//post = new HttpPost(prop.getString("CentralServerQuery").trim() + "/" + id);
			//post = new HttpPost("https://localhost:8443/mifosng-provider/api/v1/entitlements/"+ id);
			//post.setHeader("Authorization", "Basic " + new String(encoded));
			put.setHeader("Authorization", "Basic " + new String(encoded));
			put.setHeader("Content-Type", "application/json");
			put.addHeader("Hologram-TrackTrace-TenantId", prop.getString("tenantIdentfier"));
			
			JSONObject object = new JSONObject();
			object.put("responseStatus", status);
			object.put("responseDate", "date");
			object.put("responseMessage", responceData);
			
			logger.info(object.toString());
			
			StringEntity se = new StringEntity(object.toString());
			put.setEntity(se);
			HttpResponse response = httpClient.execute(put);
			response.getEntity().consumeContent();
			
			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());			
				String error = String.valueOf(response.getStatusLine().getStatusCode());
				BufferedReader br2 = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				while((errorOutput = br2.readLine()) != null){
					error = errorOutput;
				}
				br2.close();
				throw new Exception(error.toString());
			}
			else{
				
				logger.info("record is Updated Successfully in Pu System");
				logger.info("responce data from central...is ......"+response.getStatusLine().getStatusCode());
				logger.info("responce content  data from Pu...is ......"+response.getEntity().getContent());
			}
		  

		} catch (IOException e) {
			logger.error("IOException : " + e.getMessage() + ". verify the Central system server running or not");
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e1) {
				logger.error("thread is Interrupted for the : " + e1.getCause().getLocalizedMessage());
			}
		} catch (Exception e) {
		    logger.error("Exception : " + e.getLocalizedMessage());
	    }
	  
	}*/
	
	
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
				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {
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
}
