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

public class CRMHandler implements Handler {
	private LocalDate lastMonth;
	{
		LocalDate now = LocalDate.now();
		lastMonth = now.minusMonths(1);

	}

	public static void main(String[] args) {
		SniperDB.connectToDB();
		CRMHandler h = new CRMHandler();
		h.handle(SniperDB.getConnectionFromPool());
		SniperDB.closeConnection();
		System.exit(1);
	}

	@Override
	public void handle(Connection c) {
		List<Client> clients = CommonQueries.getAppClients(c, AppId.LeadSpot);
		// removing LeadsPortal Clients
		clients = clients.stream().filter(client -> !client.getClientApps().contains(AppId.LeadsOnDemand))
				.collect(Collectors.toList());
		List<Client> crmNotUsedClients = new LinkedList<>();
		List<Client> noNewLeadsClients = new LinkedList<>();
		List<Client> hasOverDueClients = new LinkedList<>();
		for (Client client : clients) {
			LocalDate registerDate = client.getRegisterTime();
			if (lastMonth.isEqual(registerDate)) {
				System.out.println(client);
				boolean crmNotUsed = crmNotUsed(c, client);
				if (crmNotUsed) {
					crmNotUsedClients.add(client);
					continue;
				}
				boolean noNewLeads = noNewLeads(c, client);
				if (noNewLeads)
					noNewLeadsClients.add(client);
			}
			
			boolean hasOverdueLeads = hasOverdueLeads(c, client);
			if (hasOverdueLeads)
				hasOverDueClients.add(client);
		}
		sendEmailToCients(crmNotUsedClients, "Start using LeadSpot CRM", Template.CRM_NOT_USED);
		sendEmailToCients(noNewLeadsClients, "Continue using LeadSpot CRM", Template.NO_NEW_LEADS);
		sendEmailToCients(hasOverDueClients, "You have overdue leads in LeadSpot CRM", Template.CRM_OVERDUE_LEADS);
	}

	private void sendEmailToCients(List<Client> clients, String header, Template template) {
		clients.forEach(client -> sendEmail(client, header, template));
	}

	private void sendEmail(Client client, String header, Template template) {
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.LeadSpot).setHeader(header)
				.setRecevier(client.getEmailAddress()).setTemplate(template).addValue("userName", client.getName())
				.build();
		System.out.println(request);
		EmailServerClient.sendRequest(request);
	}

	private boolean crmNotUsed(Connection c, Client client) {
		final String query = "SELECT cc.id\r\n" + "FROM lead_spot.client_customer cc\r\n"
				+ "WHERE cc.client_id = ? \r\n" + "LIMIT 1;";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, client.getId());
			System.out.println(stmt.toString());
			ResultSet res = stmt.executeQuery();
			return res.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}

	private boolean noNewLeads(Connection c, Client client) {
		final String query = "SELECT cc.id\r\n" + "FROM lead_spot.client_customer cc\r\n"
				+ "WHERE cc.client_id = ? \r\n"
				+ "AND cc.active = 1 AND cc.creation_time > DATE_SUB(NOW(),INTERVAL 1 MONTH)\r\n" + "LIMIT 1;";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, client.getId());
			System.out.println(stmt);
			ResultSet res = stmt.executeQuery();
			return !res.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean hasOverdueLeads(Connection c, Client client) {
		final String query = "SELECT cc.id\r\n" + "FROM lead_spot.client_customer cc\r\n"
				+ "INNER JOIN lead_spot.customer_management_data cmd ON cc.id = cmd.customer_id\r\n"
				+ "WHERE cc.client_id = ? AND cc.active = 1 AND cc.action_date = DATE(NOW()); ";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, client.getId());
			System.out.println(stmt.toString());
			ResultSet res = stmt.executeQuery();
			return res.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
