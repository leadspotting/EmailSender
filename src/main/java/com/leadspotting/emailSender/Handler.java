package com.leadspotting.emailSender;

import java.sql.Connection;

import com.leadspotting.emailSender.Blue12.Blue12WeeklyReportHandler;
import com.leadspotting.emailSender.LeadsOnDemand.NoProjectHandler;
import com.leadspotting.emailSender.Leadspot.CRMHandler;
import com.leadspotting.emailSender.Leadspot.ClientsPlansHandler;
import com.leadspotting.emailSender.Leadspot.InactiveClientsHandler;
import com.leadspotting.emailSender.Leadspot.QuotaRenewdHandler;
import com.leadspotting.emailSender.Leadspot.UnsubscribedClientsHandler;

public interface Handler {
	/**
	 * 
	 * @param c DB connection
	 */
	public void handle(Connection c);

	public static Handler getHandlerById(int id) throws IllegalArgumentException {

		switch (id) {
		case 1:
			return new InactiveClientsHandler();
		case 2:
			return new ClientsPlansHandler();
		case 3: 
			return new UnsubscribedClientsHandler();
		case 4: 
			return new QuotaRenewdHandler();
		case 5:
			return new NoProjectHandler();
		case 6: 
			return new CRMHandler();
		case 7:
			return new Blue12WeeklyReportHandler();
		default:
			throw new IllegalArgumentException("Provided handler id is not associated with a handler");
		}
	}
}
