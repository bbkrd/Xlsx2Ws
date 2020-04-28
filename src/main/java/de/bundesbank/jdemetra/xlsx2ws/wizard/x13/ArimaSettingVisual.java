/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.spec.x13.ArimaSetting;
import javax.swing.JPanel;

public final class ArimaSettingVisual extends JPanel {

    public ArimaSettingVisual() {
        initComponents();
        changeAllCheckboxes(true);
    }

    @Override
    public String getName() {
        return "ARIMA Settings (X13)";
    }

    public ArimaSetting createSetting() {
        return new ArimaSetting(cbMean.isSelected(),
                cbArima.isSelected(),
                cbP.isSelected(),
                cbQ.isSelected(),
                cbBP.isSelected(),
                cbBQ.isSelected(),
                cbAcceptDefault.isSelected(),
                cbCancelLimit.isSelected(),
                cbInitialUR.isSelected(),
                cbFinalUR.isSelected(),
                cbMixed.isSelected(),
                cbBalanced.isSelected(),
                cbArmaLimit.isSelected(),
                cbReduceCV.isSelected(),
                cbLjungboxLimit.isSelected(),
                cbURFinal.isSelected());

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cbAcceptDefault = new javax.swing.JCheckBox();
        cbCancelLimit = new javax.swing.JCheckBox();
        cbInitialUR = new javax.swing.JCheckBox();
        cbFinalUR = new javax.swing.JCheckBox();
        cbMixed = new javax.swing.JCheckBox();
        cbBalanced = new javax.swing.JCheckBox();
        cbArmaLimit = new javax.swing.JCheckBox();
        cbReduceCV = new javax.swing.JCheckBox();
        cbLjungboxLimit = new javax.swing.JCheckBox();
        cbURFinal = new javax.swing.JCheckBox();
        cbMean = new javax.swing.JCheckBox();
        cbArima = new javax.swing.JCheckBox();
        cbP = new javax.swing.JCheckBox();
        cbQ = new javax.swing.JCheckBox();
        cbBP = new javax.swing.JCheckBox();
        cbBQ = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        all = new javax.swing.JButton();
        none = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(430, 312));

        org.openide.awt.Mnemonics.setLocalizedText(cbAcceptDefault, "Accept Default");

        org.openide.awt.Mnemonics.setLocalizedText(cbCancelLimit, "Cancel limit");

        org.openide.awt.Mnemonics.setLocalizedText(cbInitialUR, "Initial unit root");

        org.openide.awt.Mnemonics.setLocalizedText(cbFinalUR, "Final unit root");

        org.openide.awt.Mnemonics.setLocalizedText(cbMixed, "Mixed");

        org.openide.awt.Mnemonics.setLocalizedText(cbBalanced, "Balanced");

        org.openide.awt.Mnemonics.setLocalizedText(cbArmaLimit, "ARMA limit");

        org.openide.awt.Mnemonics.setLocalizedText(cbReduceCV, "Reduce CV");

        org.openide.awt.Mnemonics.setLocalizedText(cbLjungboxLimit, "Ljungbox limit");

        org.openide.awt.Mnemonics.setLocalizedText(cbURFinal, "UR final");

        org.openide.awt.Mnemonics.setLocalizedText(cbMean, "Mean");

        org.openide.awt.Mnemonics.setLocalizedText(cbArima, "ARIMA model");

        org.openide.awt.Mnemonics.setLocalizedText(cbP, "P parameter");

        org.openide.awt.Mnemonics.setLocalizedText(cbQ, "Q parameter");

        org.openide.awt.Mnemonics.setLocalizedText(cbBP, "BP parameter");

        org.openide.awt.Mnemonics.setLocalizedText(cbBQ, "BQ parameter");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "<html>Choose which parts of the <b>ARIMA</b> specification should be written to the XLSX.</html>");

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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbCancelLimit)
                            .addComponent(cbAcceptDefault)
                            .addComponent(cbInitialUR)
                            .addComponent(cbFinalUR)
                            .addComponent(cbMixed))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbBalanced)
                            .addComponent(cbArmaLimit)
                            .addComponent(cbReduceCV)
                            .addComponent(cbLjungboxLimit)
                            .addComponent(cbURFinal))
                        .addGap(60, 60, 60)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbQ)
                            .addComponent(cbP)
                            .addComponent(cbArima)
                            .addComponent(cbMean)
                            .addComponent(cbBP)
                            .addComponent(cbBQ)))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(all, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(none)))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbAcceptDefault)
                    .addComponent(cbBalanced)
                    .addComponent(cbMean))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbCancelLimit)
                    .addComponent(cbArmaLimit)
                    .addComponent(cbArima))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbInitialUR)
                    .addComponent(cbReduceCV)
                    .addComponent(cbP))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbFinalUR)
                    .addComponent(cbLjungboxLimit)
                    .addComponent(cbQ))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbMixed)
                    .addComponent(cbURFinal)
                    .addComponent(cbBP))
                .addGap(18, 18, 18)
                .addComponent(cbBQ)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
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
    private javax.swing.JCheckBox cbAcceptDefault;
    private javax.swing.JCheckBox cbArima;
    private javax.swing.JCheckBox cbArmaLimit;
    private javax.swing.JCheckBox cbBP;
    private javax.swing.JCheckBox cbBQ;
    private javax.swing.JCheckBox cbBalanced;
    private javax.swing.JCheckBox cbCancelLimit;
    private javax.swing.JCheckBox cbFinalUR;
    private javax.swing.JCheckBox cbInitialUR;
    private javax.swing.JCheckBox cbLjungboxLimit;
    private javax.swing.JCheckBox cbMean;
    private javax.swing.JCheckBox cbMixed;
    private javax.swing.JCheckBox cbP;
    private javax.swing.JCheckBox cbQ;
    private javax.swing.JCheckBox cbReduceCV;
    private javax.swing.JCheckBox cbURFinal;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton none;
    // End of variables declaration//GEN-END:variables

    private void changeAllCheckboxes(boolean selected) {
        cbMean.setSelected(selected);
        cbArima.setSelected(selected);
        cbP.setSelected(selected);
        cbQ.setSelected(selected);
        cbBP.setSelected(selected);
        cbBQ.setSelected(selected);
        cbAcceptDefault.setSelected(selected);
        cbCancelLimit.setSelected(selected);
        cbInitialUR.setSelected(selected);
        cbFinalUR.setSelected(selected);
        cbMixed.setSelected(selected);
        cbBalanced.setSelected(selected);
        cbArmaLimit.setSelected(selected);
        cbReduceCV.setSelected(selected);
        cbLjungboxLimit.setSelected(selected);
        cbURFinal.setSelected(selected);
    }
}
