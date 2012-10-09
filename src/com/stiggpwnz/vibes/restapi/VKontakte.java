package com.stiggpwnz.vibes.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import android.net.Uri;
import android.util.Log;

import com.stiggpwnz.vibes.VibesApplication;

public class VKontakte extends RestAPI {

	public static final String CLIENT_ID = "3027476";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String EXPIRES_IN = "expires_in";
	public static final String USER_ID = "user_id";
	public static final String CALLBACK_URL = "http://api.vk.com/blank.html";

	private static final String SCOPE = "friends,audio,wall,groups"; // GIVEN_RIGHTS
	private static final String AUTH_URL = "http://api.vk.com/oauth/authorize";
	private static final String API_URL = "https://api.vk.com/method/";

	private static final String AUDIO_ADD = "audio.add?";
	private static final String AUDIO_DELETE = "audio.delete?";
	private static final String AUDIO_GET = "audio.get?";
	private static final String AUDIO_GET_BY_ID = "audio.getById?";
	private static final String AUDIO_GET_ALBUMS = "audio.getAlbums?";
	private static final String AUDIO_RESTORE = "audio.restore?";
	private static final String AUDIO_SEARCH = "/method/audio.search";
	private static final String FRIENDS_GET = "friends.get?";
	private static final String GROUPS_GET = "groups.get?";
	public static final String NEWSFEED_GET = "newsfeed.get?";
	private static final String USERS_GET = "users.get?";
	private static final String WALL_GET = "wall.get?";

	private static final String AUDIO = "audio";
	private static final String AUDIOS = "audios";
	private static final String FIELDS = "fields";
	private static final String FILTER = "filter";
	private static final String FILTERS = "filters";
	private static final String ATTACHMENTS = "attachments";
	private static final String ITEMS = "items";
	private static final String RESPONSE = "response";
	private static final String COUNT = "count";
	private static final String POST = "post";
	private static final String OID = "oid";
	private static final String AID = "aid";
	private static final String GID = "gid";
	private static final String UID = "uid";
	private static final String URL = "url";
	private static final String TITLE = "title";
	private static final String ARTIST = "artist";
	private static final String OWNER = "owner";
	private static final String OWNER_ID = "owner_id";
	private static final String OFFSET = "offset";
	private static final String ALBUM_ID = "album_id";
	private static final String PERFORMER = "performer";
	private static final String PHOTO = "photo";
	private static final String NAME = "name";
	private static final String LAST_NAME = "last_name";
	private static final String FIRST_NAME = "first_name";
	private static final String PHOTO_REC = "photo_rec";
	private static final String HINTS = "hints";
	private static final String ORDER = "order";
	private static final String UNIT_FIELDS = "uid,first_name,last_name,photo_rec&";
	private static final String ERROR_CODE = "error_code";
	private static final String ERROR = "error";

	public int maxNews;
	public int maxAudios;

	private String accessToken;
	private int userId;

	private List<HttpPost> audioUrlRequests;

	public VKontakte(String accesToken, HttpClient client, int userId, int maxNews, int maxAudios) {
		super(client);
		this.accessToken = accesToken;
		this.userId = userId;
		this.maxNews = maxNews;
		this.maxAudios = maxAudios;
		audioUrlRequests = new LinkedList<HttpPost>();
	}

