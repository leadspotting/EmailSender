package com.leadspotting.emailSender.Leadspot;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import com.leadspotting.commons.models.AppId;
import com.leadspotting.commons.models.SendEmailRequest;
import com.leadspotting.commons.models.Template;
import com.leadspotting.commons.services.EmailServerClient;
import com.leadspotting.emailSender.CommonQueries;
import com.leadspotting.emailSender.Handler;
import com.leadspotting.emailSender.SniperDB;
import com.leadspotting.emailSender.models.Client;

public class ResendVerficationEmailHandler implements Handler {
	public static void main(String[] args) {
		SniperDB.connectToDB();
		new ResendVerficationEmailHandler().handle(SniperDB.getConnectionFromPool());
	}
	@Override
	public void handle(Connection c) {
		List<Client> allClients = CommonQueries.getAppUnverfiedClients(c, AppId.LeadSpot);
		var before30Mins = LocalDateTime.now().minusMinutes(30);
		var before40Mins = LocalDateTime.now().minusMinutes(40);
		for (var client : allClients) {
			var registerTime = client.getRegisterTime();
			if (registerTime.isBefore(before30Mins) && registerTime.isAfter(before40Mins)) {
				sendEmail(client, Template.RESEND_VERFICATION_EMAIL,"Do not forget to join the LeadSpot and start discovering emails!");
			}
		}
	}

	public void sendEmail(Client client, Template template, String emailTitle) {
		var host = AppId.getAppDefaultHost(AppId.LeadSpot);
		SendEmailRequest request = new SendEmailRequest.Builder().setAppId(AppId.LeadSpot).setTemplate(template)
				.setHeader(emailTitle).setRecevier(client.getEmailAddress()).addValue("userName", client.getName())
				.addValue("appURL", host).addValue(host + "/resendVerfication", emailTitle).build();
		EmailServerClient.sendRequest(request);
	}

}
