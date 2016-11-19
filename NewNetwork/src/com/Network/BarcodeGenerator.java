package com.Network;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

import org.apache.log4j.Logger;

public class BarcodeGenerator {static Logger logger = Logger.getLogger("");
SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("ddMMyyyy");
private String barcode;
String Companycode,Product, batch, size, brandName,cottonSize;
Long Productionunit;
BigDecimal mrp;
private String lineid;

public String Genarator(Queue<HologramProcessRequestData> queue, int uid)  {
	String resultCommand = null;
	for (HologramProcessRequestData lineData : queue) {
		int countryCode = 890;
		Companycode = lineData.getCompanyCode().substring(0, 3);
		Product =  String.format("%0" + 4 + "d", Long.parseLong(lineData.getProductCode().substring(0, 3)));
		Productionunit = lineData.getProductionUnitMasterId();
		batch = lineData.getBatchNumber();
		cottonSize = String.valueOf(lineData.getCottonSize()).substring(0, 2);
		size = lineData.getSize().substring(0,3);
		mrp = lineData.getMrp();
		brandName = lineData.getBrandName();
		Date date = new Date();
		String datestring = SIMPLEDATEFORMAT.format(date);
		String unique = String.format("%0" + 5 + "d", uid);
		lineid = String.format("%0" + 3 + "d", lineData.getLineNameId());

//					3			3				3			4		5		3		2					8				3					5	
		barcode = countryCode+Companycode + Productionunit+Product +batch+size+cottonSize+datestring+lineid+ unique;
		logger.info(barcode.length());
		logger.info(barcode);
		//barcode = countryCode+"-"+Companycode+"-" + Productionunit+"-" +Product +"-" +batch+"-" +datestring +"-" + unique;
		//JDI|1|Var1="+sanju+"|Var2=DIR|Var3=750ML|Var4=BN0001|Var5=RS.100/-|Var6=21092016|\r
//		resultCommand = "JDI|1|Var1="+barcode+"|Var2="+brandName+"|Var3="+size+"|Var4="+batch+"|Var5=RS."+mrp+"|Var6="+datestring+"|\r";
		resultCommand = "JDI|1|Var1-1="+barcode+"|Var2-1="+brandName+"|Var3-1="+size+"|Var4-1="+batch+"|Var5-1=RS."+mrp+"|Var6-1="+datestring+"|Var9-1="+lineData.getCottonSize()+"|\r";

		System.out.println("resultCommand: "+resultCommand);
	}
	return resultCommand;
}
}
