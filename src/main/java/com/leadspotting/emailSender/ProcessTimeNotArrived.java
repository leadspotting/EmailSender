package com.leadspotting.emailSender;

public class ProcessTimeNotArrived extends Exception {
	
	private static final long serialVersionUID = 2012712530231071138L;
	public ProcessTimeNotArrived(String msg) {
		super(msg);
	}
	
}
