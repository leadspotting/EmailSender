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
import com.leadspotting.emailSender.Handler;
import com.leadspotting.emailSender.SniperDB;
import com.leadspotting.emailSender.models.Client;
import com.leadspotting.emailSender.models.ClientPlan;

public class QuotaRenewdHandler implements Handler {
	public static void main(String[] args) {
		SniperDB.connectToDB();
		new QuotaRenewdHandler().handle(SniperDB.getConnectionFromPool());
	}

	@Override
	public void handle(Connection c) {
		List<Client> allClients = CommonQueries.getAppClients(c, AppId.LeadSpot);
		// removing LeadsPortal Clients
		allClients = allClients.stream().filter(client -> !client.getClientApps().contains(AppId.LeadsOnDemand))
				.collect(Collectors.toList());
		LocalDate today = LocalDate.now();
		for (Client client : allClients) {
			ClientPlan cp = CommonQueries.getClientPlan(c, client.getId());
			if (today.isEqual(cp.getPlanEnd())) {
				String subType = cp.getSubscriptionType();
				LocalDate newDate = subType.equalsIgnoreCase("monthly") ? today.plusMonths(1) : today.plusYears(1);
				System.out.println(client);
				System.out.println(newDate);
				System.out.println();
				updatePlanEnd(c, cp.getInternalSubscriptionId(), newDate);
				updateQuota(c, client.getId());
				sendResetSuccessfullEmail(client,cp);
			}

		}
	}

	public void sendResetSuccessfullEmail(Client client, ClientPlan cp) {
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.LeadSpot)
				.setTemplate(Template.QUOTA_RENEWED).setHeader("Your LeadSpot quota is renewed)")
				.setRecevier(client.getEmailAddress()).addValue("userName", client.getName())
				.addValue("planType", cp.getSubscriptionType()).addValue("planName", cp.getPlan().getName())
				.addValue("appURL", AppId.getAppDefaultHost(AppId.LeadsOnDemand)).build();
		EmailServerClient.sendRequest(request);

	}

	public void updateQuota(Connection c, int clientId) {
		final String query = "SELECT contact_enrichment,lead_generation,linkedin_plugin_exports,"
				+ "company_search,company_contact_discovery " + " FROM lead_spot.client_quota WHERE client_id = ? ";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				int contactEnrichment = res.getInt("contact_enrichment");
				int leadGeneration = res.getInt("lead_generation");
				int linkedIn = res.getInt("linkedin_plugin_exports");
				int companySearch = res.getInt("company_search");
				int contactDiscovery = res.getInt("company_contact_discovery");
				saveQuotaToHistory(c, clientId, contactEnrichment, leadGeneration, linkedIn, companySearch,
						contactDiscovery);
				resetQuota(c, clientId);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void resetQuota(Connection c, int clientId) {
		final String query = "REPLACE INTO lead_spot.client_quota(client_id) VALUES(?)";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveQuotaToHistory(Connection c, int clientId, int contactEnrichment, int leadGeneration, int linkedIn,
			int companySearch, int contactDiscovery) {
		final String query = "INSERT INTO lead_spot.client_previous_quota(client_id,"
				+ "expired_date,contact_enrichment,lead_generation,linkedin_plugin_exports,"
				+ "company_search,company_contact_discovery) VALUES(?,?,?,?,?,?,?)";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			stmt.setDate(2, Date.valueOf(LocalDate.now()));
			stmt.setInt(3, contactEnrichment);
			stmt.setInt(4, leadGeneration);
			stmt.setInt(5, linkedIn);
			stmt.setInt(6, companySearch);
			stmt.setInt(7, contactDiscovery);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void updatePlanEnd(Connection c, int internalSubscriptionId, LocalDate newDate) {
		final String query = "UPDATE lead_spot.client_plan SET plan_end = ? WHERE internal_subscription_id = ?";
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setDate(1, Date.valueOf(newDate));
			stmt.setInt(2, internalSubscriptionId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
