package com.stiggpwnz.vibes.vk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Error extends Exception {

	private static final long serialVersionUID = 3449549226069859662L;

	public int error_code;
	public String error_msg;
}
