package com.stiggpwnz.vibes.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.stiggpwnz.vibes.fragments.ControlsFragment;
import com.stiggpwnz.vibes.fragments.PlaylistFragment;

public class FragmentPagesAdapter extends FragmentPagerAdapter {

	public FragmentPagesAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int page) {
		switch (page) {
		case 0:
			ControlsFragment controls = new ControlsFragment();
			return controls;

		default:
			PlaylistFragment playlist = new PlaylistFragment();
			return playlist;
		}
	}

	@Override
	public int getCount() {
		return 2;
	}

}
