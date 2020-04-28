/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.spec.x13.X11Setting;
import javax.swing.JPanel;

public final class X11SettingVisual extends JPanel {

    public X11SettingVisual() {
        initComponents();
        changeAllCheckboxes(true);
    }

    @Override
    public String getName() {
        return "X11 Settings (X13)";
    }

    public X11Setting createSetting() {
        return new X11Setting(
                cbMode.isSelected(),
                cbSeasonal.isSelected(),
                cbLowerSigma.isSelected(),
                cbUpperSigma.isSelected(),
                cbSeasonalFilter.isSelected(),
                cbHenderson.isSelected(),
                cbCalendarSigma.isSelected(),
                cbExcludeForecast.isSelected(),
                cbBiasCorrection.isSelected(),
                cbMaxLead.isSelected(),
                cbMaxBack.isSelected());

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        cbMode = new javax.swing.JCheckBox();
        cbSeasonal = new javax.swing.JCheckBox();
        cbLowerSigma = new javax.swing.JCheckBox();
        cbUpperSigma = new javax.swing.JCheckBox();
        cbSeasonalFilter = new javax.swing.JCheckBox();
        cbHenderson = new javax.swing.JCheckBox();
        cbCalendarSigma = new javax.swing.JCheckBox();
        cbExcludeForecast = new javax.swing.JCheckBox();
        cbBiasCorrection = new javax.swing.JCheckBox();
        cbMaxLead = new javax.swing.JCheckBox();
        cbMaxBack = new javax.swing.JCheckBox();
        all = new javax.swing.JButton();
        none = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(430, 312));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "<html>Choose which parts of the <b>X11</b> specification should be written to the XLSX.</html>");

        org.openide.awt.Mnemonics.setLocalizedText(cbMode, "Mode");

        org.openide.awt.Mnemonics.setLocalizedText(cbSeasonal, "Seasonal");

        org.openide.awt.Mnemonics.setLocalizedText(cbLowerSigma, "Lower sigma");

        org.openide.awt.Mnemonics.setLocalizedText(cbUpperSigma, "Upper sigma");

        org.openide.awt.Mnemonics.setLocalizedText(cbSeasonalFilter, "Seasonal filter");

        org.openide.awt.Mnemonics.setLocalizedText(cbHenderson, "Henderson");

        org.openide.awt.Mnemonics.setLocalizedText(cbCalendarSigma, "Calendar sigma");

        org.openide.awt.Mnemonics.setLocalizedText(cbExcludeForecast, "Exclude forecast");

        org.openide.awt.Mnemonics.setLocalizedText(cbBiasCorrection, "Bias correction");

        org.openide.awt.Mnemonics.setLocalizedText(cbMaxLead, "Forecast");

        org.openide.awt.Mnemonics.setLocalizedText(cbMaxBack, "Backcast");

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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbSeasonal)
                            .addComponent(cbMode)
                            .addComponent(cbLowerSigma)
                            .addComponent(cbUpperSigma))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbExcludeForecast)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbSeasonalFilter)
                                    .addComponent(cbHenderson)
                                    .addComponent(cbCalendarSigma))
                                .addGap(32, 32, 32)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbMaxLead)
                                    .addComponent(cbMaxBack)
                                    .addComponent(cbBiasCorrection)))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(all, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(none)))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbMode)
                    .addComponent(cbSeasonalFilter)
                    .addComponent(cbBiasCorrection))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSeasonal)
                    .addComponent(cbHenderson)
                    .addComponent(cbMaxLead))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbLowerSigma)
                    .addComponent(cbCalendarSigma)
                    .addComponent(cbMaxBack))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbUpperSigma)
                    .addComponent(cbExcludeForecast))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(all, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(none))
                .addContainerGap())
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
    private javax.swing.JCheckBox cbBiasCorrection;
    private javax.swing.JCheckBox cbCalendarSigma;
    private javax.swing.JCheckBox cbExcludeForecast;
    private javax.swing.JCheckBox cbHenderson;
    private javax.swing.JCheckBox cbLowerSigma;
    private javax.swing.JCheckBox cbMaxBack;
    private javax.swing.JCheckBox cbMaxLead;
    private javax.swing.JCheckBox cbMode;
    private javax.swing.JCheckBox cbSeasonal;
    private javax.swing.JCheckBox cbSeasonalFilter;
    private javax.swing.JCheckBox cbUpperSigma;
    private javax.swing.JButton none;
    // End of variables declaration//GEN-END:variables

    private void changeAllCheckboxes(boolean selected) {
        cbMode.setSelected(selected);
        cbSeasonal.setSelected(selected);
        cbLowerSigma.setSelected(selected);
        cbUpperSigma.setSelected(selected);
        cbSeasonalFilter.setSelected(selected);
        cbHenderson.setSelected(selected);
        cbCalendarSigma.setSelected(selected);
        cbExcludeForecast.setSelected(selected);
        cbBiasCorrection.setSelected(selected);
        cbMaxLead.setSelected(selected);
        cbMaxBack.setSelected(selected);
    }

}