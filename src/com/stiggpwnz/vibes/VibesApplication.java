package com.stiggpwnz.vibes;

import android.app.Application;

public class VibesApplication extends Application {

	public static final String VIBES = "meridian";

	private Settings settings;
	private boolean serviceRunning;

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

}
