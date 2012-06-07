package org.glandais.gpx.gearschooser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vaadin.Application;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class GearsApplication extends Application {

	private static final long serialVersionUID = -5836985510333662343L;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

	@Override
	public void init() {
		setTheme("gears");

		Window window = new Window("Gears chooser");
		setMainWindow(window);

		TabSheet tabsheet = new TabSheet();
		tabsheet.setSizeFull();
		window.setContent(tabsheet);

		List<GearsTab> tabs = new ArrayList<GearsTab>();
		tabs.add(new GPXAnalyzer());
		tabs.add(new SpeedComputer());

		for (GearsTab gearsTab : tabs) {
			gearsTab.setApplication(this);
			tabsheet.addTab(gearsTab.getComponent(), gearsTab.getCaption());
		}
	}

	void createWindow(Table table, final String tableName, String windowTitle) {
		Window newWindow = new Window(windowTitle);
		table.setSizeFull();

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();

		final Table tableExported = table;

		Button excelExportButton = new Button("Export to Excel");
		excelExportButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -7431482683590698194L;

			public void buttonClick(final ClickEvent event) {
				ExcelExport excelExport = new ExcelExport(tableExported);
				excelExport.excludeCollapsedColumns();
				excelExport.setExportFileName(tableName + "-" + DATE_FORMAT.format(new Date()) + ".xls");
				excelExport.export();
			}
		});

		verticalLayout.addComponent(excelExportButton);
		verticalLayout.addComponent(table);
		verticalLayout.setExpandRatio(table, 1.0f);

		newWindow.setContent(verticalLayout);

		newWindow.setWidth("800px");
		getMainWindow().addWindow(newWindow);
	}

}
