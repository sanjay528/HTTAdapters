package com.holo;

public class ProcessRequestData {

	private Long id;
	private String entityName;
	private String entityType;
	private String requestUrl;
	private String requestDate;
	private String requestMessage;
	private String responceMessage;
	private String responceStatus;
	private String responceDate;
	private String requestAction;
	private String baseUrl;
	private String userName;
	private String password;
	private String resourceId;

	// private RequestMessage requestMessageData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}

	public String getResponceMessage() {
		return responceMessage;
	}

	public void setResponceMessage(String responceMessage) {
		this.responceMessage = responceMessage;
	}

	public String getResponceStatus() {
		return responceStatus;
	}

	public void setResponceStatus(String responceStatus) {
		this.responceStatus = responceStatus;
	}

	public String getResponceDate() {
		return responceDate;
	}

	public void setResponceDate(String responceDate) {
		this.responceDate = responceDate;
	}

	/*
	 * public RequestMessage getRequestMessageData() { return
	 * requestMessageData; } public void setRequestMessageData(RequestMessage
	 * requestMessageData) { this.requestMessageData = requestMessageData; }
	 */
	public String getRequestMessage() {
		return requestMessage;
	}

	public void setRequestMessage(String requestMessage) {
		this.requestMessage = requestMessage;
	}

	public String getRequestAction() {
		return requestAction;
	}

	public void setRequestAction(String requestAction) {
		this.requestAction = requestAction;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

}
