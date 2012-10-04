package com.stiggpwnz.vibes.fragments;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.methods.HttpPost;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.stiggpwnz.vibes.PlayerActivity;
import com.stiggpwnz.vibes.R;
import com.stiggpwnz.vibes.VibesApplication;
import com.stiggpwnz.vibes.imageloader.ImageLoader;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.VKontakteException;

public class ControlsFragment extends SherlockFragment implements OnClickListener, OnSeekBarChangeListener {

	public static interface Listener extends FragmentListener {

		public String getAlbumImageUrl();

		public void onPlayButtonPressed(View v);

		public void onNextButtonPressed(View v);

		public void onPrevButtonPressed(View v);

		public void onShuffleButtonPressed(View v);

		public void onRepeatButtonPressed(View v);

		public int add(Song song) throws IOException, VKontakteException;

		public boolean delete(Song song) throws IOException, VKontakteException;

		public void love(Song song);

		public void unlove(Song song);

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

		public boolean getShuffle();
	}

	private Listener listener;
	private LoadAndSetAlbumImage loadAndSetAlbumImage;

	private TextView title;
	private TextView artist;

	private ImageView albumImage;

	private Button love;
	private Button playButton;

	private SeekBar seekBar;
	private TextView textPassed;
	private TextView textLeft;

	private ProgressBar buffering;
	private TextView textBuffering;

	private boolean large;
	private Song song;

	public ControlsFragment() {
		Log.d(VibesApplication.VIBES, "creating new controls");
	}

	@Override
	public void onAttach(Activity activity) {
		listener = (Listener) activity;
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.controls, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(VibesApplication.VIBES, "onViewCreated controls");

		title = (TextView) view.findViewById(R.id.title);
		title.setTypeface(listener.getTypeface());

		artist = (TextView) view.findViewById(R.id.artist);
		artist.setTypeface(listener.getTypeface());

		albumImage = (ImageView) view.findViewById(R.id.imageAlbum);

		seekBar = (SeekBar) view.findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);

		love = (Button) view.findViewById(R.id.btnLove);
		love.setOnClickListener(this);

		playButton = (Button) view.findViewById(R.id.btnPlay);
		if (playButton != null) {
			playButton.setOnClickListener(this);

			View next = view.findViewById(R.id.btnNext);
			next.setOnClickListener(this);

			View prev = view.findViewById(R.id.btnPrev);
			prev.setOnClickListener(this);

			large = true;
		} else {
			textPassed = (TextView) view.findViewById(R.id.textPassed);
			textPassed.setTypeface(listener.getTypeface());

			textLeft = (TextView) view.findViewById(R.id.textLeft);
			textLeft.setTypeface(listener.getTypeface());

			buffering = (ProgressBar) view.findViewById(R.id.progressCircle);

			textBuffering = (TextView) view.findViewById(R.id.textBuffering);
			textBuffering.setTypeface(listener.getTypeface());
		}

		View shuffle = view.findViewById(R.id.btnShuffle);
		if (listener.getShuffle())
			shuffle.setBackgroundResource(R.drawable.shuffle_blue);
		shuffle.setOnClickListener(this);

		View repeat = view.findViewById(R.id.btnRepeat);
		repeat.setOnClickListener(this);

