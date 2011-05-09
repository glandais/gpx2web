var points = new Array();
var strokeColor = '#f00';
var minlon;
var maxlon;
var minlat;
var maxlat;

function clearMap() {
	myFirstMap.removeAllLayers();
	minlon = 180;
	maxlon = -180;
	minlat = 90;
	maxlat = -90;
}

function startPath() {
	points = new Array();
}

function addPathPoint(rlon, rlat) {
	points.push( {
		lon : rlon,
		lat : rlat
	});
	minlon = Math.min(minlon, rlon);
	maxlon = Math.max(maxlon, rlon);
	minlat = Math.min(minlat, rlat);
	maxlat = Math.max(maxlat, rlat);
}

function endPath() {
	polyline = new ViaMichelin.Api.Map.PolyLine( {
		coords : points,
		strokeColor : strokeColor,
		strokeOpacity : 0.6,
		strokeWeight : 8
	});
	myFirstMap.addLayer(polyline);
	if (strokeColor == '#f00') {
		strokeColor = '#c30';
	} else {
		strokeColor = '#f00';
	}
}

function addWaypoint(rlon, rlat, caption) {
	var marker = new ViaMichelin.Api.Map.Marker(
			{
				coords : {
					lon : rlon,
					lat : rlat
				},
				htm : caption,
				icon : {
					url : 'http://maps.gstatic.com/intl/fr_fr/mapfiles/ms/micons/red-pushpin.png',
					offsetX : -16,
					offsetY : -16
				},
				autoOpen : true
			});
	myFirstMap.addLayer(marker);
}

function zoomOut() {
	myFirstMap.mapZoomOut();
}

function zoomLevel() {
	return myFirstMap.getZoomLevel();
}

function getTiles() {
	var tiles = new Array();

	layer = myFirstMap.tileLayers[myFirstMap.masterTile];

	tiles.push(layer.configuration.tileWidth);

	if (layer.HTMLElement && layer.HTMLElement.parentNode) {
		var c = -parseInt(layer.HTMLElement.parentNode.style.top)
				- parseInt(layer.HTMLElement.style.top);
		var d = -parseInt(layer.HTMLElement.parentNode.style.left)
				- parseInt(layer.HTMLElement.style.left);
		var a = c + parseInt(layer.HTMLElement.parentNode.style.height);
		var b = d + parseInt(layer.HTMLElement.parentNode.style.width);
		var h = Math.floor(c / layer.configuration.tileHeight);
		var l = Math.floor(d / layer.configuration.tileWidth);
		var n = Math.ceil(b / layer.configuration.tileWidth);
		var m = Math.ceil(a / layer.configuration.tileHeight);

		var minAbsoluteX = l - 5;
		var minAbsoluteY = h - 5;
		var maxTilesInWidth = n - minAbsoluteX + 2 * 5;
		var maxTilesInHeight = m - minAbsoluteY + 2 * 5;

		if (maxlon - maxlon < 350) {

			var minCoords = {
				lon : minlon,
				lat : maxlat
			};
			var minPixCoords = myFirstMap.convertGeoToPixel(minCoords);
			var maxCoords = {
				lon : maxlon,
				lat : minlat
			};
			var maxPixCoords = myFirstMap.convertGeoToPixel(maxCoords);

			minAbsoluteX = Math.floor((minPixCoords.x + d)
					/ layer.configuration.tileWidth);
			minAbsoluteY = Math.floor((minPixCoords.y + c)
					/ layer.configuration.tileHeight);

			var maxAbsoluteX = Math.ceil((maxPixCoords.x + d)
					/ layer.configuration.tileWidth);
			var maxAbsoluteY = Math.ceil((maxPixCoords.y + c)
					/ layer.configuration.tileHeight);

			maxTilesInWidth = maxAbsoluteX - minAbsoluteX;
			maxTilesInHeight = maxAbsoluteY - minAbsoluteY;
			// minAbsoluteX = Math.floor(minCoords.x + c);
			// minAbsoluteY = Math.floor(minCoords.x + d);
			
			minAbsoluteX = minAbsoluteX - 1;
			minAbsoluteY = minAbsoluteY - 1;
			
			maxTilesInWidth = maxTilesInWidth + 2;
			maxTilesInHeight = maxTilesInHeight + 2;
		}

		var g = 0;
		while (g < maxTilesInWidth) {
			var e = 0;
			while (e < maxTilesInHeight) {
				var x = minAbsoluteX + g;
				var y = minAbsoluteY + e;
				tiles.push(getTile(x, y, layer));
				e++;
			}
			g++
		}
	}

	var result = JSON.stringify(tiles);
	return result;
}

function getTile(x, y, layer) {
	var tile = new Array();

	var url = layer.encodeMapDirectURL(x
			+ parseInt(layer.configuration.xPixelCenter
					/ layer.configuration.tileWidth), y
			+ parseInt(layer.configuration.yPixelCenter
					/ layer.configuration.tileHeight));
	tile.push(url);

	var c = parseInt(layer.HTMLElement.parentNode.style.top)
			+ parseInt(layer.HTMLElement.style.top);
	var d = parseInt(layer.HTMLElement.parentNode.style.left)
			+ parseInt(layer.HTMLElement.style.left);

	var px = x * layer.configuration.tileWidth + d;
	var py = y * layer.configuration.tileHeight + c;

	var fBG = new ViaMichelin.Api.Map.Point(px, py
			+ layer.configuration.tileHeight);
	fBG = myFirstMap.convertPixelToGeo(fBG);

	tile.push(fBG.lon);
	tile.push(fBG.lat);

	var fBD = new ViaMichelin.Api.Map.Point(px + layer.configuration.tileWidth,
			py + layer.configuration.tileHeight);
	fBD = myFirstMap.convertPixelToGeo(fBD);

	tile.push(fBD.lon);
	tile.push(fBD.lat);

	var fHD = new ViaMichelin.Api.Map.Point(px + layer.configuration.tileWidth,
			py);
	fHD = myFirstMap.convertPixelToGeo(fHD);

	tile.push(fHD.lon);
	tile.push(fHD.lat);

	var fHG = new ViaMichelin.Api.Map.Point(px, py);
	fHG = myFirstMap.convertPixelToGeo(fHG);

	tile.push(fHG.lon);
	tile.push(fHG.lat);

	return tile;
}

