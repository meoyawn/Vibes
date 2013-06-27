package com.stiggpwnz.vibes.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.android.debug.hv.ViewServer;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.fragments.NewsFeedFragment;
import com.stiggpwnz.vibes.util.Persistance;

public class MainActivity extends SherlockFragmentActivity {

	private SherlockActionBarDrawerToggle drawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Persistance.getPassword() == null) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.main_fragment);

		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		drawerToggle = new SherlockActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_dark, R.string.opened, R.string.closed);
		drawerLayout.setDrawerListener(drawerToggle);
		drawerToggle.syncState();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.left_drawer, new NavigationFragment()).add(R.id.content_frame, new NewsFeedFragment())
					.commit();
		}

		// TODO remove
		ViewServer.get(this).addWindow(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ViewServer.get(this).setFocusedWindow(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ViewServer.get(this).removeWindow(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
}
