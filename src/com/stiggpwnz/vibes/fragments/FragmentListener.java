package com.stiggpwnz.vibes.fragments;

import android.graphics.Typeface;
import android.support.v4.app.Fragment;

import com.stiggpwnz.vibes.GeneralListener;
import com.stiggpwnz.vibes.restapi.Playlist;

public interface FragmentListener extends GeneralListener {

	public Typeface getTypeface();

	public void unknownError();

	public void internetFail();

	public void authFail();

	public void accessDenied();

	public void onViewCreated(Fragment fragment);

	public Playlist getSelectedPlaylist();

}
