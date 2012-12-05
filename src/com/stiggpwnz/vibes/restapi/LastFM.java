package com.stiggpwnz.vibes.restapi;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.DisplayMetrics;
import android.util.Log;

import com.stiggpwnz.vibes.VibesApplication;

public class LastFM extends RestAPI {

	public static final String WITHOUT_IMAGE = "no image";

	private static final String API_KEY = "59ce954b080ef3eb99cca836896dbf5e";
	private static final String API_SECRET = "d4c1fab919d52f46fd1d2829a37d127c";

	private static final String API_SCHEME = "http";
	private static final String API_AUTHORITY = "ws.audioscrobbler.com";
	private static final String API_PATH = "/2.0/";

	private static final String AUTH_GET_MOBILE_SESSION = "auth.getMobileSession";
	private static final String TRACK_GET_INFO = "track.getInfo";
	private static final String TRACK_UNLOVE = "track.unlove";
	private static final String TRACK_LOVE = "track.love";
	private static final String TRACK_SCROBBLE = "track.scrobble";
	private static final String TRACK_UPDATE_NOW_PLAYING = "track.updateNowPlaying";
	private static final String USER_GET_INFO = "user.getInfo";

	private static final String TIMESTAMP = "timestamp";
	private static final String USERNAME = "username";
	private static final String API_KEY_STRING = "api_key";
	private static final String TRACK = "track";
	private static final String AUTOCORRECT = "autocorrect";
	private static final String AUTH_TOKEN = "authToken";
	private static final String API_SIGNATURE = "api_sig";
	private static final String USER = "user";
	private static final String SESSION_KEY = "sk";
	private static final String METHOD = "method";
	private static final String OK = "ok";
	private static final String STATUS = "status";
	private static final String KEY = "key";
	private static final String SESSION = "session";
	private static final String IMAGE = "image";
	private static final String ALBUM = "album";
	private static final String NAME = "name";
	private static final String ARTIST = "artist";

	private List<HttpPost> imageRequestQueue;

	private DocumentBuilder builder;
	private String session;
	private int density;

	public LastFM(HttpClient client, String session, int densityDpi) {
		super(client);
		try {
			this.session = session;
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			imageRequestQueue = new LinkedList<HttpPost>();
			this.density = densityDpi;
		} catch (ParserConfigurationException e) {

		}
	}

