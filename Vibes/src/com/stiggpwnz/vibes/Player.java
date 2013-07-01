package com.stiggpwnz.vibes;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Looper;

import com.stiggpwnz.vibes.util.BusProvider;

public class Player implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnPreparedListener, OnSeekCompleteListener {

	private final MediaPlayer mp = new MediaPlayer();
	private State state;

	private String source;

	public Player() {
		mp.setOnBufferingUpdateListener(this);
		mp.setOnCompletionListener(this);
		mp.setOnErrorListener(this);
		mp.setOnPreparedListener(this);
		mp.setOnSeekCompleteListener(this);
		BusProvider.register(this);
	}

	public void release() {
		BusProvider.unregister(this);
		mp.release();
	}

	private void prepareAsync() {
		switch (state) {
		case PAUSED:
			mp.start();
			state = State.PLAYING;
			break;

		case NOT_PREPARED:
			// TODO prepare
			break;

		case RESETING_TO_PREPARE:
			state = State.RESETING_TO_PLAY;
			break;

		case PREPARING_TO_PAUSE:
			state = State.PREPARING_TO_PLAY;
			break;

		case SEEKING_TO_PAUSE:
			state = State.SEEKING_TO_PLAY;
			break;

		case PLAYING:
		case SEEKING_TO_PLAY:
		case PREPARING_TO_PLAY:
		case RESETING_TO_PLAY:
			// do nothing, just carry on
			break;
		}
	}

	public void pause() {

	}

	public void prepare(String source) {
		if (!this.source.equals(source)) {
			this.source = source;

			switch (state) {
			case RESETING_TO_PREPARE:
				break;

			case NOT_PREPARED:
				state = State.PREPARING_TO_PAUSE;
				prepareAsync();
				break;

			case RESETING_TO_PLAY:
				state = State.RESETING_TO_PREPARE;
				break;

			case PREPARING_TO_PLAY:
			case SEEKING_TO_PLAY:
			case PLAYING:
				state = State.RESETING_TO_PLAY;
				resetAsync();
				break;

			case PREPARING_TO_PAUSE:
			case SEEKING_TO_PAUSE:
			case PAUSED:
				state = State.RESETING_TO_PREPARE;
				resetAsync();
				break;
			}
		} else {

		}
	}

	private void onResetComplete() {
		switch (state) {
		case RESETING_TO_PLAY:
			state = State.PREPARING_TO_PLAY;
			prepareAsync();
			break;

		case RESETING_TO_PREPARE:
			state = State.PREPARING_TO_PAUSE;
			prepareAsync();
			break;

		default:
			throw new IllegalStateException("Reseting completed in wrong state");
		}
	}

	private State getOldAndSetNewState(State newState) {
		State oldState = state;
		state = newState;
		return oldState;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	private final Runnable reset = new Runnable() {

		@Override
		public void run() {
			mp.reset();
			onResetComplete();
		}
	};

	private void resetAsync() {
		runInBackground(reset);
	}

	private static void runInBackground(Runnable runnable) {
		if (runnable == null) {
			return;
		}

		if (Looper.myLooper() == Looper.getMainLooper()) {
			new Thread(runnable).start();
		} else {
			runnable.run();
		}
	}

	public static enum State {
		NOT_PREPARED,
		PLAYING,
		PAUSED,
		PREPARING_TO_PLAY,
		PREPARING_TO_PAUSE,
		RESETING_TO_PLAY,
		RESETING_TO_PREPARE,
		SEEKING_TO_PLAY,
		SEEKING_TO_PAUSE;
	}

	public State getState() {
		return state;
	}

}
