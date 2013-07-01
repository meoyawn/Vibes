package com.stiggpwnz.vibes;

import android.media.MediaPlayer;

public class Player {

	private final MediaPlayer mp = new MediaPlayer();
	private Playlist playlist;
	private State state;

	public Player() {

	}

	public Playlist getPlaylist() {
		return playlist;
	}

	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
	}

	public void play() {

	}

	public void pause() {

	}

	public void next() {
		increment(1);
	}

	public void prev() {
		increment(-1);
	}

	private void increment(int i) {
		playlist.increment(i);
		switch (state) {
		case NOT_PREPARED:

			break;
		case PAUSED:
			break;
		case PLAYING:
			break;
		case PREPARING_TO_PAUSE:
			break;
		case PREPARING_TO_PLAY:
			break;
		case RESETING_TO_PAUSE:
			break;
		case RESETING_TO_PLAY:
			break;
		case SEEKING_TO_PLAY:
			break;
		case SEEKING_TO_PAUSE:
			break;
		}
	}

	private void resetAndPlay() {
		runInBackground(new Runnable() {

			@Override
			public void run() {
				state = State.RESETING_TO_PLAY;
				mp.reset();
				play();
			}
		});
	}

	private void runInBackground(Runnable runnable) {
		new Thread(runnable).start();
	}

	public static enum State {
		NOT_PREPARED, PLAYING, PAUSED, PREPARING_TO_PLAY, PREPARING_TO_PAUSE, RESETING_TO_PLAY, RESETING_TO_PAUSE, SEEKING_TO_PLAY, SEEKING_TO_PAUSE;
	}
}