	public boolean love(Song song) {
		try {
			String artist = song.performer;
			String title = song.title;

			String singature = md5(API_KEY_STRING + API_KEY + ARTIST + artist + METHOD + TRACK_LOVE + SESSION_KEY + session + TRACK + title + API_SECRET);

			URI uri = new URI(API_SCHEME, API_AUTHORITY, API_PATH, METHOD + "=" + TRACK_LOVE + "&" + TRACK + "=" + title.replace("&", "%26") + "&" + ARTIST + "="
					+ artist.replace("&", "%26") + "&" + API_KEY_STRING + "=" + API_KEY + "&" + SESSION_KEY + "=" + session + "&" + API_SIGNATURE + "=" + singature, null);

			Element element = execute(uri);
			if (element != null)
				return element.getAttribute(STATUS).equals(OK);

		} catch (URISyntaxException e) {
			return false;
		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (SAXException e) {
			return false;
		}
		return false;
	}

	public boolean unlove(Song song) {
		try {
			String artist = song.performer;
			String title = song.title;

			String singature = md5(API_KEY_STRING + API_KEY + ARTIST + artist + METHOD + TRACK_UNLOVE + SESSION_KEY + session + TRACK + title + API_SECRET);

			URI uri = new URI(API_SCHEME, API_AUTHORITY, API_PATH, METHOD + "=" + TRACK_UNLOVE + "&" + TRACK + "=" + title.replace("&", "%26") + "&" + ARTIST + "="
					+ artist.replace("&", "%26") + "&" + API_KEY_STRING + "=" + API_KEY + "&" + SESSION_KEY + "=" + session + "&" + API_SIGNATURE + "=" + singature, null);

			Element element = execute(uri);
			if (element != null)
				return element.getAttribute(STATUS).equals(OK);

		} catch (URISyntaxException e) {
			return false;
		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (SAXException e) {
			return false;
		}
		return false;
	}

	public boolean updateNowPlaying(Song song) {
		if (song != null) {
			try {
				String artist = song.performer;
				String title = song.title;

				String singature = md5(API_KEY_STRING + API_KEY + ARTIST + artist + METHOD + TRACK_UPDATE_NOW_PLAYING + SESSION_KEY + session + TRACK + title
						+ API_SECRET);

				URI uri = new URI(API_SCHEME, API_AUTHORITY, API_PATH, METHOD + "=" + TRACK_UPDATE_NOW_PLAYING + "&" + TRACK + "=" + title.replace("&", "%26") + "&"
						+ ARTIST + "=" + artist.replace("&", "%26") + "&" + API_KEY_STRING + "=" + API_KEY + "&" + SESSION_KEY + "=" + session + "&" + API_SIGNATURE
						+ "=" + singature, null);

				Element element = execute(uri);
				if (element != null)
					return element.getAttribute(STATUS).equals(OK);
			} catch (Exception e) {

			}
		}
		return false;
	}

	public boolean scrobble(Song song, long timeStamp) {
		if (song != null) {
			try {
				String artist = song.performer;
				String title = song.title;

				String singature = md5(API_KEY_STRING + API_KEY + ARTIST + artist + METHOD + TRACK_SCROBBLE + SESSION_KEY + session + TIMESTAMP + timeStamp + TRACK
						+ title + API_SECRET);

				URI uri = new URI(API_SCHEME, API_AUTHORITY, API_PATH, METHOD + "=" + TRACK_SCROBBLE + "&" + TIMESTAMP + "=" + timeStamp + "&" + TRACK + "="
						+ title.replace("&", "%26") + "&" + ARTIST + "=" + artist.replace("&", "%26") + "&" + API_KEY_STRING + "=" + API_KEY + "&" + SESSION_KEY + "="
						+ session + "&" + API_SIGNATURE + "=" + singature, null);

				Element element = execute(uri);

				if (element != null)
					return element.getAttribute(STATUS).equals(OK);

			} catch (Exception e) {

			}
		}
		return false;
	}

	public String[] auth(String username, String password) {
		try {
			URI uri = new URI(API_SCHEME, API_AUTHORITY, API_PATH, METHOD + "=" + AUTH_GET_MOBILE_SESSION + "&" + USERNAME + "=" + username.toLowerCase() + "&"
					+ AUTH_TOKEN + "=" + authToken(username, password) + "&" + API_KEY_STRING + "=" + API_KEY + "&" + API_SIGNATURE + "="
					+ authApiSig(username, password), null);

			Element element = execute(uri);

			if (element != null) {
				NodeList list = element.getElementsByTagName(SESSION);
				element = (Element) list.item(0);
				if (element != null) {
					String[] params = new String[3];

					list = element.getElementsByTagName(NAME);
					Element element1 = (Element) list.item(0);
					params[0] = element1.getFirstChild().getNodeValue();

					list = element.getElementsByTagName(KEY);
					element1 = (Element) list.item(0);
					params[1] = element1.getFirstChild().getNodeValue();
					session = params[1];

					params[2] = getUserImageURL(params[0]);

					return params;
				}
			}
		} catch (URISyntaxException e) {
			return null;
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (SAXException e) {
			return null;
		}
		return null;
	}

	private String getUserImageURL(String username) {
		try {
			URI uri = new URI(API_SCHEME, API_AUTHORITY, API_PATH, METHOD + "=" + USER_GET_INFO + "&" + USER + "=" + username + "&" + API_KEY_STRING + "=" + API_KEY,
					null);
			Element element = execute(uri);
			if (element != null) {
				NodeList list = element.getElementsByTagName(IMAGE);

				switch (density) {
				case DisplayMetrics.DENSITY_LOW:
					element = (Element) list.item(1);
					break;
				case DisplayMetrics.DENSITY_MEDIUM:
					element = (Element) list.item(2);
					break;
				default:
					element = (Element) list.item(3);
					break;
				}
				if (element != null && element.getFirstChild() != null)
					return element.getFirstChild().getNodeValue();
			}
		} catch (URISyntaxException e) {
			return null;
		} catch (ParseException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (SAXException e) {
			return null;
		}
		return null;
	}

	public String getAlbumImageUrl(Song song) {
		try {
			URI uri = new URI(API_SCHEME, API_AUTHORITY, API_PATH, METHOD + "=" + TRACK_GET_INFO + "&" + API_KEY_STRING + "=" + API_KEY + "&" + ARTIST + "="
					+ song.performer.replace("&", "%26") + "&" + TRACK + "=" + song.title.replace("&", "%26") + "&" + AUTOCORRECT + "=1", null);

			Element element = execute(uri);
			if (element != null) {
				NodeList list = element.getElementsByTagName(TRACK);
				element = (Element) list.item(0);
				if (element != null) {
					list = element.getElementsByTagName(ALBUM);
					element = (Element) list.item(0);
					if (element != null) {
						list = element.getElementsByTagName(IMAGE);
						element = (Element) list.item(3);
						return element.getFirstChild().getNodeValue();
					}
					return WITHOUT_IMAGE;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	protected Element execute(URI uri) throws IOException, ClientProtocolException, SAXException {
		String url = uri.toString().replace("%2526", "%26");

		Log.d(VibesApplication.VIBES, url);

		HttpPost request = new HttpPost(url);
		if (url.contains(TRACK_GET_INFO) && imageRequestQueue != null)
			synchronized (this) {
				imageRequestQueue.add(request);
			}

		InputSource is = new InputSource(new StringReader(executeRequest(request)));
		Document doc = builder.parse(is);
		return doc.getDocumentElement();
	}

	public List<HttpPost> getImageRequestQueue() {
		return imageRequestQueue;
	}

	private String authToken(String username, String password) {
		return md5(username.toLowerCase() + md5(password));
	}

	private String authApiSig(String username, String password) {
		return md5(API_KEY_STRING + API_KEY + AUTH_TOKEN + authToken(username, password) + METHOD + AUTH_GET_MOBILE_SESSION + USERNAME + username.toLowerCase()
				+ API_SECRET);
	}

	public void setSession(String session) {
		this.session = session;
	}

}
