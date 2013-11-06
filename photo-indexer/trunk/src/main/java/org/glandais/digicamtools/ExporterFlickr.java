package org.glandais.digicamtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.glandais.digicamtools.flickr.FolderComparator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.Parameter;
import com.googlecode.flickrjandroid.RequestContext;
import com.googlecode.flickrjandroid.Response;
import com.googlecode.flickrjandroid.auth.Permission;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthException;
import com.googlecode.flickrjandroid.oauth.OAuthInterface;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.oauth.OAuthUtils;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;
import com.googlecode.flickrjandroid.uploader.UploadMetaData;
import com.googlecode.flickrjandroid.uploader.Uploader;

public class ExporterFlickr {

	private static final String API_KEY = "778426b89082e3a2fd1948a8e5d48c75";
	private static final String API_SECRET = "f307bdefeaeeed18";

	private static final File OAUTH_FILE = new File("oauth.bin");

	public static void main(String[] args) throws FlickrException, IOException,
			JSONException, ClassNotFoundException, SAXException {
		String folder = args[0];
		new ExporterFlickr().process(new File(folder));
	}

	private Flickr flickr;
	private OAuth flickrAuth;

	public void process(File root) {

		flickr = new Flickr(API_KEY, API_SECRET);

		try {
			authenticate();
		} catch (Exception e) {
			System.err.println("Failed to authenticate");
			e.printStackTrace();
			return;
		}

		Map<String, Photoset> photosetMap;
		try {
			photosetMap = retrievePhotosets();
		} catch (Exception e) {
			System.err.println("Failed to get sets");
			e.printStackTrace();
			return;
		}

		// Scanning folders
		System.out.println("Scanning folders");
		Map<String, List<File>> folders = new HashMap<String, List<File>>();
		scanFolders(root, folders);

		Map<String, File> fileForfolder = new TreeMap<String, File>(
				new FolderComparator(-1));
		Map<String, List<File>> imagesForFolder = new HashMap<String, List<File>>();
		for (Entry<String, List<File>> entry : folders.entrySet()) {
			if (entry.getValue().size() == 1) {
				File parentFile = entry.getValue().get(0);
				List<File> photos = scanPhotos(parentFile);
				if (photos.size() > 0) {
					fileForfolder.put(entry.getKey(), parentFile);
					imagesForFolder.put(entry.getKey(), photos);
				}
			} else {
				System.out.println("Not processing " + entry.getKey()
						+ " : multiple folders with same name :");
				for (File folder : entry.getValue()) {
					System.out.println(folder.getAbsolutePath());
				}
			}
		}

		System.out.println("Adding missing sets");
		for (Entry<String, File> entry : fileForfolder.entrySet()) {
			if (!photosetMap.containsKey(entry.getKey())) {
				try {
					boolean first = true;
					String setId = null;
					for (File file : imagesForFolder.get(entry.getKey())) {
						System.out.println("Uploading "
								+ file.getAbsolutePath());
						String id = uploadPhoto(file);
						if (first) {
							System.out.println("Create set " + entry.getKey());
							setId = createSet(entry.getKey(), id);
							first = false;
						} else {
							putPhotoInSet(id, setId);
						}
					}
				} catch (Exception e) {
					System.err.println("Failed to process set...");
					e.printStackTrace();
				}
			}
		}

		System.out.println("Processing sets from flickr");
		for (Entry<String, Photoset> entry : photosetMap.entrySet()) {
			Photoset photoset = entry.getValue();
			String setTitle = photoset.getTitle();

			System.out.println("Processing " + setTitle);

			String photosetId = photoset.getId();
			Map<String, String> mapPhotosetPhotos = null;

			try {
				mapPhotosetPhotos = getPhotosAndSort(photosetId);
			} catch (Exception e) {
				System.err.println("Failed to get photos...");
				e.printStackTrace();
			}

			if (mapPhotosetPhotos != null) {
				File folder = fileForfolder.get(setTitle);
				List<File> localPhotos = imagesForFolder.get(setTitle);
				processFolder(folder, localPhotos, setTitle, photosetId,
						mapPhotosetPhotos);
			}
		}

	}

