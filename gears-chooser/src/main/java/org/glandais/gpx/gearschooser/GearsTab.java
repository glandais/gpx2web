package org.glandais.gpx.gearschooser;

import com.vaadin.ui.Component;

public interface GearsTab {

	String getCaption();

	Component getComponent();

	void setApplication(GearsApplication application);

}
