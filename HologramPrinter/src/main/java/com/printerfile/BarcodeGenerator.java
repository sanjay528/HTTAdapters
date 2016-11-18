package com.printerfile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

import org.apache.log4j.Logger;


public class BarcodeGenerator {
	
	private String barcode;
	static Logger logger = Logger.getLogger("");
	SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("ddMMyyyy");
	String Companycode,Product, batch;
	Long Productionunit,cottonSize;
	private Boolean result;
	private String brand;
	private String quantity;
	private BigInteger price;
	private String datestring;
	public String Genarator(Queue<HologramProcessRequestData> messageQueue,	String barcodeimage, int uid) {

		for (HologramProcessRequestData lineData : messageQueue) {
			Companycode = lineData.getCompanyCode().substring(0, 3);
			Product = lineData.getProductCode().substring(0, 4);
			Productionunit = lineData.getProductionUnitMasterId();
			batch = lineData.getBatchNumber();
			cottonSize = lineData.getCottonSize();
			brand = lineData.getApprovedBy();
			quantity= lineData.getSize();
			Date date = new Date();
			datestring = SIMPLEDATEFORMAT.format(date);
			String unique = String.format("%0" + 5 + "d", uid);
			String Line = String.format("%0" + 3 + "d", lineData.getLineNameId());
			System.out.println("Uid:        "+ unique+"\n datestring: "+datestring);
			price = lineData.getMrp().toBigInteger();

			barcode = 890+Companycode + Productionunit+Product +batch+quantity.substring(0,3)+cottonSize+datestring+Line+ unique;

			result = CreatePrn(barcode, barcodeimage,unique,messageQueue);
		
	}if(result==true){
		return barcode;
	}
	else{
		return "barcode not generated";
	}

		
	}

	private Boolean CreatePrn(String barcode, String barcodeimage,	String unique, Queue<HologramProcessRequestData> messageQueue) {

		
		try {
			
			 String number = "SIZE 101.6 mm, 25.0 mm\n" +
		        		"GAP 3 mm, 0 mm\n" +
		        		"DIRECTION 0,0\n" +
		        		"REFERENCE 0,0\n" +
		        		"OFFSET 0 mm\n" +
		        		"SET PEEL ON\n" +
		        		"CLS\n" +
		        		"CODEPAGE 1252\n" +
		        		"TEXT 606,99,\"1\",180,1,1,\"Brand Name: "+ brand+"\"\n" +
		        		"TEXT 659,65,\"1\",180,1,1,\"Size: "+quantity+"\"\n" +
		        		"TEXT 420,65,\"1\",180,1,1,\"MRP: Rs."+price+"/-\"\n" +
		        		"TEXT 659,35,\"1\",180,1,1,\"BATCH: "+batch+"\"\n" +
		        		"TEXT 420,35,\"1\",180,1,1,\"MFG Date: "+datestring+"(*Trail Run)\"\n" +
		        		"BARCODE 714,188,\"128\",49,1,180,2,4,\""+barcode+"\"\n" +
		        		"QRCODE 785,101,L,3,A,180,M2,S7,\""+barcode+"\"\n" +
		        		"QRCODE 120,98,L,3,A,180,M2,S7,\""+barcode+"\"\n" +
		        		"PRINT 1,2\n" +
		        		" CLS\n";
			 
			FileWriter fileWriter = new FileWriter(barcodeimage+"/"+barcode+".prn");
			BufferedWriter bw = new BufferedWriter(fileWriter);
			bw.write(number);
			bw.flush();
			bw.close();		
			logger.info("prn file created with Name:"+barcode);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
       
		
	}

}
