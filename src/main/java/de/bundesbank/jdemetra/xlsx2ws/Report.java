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
import java.util.List;
import java.util.logging.Level;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 *
 * @author s4504tw
 */
public final class Report extends JDialog {

    public Report(List<ReportItem> items) {
        setModalityType(DEFAULT_MODALITY_TYPE);
        setTitle("Report");

        DefaultListModel<ReportItem> dataModel = new DefaultListModel<>();
        items.forEach(dataModel::addElement);
        JTextArea reportText = new JTextArea();
        reportText.setPreferredSize(new Dimension(500, 470));
        reportText.setEditable(false);

        JList itemList = new JList(dataModel);
        itemList.setCellRenderer(new ReportItemCellRenderer());
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener((e) -> {
            int selectedIndex = itemList.getSelectedIndex();
            if (selectedIndex >= 0) {
                reportText.setText(items.get(selectedIndex).generateReport());
            } else {
                reportText.setText(null);
            }
        });
        Dimension buttonDimension = new Dimension(100, 30);

        JButton saveReportButton = new JButton("Save Report");
        saveReportButton.setPreferredSize(buttonDimension);
        JButton okayButton = new JButton("Close");
        okayButton.setPreferredSize(buttonDimension);
        okayButton.addActionListener((e) -> {
            this.dispose();
        });
        JScrollPane jScrollPane = new JScrollPane(itemList);
        jScrollPane.setPreferredSize(new Dimension(200, 500));

        JSplitPane buttonSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, saveReportButton, okayButton);
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, reportText, buttonSplit);
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
                    setBackground(Color.RED);
                } else if (highestLevel >= Level.WARNING.intValue()) {
                    setBackground(Color.ORANGE);
                } else if (highestLevel >= Level.INFO.intValue()) {
                    setBackground(Color.YELLOW);
                } else {
                    setBackground(Color.GREEN);
                }
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;

        }
    }
}
