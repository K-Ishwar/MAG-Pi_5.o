package com.magpi.util;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
//import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.properties.UnitValue;
import com.magpi.model.TestPart;
import com.magpi.model.TestSession;
import com.magpi.ui.table.PersistentColorTableModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for exporting test data to PDF format
 */
public class PdfExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Exports the current test session to a PDF file
     * @param session The test session to export
     * @param headshotTable The headshot table to include in the PDF
     * @param coilshotTable The coilshot table to include in the PDF
     * @param parentComponent The parent component for dialog display
     */
    public static void exportToPdf(TestSession session, JTable headshotTable, JTable coilshotTable, Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save PDF Report");
        int userSelection = fileChooser.showSaveDialog(parentComponent);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
        }

        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
            // Create PDF document
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            document.add(new Paragraph("Magnetic Particle Inspection Report")
                    .setFontSize(16)
                    .setBold());

            document.add(new Paragraph("\n"));

            // Add metadata section
            document.add(new Paragraph("Report Details")
                    .setFontSize(14)
                    .setBold());

            // Create a table for metadata
            Table metadataTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

            addMetadataRow(metadataTable, "Operator Name:", session.getOperatorName());
            addMetadataRow(metadataTable, "Part Description:", session.getPartDescription());
            addMetadataRow(metadataTable, "Machine ID:", session.getMachineId());
            addMetadataRow(metadataTable, "Company Name:", session.getCompanyName());
            addMetadataRow(metadataTable, "Supervisor ID:", session.getSupervisorId());
            addMetadataRow(metadataTable, "Date:", session.getStartTime().format(DATE_FORMATTER));
            addMetadataRow(metadataTable, "Start Time:", session.getStartTime().format(TIME_FORMATTER));

            if (session.getEndTime() != null) {
                addMetadataRow(metadataTable, "End Time:", session.getEndTime().format(TIME_FORMATTER));
            }

            addMetadataRow(metadataTable, "Parameters:",
                    "Headshot (" + session.getHeadShotThreshold() + "), " +
                    "Coilshot (" + session.getCoilShotThreshold() + ")");

            addMetadataRow(metadataTable, "Total Parts Tested:", String.valueOf(session.getTotalPartsCount()));
            addMetadataRow(metadataTable, "Accepted Parts:", String.valueOf(session.getAcceptedPartsCount()));
            addMetadataRow(metadataTable, "Rejected Parts:", String.valueOf(session.getRejectedPartsCount()));

            document.add(metadataTable);
            document.add(new Paragraph("\n"));

            // Headshot Table
            if (headshotTable.getRowCount() > 0) {
                document.add(new Paragraph("Headshot Measurements")
                        .setFontSize(14)
                        .setBold());
                Table headTable = createPdfTableWithColors(headshotTable);
                document.add(headTable);
                document.add(new Paragraph("\n"));
            }

            // Coilshot Table
            if (coilshotTable.getRowCount() > 0) {
                document.add(new Paragraph("Coilshot Measurements")
                        .setFontSize(14)
                        .setBold());
                Table coilTable = createPdfTableWithColors(coilshotTable);
                document.add(coilTable);
            }

            document.close();

            JOptionPane.showMessageDialog(parentComponent,
                    "Report exported successfully to " + selectedFile.getName(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent,
                    "Error exporting to PDF: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void addMetadataRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)).setBold());
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "")));
    }

    /**
     * Creates a PDF table with color highlighting from a JTable
     * @param table The JTable to convert
     * @return A PDF Table representation
     */
    private static Table createPdfTableWithColors(JTable table) {
        javax.swing.table.TableModel model = table.getModel();
        Table pdfTable = new Table(UnitValue.createPercentArray(model.getColumnCount())).useAllAvailableWidth();

        // Add headers
        for (int col = 0; col < model.getColumnCount(); col++) {
            pdfTable.addHeaderCell(new Cell()
                    .add(new Paragraph(model.getColumnName(col)))
                    .setBold());
        }

        // Add data with colors if available
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                Object value = model.getValueAt(row, col);
                Cell pdfCell = new Cell();
                pdfCell.add(new Paragraph(value != null ? value.toString() : ""));

                // Get cell color from custom renderer if available
                if (model instanceof PersistentColorTableModel) {
                    PersistentColorTableModel colorModel = (PersistentColorTableModel) model;
                    Color cellColor = colorModel.getCellColor(row, col);

                    if (cellColor != null) {
                        // Convert AWT Color to iText Color
                        pdfCell.setBackgroundColor(new DeviceRgb(
                                cellColor.getRed(),
                                cellColor.getGreen(),
                                cellColor.getBlue()
                        ));
                    }
                }

                pdfTable.addCell(pdfCell);
            }
        }

        return pdfTable;
    }
}