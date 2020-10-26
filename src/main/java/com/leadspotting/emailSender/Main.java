package com.leadspotting.emailSender;

import com.leadspotting.emailSender.Leadspot.EmailProcess;
import com.leadspotting.emailSender.Leadspot.ProcessTimeNotArrived;

public class Main {
	public static void main(String[] args) {
		int handlerId = -1;
		if (args != null && args.length > 0) {
			handlerId = Integer.parseInt(args[0]);
		}
		SniperDB.connectToDB();

		try (EmailProcess ep = new EmailProcess(handlerId)) {
			ep.run();
		} catch (ProcessTimeNotArrived e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
}
