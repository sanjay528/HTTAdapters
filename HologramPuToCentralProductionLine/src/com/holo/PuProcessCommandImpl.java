package com.holo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
public class PuProcessCommandImpl {
	private final static Logger logger = Logger.getLogger(PuProcessCommandImpl.class);
	
	private static HttpPost post;
	private static HttpPut put;
	private static PropertiesConfiguration prop;
	private static String tenantIdentifier;
	private static HttpClient httpClient;
	private static byte[] encoded;
	public static int wait;
	private final static String dateFormat = "dd MMMM yyyy";
	private final static SimpleDateFormat dateformater = new SimpleDateFormat(dateFormat);
	

	public PuProcessCommandImpl(final PropertiesConfiguration properties,final HttpClient wrapClient) {
		prop = properties;
		httpClient = wrapClient;
		tenantIdentifier = prop.getString("tenantIdentfier");
		encoded = Base64.encodeBase64((prop.getString("centralUserName").trim() + ":" + prop.getString("centralPassword").trim()).getBytes());
	}

	public void processRequest(final ProcessRequestData processRequestData) {
		
		logger.info("....Enter processRequest ...."+processRequestData.toString());
		logger.info("....Entity type  ...."+processRequestData.getEntityType());
		logger.info("....Request Action ...."+processRequestData.getRequestAction());
		logger.info("....Request Action  loop result UPDATE ...."+processRequestData.getRequestAction().equalsIgnoreCase("UPDATE"));
		logger.info("....Request Action  loop result CREATE  ...."+processRequestData.getRequestAction().equalsIgnoreCase("CREATE"));
		logger.info("....Request Action  loop result APPROVE ...."+processRequestData.getRequestAction().equalsIgnoreCase("APPROVE"));
		logger.info("....Request Base Url is ...."+processRequestData.getBaseUrl());
		//this for only production line update 
		if(processRequestData.getRequestAction().equalsIgnoreCase("UPDATE") && processRequestData.getEntityType().equalsIgnoreCase("PRODUCTION UNIT") ){
			
			try {
				logger.info("Put Method ..'UPDATE'..Calling......");
				
				put = new HttpPut(processRequestData.getBaseUrl().trim()+"/productionline/editwithpid/"+processRequestData.getResourceId());
				//put = new HttpPut(processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl().split("/", 1)+"/editwithpid/"+processRequestData.getResourceId());
				
				logger.info("Put Method ..'UPDATE'...Url is ............."+processRequestData.getBaseUrl().trim()+"/productionline/editwithpid/"+processRequestData.getResourceId());
				//put = new HttpPut(prop.getString("CentralUrl").trim()+processRequestData.getRequestUrl());
				//logger.info(" put url is .."+prop.getString("CentralUrl").trim()+processRequestData.getRequestUrl());
				put.setHeader("Authorization", "Basic " + new String(encoded));
				put.setHeader("Content-Type", "application/json");
				put.addHeader("Hologram-TrackTrace-TenantId", prop.getString("tenantIdentfier"));
				
				JSONObject object = new JSONObject(processRequestData.getRequestMessage().toString());
				
				logger.info(object.toString());
				
				StringEntity se = new StringEntity(object.toString());
				put.setEntity(se);
				HttpResponse response = httpClient.execute(put);
				response.getEntity().consumeContent();
				
				if (response.getStatusLine().getStatusCode() != 200) {
					logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
					final String status="failed";
					final Long id=processRequestData.getId();
					String responceData=response.toString();
					logger.info("Sending data id is "+id+"status is "+status+" Responce data "+responceData+"");
					responceUpdateProcessRequest(id,status,responceData);
					String error = String.valueOf(response.getStatusLine().getStatusCode());
				    BufferedReader br2 = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
					br2.close();
					throw new Exception(error);
				}
				else{
					
					logger.info("Record is Updated Successfully in Central System");
					logger.info("Responce data from Central System...is ......"+response.getStatusLine().getStatusCode());
					logger.info("responce content  data from  Central System...is ......"+response.getEntity().getContent());
					
					final String status="success";
					final Long id=processRequestData.getId();
					String responceData=response.getStatusLine().toString();
					logger.info("Sending data id is "+id+"status is "+status+" Responce data "+responceData+"");
					responceUpdateProcessRequest(id,status,responceData);
					
				}

			} catch (IOException e) {
				logger.error("IOException : " + e.getMessage() + " Verify the Central system server running or not");
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e1) {
					logger.error("thread is Interrupted for the : " + e1.getCause().getLocalizedMessage());
				}
			} catch (Exception e) {
			    logger.error("Exception : " + e.getLocalizedMessage());
		    }
		  
		}// end of if update
		if(processRequestData.getRequestAction().equalsIgnoreCase("UPDATE") && processRequestData.getEntityType().equalsIgnoreCase("SUPPLIER PURCHASE ORDER") ){
			
			try {
				logger.info("Put Method ..'UPDATE SUPPLIER '..Calling......");
				
				put = new HttpPut(processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl());
				//put = new HttpPut(processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl().split("/", 1)+"/editwithpid/"+processRequestData.getResourceId());
				
				logger.info("Put Method ..'UPDATE'...Url is ............."+processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl());
				//put = new HttpPut(prop.getString("CentralUrl").trim()+processRequestData.getRequestUrl());
				//logger.info(" put url is .."+prop.getString("CentralUrl").trim()+processRequestData.getRequestUrl());
				put.setHeader("Authorization", "Basic " + new String(encoded));
				put.setHeader("Content-Type", "application/json");
				put.addHeader("Hologram-TrackTrace-TenantId", prop.getString("tenantIdentfier"));
				
				JSONObject object = new JSONObject(processRequestData.getRequestMessage().toString());
				
				logger.info(object.toString());
				
				StringEntity se = new StringEntity(object.toString());
				put.setEntity(se);
				HttpResponse response = httpClient.execute(put);
				response.getEntity().consumeContent();
				
				if (response.getStatusLine().getStatusCode() != 200) {
					logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
					final String status="failed";
					final Long id=processRequestData.getId();
					String responceData=response.toString();
					logger.info("Sending data id is "+id+"status is "+status+" Responce data "+responceData+"");
					responceUpdateProcessRequest(id,status,responceData);
					String error = String.valueOf(response.getStatusLine().getStatusCode());
				    BufferedReader br2 = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
					br2.close();
					throw new Exception(error);
				}
				else{
					
					logger.info("Record is Updated Successfully in Central System");
					logger.info("Responce data from Central System...is ......"+response.getStatusLine().getStatusCode());
					logger.info("responce content  data from  Central System...is ......"+response.getEntity().getContent());
					
					final String status="success";
					final Long id=processRequestData.getId();
					String responceData=response.getStatusLine().toString();
					logger.info("Sending data id is "+id+"status is "+status+" Responce data "+responceData+"");
					responceUpdateProcessRequest(id,status,responceData);
					
				}

			} catch (IOException e) {
				logger.error("IOException : " + e.getMessage() + " Verify the Central system server running or not");
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e1) {
					logger.error("thread is Interrupted for the : " + e1.getCause().getLocalizedMessage());
				}
			} catch (Exception e) {
			    logger.error("Exception : " + e.getLocalizedMessage());
		    }
		  
		}// end of 
		
		if(processRequestData.getRequestAction().equalsIgnoreCase("CREATE")){
			
			 try {
		    	  
		    	  logger.info("Post Method ..'CREATE'..Calling......");
		    	  
		    	  post = new HttpPost(processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl());
		    	  
		    	  logger.info("Post Method calling url is "+processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl());
		    	  logger.info(post);
				    //post = new HttpPost(prop.getString("CentralUrl").trim()+processRequestData.getRequestUrl());
					//logger.info(post);
					//post = new HttpPost(prop.getString("CentralServerQuery").trim() + "/" + id);
					post.setHeader("Authorization", "Basic " + new String(encoded));
					post.setHeader("Content-Type", "application/json");
					post.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
					
					//JsonObject object = new JsonObject(processRequestData.getRequestMessage());
					JSONObject object = new JSONObject(processRequestData.getRequestMessage().toString());
					//object.put(processRequestData.getRequestMessage());
					
					logger.info(object.toString());
					
					StringEntity se = new StringEntity(object.toString());
					post.setEntity(se);
					HttpResponse response = httpClient.execute(post);
					response.getEntity().consumeContent();
					
					
					if (response.getStatusLine().getStatusCode() != 200) {
						logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
						final String status="failed";
						final Long id=processRequestData.getId();
						String responceData=response.getStatusLine().toString();
						
						logger.info("Sending data id is "+id+"status is "+status+" Responce data "+responceData+"");
						
						responceUpdateProcessRequest(id,status,responceData);
						String error = String.valueOf(response.getStatusLine().getStatusCode());
					    BufferedReader br2 = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
					    /*while((errorOutput = br2.readLine()) != null){
							error = errorOutput;
						}*/
						br2.close();
						throw new Exception(error);
					}
					else{
						logger.info("****...Record is Updated Successfully in Central System....********");
						logger.info("Responce data from cenrtral is....."+response.getStatusLine().toString());
						
						final String status="success";
						final Long id=processRequestData.getId();
						String responceData=response.getStatusLine().toString();
						
						logger.info("Sending data id is "+id+"status is "+status+" Responce data "+responceData+"");
						responceUpdateProcessRequest(id,status,responceData);
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
	      } 
		
		    if(processRequestData.getRequestAction().equalsIgnoreCase("APPROVE")){
     			
    			 try {
    		    	  logger.info("Post Method ..'APPROVE'..Calling..");
    		    	  
    		    	  post = new HttpPost(processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl());
    		    	  
    		    	  logger.info("Post Method ..'APPROVE' calling url is "+processRequestData.getBaseUrl().trim()+processRequestData.getRequestUrl());
    		    	  logger.info(post);
    					//post = new HttpPost(prop.getString("CentralUrl").trim()+processRequestData.getRequestUrl());
    					//logger.info(post);
    					//post = new HttpPost(prop.getString("CentralServerQuery").trim() + "/" + id);
    					post.setHeader("Authorization", "Basic " + new String(encoded));
    					post.setHeader("Content-Type", "application/json");
    					post.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
    					
    					//JsonObject object = new JsonObject(processRequestData.getRequestMessage());
    					JSONObject object = new JSONObject(processRequestData.getRequestMessage().toString());
    					//object.put(processRequestData.getRequestMessage());
    					
    					logger.info(object.toString());
    					
    					StringEntity se = new StringEntity(object.toString());
    					post.setEntity(se);
    					HttpResponse response = httpClient.execute(post);
    					response.getEntity().consumeContent();
    					

    					if (response.getStatusLine().getStatusCode() != 200) {
    						logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
    						final String status="failed";
    						final Long id=processRequestData.getId();
    						String responceData=response.toString();
    						logger.info("Sending data id is "+id+"status is "+status+" Responce data "+responceData+"");
    						
    						responceUpdateProcessRequest(id,status,responceData);
    						String error = String.valueOf(response.getStatusLine().getStatusCode());
    					    BufferedReader br2 = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
    					    /*while((errorOutput = br2.readLine()) != null){
    							error = errorOutput;
    						}*/
    						br2.close();
    						throw new Exception(error);
    					}
    					else{
    						logger.info("****...Record Updated Sucessfully in Central System......******");
    						logger.info("Responce data from Cenrtral is..."+response.getStatusLine().toString());
    						
    						final String status="success";
    						final Long id=processRequestData.getId();
    						String responceData=response.getStatusLine().toString();
    						
    						logger.info("Responce Updating data id is "+id+"status is "+status+" Responce data "+responceData+"");
    						responceUpdateProcessRequest(id,status,responceData);
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
    			 
    	      } else{
	    	  logger.error("##########.......NO DATA FOR 'CREATE' & 'UPDATE' & 'APPROVE' ...................########");  
	         }
	  }//end of process()
	
	public static void responceUpdateProcessRequest(final Long id,final String status, final String responceData){	
		
		try {

			if (responceData == null) {
				throw new NullPointerException();
			}
			if (id == null) {
				throw new NullPointerException();
			}
			if (status == null) {
				throw new NullPointerException();
			} else {

				sendResponse(id, status, responceData);
			}	
		} catch(NullPointerException e){
			logger.error("responceUpdateProcessRequest()-NullPointerException : Output from the Central System Server is : " + e.getMessage());
		} catch (Exception e) {
		    logger.error("Exception : " + e.getMessage());
	    }
		
	}
	
	
	public static void process(final String value, final Long id, final String requestType){	
	
		try{		
			
			if(value==null){
				throw new NullPointerException();
			}else{		
				//PuConsumer.sendResponse(value, id, requestType);
			}		
		} catch(NullPointerException e){
			logger.error("Process()-NullPointerException : Output from the Pu System Server is : " + value);
		} catch (Exception e) {
		    logger.error("Exception : " + e.getMessage());
	    }
		
	}
	
	
	public static void sendResponse(final Long id, final String status,final String responceData) {

		try {

			logger.info("##....Responce Update In Local Syatem Method.......###");

			put = new HttpPut(prop.getString("PuServerUrl").trim() + "/" + id);

			logger.info("Responce Update In Local System Url is....." + prop.getString("PuServerUrl").trim()+ "/" + id);
			logger.info(put);
			// post = new HttpPost(prop.getString("CentralServerQuery").trim() +
			// "/" + id);
			// post = new
			// HttpPost("https://localhost:8443/mifosng-provider/api/v1/entitlements/"+
			// id);
			// post.setHeader("Authorization", "Basic " + new String(encoded));
			put.setHeader("Authorization", "Basic " + new String(encoded));
			put.setHeader("Content-Type", "application/json");
			put.addHeader("Hologram-TrackTrace-TenantId",prop.getString("tenantIdentfier"));

			JSONObject object = new JSONObject();
			object.put("responseStatus", status);
			// object.put("responseDate", dateformater.format(new Date()));
			object.put("responseMessage", responceData);

			logger.info(object.toString());

			StringEntity se = new StringEntity(object.toString());
			put.setEntity(se);
			HttpResponse response = httpClient.execute(put);
			response.getEntity().consumeContent();

			if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("Failed : HTTP error code : "+ response.getStatusLine().getStatusCode());
				String error = String.valueOf(response.getStatusLine().getStatusCode());
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						(response.getEntity().getContent())));
				/*
				 * while((errorOutput = br2.readLine()) != null){ error =
				 * errorOutput; }
				 */
				br2.close();
				throw new Exception(error.toString());
			} else {

				logger.info("Record is Updated Successfully in local System");
				logger.info("Responce data from local System is ... ......"+ response.getStatusLine().getStatusCode());
				logger.info("Responce content  data from local System is......."+ response.getEntity().getContent());
			}

		} catch (IOException e) {
			logger.error("IOException : " + e.getMessage()
					+ ". verify the local system server running or not");
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e1) {
				logger.error("thread is Interrupted for the : "
						+ e1.getCause().getLocalizedMessage());
			}
		} catch (Exception e) {
			logger.error("Exception : " + e.getLocalizedMessage());
		}

	}

	
	
}
