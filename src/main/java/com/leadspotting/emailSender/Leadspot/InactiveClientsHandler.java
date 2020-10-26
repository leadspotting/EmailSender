package com.leadspotting.emailSender.Leadspot;

import java.sql.Connection;
import java.sql.Date;
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
import com.leadspotting.emailSender.SniperDB;
import com.leadspotting.emailSender.models.Client;

public class InactiveClientsHandler implements Handler {
	private LocalDate yesterday;
	private LocalDate lastWeek;
	private LocalDate beforeTwoWeeks;
	private LocalDate lastMonth;
	{
		LocalDate now = LocalDate.now();
		yesterday = now.minusDays(1);
		lastWeek = now.minusWeeks(1);
		beforeTwoWeeks = now.minusWeeks(2);
		lastMonth = now.minusMonths(1);

	}

	public static void main(String[] args) {
		InactiveClientsHandler rbi = new InactiveClientsHandler();
		SniperDB.connectToDB();
		rbi.handle(SniperDB.getConnectionFromPool());
		SniperDB.closeConnection();
	}

	public void handle(Connection c) {
		List<Client> allClients = CommonQueries.getAppClients(c, AppId.LeadSpot);
		// removing LeadsPortal Clients
		allClients = allClients.stream().filter(client -> !client.getClientApps().contains(AppId.LeadPortal))
				.collect(Collectors.toList());
		handleRegisteredInactive(c, allClients);
		handleInactive(c, allClients);
	}

	

	/**
	 * handles users that last logged in to the system exactly 2 weeks ago or 1
	 * month ago .
	 * 
	 * @param c
	 * @param allClients
	 */
	private void handleInactive(Connection c, List<Client> allClients) {
		List<Client> loggedInBeforeLastMonth = new LinkedList<>();
		List<Client> loggedInBeforeTwoWeeks = new LinkedList<>();
		for (Client client : allClients) {
			LocalDate lastLogin = client.getLastLogin();
			// this condition indicates that the user has not logged in, which is handled by
			// the handleRegisteredInactive method.
			if (lastLogin == null)
				continue;
			if (lastLogin.isEqual(lastMonth))
				loggedInBeforeLastMonth.add(client);
			if (lastLogin.isEqual(beforeTwoWeeks)) {
				loggedInBeforeTwoWeeks.add(client);
			}
		}
		loggedInBeforeLastMonth.forEach(client -> {
			sendEmail(client, Template.INACTIVE_1_MONTH,"Leads are waiting for you in LeadSpot");
		});
		loggedInBeforeTwoWeeks.forEach(client -> {
			sendEmail(client, Template.INACTIVE_2_WEEKS,"Leads are waiting for you in LeadSpot");
		});
	}

	/**
	 * handles users that did not login to the system at all, and registered exactly
	 * ( 1 day ago || 1 week ago || 1 month ago).
	 * 
	 * @param c
	 * @param allClients
	 */
	private void handleRegisteredInactive(Connection c, List<Client> allClients) {

		List<Client> registeredYesterdayClients = new LinkedList<>();
		List<Client> registeredLastWeekClients = new LinkedList<>();
		List<Client> registeredLastMonthClients = new LinkedList<>();

		for (Client client : allClients) {
			LocalDate registerTime = client.getRegisterTime();
			LocalDate lastLogin = client.getLastLogin();
			// if the user logged in to the system, then this handler won't send an email.
			if (lastLogin != null)
				continue;

			if (yesterday.isEqual(registerTime))
				registeredYesterdayClients.add(client);
			if (lastWeek.isEqual(registerTime))
				registeredLastWeekClients.add(client);
			if (lastMonth.isEqual(registerTime))
				registeredLastMonthClients.add(client);
		}
		registeredYesterdayClients.forEach(client -> {
//			boolean noActivity = checkClientActivity(c, client.getId(), yesterday);
//			if (noActivity)
			sendEmail(client, Template.REGISTERED_BUT_INACTIVE,"Reminder to start using LeadSpot");

		});
		registeredLastWeekClients.forEach(client -> {
//			boolean noActivity = checkClientActivity(c, client.getId(), lastWeek);
//			if (noActivity)
			sendEmail(client, Template.REGISTERED_BUT_INACTIVE,"Reminder to start using LeadSpot");
		});

		registeredLastMonthClients.forEach(client -> {
//			boolean noActivity = checkClientActivity(c, client.getId(), lastMonth);
//			if (noActivity)
			sendEmail(client, Template.REGISTERED_BUT_INACTIVE,"Reminder to start using LeadSpot");
		});
	}

	public void sendEmail(Client client, Template template,String emailTitle) {
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.LeadSpot).setTemplate(template)
				.setHeader(emailTitle).setRecevier(client.getEmailAddress())
				.addValue("userName", client.getName()).addValue("appURL", AppId.getAppDefaultHost(AppId.LeadSpot))
				.build();
		EmailServerClient.sendRequest(request);
	}

	public boolean checkClientActivity(Connection c, int clientId, LocalDate since) {
		return !hasNewCustomer(c, clientId, since) && !hasNewCompany(c, clientId, since)
				&& !hasQuotaUpdate(c, clientId, since);

	}
	private boolean hasNewCustomer(Connection c, int clientId, LocalDate since) {
		final String query = "SELECT * \r\n" + "FROM lead_spot.client_customer cc \r\n"
				+ "WHERE cc.client_id = ? AND cc.creation_time > ? \n" + "LIMIT 1;";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			stmt.setDate(2, Date.valueOf(since));
			System.out.println(stmt.toString());
			ResultSet res = stmt.executeQuery();
			return res.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean hasNewCompany(Connection c, int clientId, LocalDate since) {
		final String query = "SELECT * \r\n" + "FROM lead_spot.client_company cc \r\n"
				+ "WHERE cc.client_id = ? AND cc.creation_time > ? \n" + "LIMIT 1;";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			stmt.setDate(2, Date.valueOf(since));
			System.out.println(stmt.toString());
			ResultSet res = stmt.executeQuery();
			return res.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean hasQuotaUpdate(Connection c, int clientId, LocalDate since) {
		final String query = "SELECT * \r\n" + "FROM lead_spot.client_quota cc \r\n"
				+ "WHERE cc.client_id = ? AND cc.update_time > ? \n" + "LIMIT 1;";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			stmt.setDate(2, Date.valueOf(since));
			System.out.println(stmt.toString());
			ResultSet res = stmt.executeQuery();
			return res.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
