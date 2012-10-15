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
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

import com.jakewharton.notificationcompat2.NotificationCompat2;
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
		if (song != null && !downloadList.contains(song.aid)) {
			downloadList.add(song.aid);
			new DownloaderThread(song).execute();
		}
	}

	private class DownloaderThread extends AsyncTask<Void, Void, Void> {

		private String filename;
		private Notification notification;
		private Song song;
		private File outputFile;
		private String messsage;
		private final PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(context, PlayerActivity.class), 0);

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
				String secondary = context.getString(R.string.to) + " " + path;
				while ((bufferLength = input.read(buffer)) > 0) {
					if (isCancelled())
						return null;
					total += bufferLength;
					output.write(buffer, 0, bufferLength);

					// publishing the progress....
					int progress = (int) (total * 100 / fileLength);
					if (progress > lastprogress) {
						lastprogress = progress;

						if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
							notification.contentView.setProgressBar(R.id.downloadProgress, 100, progress, false);
							notification.contentView.setTextViewText(R.id.textPercent, progress + " %");
						} else {
							notification = new NotificationCompat2.Builder(context).setSmallIcon(R.drawable.download_icon).setContentTitle(song.toString())
									.setTicker(song.toString()).setContentText(secondary).setContentIntent(intent).setProgress(100, progress, false).build();
							notification.flags |= Notification.FLAG_ONGOING_EVENT;
						}

						notificationManager.notify(DOWNLOADING, song.aid, notification);
					}

				}

				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				messsage = String.format("%s: %s", e.getClass().getName(), e.getLocalizedMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			cancelNotification();
			downloadList.remove(Integer.valueOf(song.aid));
			if (showFinishedNotification)
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
			int icon = success ? R.drawable.ok : R.drawable.cancel;
			String contentTitle = song.toString();
			String contentText = context.getString(success ? R.string.download_success : R.string.download_fail);
			Log.d(VibesApplication.VIBES, contentText);
			Notification notification = new NotificationCompat2.Builder(context).setContentTitle(contentTitle).setContentText(contentText).setSmallIcon(icon)
					.setContentIntent(intent).setTicker(contentTitle).build();
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notificationManager.notify(FINISHED, song.aid, notification);
		}

		private void showNotification() {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				notification = new NotificationCompat2.Builder(context).setSmallIcon(R.drawable.download_icon).setContentIntent(intent).setTicker(song.toString()).build();
				notification.contentView = new RemoteViews(context.getPackageName(), R.layout.downloader);
				notification.contentView.setTextViewText(R.id.downloadTitle, song.toString());
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				notificationManager.notify(DOWNLOADING, song.aid, notification);
			}
		}

		private void cancelNotification() {
			notificationManager.cancel(DOWNLOADING, song.aid);
		}

	}
}
