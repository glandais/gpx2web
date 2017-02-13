package org.glandais.srtm.loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;

public class Tour {

	public static void main(String[] args) {
		System.getProperties().setProperty("httpclient.useragent", "Mozilla/4.0");
		HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

		int i = 0;
		while (true) {
			long dossier = i / 100;
			long now = System.currentTimeMillis();
			String url = "http://fep-api.dimensiondata.com/stages/116/rider-telemetry";
			File dest = new File("d:\\tour\\" + dossier + "\\" + now + "_riders.json");
			get(client, url, dest);

			url = "http://fep-api.dimensiondata.com/stages/116/group-telemetry?datasource=aso";
			dest = new File("d:\\tour\\" + dossier + "\\" + now + "_groups.json");
			get(client, url, dest);

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}
	}

	private static void get(HttpClient client, String url, File dest) {
		GetMethod get = new GetMethod(url);
		get.setRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
		get.setFollowRedirects(true);
		try {
			client.executeMethod(get);
			InputStream in = new BufferedInputStream(get.getResponseBodyAsStream());
			FileUtils.copyInputStreamToFile(in, dest);
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
