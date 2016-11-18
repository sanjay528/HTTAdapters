package com.printersend;

import org.apache.commons.configuration.PropertiesConfiguration;

public class MainAdapterThread {

	private static PropertiesConfiguration prop;
	public static void main(String[] args) {
		

		try {
				prop = new PropertiesConfiguration("printer.ini");
			System.out.println("Adapter Started...");

			//HologramPrinterProducer p = new HologramPrinterProducer(prop);
			HologramPrinterConsumer c = new HologramPrinterConsumer(prop);
            
			
			//Thread t1 = new Thread(p);
			Thread t2 = new Thread(c);
			//t1.start();
			t2.start();

		} catch (Exception exception) {
			System.out.println("failure : throwing " + exception.getClass().getSimpleName());
		
		}
		
	}
		
}
