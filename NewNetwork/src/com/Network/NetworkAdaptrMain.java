package com.Network;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class NetworkAdaptrMain {

	private static final Logger logger = Logger.getLogger(NetworkAdaptrMain.class);

	private static PropertiesConfiguration prop;

	private static HttpClient httpClient = new DefaultHttpClient();;

	public static void main(String[] args) {

		try {
			
			Queue<HologramProcessRequestData> sharedQueue = new ConcurrentLinkedQueue<HologramProcessRequestData>();
			prop = new PropertiesConfiguration("hologramNetworkPrinterIntegrator.ini");
			logger.info("Adapter Started...");

			final String logPath = prop.getString("LogFilePath");
			File filelocation = new File(logPath);
			if (!filelocation.isDirectory()) {
				filelocation.mkdirs();
			}

			Logger logger = Logger.getRootLogger();
			FileAppender appender = (FileAppender) logger.getAppender("fileAppender");
			appender.setFile(logPath + "/hologramIntegrator.log");
			appender.activateOptions();
			httpClient = wrapClient(httpClient);

			HologramNetworkPrinterCommandProducer producer = new HologramNetworkPrinterCommandProducer(sharedQueue,prop,httpClient);
			HologramNetworkPrinterConsumer producerC = new HologramNetworkPrinterConsumer(sharedQueue,prop,httpClient);

			Thread t1 = new Thread(producer);
			Thread t3 = new Thread(producerC);

			t1.start();
			t3.start();
		} catch (ConfigurationException e) {
			logger.error("(ConfigurationException) Properties file loading error.... : " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exception : " + e.getMessage());

		}

	}

	public static HttpClient wrapClient(HttpClient base) {

		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@SuppressWarnings("unused")
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				@SuppressWarnings("unused")
				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
						throws java.security.cert.CertificateException {

				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
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
