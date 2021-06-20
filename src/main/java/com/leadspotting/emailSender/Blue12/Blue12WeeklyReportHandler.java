package com.leadspotting.emailSender.Blue12;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.leadspotting.commons.models.AppId;
import com.leadspotting.commons.models.SendEmailRequest;
import com.leadspotting.commons.models.Template;
import com.leadspotting.commons.services.EmailServerClient;
import com.leadspotting.commons.utils.SpatialUtils;
import com.leadspotting.emailSender.CommonQueries;
import com.leadspotting.emailSender.Handler;
import com.leadspotting.emailSender.SniperDB;
import com.leadspotting.emailSender.models.Blue12ReportAlert;
import com.leadspotting.emailSender.models.Client;
import com.leadspotting.emailSender.models.UserGeo;

public class Blue12WeeklyReportHandler implements Handler {

	private static int ADMIN_ID = 2166;
	private static String articleTemplate;
	static {
		articleTemplate = EmailServerClient.getTemplate(Template.WEEKLY_REPORT_ARTICLE_BLUE_12, AppId.Blue12);
		System.out.println(articleTemplate);
	}

	public static void main(String[] args) {
		SniperDB.connectToDB();
		Handler h = new Blue12WeeklyReportHandler();
		h.handle(SniperDB.getConnectionFromPool());
	}

	public UserGeo getUserGeo(Connection c, int clientId) {
		final String q = "SELECT lat,lng,radius FROM blue_12_client_geo WHERE client_id = ?";
		try (var stmt = c.prepareStatement(q)) {
			stmt.setInt(1, clientId);
			var res = stmt.executeQuery();
			if (res.next()) {
				var geo = new UserGeo();
				geo.setClientId(clientId);
				geo.setLat(res.getDouble("lat"));
				geo.setLng(res.getDouble("lng"));
				geo.setRadius(res.getDouble("radius"));
				return geo;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isSubscribed(Connection c, Client client) {
		final String query = "SELECT unsubscribed FROM unsubscribed_from_email_clients WHERE client_id = ? ";
		try (var stmt = c.prepareCall(query)) {
			stmt.setInt(1, client.getId());
			var res = stmt.executeQuery();
			;
			if (res.next()) {
				int sub = res.getInt("unsubscribed");
				return sub != 0;
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<Blue12ReportAlert> getPosts(Connection c) {
		final String query = "SELECT q.postId,q.url,q.headline,q.creation_time,q.longitude,q.latitude\n"
				+ "FROM post_qualification q\n"
				+ "WHERE q.userId = ? AND q.creation_time > DATE_SUB(NOW(),INTERVAL 1 WEEK)";
		List<Blue12ReportAlert> posts = new LinkedList<>();
		try (var stmt = c.prepareStatement(query)) {
			stmt.setInt(1, ADMIN_ID);
			var res = stmt.executeQuery();
			while (res.next()) {
				try {
					long postId = res.getLong("postId");
					String url = res.getString("url");
					String headline = res.getString("headline");
					String creationTime = res.getString("creation_time");
					double longitude = res.getDouble("longitude");
					double latitude = res.getDouble("latitude");
					var post = new Blue12ReportAlert();
					post.setId(postId);
					post.setUrl(url);
					post.setTitle(headline);
					post.setDate(creationTime);
					post.setSource(getDomainFromURL(url));
					post.setLat(latitude);
					post.setLng(longitude);
					posts.add(post);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return posts;
	}

	static Pattern domainNamePatttern = Pattern
			.compile("(?:http(?:s)?://)?(?:www\\.|ww2\\.)?([a-zA-Z0-9-]*?\\.[a-zA-Z0-9-\\.]+)");

	public static String getDomainFromURL(String url) throws MalformedURLException {
		if (url == null)
			throw new MalformedURLException();
		var m = domainNamePatttern.matcher(url);
		if (m.find()) {
			return m.group(1);
		} else {
			throw new MalformedURLException();
		}
	}

	@Override
	public void handle(Connection c) {
		List<Client> allClients = CommonQueries.getAppClients(c, AppId.Blue12);
		System.out.println(allClients);

		var posts = getPosts(c);
//		posts.forEach(System.out::println);
		for (var client : allClients) {
			var geo = getUserGeo(c, client.getId());
			if (!isSubscribed(c, client))
				continue;
			if (geo == null)
				continue;
			var filtered = filterPostsOnGeoLocation(posts, geo);
			if (!filtered.isEmpty())
				sendReportToClient(c, client, filtered);
		}
	}

	public void sendReportToClient(Connection c, Client client, List<Blue12ReportAlert> alerts) {
		var content = new StringBuilder();
		Function<String, String> sorrundWithCurlyB = s -> "\\{\\{".concat(s).concat("\\}\\}");
		for (var alert : alerts) {
			var alertContent = new String(articleTemplate.getBytes())
					.replaceAll(sorrundWithCurlyB.apply("url"), alert.getUrl())
					.replaceAll(sorrundWithCurlyB.apply("title"), alert.getTitle())
					.replaceAll(sorrundWithCurlyB.apply("source"), alert.getSource())
					.replaceAll(sorrundWithCurlyB.apply("date"), alert.getDate());
			content.append(alertContent);
		}
//		var alertTempate = Template.WEEKLY_REPORT_ARTICLE_BLUE_12;
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.Blue12)
				.setTemplate(Template.WEEKLY_REPORT_BLUE_12).setHeader("Blue 12 Weekly Report")
				.setRecevier(client.getEmailAddress()).addValue("userName", client.getName()).setSender("Blue 12")
				.addValue("content", content.toString()).addValue("appURL", AppId.getAppDefaultHost(AppId.LeadSpot))
				.build();
		EmailServerClient.sendRequest(request);
	}

	public List<Blue12ReportAlert> filterPostsOnGeoLocation(List<Blue12ReportAlert> alerts, UserGeo geo) {
		List<Blue12ReportAlert> filteredAlerts = new LinkedList<>();
		var requiredRadiusInKM = geo.getRadius();
		var requiredLat = geo.getLat();
		var requiredLng = geo.getLng();
		for (var alert : alerts) {
			var disInM = SpatialUtils.distance(requiredLat, alert.getLat(), requiredLng, alert.getLng(), 0, 0);
			if (disInM < requiredRadiusInKM * 1000)
				filteredAlerts.add(alert);
		}
		return filteredAlerts.stream().sorted((a1, a2) -> Long.compare(a2.getId(), a1.getId())).limit(10)
				.collect(Collectors.toList());
	}

}
