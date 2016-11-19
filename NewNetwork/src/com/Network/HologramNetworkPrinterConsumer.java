package com.Network;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;

public class HologramNetworkPrinterConsumer implements Runnable {

	
	static Socket requestSocket = null;
	String message;
	private String barcodeResult;
	public static int adapterSleepPeriod;
	static Logger logger = Logger.getLogger("");
	private Queue<HologramProcessRequestData> queue;
	private int uid =1;
	private Queue<String> barcodeQueue =new ConcurrentLinkedQueue<String>();
	private BarcodeGenerator barcodeGenrator;
	private String host;
	private static Socket socket;
	public HologramNetworkPrinterConsumer(Queue<HologramProcessRequestData> queue,	PropertiesConfiguration prop, HttpClient httpClient) {
	
		this.queue =queue;
		adapterSleepPeriod = prop.getInt("adapterSleepPeriod");
		this.host= prop.getString("host");
		barcodeGenrator = new BarcodeGenerator();
		for(int i =0;i<=20;i++){
			 barcodeResult = barcodeGenrator.Genarator(queue,uid);
			 barcodeQueue.offer(barcodeResult);
			 logger.info( barcodeResult);
			 uid++;
		}
		
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
		try {
				for (String item : barcodeQueue) {
					if (barcodeQueue.size() >= 15) {
					barcodeQueue.poll();
					processRequest(item);
				}
					else {
						BarcodeGenrator();
						Thread.sleep(adapterSleepPeriod);
					}
			} 
		} catch (InterruptedException e) {
			e.printStackTrace();
			run();
		}

	}
	private void processRequest(String jobupdate) {

        try
        {
            int port = 1000;
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
           
          
           String sendMessage = jobupdate ;
            bw.write(sendMessage);
            bw.flush();
            logger.info("Message sent to the server : "+sendMessage);
           
          //Get the return message from the server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
           logger.info("Message received from the server : " +message);
           
        //}
        }
        catch (Exception exception)
        {
            exception.printStackTrace();

        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
                Thread.sleep(10000);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
	
	}


	private void BarcodeGenrator() throws InterruptedException {
		barcodeGenrator = new BarcodeGenerator();
		for (int i = 0; i <= 5; i++) {
			barcodeResult = barcodeGenrator.Genarator(queue,uid);
			barcodeQueue.offer(barcodeResult);
				uid++;
				run();
			}

	}

}
