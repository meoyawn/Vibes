package com.stiggpwnz.vibes.vk;

public class AuthException extends Exception {

	private static final long serialVersionUID = -7142126671122993835L;

	public static enum Reason {
		MISSING_CREDENTIALS, UNKNOWN_FATAL, CAPTCHA_NEEDED
	}

	private final Reason reason;

	public AuthException(Reason reason) {
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}
}
