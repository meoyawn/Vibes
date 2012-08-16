package com.stiggpwnz.vibes;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Application;

public class VibesApplication extends Application {

	public static final String VIBES = "meridian";

	private Settings settings;
	private boolean serviceRunning = false;

	public Settings getSettings() {
		if (settings == null)
			settings = new Settings(this);
		return settings;
	}

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	public static AbstractHttpClient threadSafeHttpClient() {
		AbstractHttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		int connectionTimeout = 3000;
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		int socketTimeout = 5000;
		HttpConnectionParams.setSoTimeout(params, socketTimeout);

		SchemeRegistry registry = client.getConnectionManager().getSchemeRegistry();

		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);
		client = new DefaultHttpClient(manager, params);
		return client;
	}

}