	public static String getAuthUrl() {
		return AUTH_URL + "?client_id=" + CLIENT_ID + "&scope=" + SCOPE + "&redirect_uri=" + CALLBACK_URL + "&display=touch&response_type=token";
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Unit getSelf() throws IOException, VKontakteException {
		try {
			URI url = new URI(API_URL + USERS_GET + "&uids=" + userId + "&" + FIELDS + "=" + UNIT_FIELDS + ACCESS_TOKEN + "=" + accessToken);

			JSONObject jsonResponse = execute(url);
			if (jsonResponse.has(RESPONSE)) {
				return parseUnit(jsonResponse.getJSONArray(RESPONSE).getJSONObject(0));
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (JSONException e) {

		} catch (URISyntaxException e) {

		}
		return null;
	}

	public int add(Song song) throws IOException, VKontakteException {
		try {
			if (song.ownerid == userId)
				return restore(song);

			URI url = new URI(API_URL + AUDIO_ADD + ACCESS_TOKEN + "=" + accessToken + "&" + AID + "=" + song.aid + "&" + OID + "=" + song.ownerid);

			JSONObject jsonResponse = execute(url);
			if (jsonResponse.has(RESPONSE))
				return jsonResponse.getInt(RESPONSE);
			else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}

		} catch (JSONException e) {

		} catch (URISyntaxException e) {

		}
		Log.d(VibesApplication.VIBES, "add() returning null: some fail ");
		return 0;
	}

	private int restore(Song song) throws IOException, VKontakteException, URISyntaxException, JSONException {
		URI url = new URI(API_URL + AUDIO_RESTORE + ACCESS_TOKEN + "=" + accessToken + "&" + AID + "=" + song.aid);

		JSONObject jsonResponse = execute(url);
		if (jsonResponse.has(RESPONSE))
			return jsonResponse.getJSONObject(RESPONSE).getInt(AID);
		else if (jsonResponse.has(ERROR)) {
			jsonResponse = jsonResponse.getJSONObject(ERROR);
			throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
		}

		Log.d(VibesApplication.VIBES, "add() returning null: some fail ");
		return 0;
	}

	public boolean delete(Song song) throws IOException, VKontakteException {
		try {
			URI url = new URI(API_URL + AUDIO_DELETE + ACCESS_TOKEN + "=" + accessToken + "&" + AID + "=" + song.myAid + "&" + OID + "=" + userId);

			JSONObject jsonResponse = execute(url);

			if (jsonResponse.has(RESPONSE)) {
				return jsonResponse.getInt(RESPONSE) == 1;
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (JSONException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}
		return false;
	}

	public List<Song> search(String search, int offset) throws IOException, VKontakteException {
		try {
			URI uri = new URI("https", "api.vk.com", AUDIO_SEARCH, COUNT + "=" + maxAudios + "&" + OFFSET + "=" + offset + "&" + ACCESS_TOKEN + "=" + accessToken + "&q="
					+ search.replace("&", "%26"), null);

			JSONObject jsonResponse = execute(uri);

			if (jsonResponse.has(RESPONSE)) {
				List<Song> result = songsFromJson(jsonResponse, true, false);
				return result;
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (URISyntaxException e) {

		} catch (JSONException e) {

		}
		return null;
	}

	public List<Song> getAudios(int ownerId, int albumId, int offset) throws IOException, VKontakteException {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append(API_URL + AUDIO_GET + COUNT + "=" + maxAudios + "&" + ACCESS_TOKEN + "=" + accessToken);

			if (ownerId > 0)
				buffer.append("&" + UID + "=" + ownerId);
			else if (ownerId < 0)
				buffer.append("&" + GID + "=" + -ownerId);

			if (albumId != 0)
				buffer.append("&" + ALBUM_ID + "=" + albumId);

			if (offset > 0)
				buffer.append("&" + OFFSET + "=" + offset);

			URI uri = new URI(buffer.toString());

			JSONObject jsonResponse = execute(uri);

			if (jsonResponse.has(RESPONSE)) {
				boolean own = ownerId == userId;
				List<Song> result = songsFromJson(jsonResponse, false, own);
				return result;
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}

		} catch (URISyntaxException e) {

		} catch (JSONException e) {

		}
		return null;
	}

	public List<Song> getWallAudios(int ownerId, int offset, boolean owner) throws IOException, VKontakteException {
		try {
			StringBuffer buffer = new StringBuffer(API_URL + WALL_GET + COUNT + "=" + maxNews + "&" + ACCESS_TOKEN + "=" + accessToken);
			if (ownerId != 0)
				buffer.append("&" + OWNER_ID + "=" + ownerId);
			if (offset > 0)
				buffer.append("&" + OFFSET + "=" + offset);
			if (owner)
				buffer.append("&" + FILTER + "=" + OWNER);

			URI uri = new URI(buffer.toString());

			JSONObject jsonResponse = execute(uri);

			if (jsonResponse.has(RESPONSE)) {
				List<Song> songs = new ArrayList<Song>();
				JSONArray posts = jsonResponse.getJSONArray(RESPONSE);

				int n = posts.length();
				for (int i = 1; i < n; i++) {
					if (posts.getJSONObject(i).has(ATTACHMENTS)) {
						JSONArray attachments = posts.getJSONObject(i).getJSONArray(ATTACHMENTS);

						int m = attachments.length();
						for (int j = 0; j < m; j++) {
							Song song = parseAttachement(attachments.getJSONObject(j));
							if (song != null)
								songs.add(song);
						}
					}
				}
				return songs;
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (JSONException e) {

		} catch (URISyntaxException e) {

		}
		return null;
	}

	public List<Song> getNewsFeedAudios(int offset) throws IOException, VKontakteException {
		try {
			StringBuffer buffer = new StringBuffer(API_URL + NEWSFEED_GET + FILTERS + "=" + POST + "&" + COUNT + "=" + maxNews + "&" + ACCESS_TOKEN + "=" + accessToken);

			if (offset > 0)
				buffer.append("&" + OFFSET + "=" + offset);
			URI uri = new URI(buffer.toString());

			JSONObject jsonResponse = execute(uri);

			if (jsonResponse.has(RESPONSE)) {
				jsonResponse = jsonResponse.getJSONObject(RESPONSE);
				if (jsonResponse.has(ITEMS)) {
					List<Song> songs = new ArrayList<Song>();
					JSONArray posts = jsonResponse.getJSONArray(ITEMS);
					int n = posts.length();
					for (int i = 0; i < n; i++) {
						if (posts.getJSONObject(i).has(ATTACHMENTS)) {
							JSONArray attachments = posts.getJSONObject(i).getJSONArray(ATTACHMENTS);

							int m = attachments.length();
							for (int j = 0; j < m; j++) {
								Song song = parseAttachement(attachments.getJSONObject(j));
								if (song != null)
									songs.add(song);
							}
						}
					}
					return songs;
				}
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (JSONException e) {

		} catch (URISyntaxException e) {

		}
		Log.d(VibesApplication.VIBES, "getSongs() returning null: API error");
		return null;
	}

	public void setSongUrl(Song song) throws IOException, VKontakteException {
		try {
			URI uri = new URI(API_URL + AUDIO_GET_BY_ID + AUDIOS + String.format("=%s_%s&", song.ownerid, song.aid) + ACCESS_TOKEN + "=" + accessToken);
			JSONObject jsonResponse = execute(uri);
			if (jsonResponse.has(RESPONSE)) {
				jsonResponse = jsonResponse.getJSONArray(RESPONSE).getJSONObject(0);
				song.url = Uri.parse(jsonResponse.getString(URL)).toString();
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (URISyntaxException e) {

		} catch (JSONException e) {

		}
	}

	public List<Album> getAlbums(int ownerId, int offset) throws IOException, VKontakteException {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append(API_URL + AUDIO_GET_ALBUMS + COUNT + "=100&" + OFFSET + "=" + offset + "&" + ACCESS_TOKEN + "=" + accessToken);
			if (ownerId != 0) {
				if (ownerId > 0)
					buffer.append("&" + UID + "=" + ownerId);
				else
					buffer.append("&" + GID + "=" + -ownerId);
			}

			URI uri = new URI(buffer.toString());
			JSONObject jsonResponse = execute(uri);

			if (jsonResponse.has(RESPONSE)) {
				JSONArray array = jsonResponse.getJSONArray(RESPONSE);
				List<Album> albums = new ArrayList<Album>();
				int n = array.length();
				for (int i = 1; i < n; i++) {
					JSONObject album = array.getJSONObject(i);
					int id = album.getInt(ALBUM_ID);
					String name = album.getString(TITLE);
					name = Jsoup.parse(name).text();
					albums.add(new Album(id, name));
				}
				return albums;
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}

		} catch (URISyntaxException e) {

		} catch (JSONException e) {

		}
		return null;
	}

	public List<Unit> getFriends(boolean orderByAlphabet) throws IOException, VKontakteException {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append(API_URL + FRIENDS_GET + FIELDS + "=" + UNIT_FIELDS + ACCESS_TOKEN + "=" + accessToken);
			if (orderByAlphabet)
				buffer.append("&" + ORDER + "=" + NAME);
			else
				buffer.append("&" + ORDER + "=" + HINTS);

			URI url = new URI(buffer.toString());
			JSONObject jsonResponse = execute(url);

			if (jsonResponse.has(RESPONSE)) {
				JSONArray friends = jsonResponse.getJSONArray(RESPONSE);
				List<Unit> units = new ArrayList<Unit>();
				int n = friends.length();
				for (int i = 0; i < n; i++)
					units.add(parseUnit(friends.getJSONObject(i)));
				return units;
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (URISyntaxException e) {

		} catch (JSONException e) {

		}
		return null;
	}

	private Unit parseUnit(JSONObject unit) throws JSONException {
		int id = unit.getInt(UID);
		String name = String.format("%s %s", unit.getString(FIRST_NAME), unit.getString(LAST_NAME));
		name = Jsoup.parse(name).text();
		String photo = Uri.parse(unit.getString(PHOTO_REC)).toString();
		return new Unit(id, name, photo);
	}

	public List<Unit> getGroups() throws IOException, VKontakteException {
		try {
			URI url = new URI(API_URL + GROUPS_GET + "extended=1&" + ACCESS_TOKEN + "=" + accessToken);
			JSONObject jsonResponse = execute(url);
			if (jsonResponse.has(RESPONSE)) {
				JSONArray groups = jsonResponse.getJSONArray(RESPONSE);
				List<Unit> units = new ArrayList<Unit>();
				int n = groups.length();
				for (int i = 1; i < n; i++) {
					JSONObject group = groups.getJSONObject(i);
					int gid = -group.getInt(GID);
					String name = group.getString(NAME);
					name = Jsoup.parse(name).text();
					String photo = Uri.parse(group.getString(PHOTO)).toString();
					units.add(new Unit(gid, name, photo));
				}
				return units;
			} else if (jsonResponse.has(ERROR)) {
				jsonResponse = jsonResponse.getJSONObject(ERROR);
				throw new VKontakteException(jsonResponse.getInt(ERROR_CODE));
			}
		} catch (URISyntaxException e) {

		} catch (JSONException e) {

		}
		return null;
	}

	private static Song parseAttachement(JSONObject attachment) throws JSONException {
		if (attachment.has(AUDIO)) {
			JSONObject audio = attachment.getJSONObject(AUDIO);
			int aid = audio.getInt(AID);
			int owner = audio.getInt(OWNER_ID);
			String artist = audio.getString(PERFORMER);
			artist = Jsoup.parse(artist).text();
			String name = audio.getString(TITLE);
			name = Jsoup.parse(name).text();
			return new Song(aid, owner, artist, name);
		}
		return null;
	}

	private static List<Song> songsFromJson(JSONObject jsonResponse, boolean search, boolean own) throws JSONException {
		List<Song> songs = new ArrayList<Song>();
		JSONArray array = jsonResponse.getJSONArray(RESPONSE);
		int n = array.length();
		for (int i = search ? 1 : 0; i < n; i++) {
			JSONObject json = array.getJSONObject(i);

			int aid = json.getInt(AID);

			int owner = json.getInt(OWNER_ID);

			String artist = json.getString(ARTIST);
			artist = Jsoup.parse(artist).text();

			String name = json.getString(TITLE);
			name = Jsoup.parse(name).text();

			String url = Uri.parse(json.getString(URL)).toString();

			Song song = new Song(aid, owner, artist, name, url);

			if (own) {
				song.myAid = aid;
				song.loved = true;
			}

			songs.add(song);
		}
		return songs;
	}

	protected JSONObject execute(URI uri) throws IOException, JSONException {
		String url = uri.toString().replace("%2526", "%26");

		Log.d(VibesApplication.VIBES, url);

		HttpPost request = new HttpPost(url);
		if (url.contains(AUDIO_GET_BY_ID) && audioUrlRequests != null) {
			synchronized (audioUrlRequests) {
				audioUrlRequests.add(request);
			}
		}

		JSONObject jsonResponse = new JSONObject(executeRequest(request));
		return jsonResponse;
	}

	public List<HttpPost> getAudioUrlRequests() {
		return audioUrlRequests;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

}
