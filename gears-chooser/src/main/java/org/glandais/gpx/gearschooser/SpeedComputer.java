package org.glandais.gpx.gearschooser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.glandais.gpx.braquet.db.Cassette;
import org.glandais.gpx.braquet.db.Pedalier;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Select;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class SpeedComputer implements GearsTab {

	private GearsApplication application;
	private Table table;
	private Slider rpmModifier;
	private Select selectPedalier;
	private Select selectCassette;

	public String getCaption() {
		return "Speed computer";
	}

	public Component getComponent() {
		VerticalLayout verticalLayout = new VerticalLayout();

		List<Pedalier> pedaliers = Arrays.asList(Pedalier.values());
		selectPedalier = new Select("Crankset", pedaliers);
		selectPedalier.setItemCaptionMode(Select.ITEM_CAPTION_MODE_ID);
		selectPedalier.setValue(pedaliers.get(0));
		selectPedalier.setWidth("250px");

		List<Cassette> cassettes = Arrays.asList(Cassette.values());
		selectCassette = new Select("Cogset", cassettes);
		selectCassette.setItemCaptionMode(Select.ITEM_CAPTION_MODE_ID);
		selectCassette.setValue(cassettes.get(0));
		selectCassette.setWidth("400px");

		rpmModifier = new Slider("Rpm", 20, 140);
		rpmModifier.setWidth("300px");
		try {
			rpmModifier.setValue(80.0);
		} catch (ValueOutOfBoundsException e) {
			e.printStackTrace();
		}

		table = new Table();
		verticalLayout.addComponent(selectPedalier);
		verticalLayout.addComponent(selectCassette);
		verticalLayout.addComponent(rpmModifier);
		Button refreshButton = new Button("Refresh");
		refreshButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 9050167944752673838L;

			public void buttonClick(final ClickEvent event) {
				fillTable();
			}
		});
		table.setWidth("800px");
		table.setHeight("300px");
		verticalLayout.addComponent(refreshButton);
		verticalLayout.addComponent(table);
		verticalLayout.setExpandRatio(table, 1.0f);
		verticalLayout.setSizeFull();

		fillTable();

		return verticalLayout;
	}

	private void fillTable() {
		Collection<?> containerPropertyIds = new ArrayList(table.getContainerPropertyIds());
		for (Object object : containerPropertyIds) {
			table.removeContainerProperty(object);
		}
		Collection<?> itemIds = new ArrayList(table.getItemIds());
		for (Object itemId : itemIds) {
			table.removeItem(itemId);
		}

		Cassette cassette = (Cassette) selectCassette.getValue();
		Pedalier pedalier = (Pedalier) selectPedalier.getValue();
		Double rpm = (Double) rpmModifier.getValue();

		if (cassette != null && pedalier != null) {

			table.addContainerProperty("Ring", Long.class, null);
			for (int pignon : cassette.pignons) {
				table.addContainerProperty(Integer.toString(pignon), Double.class, null);
			}

			int i = 0;
			for (int plateau : pedalier.plateaux) {

				Object[] values = new Object[cassette.pignons.length + 1];
				values[0] = new Integer(plateau);
				for (int j = 0; j < cassette.pignons.length; j++) {
					int pignon = cassette.pignons[j];
					values[j + 1] = getSpeed(rpm, plateau, pignon);
				}
				table.addItem(values, new Integer(i));
				i++;
			}

		}
	}

	private Double getSpeed(double rpm, int plateau, int pignon) {
		double speed = 0.0;
		speed = (1.0 * rpm) * ((1.0 * plateau) / (1.0 * pignon)) * 60.0 * 0.0007 * Math.PI;
		return Math.round(speed * 10.0) / 10.0;
	}

	public void setApplication(GearsApplication application) {
		this.application = application;
	}

}
