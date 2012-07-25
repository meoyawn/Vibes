package com.stiggpwnz.vibes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class Api {

	protected static final String TRACK_GET_INFO = "track.getInfo";
	protected static final String AUDIO_GET_BY_ID = "audio.getById?";

	private static final String UTF_8 = "UTF-8";
	private static final String MD5 = "MD5";

	public List<HttpPost> imageRequestQueue;
	public List<HttpPost> audioUrlRequests;

	protected HttpClient client;

	protected Api(HttpClient client) {
		this.client = client;
	}

	protected String executeURL(URI uri) throws ClientProtocolException, IOException {
		String url = uri.toString().replace("%2526", "%26");

		Log.d(VibesApplication.VIBES, url);

		HttpPost request = new HttpPost(url);
		if (url.contains(TRACK_GET_INFO) && imageRequestQueue != null) {
			synchronized (this) {
				imageRequestQueue.add(request);
			}
		} else if (url.contains(AUDIO_GET_BY_ID) && audioUrlRequests != null) {
			synchronized (this) {
				audioUrlRequests.add(request);
			}
		}

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
		} catch (NoSuchAlgorithmException e) {

		} catch (UnsupportedEncodingException e) {

		}
		return null;
	}
}
