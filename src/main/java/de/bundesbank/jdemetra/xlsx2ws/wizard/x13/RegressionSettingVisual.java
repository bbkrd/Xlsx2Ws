/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.spec.x13.RegressionSetting;
import javax.swing.JPanel;

public final class RegressionSettingVisual extends JPanel {

    public RegressionSettingVisual() {
        initComponents();
        changeAllCheckboxes(true);
    }

    @Override
    public String getName() {
        return "Regression Settings (X13)";
    }

    public RegressionSetting createSetting() {
        return new RegressionSetting(
                cbHolidays.isSelected(),
                cbTradingDaysType.isSelected(),
                cbAutoAdjust.isSelected(),
                cbLeapYear.isSelected(),
                cbW.isSelected(),
                cbUserVariables.isSelected(),
                cbTradingDaysTest.isSelected(),
                cbEaster.isSelected(),
                cbPrespecifiedOutliers.isSelected(),
                cbUserDefinedVariables.isSelected(),
                cbFixedRegressionCoefficients.isSelected());

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbHolidays = new javax.swing.JCheckBox();
        cbTradingDaysType = new javax.swing.JCheckBox();
        cbAutoAdjust = new javax.swing.JCheckBox();
        cbLeapYear = new javax.swing.JCheckBox();
        cbW = new javax.swing.JCheckBox();
        cbUserVariables = new javax.swing.JCheckBox();
        cbTradingDaysTest = new javax.swing.JCheckBox();
        cbEaster = new javax.swing.JCheckBox();
        cbPrespecifiedOutliers = new javax.swing.JCheckBox();
        cbUserDefinedVariables = new javax.swing.JCheckBox();
        cbFixedRegressionCoefficients = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        all = new javax.swing.JButton();
        none = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(430, 312));

        org.openide.awt.Mnemonics.setLocalizedText(cbHolidays, "Holidays");

        org.openide.awt.Mnemonics.setLocalizedText(cbTradingDaysType, "Trading days type");

        org.openide.awt.Mnemonics.setLocalizedText(cbAutoAdjust, "Auto adjust");

        org.openide.awt.Mnemonics.setLocalizedText(cbLeapYear, "Leap year");

        org.openide.awt.Mnemonics.setLocalizedText(cbW, "Stock trading day (w)");

        org.openide.awt.Mnemonics.setLocalizedText(cbUserVariables, "User variables");

        org.openide.awt.Mnemonics.setLocalizedText(cbTradingDaysTest, "Trading days test");

        org.openide.awt.Mnemonics.setLocalizedText(cbEaster, "Easter");

        org.openide.awt.Mnemonics.setLocalizedText(cbPrespecifiedOutliers, "Prespecified outliers");

        org.openide.awt.Mnemonics.setLocalizedText(cbUserDefinedVariables, "User defined variables");

        org.openide.awt.Mnemonics.setLocalizedText(cbFixedRegressionCoefficients, "Fixed regression coefficients");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "<html>Choose which parts of the <b>REGRESSION</b> specification should be written to the XLSX.</html>");

        org.openide.awt.Mnemonics.setLocalizedText(all, "Select all");
        all.setMaximumSize(new java.awt.Dimension(89, 23));
        all.setMinimumSize(new java.awt.Dimension(89, 23));
        all.setPreferredSize(new java.awt.Dimension(89, 23));
        all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(none, "Select none");
        none.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbHolidays)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbTradingDaysType)
                            .addComponent(cbAutoAdjust)
                            .addComponent(cbLeapYear))
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbEaster)
                            .addComponent(cbUserVariables)
                            .addComponent(cbTradingDaysTest)
                            .addComponent(cbW)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbPrespecifiedOutliers)
                        .addGap(18, 18, 18)
                        .addComponent(cbUserDefinedVariables))
                    .addComponent(cbFixedRegressionCoefficients)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(all, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(none)))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbHolidays)
                    .addComponent(cbW))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbTradingDaysType)
                    .addComponent(cbUserVariables))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbAutoAdjust)
                    .addComponent(cbTradingDaysTest))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbLeapYear)
                    .addComponent(cbEaster))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbPrespecifiedOutliers)
                    .addComponent(cbUserDefinedVariables))
                .addGap(18, 18, 18)
                .addComponent(cbFixedRegressionCoefficients)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(all, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(none))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void allActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allActionPerformed
        changeAllCheckboxes(true);
    }//GEN-LAST:event_allActionPerformed

    private void noneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noneActionPerformed
        changeAllCheckboxes(false);
    }//GEN-LAST:event_noneActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton all;
    private javax.swing.JCheckBox cbAutoAdjust;
    private javax.swing.JCheckBox cbEaster;
    private javax.swing.JCheckBox cbFixedRegressionCoefficients;
    private javax.swing.JCheckBox cbHolidays;
    private javax.swing.JCheckBox cbLeapYear;
    private javax.swing.JCheckBox cbPrespecifiedOutliers;
    private javax.swing.JCheckBox cbTradingDaysTest;
    private javax.swing.JCheckBox cbTradingDaysType;
    private javax.swing.JCheckBox cbUserDefinedVariables;
    private javax.swing.JCheckBox cbUserVariables;
    private javax.swing.JCheckBox cbW;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton none;
    // End of variables declaration//GEN-END:variables

    private void changeAllCheckboxes(boolean selected) {
        cbHolidays.setSelected(selected);
        cbTradingDaysType.setSelected(selected);
        cbAutoAdjust.setSelected(selected);
        cbLeapYear.setSelected(selected);
        cbW.setSelected(selected);
        cbUserVariables.setSelected(selected);
        cbTradingDaysTest.setSelected(selected);
        cbEaster.setSelected(selected);
        cbPrespecifiedOutliers.setSelected(selected);
        cbUserDefinedVariables.setSelected(selected);
        cbFixedRegressionCoefficients.setSelected(selected);
    }
}