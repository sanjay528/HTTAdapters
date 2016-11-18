package com.printerfile;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class PrinterAdpaterMain {

	static Logger logger = Logger.getLogger("");
	private static PropertiesConfiguration prop;
	static HttpClient httpClient = new DefaultHttpClient();

	
	public static void main(String[] args) throws Exception {
		try {
			Queue<HologramProcessRequestData> queue = new ConcurrentLinkedQueue<HologramProcessRequestData>();
			prop = new PropertiesConfiguration("prnfile.ini");
			System.out.println("Adapter Started...");

			String logPath=prop.getString("LogFilePath");
			File filelocation = new File(logPath);			
			if(!filelocation.isDirectory()){
				filelocation.mkdirs();
			}
			Logger logger = Logger.getRootLogger();
			FileAppender appender = (FileAppender)logger.getAppender("fileAppender");
			appender.setFile(logPath+"/hologramIntegrator.log");
			appender.activateOptions();
			httpClient = wrapClient(httpClient);
			HologramPrinterCommandProducer p = new HologramPrinterCommandProducer(queue,prop,httpClient);
			//HologramPrinterCommandConusmer c = new HologramPrinterCommandConusmer(prop);
            
			Thread t1 = new Thread(p);
			//Thread t2 = new Thread(c);
			t1.start();
			//t2.start();

		} catch (Exception exception) {
			System.out.println("failure : throwing " + exception.getClass().getSimpleName());
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

	public static void sendToUserEmail(String string, String errorMessage) {
		// TODO Auto-generated method stub
		
	}


}
