package com.stiggpwnz.vibes.events;

public class Events {

	public static enum Error {
		INTERNET,
		VK_AUTHORIZATION,
		VK_ACCESS_DENIED,
		NO_SDCARD,
		NO_SPACE_LEFT_ON_DEVICE
	}

	public static class Timer {

		private final int minutes;

		public Timer(int minutes) {
			this.minutes = minutes;
		}

		public int getMinutes() {
			return minutes;
		}
	}

}
