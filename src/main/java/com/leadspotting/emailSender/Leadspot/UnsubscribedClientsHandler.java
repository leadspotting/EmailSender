package com.leadspotting.emailSender.Leadspot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.leadspotting.commons.models.AppId;
import com.leadspotting.commons.models.SendEmailRequest;
import com.leadspotting.commons.models.Template;
import com.leadspotting.commons.services.EmailServerClient;
import com.leadspotting.commons.utils.CollectionsUtils;
import com.leadspotting.emailSender.CommonQueries;
import com.leadspotting.emailSender.models.Client;
import com.leadspotting.emailSender.models.ClientPlan;

public class UnsubscribedClientsHandler implements Handler {
	private LocalDate lastWeek;
	private LocalDate lastMonth;
	{
		LocalDate now = LocalDate.now();
		lastWeek = now.minusWeeks(1);
		lastMonth = now.minusMonths(1);

	}

	@Override
	public void handle(Connection c) {
		List<Client> allClients = CommonQueries.getAppClients(c, AppId.LeadSpot);

		for (Client client : allClients) {
			LocalDate unsubscribedDate = getUnsbscribedDate(c, client.getId());
			// did not unsubscribe.
			if (unsubscribedDate == null)
				continue;
			ClientPlan currentPlan = CommonQueries.getClientPlan(c, client.getId());
			// has subscribed again
			if (currentPlan.getPlan().getId() > 1)
				continue;
			if (lastWeek.isEqual(unsubscribedDate) || lastMonth.isEqual(unsubscribedDate)) {

			}
		}
	}

	public void sendUnsubscribedEmail(Client client) {
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.LeadSpot)
				.setTemplate(Template.UNSUBSCRIBED_1_WEEK_1_MONTH).setHeader("We are sorry to see you go...")
				.setRecevier(client.getEmailAddress()).setValues(CollectionsUtils.mapOf("userName", client.getName()))
				.build();
		EmailServerClient.sendRequest(request);
	}

	public LocalDate getUnsbscribedDate(Connection c, int clientId) {
		final String query = "SELECT cs.canceled_date " + "FROM lead_spot.canceled_subscriptions cs "
				+ "INNER JOIN lead_spot.client_plan cp ON cs.internal_subscription_id = cp.internal_subscription_id "
				+ "WHERE cp.client_id = ? " + "ORDER by internal_subscription_id DESC " + "LIMIT 1";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				return res.getDate("canceled_date").toLocalDate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
