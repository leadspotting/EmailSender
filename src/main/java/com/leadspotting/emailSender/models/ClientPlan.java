package com.leadspotting.emailSender.models;

import java.time.LocalDate;

public class ClientPlan {
	int internalSubscriptionId;
	Plan plan;
	String subscriptionType;
	LocalDate planStart;
	LocalDate planEnd;
	
	public String getSubscriptionType() {
		return subscriptionType;
	}
	public void setSubscriptionType(String subscriptionType) {
		this.subscriptionType = subscriptionType;
	}
	
	public LocalDate getPlanStart() {
		return planStart;
	}
	public void setPlanStart(LocalDate planStart) {
		this.planStart = planStart;
	}
	public LocalDate getPlanEnd() {
		return planEnd;
	}
	public void setPlanEnd(LocalDate planEnd) {
		this.planEnd = planEnd;
	}
	public Plan getPlan() {
		return plan;
	}
	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	public int getInternalSubscriptionId() {
		return internalSubscriptionId;
	}
	public void setInternalSubscriptionId(int internalSubscriptionId) {
		this.internalSubscriptionId = internalSubscriptionId;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClientPlan [internalSubscriptionId=").append(internalSubscriptionId).append(", plan=")
				.append(plan).append(", subscriptionType=").append(subscriptionType).append(", planStart=")
				.append(planStart).append(", planEnd=").append(planEnd).append("]");
		return builder.toString();
	}
	
	
}
