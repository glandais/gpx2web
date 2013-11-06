package org.glandais.digicamtools.flickr;

import java.util.Comparator;

public class FolderComparator implements Comparator<String> {

	private int ratio;

	public FolderComparator(int i) {
		super();
		this.ratio = i;
	}

	public int compare(String o1, String o2) {
		char c1 = o1.charAt(0);
		char c2 = o2.charAt(0);
		boolean n1 = (c1 >= 48 && c1 <= 57);
		boolean n2 = (c2 >= 48 && c2 <= 57);
		if (n1 == n2) {
			if (n1) {
				return -o1.compareTo(o2) * ratio;
			} else {
				return o1.compareTo(o2) * ratio;
			}
		} else {
			if (n1) {
				return -1 * ratio;
			} else {
				return 1 * ratio;
			}
		}
	}
}
