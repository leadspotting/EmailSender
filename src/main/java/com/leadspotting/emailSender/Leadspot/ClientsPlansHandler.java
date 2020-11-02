package com.leadspotting.emailSender.Leadspot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.leadspotting.commons.models.AppId;
import com.leadspotting.commons.models.SendEmailRequest;
import com.leadspotting.commons.models.Template;
import com.leadspotting.commons.services.EmailServerClient;
import com.leadspotting.emailSender.CommonQueries;
import com.leadspotting.emailSender.Handler;
import com.leadspotting.emailSender.SniperDB;
import com.leadspotting.emailSender.models.Client;
import com.leadspotting.emailSender.models.ClientPlan;
import com.leadspotting.emailSender.models.Plan;

public class ClientsPlansHandler implements Handler {
	
	public static void main(String[] args) {
		SniperDB.connectToDB();
		new ClientsPlansHandler().handle(SniperDB.getConnectionFromPool());
	}

	public void handle(Connection c) {
		List<Client> allClients = CommonQueries.getAppClients(c, AppId.LeadSpot);
		// removing LeadsPortal Clients
		allClients = allClients.stream().filter(client -> !client.getClientApps().contains(AppId.LeadsOnDemand))
				.collect(Collectors.toList());
		List<Plan> allPlans = CommonQueries.getAllPlans(c);
		for (Client client : allClients) {
			ClientPlan cp = CommonQueries.getClientPlan(c, client.getId());
			Plan plan = cp.getPlan();
			int index = allPlans.indexOf(plan);
			// Don't send recommendation email for enterprise users.
			if (index == allPlans.size() - 1 || index == allPlans.size() - 2)
				continue;
			String planName = plan.getName();
			if (cp.getSubscriptionType().equalsIgnoreCase("monthly"))
				sendPlanPeriodRecommendationEmail(client, planName);
			sendPlanTypeRecommendationEmail(client, planName, allPlans.get(index + 1).getName());
			System.out.println();
		}
	}

	public void sendPlanPeriodRecommendationEmail(Client client, String currentPlan) {
		SendEmailRequest request = new SendEmailRequest.Builder().setHeader("Offer to upgrade your LeadSpot plan")
				.setAppId(AppId.LeadSpot).setTemplate(Template.PLAN_UPGRADE_RECOMMENDATION_PERIOD)
				.setRecevier(client.getEmailAddress()).addValue("userName", client.getName())
				.addValue("currentPlan", currentPlan).addValue("appURL", AppId.getAppDefaultHost(AppId.LeadsOnDemand))
				.build();
		System.out.println(request);
//		EmailServerClient.sendRequest(request);
	}

	public void sendPlanTypeRecommendationEmail(Client client, String currentPlan, String recommendedPlan) {
		SendEmailRequest request = new SendEmailRequest.Builder().setHeader("Offer to upgrade your LeadSpot plan")
				.setAppId(AppId.LeadSpot).setTemplate(Template.PLAN_UPGRADE_RECOMMENDATION_TYPE)
				.setRecevier(client.getEmailAddress()).addValue("userName", client.getName())
				.addValue("currentPlan", currentPlan).addValue("recommendedPlan", recommendedPlan)
				.addValue("appURL", AppId.getAppDefaultHost(AppId.LeadsOnDemand)).build();
		System.out.println(request);

		EmailServerClient.sendRequest(request);
	}


}
