package com.stiggpwnz.vibes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Vkontakte;

public class Downloader {

<<<<<<< HEAD
=======
	public static interface OnActionListener {
		public void onDownloadException(String messsage);
	}

>>>>>>> origin/massive-refactoring
	private NotificationManager manager;
	private Context context;
	private Vkontakte vkontakte;
	private List<Integer> downloadQueue;
	private OnActionListener listener;
	private String path;

	public Downloader(Context context, OnActionListener listener, NotificationManager manager, Vkontakte vkontakte, List<Integer> downloadQueue, String path) {
		this.context = context;
		this.listener = listener;
		this.manager = manager;
		this.vkontakte = vkontakte;
		this.downloadQueue = downloadQueue;
		this.path = path;
	}

	public void download(Song song) throws IOException {
		if (!downloadQueue.contains(Integer.valueOf(song.aid))) {
			downloadQueue.add(Integer.valueOf(song.aid));
			new DownloaderThread(song).execute();
		}
	}

	private class DownloaderThread extends AsyncTask<Void, Void, Void> {

		private String songName;
		private Notification notification;
		private Song song;
		private File outputFile;
		private String messsage;

		public DownloaderThread(Song song) throws IOException {
			this.song = song;

			File directory;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				directory = new File(path);
			else
				throw new IOException(context.getString(R.string.insertSdCard));

			directory.mkdirs();

			songName = String.format("%s - %s.mp3", song.performer, song.title);
			outputFile = new File(directory, songName);
			int tries = 0;
			while (outputFile.exists()) {
				tries++;
				songName = String.format("%s - %s (%d).mp3", song.performer, song.title, tries);
				outputFile = new File(directory, songName);
			}
			Log.d(VibesApplication.VIBES, outputFile.getAbsolutePath());
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showNotification();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.currentThread().setName("Downloading " + songName);
				if (song.url == null)
					vkontakte.setSongUrl(song);

				URL url = new URL(song.url);

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.connect();

				// this will be useful so that you can show a typical 0-100% progress bar
				int fileLength = urlConnection.getContentLength();

				// download the file
				InputStream input = urlConnection.getInputStream();
				@SuppressWarnings("resource")
				OutputStream output = new FileOutputStream(outputFile);

				byte buffer[] = new byte[1024];
				int total = 0;
				int bufferLength = 0;
				int lastprogress = 0;
				while ((bufferLength = input.read(buffer)) > 0) {
					if (isCancelled())
						return null;
					total += bufferLength;

					// publishing the progress....
					int progress = (int) (total * 100 / fileLength);
					if (progress > lastprogress) {
						lastprogress = progress;
						notification.contentView.setProgressBar(R.id.downloadProgress, 100, progress, false);
						manager.notify(song.aid, notification);
					}

					output.write(buffer, 0, bufferLength);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				messsage = e.getLocalizedMessage();
				Log.d(VibesApplication.VIBES, e.getClass().getName().toString());
				cancel(false);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			cancelNotification();
			downloadQueue.remove(Integer.valueOf(song.aid));
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			cancelNotification();
			downloadQueue.remove(Integer.valueOf(song.aid));
			if (outputFile.exists())
				outputFile.delete();
			if (messsage != null)
				listener.onDownloadException(messsage);
		}

		private void showNotification() {
			String title = String.format("%s %s", context.getString(R.string.downloading), songName);
			notification = new Notification(R.drawable.download_icon, title, System.currentTimeMillis());
			notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.downloader);

			Intent notifyIntent = new Intent(context, PlayerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, 0);

			notification.contentIntent = intent;
			notification.contentView.setTextViewText(R.id.downloadTitle, title);

			manager.notify(song.aid, notification);
		}

		private void cancelNotification() {
			manager.cancel(song.aid);
		}

	}
}
