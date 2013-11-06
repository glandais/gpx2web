package org.glandais.digicamtools.distance;

import static java.lang.Math.max;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Highly influended from
 * http://blog.notdot.net/2007/4/Damn-Cool-Algorithms-Part-1-BK-Trees
 * 
 * @author Nirav Thaker
 */
public class BKTree {
	public static class BKNode {
		final String name;
		final Map<Integer, BKNode> children = new HashMap<Integer, BKNode>();

		public BKNode(String name) {
			this.name = name;
		}

		protected BKNode childAtDistance(int pos) {
			return children.get(pos);
		}

		private void addChild(int pos, BKNode child) {
			children.put(pos, child);
		}

		public List<String> search(String node, int maxDistance) {
			int distance = distance(this.name, node);
			List<String> matches = new LinkedList<String>();
			if (distance <= maxDistance)
				matches.add(this.name);
			if (children.size() == 0)
				return matches;
			int i = max(1, distance - maxDistance);
			for (; i <= distance + maxDistance; i++) {
				BKNode child = children.get(i);
				if (child == null)
					continue;
				matches.addAll(child.search(node, maxDistance));
			}
			return matches;
		}
	}

	private BKNode root;

	/**
	 * Performs a fuzzy search.
	 * 
	 * @param q
	 * @param maxDist
	 * @return
	 */
	public List<String> search(String q, int maxDist) {
		return root.search(q, maxDist);
	}

	/**
	 * Exact word search, same as {@link search(String, 1)}
	 * 
	 * @param q
	 * @return match or empty string.
	 */
	public String search(String q) {
		List<String> list = root.search(q, 1);
		return list.isEmpty() ? "" : list.iterator().next();
	}

	public void add(String node) {
		if (node == null || node.isEmpty())
			throw new IllegalArgumentException("word can't be null or empty.");
		BKNode newNode = new BKNode(node);
		if (root == null) {
			root = newNode;
		}
		addInternal(root, newNode);
	}

	private void addInternal(BKNode src, BKNode newNode) {
		if (src.equals(newNode))
			return;
		int distance = distance(src.name, newNode.name);
		BKNode bkNode = src.childAtDistance(distance);
		if (bkNode == null) {
			src.addChild(distance, newNode);
		} else
			addInternal(bkNode, newNode);
	}

	public static int distance(String s1, String s2) {
		int counter = 0;
		for (int k = 0; k < s1.length(); k++) {
			if (s1.charAt(k) != s2.charAt(k)) {
				counter++;
			}
		}
		return counter;
	}

}
