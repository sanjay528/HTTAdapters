package com.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import javax.management.RuntimeErrorException;
import javax.security.sasl.AuthenticationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class HologramNetworkPrinterCommandProducer implements Runnable {

	private HttpClient client;
	private String lineid;
	private Queue<HologramProcessRequestData> messageQueue;
	private String tenantIdentifier;
	private int wait;
	private String encodePassword;
	private HttpGet getRequest;
	private BarcodeGenerator barcodeGenerator;
	private int uid =1;
	private String barcodeResult;
	private String errorMessage;
	private int no;
	private HttpResponse response;
	private BufferedReader br;
	private static Gson gsonConverter = new Gson();
	static Logger logger = Logger.getLogger("");

	public HologramNetworkPrinterCommandProducer(Queue<HologramProcessRequestData> queue,PropertiesConfiguration prop, HttpClient httpClient) throws IOException {

		client = httpClient;
		lineid = prop.getString("lineId");
		this.messageQueue = queue;
		tenantIdentifier = prop.getString("tenantIdentfier");
		wait = prop.getInt("adapterSleepPeriod");
		String credentials = prop.getString("username") + ":" + prop.getString("password");
		encodePassword = new String(Base64.encodeBase64(credentials.getBytes()));
		getRequest = setRequiredPostUrl(prop.getString("getlinedataurl").trim() + "/" + lineid );
		readDataFromRestfulService();
	}

	private void readDataFromRestfulService() {

		try {
			no = 1;
			response = client.execute(getRequest);
			if (response.getStatusLine().getStatusCode() == 401) {
				logger.error("Authentication Failed : HTTP error code is: " + response.getStatusLine().getStatusCode());
				client.getConnectionManager().shutdown();
				throw new AuthenticationException(
						"AuthenticationException :  BSS system server username (or) password you entered is incorrect . check in the PacketspanIntegrator.ini file");
			} else if (response.getStatusLine().getStatusCode() == 404) {
				logger.error("Resource Not Found Exception : HTTP error code is: "
						+ response.getStatusLine().getStatusCode());
				client.getConnectionManager().shutdown();
				throw new RuntimeErrorException(null,
						"Resource NotFound Exception :  BSS server system 'BSSServerQuery' url error.");
			} else if (response.getStatusLine().getStatusCode() != 200) {
				logger.error("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
				String errorOutput, error = "";
				BufferedReader br1 = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				while ((errorOutput = br1.readLine()) != null) {
					error = errorOutput;
				}
				br1.close();
				throw new Exception(error);
			}

			br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String output;
			while ((output = br.readLine()) != null) {
				List<HologramProcessRequestData> postsList = Arrays
						.asList(gsonConverter.fromJson(output, HologramProcessRequestData.class));
				int length = postsList.size();
				if (length > 0) {
					for (HologramProcessRequestData entitlement : postsList) {
						setDataForEntitlement(entitlement);
					}
				}

			}

			br.close();

		} catch (ClientProtocolException e) {
			errorMessage = "ClientProtocolException : " + e.getCause()+ ". Please Verify the HttpClient.execute() Method";
			//MainAdapter.sendToUserEmail("Hi user", errorMessage);
			logger.error("ClientProtocolException : " + e.getCause().getLocalizedMessage());

		} catch (IOException e) {
			errorMessage = "IOException : " + e.getCause() + ". verify the BSS system server running or not";
			//MainAdapter.sendToUserEmail("Hi user", errorMessage);
			logger.error(errorMessage);
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e1) {
				logger.error("thread is Interrupted for the : " + e1.getCause().getLocalizedMessage());
			}

		} catch (RuntimeErrorException e) {

			errorMessage = "RuntimeErrorException : " + e.getCause();
			logger.info("##################################################################################");
			logger.info("");
			logger.error("RuntimeErrorException: " + e.getLocalizedMessage());
			logger.info("");
			logger.info("##################################################################################");
			logger.info("");
			logger.info("Stoping Adapter...");
			System.exit(0);

		} catch (Exception e) {
			errorMessage = "Exception : " + e.getCause();
			logger.error("Exception: " + e.getLocalizedMessage());
		}

	}

	private void setDataForEntitlement(HologramProcessRequestData entitlement) {
		logger.info("productioId: " + entitlement.getId() + "  productionUnit:" + entitlement.getProductCode());
		messageQueue.offer(entitlement);
		no = no + 1;
		
	}

	private HttpGet setRequiredPostUrl(String getRequest) {
		HttpGet postRequest = new HttpGet(getRequest);
		postRequest.addHeader("Authorization", "Basic " + encodePassword);
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.addHeader("Hologram-TrackTrace-TenantId", tenantIdentifier);
		return postRequest;
	}

	@Override
	public void run() {

		try {
			logger.info("Produce() class calling ...");
			producer();
			Thread.sleep(wait);
		} catch (Exception e) {
			logger.error("thread is Interrupted for the : "
					+ e.getCause().getLocalizedMessage());
		}

	}

	private void producer() {

		readDataFromRestfulService();
	}

	

}
