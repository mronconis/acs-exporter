package com.redhat.acs.exporter.decorator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import com.redhat.acs.exporter.Exporter;
import com.redhat.acs.exporter.parser.NetGraphParser.NetGraphData;

import org.apache.commons.cli.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

public class NetGraphExcelDecorator extends ExporterDecorator {

    private final static Logger log = LogManager.getLogger(NetGraphExcelDecorator.class);

    public NetGraphExcelDecorator(Exporter exporter) {
        super(exporter);
    }

    public Optional<Void> export(CommandLine cmd, JSONObject acsCfg, JSONObject clusterCfg) throws Exception {
        Optional<Set<NetGraphData>> netGraphDatas = super.export(cmd, acsCfg, clusterCfg);
        String output = getOutputFile(cmd, clusterCfg);
        ExcelWriter excelExporter = new ExcelWriter(output);
        exportExcel(excelExporter, netGraphDatas.get());
        log.info("Output file: {}", output);
        return Optional.empty();
    }

    private void exportExcel(ExcelWriter excelExporter, Set<NetGraphData> netGraphDatas) {
        excelExporter.exportHeaderData();
        String namespace = null;
        for (NetGraphData netGraphData : netGraphDatas) {
            if (namespace != null && !namespace.equals(netGraphData.getNamespace())) {
                excelExporter.getSheetData()[ExcelWriter.NETWORK_FLOW_SHEET].markNamespaceStart();
            }
            excelExporter.exportDeploymentData(netGraphData);
            namespace = netGraphData.getNamespace();
        }
        excelExporter.finalizeExport();
    }

    private String getOutputFile(CommandLine cmd, JSONObject clusterCfg) {
        DateFormat df = new SimpleDateFormat("YYYY-MM-dd");
        StringBuilder builder = new StringBuilder();
        builder.append(cmd.getOptionValue("output"))
            .append(File.separator)
            .append("rhacs")
            .append("-")
            .append(clusterCfg.getString("name"))
            .append("-")
            .append(clusterCfg.getString("env"))
            .append("-")
            .append(df.format(new Date()))
            .append(".xlsx");
        return builder.toString();
    }

    private static class SheetData {
        private Sheet sheet;
        private int[] columnsWidth;
        private int currentRowIndex;

        private boolean namespaceStart;

        public SheetData(Sheet sheet, int[] columnsWidth, int currentRowIndex) {
            this.sheet = sheet;
            this.columnsWidth = columnsWidth;
            this.currentRowIndex = currentRowIndex;
            this.namespaceStart = false;
        }

        public SheetData(Sheet sheet, int[] columnsWidth) {
            this(sheet, columnsWidth, -1);
        }

        public int nextRowIndex() {
            if (currentRowIndex < 0)
                currentRowIndex = 0;
            else {
                currentRowIndex++;
            }
            return currentRowIndex;
        }

        public void markNamespaceStart() {
            namespaceStart = true;
        }

        public void unmarkNamespaceStart() {
            namespaceStart = false;
        }

        public boolean isNamespaceStart() {
            return namespaceStart;
        }
    }

    private static class ExcelWriter {

