package com.holo;

import java.util.Collection;

public class RequestMessage {
		
    private String productionId;
	private String productionUnitMasterId;
	private String lineNameId;
	private String companyMasterId;
	private String productMasterId;
	private String size;
	private String packType;
	private String batchNumber;
	private String productCategory;
	private String approvedBy;
	private String locale="en";
	//private Collection<ProductionLineDetail> productionLineDetails;
	public String getProductionId() {
		return productionId;
	}
	public void setProductionId(String productionId) {
		this.productionId = productionId;
	}
	public String getProductionUnitMasterId() {
		return productionUnitMasterId;
	}
	public void setProductionUnitMasterId(String productionUnitMasterId) {
		this.productionUnitMasterId = productionUnitMasterId;
	}
	public String getLineNameId() {
		return lineNameId;
	}
	public void setLineNameId(String lineNameId) {
		this.lineNameId = lineNameId;
	}
	public String getCompanyMasterId() {
		return companyMasterId;
	}
	public void setCompanyMasterId(String companyMasterId) {
		this.companyMasterId = companyMasterId;
	}
	public String getProductMasterId() {
		return productMasterId;
	}
	public void setProductMasterId(String productMasterId) {
		this.productMasterId = productMasterId;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getPackType() {
		return packType;
	}
	public void setPackType(String packType) {
		this.packType = packType;
	}
	public String getBatchNumber() {
		return batchNumber;
	}
	public void setBatchNumber(String batchNumber) {
		this.batchNumber = batchNumber;
	}
	public String getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}
	public String getApprovedBy() {
		return approvedBy;
	}
	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	/*public Collection<ProductionLineDetail> getProductionLineDetails() {
		return productionLineDetails;
	}
	public void setProductionLineDetails(
			Collection<ProductionLineDetail> productionLineDetails) {
		this.productionLineDetails = productionLineDetails;
	}
	*/
	


}
