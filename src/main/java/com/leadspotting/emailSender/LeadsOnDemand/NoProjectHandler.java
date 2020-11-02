package com.leadspotting.emailSender.LeadsOnDemand;

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
import com.leadspotting.emailSender.CommonQueries;
import com.leadspotting.emailSender.Handler;
import com.leadspotting.emailSender.models.Client;

public class NoProjectHandler implements Handler {
	private LocalDate lastWeek;
	private LocalDate lastMonth;
	{
		LocalDate now = LocalDate.now();
		lastWeek = now.minusWeeks(1);
		lastMonth = now.minusMonths(1);
	}
	
	@Override
	public void handle(Connection c) {
		List<Client> allClients = CommonQueries.getAppClients(c, AppId.LeadsOnDemand);
		handleRegisteredButInactive(c, allClients);
		handleInactive(c, allClients);
	}

	private void handleRegisteredButInactive(Connection c, List<Client> allClients) {
		for (Client client : allClients) {

			if (this.getLastestProjectDate(c, client) != null)
				continue;
			LocalDate registerDate = client.getRegisterTime();
			if (lastWeek.isEqual(registerDate) || lastMonth.isEqual(registerDate)) {
				sendRegisteredButInactive(client);
			}
		}
	}

	private void handleInactive(Connection c, List<Client> allClients) {
		for (Client client : allClients) {
			LocalDate latestProjectDate = this.getLastestProjectDate(c, client);
			if (latestProjectDate == null)
				continue;
			if (lastMonth.isEqual(latestProjectDate)) {
				sendInactiveEmail(client);
			}

		}
	}

	private void sendInactiveEmail(Client client) {
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.LeadsOnDemand)
				.setTemplate(Template.REGISTERED_BUT_INACTIVE).setHeader("Reminder for using LeadsOnDemand")
				.setRecevier(client.getEmailAddress()).addValue("userName", client.getName())
				.addValue("appURL", AppId.getAppDefaultHost(AppId.LeadsOnDemand)).build();
		System.out.println(request);
		EmailServerClient.sendRequest(request);
	}

	private void sendRegisteredButInactive(Client client) {
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.LeadsOnDemand)
				.setTemplate(Template.RegisteredButDidNotCreateProject)
				.setHeader("Reminder to start using LeadsOnDemand").setRecevier(client.getEmailAddress())
				.addValue("userName", client.getName()).addValue("appURL", AppId.getAppDefaultHost(AppId.LeadsOnDemand))
				.build();
		System.out.println(request);
		EmailServerClient.sendRequest(request);
	}

	private LocalDate getLastestProjectDate(Connection c, Client client) {
		final String query = "SELECT creation_time FROM leads_portal.project WHERE client_id = ? ORDER BY id DESC LIMIT 1";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, client.getId());
			System.out.println(stmt.toString());
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				return res.getDate("creation_time").toLocalDate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
