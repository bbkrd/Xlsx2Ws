/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.dto.ReportItem;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.filesystems.FileChooserBuilder;

/**
 *
 * @author s4504tw
 */
public final class Report extends JDialog {

    private final static DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("dd. MMMM yyyy HH:mm:ss");
    private final static String NEW_LINE = System.getProperty("line.separator");
    private final List<ReportItem> items;

    public Report(List<ReportItem> input) {
        this.items = new ArrayList<>(input);
        items.sort((o1, o2) -> {
            if (o1.isRegressor() == o2.isRegressor()) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getDocumentName(), o2.getDocumentName());
            } else {
                return o1.isRegressor() ? 1 : -1;
            }
        });
        setModalityType(DEFAULT_MODALITY_TYPE);
        setTitle("Report");

        DefaultListModel<ReportItem> dataModel = new DefaultListModel<>();
        items.forEach(dataModel::addElement);
        JTextArea reportText = new JTextArea();
        reportText.setEditable(false);

        JScrollPane textScrollPane = new JScrollPane(reportText);
        textScrollPane.setPreferredSize(new Dimension(550, 170));

        JList itemList = new JList(dataModel);
        itemList.setCellRenderer(new ReportItemCellRenderer());
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener((e) -> {
            int selectedIndex = itemList.getSelectedIndex();
            if (selectedIndex >= 0) {
                reportText.setText(items.get(selectedIndex).generateReport(false));
            } else {
                reportText.setText(null);
            }
        });
        Dimension buttonDimension = new Dimension(100, 30);

        JButton saveReport = new JButton("Save Report");
        saveReport.setPreferredSize(buttonDimension);
        saveReport.addActionListener(this::generateReport);

        JButton close = new JButton("Close");
        close.setPreferredSize(buttonDimension);
        close.addActionListener((e) -> {
            this.dispose();
        });
        JScrollPane jScrollPane = new JScrollPane(itemList);
        jScrollPane.setPreferredSize(new Dimension(150, 200));

        JSplitPane buttonSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, saveReport, close);
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScrollPane, buttonSplit);
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jScrollPane, verticalSplit);

        this.setLayout(new BorderLayout());
        this.add(horizontalSplit, BorderLayout.CENTER);
        pack();

        buttonSplit.setEnabled(false);
        buttonSplit.setDividerLocation(0.5);
        buttonSplit.setDividerSize(0);
        buttonSplit.setResizeWeight(0.5);

        verticalSplit.setEnabled(false);
        verticalSplit.setDividerSize(0);
        verticalSplit.setResizeWeight(1);

        setLocationRelativeTo(null);
    }

    private void generateReport(ActionEvent e) {
        FileChooserBuilder fileChooserBuilder = new FileChooserBuilder(this.getClass());
        fileChooserBuilder.setFileFilter(new FileNameExtensionFilter("Log files", "log"));
        fileChooserBuilder.setAcceptAllFileFilterUsed(false);
        File file = fileChooserBuilder.showSaveDialog();
        if (file == null) {
            return;
        }
        if (!file.getAbsolutePath().endsWith(".log")) {
            file = new File(file.getAbsolutePath() + ".log");
        }
        try (FileWriter writer = new FileWriter(file)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Protocol ").append(TIMESTAMP_FORMAT.format(System.currentTimeMillis()))
                    .append('\t').append(System.getProperty("user.name")).append(NEW_LINE);

            String lastDocument = "";
            for (ReportItem item : items) {
                String document = item.getDocumentName();
                if (!document.equalsIgnoreCase(lastDocument)) {
                    lastDocument = document;
                    sb.append(NEW_LINE);
                    if (item.isRegressor()) {
                        sb.append("***TsVariables ");
                    } else {
                        sb.append("***Multidocument ");
                    }
                    sb.append(document).append("***").append(NEW_LINE);
                }
                sb.append(item.getName()).append(":").append(NEW_LINE)
                        .append(item.generateReport(true)).append(NEW_LINE);
            }
            writer.append(sb.toString());
            writer.flush();
            JOptionPane.showMessageDialog(this, "Successfully saved to " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving to " + file.getAbsolutePath() + "! " + ex.getMessage());
        }

    }

    private static class ReportItemCellRenderer extends JLabel implements ListCellRenderer<ReportItem> {

        @Override
        public Component getListCellRendererComponent(JList list, ReportItem value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                int highestLevel = value.getHighestLevel();
                if (highestLevel >= Level.SEVERE.intValue()) {
                    setBackground(RED);
                } else if (highestLevel >= Level.WARNING.intValue()) {
                    setBackground(ORANGE);
                } else if (highestLevel >= Level.INFO.intValue()) {
                    setBackground(YELLOW);
                } else {
                    setBackground(GREEN);
                }
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;

        }
        private static final int ALPHA = 123;
        private static final Color GREEN = new Color(0, 255, 0, ALPHA);
        private static final Color RED = new Color(255, 0, 0, ALPHA);
        private static final Color ORANGE = new Color(255, 200, 0, ALPHA);
        private static final Color YELLOW = new Color(255, 255, 0, ALPHA);
    }
}
