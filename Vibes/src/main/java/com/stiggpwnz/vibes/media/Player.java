package com.stiggpwnz.vibes.media;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Looper;

import com.stiggpwnz.vibes.util.BusProvider;

import java.io.IOException;

public class Player implements OnBufferingUpdateListener, OnCompletionListener, OnErrorListener, OnSeekCompleteListener, OnInfoListener {

    public static enum State {
        NOT_PREPARED, PREPARED, PREPARING, RESETING, SEEKING
    }

    public static enum Action {
        PLAY, PAUSE, CLEAR
    }

    private final MediaPlayer mp = new MediaPlayer();

    private State state = State.NOT_PREPARED;
    private Action action = Action.PAUSE;
    private String source;

    // for the future seeking
    private int futurePosition = -1;

    // for the current seeking
    private int currentPosition;

    public Player() {
        mp.setOnBufferingUpdateListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        mp.setOnInfoListener(this);
        mp.setOnSeekCompleteListener(this);
        BusProvider.register(this);
    }

    public void release() {
        BusProvider.unregister(this);
        mp.release();
    }

    private final Runnable prepare = new Runnable() {

        @Override
        public void run() {
            try {
                mp.setDataSource(source);
                mp.prepare();
                onPrepareComplete();
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (IllegalStateException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Runnable reset = new Runnable() {

        @Override
        public void run() {
            mp.reset();
            onResetComplete();
        }
    };

    private void prepareAsync() {
        switch (state) {
            case NOT_PREPARED:
                state = State.PREPARING;
                runInBackground(prepare);
                break;

            case RESETING:
            case PREPARING:
            case PREPARED:
                // do nothing
                break;

            case SEEKING:
                resetAsync();
                break;
        }
    }

    private void resetAsync() {
        state = State.RESETING;
        runInBackground(reset);
    }

    private void onPrepareComplete() {
        state = State.PREPARED;

        if (futurePosition >= 0) {
            seekTo(futurePosition);
            futurePosition = -1;
        } else {
            switch (action) {
                case PLAY:
                    mp.start();
                    break;

                case PAUSE:
                    break;

                case CLEAR:
                    resetAsync();
                    break;
            }
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        state = State.PREPARED;

        if (futurePosition >= 0) {
            seekTo(futurePosition);
            futurePosition = -1;
        }
    }

    private void onResetComplete() {
        state = State.NOT_PREPARED;

        switch (action) {
            case PLAY:
            case PAUSE:
                prepareAsync();
                break;

            case CLEAR:
                break;
        }
    }

    public void play() {
        action = Action.PLAY;
        switch (state) {
            case NOT_PREPARED:
                prepareAsync();
                break;

            case PREPARED:
                mp.start();
                break;

            case PREPARING:
            case RESETING:
            case SEEKING:
                // just wait
                break;
        }
    }

    public void pause() {
        action = Action.PAUSE;
        switch (state) {
            case NOT_PREPARED:
                prepareAsync();
                break;

            case PREPARED:
            case PREPARING:
            case RESETING:
            case SEEKING:
                // do nothing or just wait
                break;
        }
    }

    public void seekTo(int msec) {
        switch (state) {
            case NOT_PREPARED:
                prepareAsync();
                futurePosition = msec;
                break;

            case PREPARED:
                state = State.SEEKING;
                currentPosition = msec;
                mp.seekTo(msec);
                break;

            case PREPARING:
            case RESETING:
            case SEEKING:
                futurePosition = msec;
                break;
        }
    }

    public void prepare(String source) {
        if (!source.equals(this.source)) {
            this.source = source;

            switch (state) {
                case NOT_PREPARED:
                    prepareAsync();
                    break;

                case SEEKING:
                case PREPARED:
                case PREPARING:
                    resetAsync();
                    break;

                case RESETING:
                    // do nothing
                    break;
            }
        } else {
            switch (state) {
                case NOT_PREPARED:
                    prepareAsync();
                    break;

                case RESETING:
                case PREPARING:
                    // do nothing
                    break;

                case PREPARED:
                    mp.seekTo(0);
                    break;

                case SEEKING:
                    futurePosition = 0;
                    break;
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        action = Action.PAUSE;
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

    public State getState() {
        return state;
    }

    public String getSource() {
        return source;
    }

    public Action getAction() {
        return action;
    }

    public int getCurrentPosition() {
        switch (state) {
            case PREPARED:
                return mp.getCurrentPosition();

            case SEEKING:
                return currentPosition;

            case PREPARING:
            case RESETING:
            case NOT_PREPARED:
                return 0;
        }
        return 0;
    }
}
