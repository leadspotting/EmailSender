package com.leadspotting.emailSender;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.leadspotting.commons.models.AppId;
import com.leadspotting.emailSender.models.Client;
import com.leadspotting.emailSender.models.ClientPlan;
import com.leadspotting.emailSender.models.Plan;

public class CommonQueries {
	public static List<Plan> getAllPlans(Connection c) {
		final String query = "SELECT p.id,p.name FROM lead_spot.plans p";
		List<Plan> plans = new LinkedList<>();
		try (ResultSet res = c.prepareCall(query).executeQuery()) {
			while (res.next()) {
				Plan plan = new Plan();
				plan.setId(res.getInt("id"));
				plan.setName(res.getString("name"));
				plans.add(plan);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return plans;
	}

	public static ClientPlan getClientPlan(Connection c, int clientId) {
		final String query = "SELECT p.id,p.name,"
				+ "cp.internal_subscription_id,cp.subscription_type,cp.plan_start,cp.plan_end\r\n"
				+ "FROM lead_spot.client_plan cp\r\n" + "INNER JOIN lead_spot.plans p ON cp.plan_id = p.id\r\n"
				+ "WHERE cp.client_id = ?\r\n" + "ORDER BY internal_subscription_id DESC LIMIT 1;";
		ClientPlan cp = new ClientPlan();
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, clientId);
			ResultSet res = stmt.executeQuery();
			if (res.next()) {
				int internalSubscriptionId = res.getInt("internal_subscription_id");
				int planId = res.getInt("id");
				String name = res.getString("name");
				String subscriptionType = res.getString("subscription_type");
				LocalDate planStart = res.getDate("plan_start").toLocalDate();
				LocalDate planEnd = res.getDate("plan_end").toLocalDate();
				
				Plan plan = new Plan();
				plan.setName(name);
				plan.setId(planId);
				cp.setPlan(plan);
				cp.setInternalSubscriptionId(internalSubscriptionId);
				cp.setSubscriptionType(subscriptionType);
				cp.setPlanStart(planStart);
				cp.setPlanEnd(planEnd);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cp;
	}

	public static List<Client> getAppClients(Connection c, AppId appId) {
		final String query = "SELECT u.id, u.name,u.emailAddress,u.lastlogin,u.creation_time,"
				+ "GROUP_CONCAT(ua2.app_id) apps\r\n"
				+ "FROM users u\r\n" 
				+ "INNER JOIN user_apps ua ON u.ID = ua.user_id\r\n"
				+"LEFT JOIN user_apps ua2 ON u.ID = ua2.user_id\n"
				+ "WHERE ua.app_id = ? AND u.active = 1 AND u.email_verified = 1 GROUP BY u.ID";
		List<Client> clients = new LinkedList<>();
		try (PreparedStatement stmt = c.prepareStatement(query)) {
			stmt.setInt(1, appId.getValue());
			System.out.println(stmt.toString());
			ResultSet res = stmt.executeQuery();
			while (res.next()) {
				Client client = new Client();
				client.setId(res.getInt("id"));
				client.setName(res.getString("name"));
				client.setEmailAddress(res.getString("emailAddress"));
				Date lastLogin = res.getDate("lastlogin");
				if (lastLogin != null)
					client.setLastLogin(lastLogin.toLocalDate());
				client.setRegisterTime(res.getDate("creation_time").toLocalDate());
				List<AppId> apps = Stream.of(res.getString("apps").split(","))
						.map(Integer::parseInt)
						.map(AppId::getApp)
						.collect(Collectors.toList());
				client.setClientApps(apps);
				clients.add(client);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return clients;
	}
}
