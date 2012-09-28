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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.VKontakte;

public class Downloader {

	private static final String DOWNLOADING = "downloading";
	private static final String FINISHED = "finished";

	private NotificationManager notificationManager;
	private Context context;
	private VKontakte vkontakte;
	private List<Integer> downloadList;
	private String path;
	private boolean showFinishedNotification;

	public Downloader(Context context, VKontakte vkontakte, List<Integer> downloadList, String path, boolean showFinishedNotification) {
		this.context = context;
		this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.vkontakte = vkontakte;
		this.downloadList = downloadList;
		this.path = path;
		this.showFinishedNotification = showFinishedNotification;
	}

	public void download(Song song) throws IOException {
		if (!downloadList.contains(Integer.valueOf(song.aid))) {
			downloadList.add(Integer.valueOf(song.aid));
			new DownloaderThread(song).execute();
		}
	}

	private class DownloaderThread extends AsyncTask<Void, Void, Void> {

		private String filename;
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

			filename = song.toString() + ".mp3";
			outputFile = new File(directory, filename);
			int tries = 0;
			while (outputFile.exists()) {
				tries++;
				filename = String.format("%s (%d).mp3", song.toString(), tries);
				outputFile = new File(directory, filename);
			}
			Log.d(VibesApplication.VIBES, outputFile.getAbsolutePath());
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showNotification();
		}

		@SuppressWarnings("resource")
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.currentThread().setName("Downloading " + filename);
				if (song.url == null)
					vkontakte.setSongUrl(song);

				URL url = new URL(song.url);

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setConnectTimeout(VibesApplication.TIMEOUT_CONNECTION);
				urlConnection.setReadTimeout(VibesApplication.TIMEOUT_SOCKET);
				urlConnection.connect();

				int fileLength = urlConnection.getContentLength();

				InputStream input = urlConnection.getInputStream();
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
						notificationManager.notify(DOWNLOADING, song.aid, notification);
					}

					output.write(buffer, 0, bufferLength);
				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				messsage = String.format("%s: %s", e.getClass().getName(), e.getLocalizedMessage());
				return null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			cancelNotification();
			downloadList.remove(Integer.valueOf(song.aid));
			showNotification(messsage == null);
			if (messsage != null && outputFile.exists())
				outputFile.delete();
			else if (messsage == null) {
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent.setData(Uri.fromFile(outputFile));
				context.sendBroadcast(intent);
			}
		}

		private void showNotification(boolean success) {
			if (showFinishedNotification) {
				int icon;
				if (success)
					icon = R.drawable.ok;
				else
					icon = R.drawable.cancel;

				Notification notification = new Notification(icon, song.toString(), System.currentTimeMillis());

				int status;
				if (success)
					status = R.string.download_success;
				else
					status = R.string.download_fail;

				CharSequence contentTitle = song.toString();
				CharSequence contentText = context.getString(status);
				Intent notificationIntent = new Intent(context, PlayerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notificationManager.notify(FINISHED, song.aid, notification);
			}
		}

		private void showNotification() {
			notification = new Notification(R.drawable.download_icon, song.toString(), System.currentTimeMillis());
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.downloader);

			Intent notifyIntent = new Intent(context, PlayerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, 0);

			notification.contentIntent = intent;
			notification.contentView.setTextViewText(R.id.downloadTitle, song.toString());

			notificationManager.notify(DOWNLOADING, song.aid, notification);
		}

		private void cancelNotification() {
			notificationManager.cancel(DOWNLOADING, song.aid);
		}

	}
}
