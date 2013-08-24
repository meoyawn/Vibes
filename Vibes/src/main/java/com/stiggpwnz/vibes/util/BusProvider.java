package com.stiggpwnz.vibes.util;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusProvider {

	private static class Holder {
		private static final Bus INSTANCE = new Bus(ThreadEnforcer.ANY);
	}

	private static Bus getInstance() {
		return Holder.INSTANCE;
	}

	public static void register(Object object) {
		getInstance().register(object);
	}

	public static void unregister(Object object) {
		getInstance().unregister(object);
	}

	public static void post(Object object) {
		getInstance().post(object);
	}
}
