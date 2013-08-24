package com.stiggpwnz.vibes.activities.base;

import android.os.Bundle;
import android.os.Looper;
import butterknife.Views;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.github.frankiesardo.icepick.bundle.Bundles;
import com.stiggpwnz.vibes.util.BusProvider;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public abstract class BaseActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundles.restoreInstanceState(this, savedInstanceState);
	}

	@Override
	public void setContentView(int layoutResId) {
		super.setContentView(layoutResId);
		Views.inject(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundles.saveInstanceState(this, outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		BusProvider.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BusProvider.unregister(this);
	}

	public void runOnBackgroundThread(Runnable runnable) {
		if (runnable == null) {
			return;
		}

		if (Looper.myLooper() != null) {
			new Thread(runnable).start();
		} else {
			runnable.run();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Crouton.cancelAllCroutons();
	}
}
