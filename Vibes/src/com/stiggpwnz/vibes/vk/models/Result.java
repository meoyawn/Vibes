package com.stiggpwnz.vibes.vk.models;

public class Result {

	public Error error;

	public boolean isResponse() {
		return error == null;
	}
}