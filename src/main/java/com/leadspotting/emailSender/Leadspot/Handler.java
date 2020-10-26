package com.leadspotting.emailSender.Leadspot;

import java.sql.Connection;

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
		default:
			throw new IllegalArgumentException("Provided handler id is not associated with a handler");
		}
	}
}