	private Map<String, Photoset> retrievePhotosets() throws IOException,
			FlickrException, JSONException {
		System.out.println("Retrieving sets");
		Photosets list = flickr.getPhotosetsInterface().getList(
				flickrAuth.getUser().getId());
		List<Photoset> photosets = new ArrayList<Photoset>(list.getPhotosets());
		Map<String, Photoset> photosetMap = new TreeMap<String, Photoset>(
				new FolderComparator(1));
		for (Photoset photoset : photosets) {
			photosetMap.put(photoset.getTitle(), photoset);
		}

		if (photosetMap.size() > 0) {
			String[] photosetIds = new String[photosetMap.size()];
			int j = 0;
			Set<Entry<String, Photoset>> entrySet = photosetMap.entrySet();
			boolean resort = false;
			for (Entry<String, Photoset> entry : entrySet) {
				photosetIds[j] = entry.getValue().getId();
				if (!photosets.get(j).getId().equals(entry.getValue().getId())) {
					resort = true;
				}
				j++;
			}
			if (resort) {
				System.out.println("Sorting sets");
				flickr.getPhotosetsInterface().orderSets(photosetIds);
			} else {
				System.out.println("Not sorting sets");
			}
		}
		return photosetMap;
	}

	private void authenticate() throws IOException, ClassNotFoundException,
			FlickrException, MalformedURLException {
		if (OAUTH_FILE.exists()) {
			flickrAuth = readOAuth();
		} else {
			OAuthToken oauthToken = flickr.getOAuthInterface().getRequestToken(
					"http://localhost");
			System.out.println(flickr.getOAuthInterface()
					.buildAuthenticationUrl(Permission.DELETE, oauthToken));
			String tokenVerifier = readParamFromCommand("Enter Token Verifier (last URL parameter) : ");
			flickrAuth = flickr.getOAuthInterface().getAccessToken(
					oauthToken.getOauthToken(),
					oauthToken.getOauthTokenSecret(), tokenVerifier);
			writeOAuth(flickrAuth);
		}

		RequestContext.getRequestContext().setOAuth(flickrAuth);
	}

