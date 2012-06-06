package org.gpx2web;

import java.util.List;

import org.gpx2web.binding.gpx.TrkType;
import org.gpx2web.binding.gpx.TrksegType;
import org.gpx2web.binding.gpx.WptType;

public class TrackFilter {

	private TrkType track;
	private TrkType filteredTrack;

	public TrackFilter(TrkType trkType) {
		this.track = trkType;
		this.filteredTrack = new TrkType();
	}

	public TrkType getFilteredTrack() {
		return filteredTrack;
	}

	public void processTrack() {
		List<TrksegType> parts = track.getTrkseg();
		for (TrksegType part : parts) {
			TrksegType filteredPart = processPart(part);
			filteredTrack.getTrkseg().add(filteredPart);
		}
	}

	private TrksegType processPart(TrksegType part) {
		TrksegType filteredPart = new TrksegType();
		List<WptType> points = part.getTrkpt();
		for (WptType wptType : points) {
//			filteredPart
		}

		return filteredPart;
	}

}
