package com.leadspotting.emailSender.models;

public class Blue12ReportAlert {
	private long id;
	private String title;
	private String source;
	private String date;
	private String url;
	private double lat;
	private double lng;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Blue12ReportAlert [id=").append(id).append(", title=").append(title).append(", source=")
				.append(source).append(", date=").append(date).append(", url=").append(url).append(", lat=").append(lat)
				.append(", lng=").append(lng).append("]");
		return builder.toString();
	}
	
}
