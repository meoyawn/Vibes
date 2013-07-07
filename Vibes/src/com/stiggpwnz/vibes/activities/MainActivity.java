package com.stiggpwnz.vibes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.webkit.CookieManager;
import butterknife.InjectView;

import com.actionbarsherlock.view.MenuItem;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.base.HomeAsUpActivity;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.fragments.NewsFeedFragment;

public class MainActivity extends HomeAsUpActivity {

	@InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;

	private SherlockActionBarDrawerToggle drawerToggle;

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.main_fragment);
	}

	@Override
	public void onViewCreated(Bundle savedInstanceState) {
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerToggle = new SherlockActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_dark, R.string.opened, R.string.closed);
		drawerLayout.setDrawerListener(drawerToggle);
		drawerToggle.syncState();
	}

	@Override
	public void onFirstCreated(View view) {
		if (CookieManager.getInstance().getCookie("vk.com") == null) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}

		getSupportFragmentManager().beginTransaction().add(R.id.left_drawer, new NavigationFragment()).add(R.id.content_frame, new NewsFeedFragment()).commit();
	}

	@Override
	public void onRecreated(Bundle savedInstanceState) {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
