package com.stiggpwnz.vibes.fragments;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;
import com.stiggpwnz.vibes.adapters.UnitsAdapter;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VKontakteException;

public class UnitsListFragment extends SherlockListFragment {

	private static final String FRIENDS = "friends";
	private static final String UNITS = "units";
	private static final String SCROLL_POSITION = "scroll position";

	private int scrollPosition;

	private VibesApplication app;

	public UnitsListFragment() {

	}

	public static UnitsListFragment newInstance(boolean friends, List<Unit> list) {
		UnitsListFragment fragment = new UnitsListFragment();
		Bundle args = new Bundle();
		args.putBoolean(FRIENDS, friends);
		args.putSerializable(UNITS, (Serializable) list);
		fragment.setArguments(args);
		return fragment;
	}

	// download units, create and set an adapter
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (VibesApplication) getSherlockActivity().getApplication();
		boolean friends = getArguments().getBoolean(FRIENDS);
		List<Unit> units = app.getUnits(friends);
		if (units == null)
			new GetUnits().execute(friends);
		else {
			UnitsAdapter unitsAdapter = new UnitsAdapter(getSherlockActivity(), units, app.getTypeface());
			setListAdapter(unitsAdapter);
		}
	}

	// set empty view, restore saved scroll position
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getString(R.string.empty));
		getListView().setSelection(savedInstanceState != null ? savedInstanceState.getInt(SCROLL_POSITION) : scrollPosition);
	}

	// save current scroll position
	@Override
	public void onDestroyView() {
		scrollPosition = getListView().getFirstVisiblePosition();
		super.onDestroyView();
	}

	// remove listener
	@Override
	public void onDetach() {
		listener = null;
		super.onDetach();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(VibesApplication.VIBES, "onSaveInstanceState units");
		super.onSaveInstanceState(outState);
		outState.putInt(SCROLL_POSITION, scrollPosition);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		listener.showUnit((Unit) getListAdapter().getItem(position));
		super.onListItemClick(l, v, position, id);
	}

	public void showUnit(Unit unit) {
		FragmentTransaction transaction = getSherlockActivity().getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
		transaction.replace(R.id.framePlaylists, UnitFragment.newInstance(unit, app.getSelectedPlaylist()), UNIT_FRAGMENT);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	private class GetUnits extends AsyncTask<Boolean, Void, List<Unit>> {

		boolean friends;

		private List<Unit> getUnits(boolean friends) {
			if (listener != null) {
				try {
					return listener.loadUnits(friends);
				} catch (IOException e) {
					listener.internetFail();
				} catch (VKontakteException e) {
					switch (e.getCode()) {
					case VKontakteException.UNKNOWN_ERROR_OCCURED:
						listener.unknownError();
						break;

					case VKontakteException.USER_AUTHORIZATION_FAILED:
						listener.authFail();
						break;

					case VKontakteException.TOO_MANY_REQUESTS_PER_SECOND:
						return getUnits(friends);
					}
				}
			}
			return null;
		}

		@Override
		protected List<Unit> doInBackground(Boolean... params) {
			Thread.currentThread().setName("Getting units");
			friends = params[0];
			return getUnits(friends);
		}

		@Override
		protected void onPostExecute(List<Unit> result) {
			if (listener != null) {
				UnitsAdapter unitsAdapter = new UnitsAdapter(getSherlockActivity(), result, listener.getTypeface(), listener.getImageLoader());
				setListAdapter(unitsAdapter);
			}
		}

	}

}