		super.onViewCreated(view, savedInstanceState);
		listener.onViewCreated(this);
	}

	@Override
	public void onDetach() {
		listener = null;
		super.onDetach();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPlay:
			listener.onPlayButtonPressed(v);
			break;

		case R.id.btnNext:
			listener.onNextButtonPressed(v);
			break;

		case R.id.btnPrev:
			listener.onPrevButtonPressed(v);
			break;

		case R.id.btnLove:
			if (song != null) {
				if (song.loved)
					unlove(song);
				else
					love(song);
			}
			break;

		case R.id.btnShuffle:
			listener.onShuffleButtonPressed(v);
			break;

		case R.id.btnRepeat:
			listener.onRepeatButtonPressed(v);
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (listener != null)
			listener.onProgressChanged(seekBar, progress, fromUser);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	private class LoadAndSetAlbumImage extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			Thread.currentThread().setName("Getting and setting an album image");
			synchronized (this) {
				if (isCancelled())
					return null;
				if (listener == null)
					return null;
				return listener.getAlbumImageUrl();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (listener != null) {
				if (result != null) {
					ImageLoader imageLoader = listener.getImageLoader();
					imageLoader.setStubId(R.drawable.music);
					imageLoader.displayImage(result, albumImage);
				} else {
					albumImage.setImageResource(R.drawable.music);
				}
			}
		}

	}

	public void nullEverything() {
		seekBar.setProgress(0);
		seekBar.setSecondaryProgress(0);
		if (!large) {
			textPassed.setText("0:00");
			textLeft.setText("0:00");
		}
	}

	public void setCurrentSong(Song song, List<HttpPost> requests) {
		if (song != null) {
			ControlsFragment.this.song = song;

			String performer = song.performer;
			artist.setText(performer);

			String name = song.title;
			title.setText(name);

			if (song.loved)
				love.setBackgroundResource(R.drawable.love_blue);
			else
				love.setBackgroundResource(R.drawable.love_grey);

			synchronized (requests) {
				if (loadAndSetAlbumImage != null && loadAndSetAlbumImage.getStatus() == AsyncTask.Status.RUNNING) {
					Log.e(VibesApplication.VIBES, "cancelling image loader: " + requests.size() + " items in queue");
					for (HttpPost request : requests)
						request.abort();
					loadAndSetAlbumImage.cancel(true);
				}
				requests.clear();
			}
			loadAndSetAlbumImage = new LoadAndSetAlbumImage();
			loadAndSetAlbumImage.execute();
		}
	}

	public void setPlayButtonDrawable(int resource) {
		if (large) {
			PlayerActivity.recycle(playButton);
			playButton.setBackgroundResource(resource);
		}
	}

	public void updateBuffering(int percent) {
		seekBar.setSecondaryProgress(seekBar.getMax() * percent / 100);
	}

	public void onBufferingStarted() {
		if (!large) {
			textBuffering.setVisibility(View.VISIBLE);
			buffering.setVisibility(View.VISIBLE);
		}
	}

	public void onBufferingEnded(int duration) {
		if (duration != 0)
			seekBar.setMax(duration);
		if (!large) {
			textBuffering.setVisibility(View.INVISIBLE);
			buffering.setVisibility(View.INVISIBLE);
		}
	}

	public void love(Song song) {
		if (song == this.song)
			love.setBackgroundResource(R.drawable.love_blue);
		new Love().execute(song);
	}

	public void unlove(Song song) {
		if (song == this.song)
			love.setBackgroundResource(R.drawable.love_grey);
		new UnLove().execute(song);
	}

	private class Love extends AsyncTask<Song, Void, Integer> {

		private Song song;

		private Integer addSong(Song song) {
			try {
				return listener.add(song);
			} catch (IOException e) {
				listener.internetFail();
			} catch (VKontakteException e) {
				switch (e.getCode()) {

				case VKontakteException.USER_AUTHORIZATION_FAILED:
					listener.authFail();
					break;

				case VKontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					return addSong(song);

				case VKontakteException.ACCESS_DENIED:
					listener.accessDenied();
					break;

				default:
					return null;
				}
			}
			return null;
		}

		@Override
		protected Integer doInBackground(Song... params) {
			Thread.currentThread().setName("Loving song");
			song = params[0];
			listener.love(song);
			return addSong(song);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result != null) {
				song.myAid = result;
				song.loved = true;
			} else if (song == ControlsFragment.this.song)
				love.setBackgroundResource(R.drawable.love_grey);
		}
	}

	private class UnLove extends AsyncTask<Song, Void, Boolean> {

		Song song;

		private Boolean deleteSong(Song song) {
			try {
				return listener.delete(song);
			} catch (IOException e) {
				listener.internetFail();
			} catch (VKontakteException e) {
				switch (e.getCode()) {

				case VKontakteException.USER_AUTHORIZATION_FAILED:
					listener.authFail();
					break;

				case VKontakteException.TOO_MANY_REQUESTS_PER_SECOND:
					return deleteSong(song);

				case VKontakteException.ACCESS_DENIED:
					listener.accessDenied();
					break;

				default:
					return false;
				}
			}
			return false;
		}

		@Override
		protected Boolean doInBackground(Song... params) {
			Thread.currentThread().setName("Unloving song");
			song = params[0];
			listener.unlove(song);
			return deleteSong(song);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				song.myAid = 0;
				song.loved = false;
			} else if (song == ControlsFragment.this.song)
				love.setBackgroundResource(R.drawable.love_blue);
		}
	}

	public void onProgressChanged(int progress, int songDuration) {
		seekBar.setProgress(progress);

		if (!large) {
			int seconds = (progress / 1000) % 60;
			int minutes = (progress / 1000) / 60;
			if (seconds > 9)
				textPassed.setText(String.format("%d:%d", minutes, seconds));
			else
				textPassed.setText(String.format("%d:0%d", minutes, seconds));

			seconds = ((songDuration - progress) / 1000) % 60;
			minutes = ((songDuration - progress) / 1000) / 60;
			if (seconds > 9)
				textLeft.setText(String.format("%d:%d", minutes, seconds));
			else
				textLeft.setText(String.format("%d:0%d", minutes, seconds));
		}
	}

}
