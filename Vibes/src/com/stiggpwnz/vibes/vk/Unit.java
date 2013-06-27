package com.stiggpwnz.vibes.vk;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;

public interface Unit {

	public static final Attribute<Unit, Integer> ID = new SimpleAttribute<Unit, Integer>("id") {

		@Override
		public Integer getValue(Unit arg0) {
			return arg0.getId();
		}
	};

	public int getId();

	public String getName();

	public String getProfilePic();
}
