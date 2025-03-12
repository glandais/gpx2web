package io.github.glandais.gpx.io.write.tabular;

import io.github.glandais.gpx.data.GPXPath;
import jakarta.inject.Singleton;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Singleton
public class XLSXFileWriter extends TabularFileWriter {

    @Override
    public void writeGPXPath(GPXPath path, File file) throws IOException {
        /*
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("data");

            CreationHelper createHelper = wb.getCreationHelper();
            DataFormat dataFormat = createHelper.createDataFormat();
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd hh:mm:ss"));

            Map<String, CellStyle> cellStyles = new HashMap<>();
            Map<ValueKeyKind, String> columnsByValueKey = new HashMap<>();

            AtomicReference<Row> row = new AtomicReference<>();
            write(path,
                    columns -> {
                        Row headerRow = sheet.createRow(0);
                        headerRow.setHeight((short) 566);
                        int j = 0;
                        for (Map.Entry<ValueKey, Set<ValueKind>> column : columns.entrySet()) {
                            for (ValueKind valueKind : column.getValue()) {
                                headerRow.createCell(j++).setCellValue(column.getKey() + "\n" + valueKind);
                                columnsByValueKey.put(new ValueKeyKind(column.getKey(), valueKind), CellReference.convertNumToColString(j - 1));
                            }
                        }
                    },
                    (i, values) -> row.set(sheet.createRow(i + 1)),
                    (i, j, values, key, value) -> {
                        Cell cell = row.get().createCell(j);
                        if (value != null) {
                            StorageUnit unit = value.unit();
                            Object value1 = value.value();
                            if (value1 instanceof Instant) {
                                cell.setCellValue(Date.from((Instant) value1));
                                cell.setCellStyle(cellStyle);
                            } else if (unit instanceof HumanUnit && (value1 instanceof Number)) {
                                cell.setCellValue(((HumanUnit) unit).getHumanValue(((Number) value1).doubleValue()));
                                cell.setCellStyle(getCellStyle(cellStyles, wb, dataFormat, unit.getFormat()));
                            } else if (value1 instanceof Number) {
                                cell.setCellValue(((Number) value1).doubleValue());
                                cell.setCellStyle(getCellStyle(cellStyles, wb, dataFormat, unit.getFormat()));
                            } else if (value1 instanceof Formul) {
                                String excelFormula = ((Formul) value1).getFormula();
                                for (ValueKeyKind input : ((Formul) value1).getInputs()) {
                                    String inputCell = columnsByValueKey.get(input) + (row.get().getRowNum() + 1);

                                    Value<?, ?> refVal = values.get(input.key(), input.kind());
                                    if (refVal != null) {
                                        String c = refVal.unit().getFormulaPartHumanToSI();
                                        if (c != null) {
                                            inputCell = inputCell + c;
                                        }
                                    }

                                    excelFormula = excelFormula.replaceAll(input.key().name(), inputCell);
                                }

                                String formulaPartSIToHuman = ((Formul) value1).getUnit().getFormulaPartSIToHuman();
                                if (formulaPartSIToHuman != null) {
                                    excelFormula = "(" + excelFormula + ")" + formulaPartSIToHuman;
                                }

                                cell.setCellFormula(excelFormula);
                            } else {
                                cell.setCellValue(unit.formatHuman(value1));
                            }
                        }
                    }
            );

            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
            sheet.createFreezePane(0, 1, 0, 1);
            try (OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
        }
         */
    }

    private CellStyle getCellStyle(Map<String, CellStyle> cellStyles, Workbook wb, DataFormat dataFormat, String format) {
        return cellStyles.computeIfAbsent(format, f -> {
            CellStyle style = wb.createCellStyle();
            style.setDataFormat(dataFormat.getFormat(format));
            return style;
        });
    }
}
