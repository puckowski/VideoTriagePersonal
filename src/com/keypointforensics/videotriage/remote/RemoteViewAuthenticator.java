package com.keypointforensics.videotriage.remote;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class RemoteViewAuthenticator extends Authenticator {
	
	/*
	 * Author: Daniel Puckowski
	 */
	
	private final PasswordAuthentication PASSWORD_AUTHENTICATION;

	public RemoteViewAuthenticator(final String username, final char[] password) {
		if (username == null || password == null) {			
			PASSWORD_AUTHENTICATION = null;
		} else {
			PASSWORD_AUTHENTICATION = new PasswordAuthentication(username, password);
		}
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return PASSWORD_AUTHENTICATION;
	}
}