package com.stiggpwnz.vibes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.webkit.CookieManager;
import butterknife.InjectView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.manuelpeinado.refreshactionitem.RefreshActionItem;
import com.manuelpeinado.refreshactionitem.RefreshActionItem.RefreshActionListener;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;
import com.squareup.otto.Subscribe;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.activities.base.HomeAsUpActivity;
import com.stiggpwnz.vibes.events.RefreshButtonComplete;
import com.stiggpwnz.vibes.events.RefreshButtonStart;
import com.stiggpwnz.vibes.events.RefreshButtonVisibility;
import com.stiggpwnz.vibes.fragments.NavigationFragment;
import com.stiggpwnz.vibes.fragments.NewsFeedFragment;
import com.stiggpwnz.vibes.util.BusProvider;
import com.stiggpwnz.vibes.util.Log;
import com.stiggpwnz.vibes.vk.VKApi;

public class MainActivity extends HomeAsUpActivity {

	@InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;

	private SherlockActionBarDrawerToggle drawerToggle;
	private RefreshActionItem refreshActionItem;
	private MenuItem refreshMenuItem;
	private RefreshButtonVisibility visibility;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (CookieManager.getInstance().getCookie(VKApi.SERVER) == null) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}

		setContentView(R.layout.main_root);

		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerToggle = new SherlockActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_dark, R.string.opened, R.string.closed);
		drawerLayout.setDrawerListener(drawerToggle);
		drawerToggle.syncState();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.left_drawer, new NavigationFragment()).add(R.id.content_frame, new NewsFeedFragment())
					.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private final RefreshActionListener refreshActionListener = new RefreshActionListener() {

		@Override
		public void onRefreshButtonClick(RefreshActionItem sender) {
			refreshActionItem.showProgress(true);
			BusProvider.post(RefreshButtonStart.INSTANCE);
		}
	};

	@Subscribe
	public void onRefreshComplete(RefreshButtonComplete event) {
		if (refreshActionItem != null) {
			refreshActionItem.showProgress(false);
		}
	}

	@Subscribe
	public void onRefreshItemVisibilityChange(RefreshButtonVisibility visibility) {
		Log.d("Visibility change " + visibility);
		if (refreshMenuItem != null) {
			refreshMenuItem.setVisible(visibility == RefreshButtonVisibility.VISIBLE);
		} else {
			this.visibility = visibility;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main_activity, menu);
		refreshMenuItem = menu.findItem(R.id.refresh_button);
		if (visibility != null) {
			refreshMenuItem.setVisible(visibility == RefreshButtonVisibility.VISIBLE);
		}
		refreshActionItem = (RefreshActionItem) refreshMenuItem.getActionView();
		refreshActionItem.setMenuItem(refreshMenuItem);
		refreshActionItem.setRefreshActionListener(refreshActionListener);
		return true;
	}
}