        private static final int BASE_COLUMN_WIDTH = 5000;
        private static final int S_COLUMN_WIDTH = BASE_COLUMN_WIDTH;
        private static final int SM_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH * 1.5);
        private static final int M_COLUMN_WIDTH = BASE_COLUMN_WIDTH * 2;
        private static final int ML_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH * 2.5);
        private static final int L_COLUMN_WIDTH = BASE_COLUMN_WIDTH * 3;
        private static final int LXL_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH * 3.5);
        private static final int XL_COLUMN_WIDTH = BASE_COLUMN_WIDTH * 4;
        private static final int XLXXL_COLUMN_WIDTH = (int) Math.round(BASE_COLUMN_WIDTH * 4.5);
        private static final int XXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH * 5;

        private static final int XXXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH * 6;
        private static final int XXXXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH * 7;
        private static final int XXXXXL_COLUMN_WIDTH = BASE_COLUMN_WIDTH * 8;

        private static final int NETWORK_FLOW_SHEET = 0;

        private SheetData[] sheetData = new SheetData[] { null, null, null };

        private Workbook workbook;

        private CellStyle headerStyle;
        private CellStyle deploymentRowStyle;
        private CellStyle deploymentRowStyleNSStart;

        private String outputFullFilepath;

        protected final String[] workloadsHeaderFields = { "Namespace", "Kind", "Name", "Flow Direction", "Namespace",
                "Name", "Port", };

        public ExcelWriter(String outputFullFilepath) {
            this.outputFullFilepath = outputFullFilepath;
            workbook = new XSSFWorkbook();

            sheetData[NETWORK_FLOW_SHEET] = new SheetData(workbook.createSheet("Active network flows"), new int[] {
                    SM_COLUMN_WIDTH, // Namespace
                    S_COLUMN_WIDTH, // Kind
                    ML_COLUMN_WIDTH, // Name
                    S_COLUMN_WIDTH, // Flow direction
                    SM_COLUMN_WIDTH, // Namespace
                    ML_COLUMN_WIDTH, // Name
                    S_COLUMN_WIDTH // Port
            });

            headerStyle = workbook.createCellStyle();
            headerStyle.setLocked(true);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont fontHeader = ((XSSFWorkbook) workbook).createFont();
            fontHeader.setFontName("Arial");
            fontHeader.setColor(IndexedColors.WHITE.getIndex());
            fontHeader.setFontHeightInPoints((short) 12);
            fontHeader.setBold(true);
            headerStyle.setFont(fontHeader);
            headerStyle.setVerticalAlignment(VerticalAlignment.TOP);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            XSSFFont fontNormal = ((XSSFWorkbook) workbook).createFont();
            fontNormal.setFontName("Arial");
            fontNormal.setColor(IndexedColors.WHITE.getIndex());
            fontNormal.setFontHeightInPoints((short) 12);

            XSSFFont fontNormalDark = ((XSSFWorkbook) workbook).createFont();
            fontNormalDark.setFontName("Arial");
            fontNormalDark.setColor(IndexedColors.BLACK.getIndex());
            fontNormalDark.setFontHeightInPoints((short) 12);

            deploymentRowStyle = workbook.createCellStyle();
            deploymentRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            deploymentRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            deploymentRowStyle.setFont(fontNormalDark);
            deploymentRowStyle.setVerticalAlignment(VerticalAlignment.TOP);
            deploymentRowStyle.setWrapText(true);
            deploymentRowStyle.setBorderBottom(BorderStyle.THIN);

            deploymentRowStyleNSStart = workbook.createCellStyle();
            deploymentRowStyleNSStart.cloneStyleFrom(deploymentRowStyle);
            deploymentRowStyleNSStart.setBorderTop(BorderStyle.THICK);
        }

        private Cell cellString(Row row, int column, String value, CellStyle style) {
            return cell(row, column, value, CellType.STRING, style);
        }

        private Cell cellNumeric(Row row, int column, int value, CellStyle style) {
            return cell(row, column, "" + value, CellType.NUMERIC, style);
        }

        private Cell cell(Row row, int column, String value, CellType type, CellStyle style) {
            Cell cell = row.createCell(column, type);
            cell.setCellValue(value);
            cell.setCellStyle(style);
            return cell;
        }

        private void addRowPadding(Row row, int startColumn, int endColumn, CellStyle style) {
            for (int c = startColumn; c <= endColumn; c++) {
                cellString(row, c, "", style);
            }
        }

        public SheetData[] getSheetData() {
            return this.sheetData;
        }

        public void exportHeaderData() {
            Row headerRow = sheetData[NETWORK_FLOW_SHEET].sheet.createRow(sheetData[NETWORK_FLOW_SHEET].nextRowIndex());

            int headerColumnIndex = 0;
            for (String headerColumn : workloadsHeaderFields) {
                sheetData[NETWORK_FLOW_SHEET].sheet.setColumnWidth(headerColumnIndex,
                        sheetData[NETWORK_FLOW_SHEET].columnsWidth[headerColumnIndex]);
                cell(headerRow, headerColumnIndex, headerColumn, CellType.STRING, headerStyle);
                headerColumnIndex++;
            }
            sheetData[NETWORK_FLOW_SHEET].sheet.createFreezePane(0, 1);
        }

        public void exportDeploymentData(NetGraphData netGraphData) {

            CellStyle style = deploymentRowStyle;
            if (sheetData[NETWORK_FLOW_SHEET].isNamespaceStart()) {
                style = deploymentRowStyleNSStart;
                sheetData[NETWORK_FLOW_SHEET].unmarkNamespaceStart();
            }

            Row row = sheetData[NETWORK_FLOW_SHEET].sheet.createRow(sheetData[NETWORK_FLOW_SHEET].nextRowIndex());
            int columnIndex = 0;
            cellString(row, columnIndex++, netGraphData.getNamespace(), style);
            cellString(row, columnIndex++, netGraphData.getKind(), style);
            cellString(row, columnIndex++, netGraphData.getName(), style);
            cellString(row, columnIndex++, netGraphData.getFlowDirection(), style);
            cellString(row, columnIndex++, netGraphData.getENamespace(), style);
            cellString(row, columnIndex++, netGraphData.getEName(), style);
            cellString(row, columnIndex++, netGraphData.getPort(), style);

            addRowPadding(row, columnIndex, workloadsHeaderFields.length - 1, style);
        }

        public void finalizeExport() throws RuntimeException {
            try (FileOutputStream outputStream = new FileOutputStream(outputFullFilepath)) {
                workbook.write(outputStream);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("file " + outputFullFilepath + " not found", e);
            } catch (IOException e) {
                throw new RuntimeException("failed writing data", e);
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    throw new RuntimeException("failed closing file " + outputFullFilepath, e);
                }
            }
        }
    }
}
