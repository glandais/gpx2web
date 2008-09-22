package org.gpx2web;

public class Gpx2WebException extends Exception {

	private static final long serialVersionUID = -6884164882766160567L;

	public Gpx2WebException(Exception e) {
		super("Gpx2WebException", e);
	}

}
