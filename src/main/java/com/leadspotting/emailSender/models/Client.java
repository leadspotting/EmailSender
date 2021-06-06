package com.leadspotting.emailSender.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.leadspotting.commons.models.AppId;

public class Client {
	private int id;
	private String name;
	private String emailAddress;
	private LocalDate lastLogin;
	private LocalDateTime  registerTime;
	private List<AppId> clientApps;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public LocalDate getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(LocalDate lastLogin) {
		this.lastLogin = lastLogin;
	}
	public LocalDateTime getRegisterTime() {
		return registerTime;
	}
	public void setRegisterTime(LocalDateTime registerTime) {
		this.registerTime = registerTime;
	}
	public List<AppId> getClientApps() {
		return clientApps;
	}
	public void setClientApps(List<AppId> clientApps) {
		this.clientApps = clientApps;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Client [id=").append(id).append(", name=").append(name).append(", emailAddress=")
				.append(emailAddress).append(", lastLogin=").append(lastLogin).append(", registerTime=")
				.append(registerTime).append(", clientApps=").append(clientApps).append("]");
		return builder.toString();
	}
	
}
