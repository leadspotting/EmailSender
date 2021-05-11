package com.leadspotting.emailSender.models;

public class UserGeo {
	private int clientId;
	private double lat;
	private double lng;
	private double radius;
	public int getClientId() {
		return clientId;
	}
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getRadius() {
		return radius;
	}
	public void setRadius(double radius) {
		this.radius = radius;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserGeo [clientId=").append(clientId).append(", lat=").append(lat).append(", lng=").append(lng)
				.append(", radius=").append(radius).append("]");
		return builder.toString();
	}
	
}