	private void processFolder(File folder, List<File> localPhotos,
			String setTitle, String photosetId,
			Map<String, String> mapPhotosetPhotos) {
		if (folder == null) {
			System.err.println(setTitle + " not found locally!");
		} else {
			Map<String, File> mapLocalPhotos = new HashMap<String, File>();
			for (File localPhoto : localPhotos) {
				mapLocalPhotos.put(getSimpleName(localPhoto.getName()),
						localPhoto);
			}

			for (Entry<String, File> entryPhoto : mapLocalPhotos.entrySet()) {
				String photoName = entryPhoto.getKey();
				if (mapPhotosetPhotos.get(photoName) == null) {
					File photo = entryPhoto.getValue();
					System.out.println("Uploading " + photo.getAbsolutePath()
							+ " to " + setTitle);
					try {
						String photoId = uploadPhoto(photo);
						putPhotoInSet(photoId, photosetId);
					} catch (Exception e) {
						System.err.println("Failed to upload photo");
						e.printStackTrace();
					}
				}
			}
			for (Entry<String, String> entryPhoto : mapPhotosetPhotos
					.entrySet()) {
				if (mapLocalPhotos.get(entryPhoto.getKey()) == null) {
					System.out.println("Removing " + entryPhoto.getKey()
							+ " from " + setTitle);
					try {
						flickr.getPhotosInterface().delete(
								entryPhoto.getValue());
					} catch (Exception e) {
						System.err.println("Failed to delete photo");
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void putPhotoInSet(String photoId, String photosetId)
			throws IOException, FlickrException, JSONException {
		flickr.getPhotosetsInterface().addPhoto(photosetId, photoId);
	}

	private String uploadPhoto(File file) throws IOException, FlickrException,
			SAXException {
		Uploader uploader = new Uploader(API_KEY, API_SECRET);
		FileInputStream fis = new FileInputStream(file);
		UploadMetaData metaData = new UploadMetaData();
		metaData.setHidden(true);
		metaData.setPublicFlag(false);
		metaData.setFriendFlag(false);
		metaData.setFamilyFlag(false);
		String result = uploader.upload(file, metaData);
		return result;
	}

	private String createSet(String title, String primaryPhotoId)
			throws IOException, FlickrException, JSONException {
		Photoset photoSet = flickr.getPhotosetsInterface().create(title, title,
				primaryPhotoId);
		return photoSet.getId();
	}

	private void sortPhotos(String photosetId, String sb) {
		try {
			List<Parameter> parameters = new ArrayList<Parameter>();
			parameters.add(new Parameter("method",
					"flickr.photosets.reorderPhotos"));
			parameters.add(new Parameter("photoset_id", photosetId));
			parameters.add(new Parameter("photo_ids", sb));
			queryFlickr(parameters);
		} catch (Exception e) {
			System.err.println("Failed to sort photos...");
			e.printStackTrace();
		}
	}

	private Map<String, String> getPhotosAndSort(String photosetId)
			throws OAuthException, IOException, JSONException, FlickrException {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("method", "flickr.photosets.getPhotos"));
		parameters.add(new Parameter("photoset_id", photosetId));
		parameters.add(new Parameter("media", "all"));
		Response postJSON = queryFlickr(parameters);
		Map<String, String> photos = new TreeMap<String, String>();
		JSONObject photosetElement = postJSON.getData().getJSONObject(
				"photoset");
		JSONArray photoElements = photosetElement.optJSONArray("photo");
		List<String> ids = new ArrayList<String>();
		for (int i = 0; photoElements != null && i < photoElements.length(); i++) {
			JSONObject photoElement = photoElements.getJSONObject(i);
			String title = "";
			JSONObject titleObj = photoElement.optJSONObject("title");
			if (titleObj != null) {
				title = titleObj.getString("_content");
			} else {
				title = photoElement.getString("title");
			}
			String photoId = photoElement.getString("id");
			ids.add(photoId);
			photos.put(getSimpleName(title), photoId);
		}

		if (photos.size() > 0) {
			String photoIdsCurrent = StringUtils.join(ids, ",");
			String photoIdsSorted = StringUtils.join(photos.values(), ",");
			if (photoIdsCurrent.equals(photoIdsSorted)) {
				System.out.println("Not sorting photos");
			} else {
				System.out.println("Sorting photos");
				sortPhotos(photosetId, photoIdsSorted);
			}
		}

		return photos;
	}

	private String getSimpleName(String name) {
		if (name.toLowerCase().endsWith(".jpg")
				|| name.toLowerCase().endsWith(".mts")
				|| name.toLowerCase().endsWith(".mp4")
				|| name.toLowerCase().endsWith(".jpeg")
				|| name.toLowerCase().endsWith(".png")) {
			int lastIndexOf = name.lastIndexOf('.');
			name = name.substring(0, lastIndexOf);
		}
		return name;
	}

	private static List<File> scanPhotos(File parentFile) {
		List<File> result = new ArrayList<File>();
		File[] listFiles = parentFile.listFiles();
		for (File file : listFiles) {
			if (file.isFile()) {
				if (file.getName().toLowerCase().endsWith(".jpg")
						|| file.getName().toLowerCase().endsWith(".mts")
						|| file.getName().toLowerCase().endsWith(".mp4")
						|| file.getName().toLowerCase().endsWith(".jpeg")
						|| file.getName().toLowerCase().endsWith(".png")) {
					result.add(file);
				}
			}
		}
		return result;
	}

	private static void scanFolders(File folder, Map<String, List<File>> folders) {
		String name = folder.getName();
		List<File> list = folders.get(name);
		if (list == null) {
			list = new ArrayList<File>();
			folders.put(name, list);
		}
		list.add(folder);
		File[] listFiles = folder.listFiles();
		for (File file : listFiles) {
			if (file.isDirectory() && !file.getName().startsWith(".")) {
				scanFolders(file, folders);
			}
		}
	}

	private Response queryFlickr(List<Parameter> parameters)
			throws OAuthException, IOException, JSONException, FlickrException {
		parameters.add(new Parameter(OAuthInterface.PARAM_OAUTH_CONSUMER_KEY,
				API_KEY));
		parameters.add(new Parameter("user_id", flickrAuth.getUser().getId()));
		OAuthUtils.addOAuthToken(parameters);
		Response postJSON = flickr.getTransport().postJSON(
				flickr.getSharedSecret(), parameters);
		return postJSON;
	}

	public static String readParamFromCommand(String message)
			throws IOException {
		// prompt the user to enter their name
		System.out.print(message);
		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return String.valueOf(br.readLine()).trim();
	}

	private static void writeOAuth(OAuth auth) throws IOException {
		FileOutputStream fos = new FileOutputStream(OAUTH_FILE);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		try {
			oos.writeObject(auth);
			oos.flush();
		} finally {
			try {
				oos.close();
			} finally {
				fos.close();
			}
		}
	}

	private static OAuth readOAuth() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(OAUTH_FILE);
		ObjectInputStream ois = new ObjectInputStream(fis);
		try {
			return (OAuth) ois.readObject();
		} finally {
			// on ferme les flux
			try {
				ois.close();
			} finally {
				fis.close();
			}
		}
	}

}