function addTile(x, y, layer) {
	var result = "<GroundOverlay>\n" + "<name>Tile " + x + " - " + y
			+ "</name>\n" + "<Icon>\n" + "  <href>";

	var url = layer.encodeMapDirectURL(x
			+ parseInt(layer.configuration.xPixelCenter
					/ layer.configuration.tileWidth), y
			+ parseInt(layer.configuration.yPixelCenter
					/ layer.configuration.tileHeight));
	result = result + url;

	result = result + "</href>\n" + "  <viewBoundScale>1</viewBoundScale>\n"
			+ "</Icon>\n" + "<gx:LatLonQuad>\n" + "  <coordinates>\n    ";

	var c = parseInt(layer.HTMLElement.parentNode.style.top)
			+ parseInt(layer.HTMLElement.style.top);
	var d = parseInt(layer.HTMLElement.parentNode.style.left)
			+ parseInt(layer.HTMLElement.style.left);

	var px = x * layer.configuration.tileWidth + d;
	var py = y * layer.configuration.tileHeight + c;

	var fBG = new ViaMichelin.Api.Map.Point(px, py
			+ layer.configuration.tileHeight);
	fBG = myFirstMap.convertPixelToGeo(fBG);
	result = result + fBG.lon + "," + fBG.lat + " ";

	var fBD = new ViaMichelin.Api.Map.Point(px + layer.configuration.tileWidth,
			py + layer.configuration.tileHeight);
	fBD = myFirstMap.convertPixelToGeo(fBD);
	result = result + fBD.lon + "," + fBD.lat + " ";

	var fHD = new ViaMichelin.Api.Map.Point(px + layer.configuration.tileWidth,
			py);
	fHD = myFirstMap.convertPixelToGeo(fHD);
	result = result + fHD.lon + "," + fHD.lat + " ";

	var fHG = new ViaMichelin.Api.Map.Point(px, py);
	fHG = myFirstMap.convertPixelToGeo(fHG);
	result = result + fHG.lon + "," + fHG.lat;

	var minLod = layer.configuration.tileWidth / 2;
	var maxLod = layer.configuration.tileWidth * 4;

	var north = Math.max(fHD.lat, fHG.lat);
	var east = Math.max(fHD.lon, fBD.lon);

	var south = Math.min(fBD.lat, fBG.lat);
	var west = Math.min(fBG.lon, fHG.lon);

	result = result + "\n  </coordinates>\n" + "</gx:LatLonQuad>\n"
			+ "<Region>\n" + "<Lod>\n" + "  <minLodPixels>" + minLod
			+ "</minLodPixels><maxLodPixels>" + maxLod + "</maxLodPixels>\n"
			+ "</Lod>\n" + "<LatLonAltBox>\n" + "  <north>" + north
			+ "</north>\n" + "  <south>" + south + "</south>\n" + "  <east>"
			+ east + "</east>\n" + "  <west>" + west + "</west>\n"
			+ "</LatLonAltBox>\n" + "</Region>\n" + "</GroundOverlay>";

	return result;
}

function getKml() {

	var kml = '<?xml version="1.0" encoding="UTF-8"?>\n'
			+ '<kml xmlns="http://www.opengis.net/kml/2.2"\n'
			+ 'xmlns:gx="http://www.google.com/kml/ext/2.2">\n' + '<Folder>\n'
			+ '<name>Michelin</name>\n';

	layer = myFirstMap.tileLayers[myFirstMap.masterTile];

	if (layer.HTMLElement && layer.HTMLElement.parentNode) {
		var c = -parseInt(layer.HTMLElement.parentNode.style.top)
				- parseInt(layer.HTMLElement.style.top);
		var d = -parseInt(layer.HTMLElement.parentNode.style.left)
				- parseInt(layer.HTMLElement.style.left);
		var a = c + parseInt(layer.HTMLElement.parentNode.style.height);
		var b = d + parseInt(layer.HTMLElement.parentNode.style.width);
		var h = Math.floor(c / layer.configuration.tileHeight);
		var l = Math.floor(d / layer.configuration.tileWidth);
		var n = Math.ceil(b / layer.configuration.tileWidth);
		var m = Math.ceil(a / layer.configuration.tileHeight);
		var dtile = 8;
		var minAbsoluteX = l - dtile;
		var minAbsoluteY = h - dtile;
		var maxTilesInWidth = n - minAbsoluteX + 2 * dtile;
		var maxTilesInHeight = m - minAbsoluteY + 2 * dtile;

		var g = 0;
		while (g < maxTilesInWidth) {
			var e = 0;
			while (e < maxTilesInHeight) {
				var x = minAbsoluteX + g;
				var y = minAbsoluteY + e;
				kml = kml + addTile(x, y, layer) + "\n";
				e++
			}
			g++
		}
	}

	kml = kml + '</Folder>\n</kml>';

	document.getElementById("kml").value = kml;

}