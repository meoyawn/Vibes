package com.stiggpwnz.vibes.restapi;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import com.stiggpwnz.vibes.VibesApplication;

import android.util.Log;

public class RestApi {

	private static final String UTF_8 = "UTF-8";
	private static final String MD5 = "MD5";

	protected HttpClient client;

	protected RestApi(HttpClient client) {
		this.client = client;
	}

	protected String executeRequest(HttpUriRequest request) throws ClientProtocolException, IOException {
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);

		Log.d(VibesApplication.VIBES, responseText);

		return responseText;
	}

	protected static String md5(String string) {
		try {
			MessageDigest md = MessageDigest.getInstance(MD5);
			md.update(string.getBytes(UTF_8));
			byte[] digest = md.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			String hashtext = bigInt.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (Exception e) {
			return null;
		}
	}
}
