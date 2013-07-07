package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;

import com.stiggpwnz.vibes.fragments.base.RetainedInterface;

public interface RetainedActivityInterface extends RetainedInterface {

	public abstract void onCreateView(Bundle savedInstanceState);

	public abstract void onViewCreated(Bundle savedInstanceState);

	public void onRecreated(Bundle savedInstanceState);
}
