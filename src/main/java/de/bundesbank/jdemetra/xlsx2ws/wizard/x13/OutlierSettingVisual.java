/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.spec.x13.OutlierSetting;
import javax.swing.JPanel;

public final class OutlierSettingVisual extends JPanel {

    public OutlierSettingVisual() {
        initComponents();
        changeAllCheckboxes(true);
    }

    @Override
    public String getName() {
        return "Outlier Settings (X13)";
    }

    public OutlierSetting createSetting() {
        return new OutlierSetting(
                cbSpan.isSelected(),
                cbCriticalValue.isSelected(),
                cbAO.isSelected(),
                cbLS.isSelected(),
                cbTC.isSelected(),
                cbSO.isSelected(),
                cbTCRate.isSelected(),
                cbMethod.isSelected());

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        cbSpan = new javax.swing.JCheckBox();
        cbCriticalValue = new javax.swing.JCheckBox();
        cbAO = new javax.swing.JCheckBox();
        cbLS = new javax.swing.JCheckBox();
        cbTC = new javax.swing.JCheckBox();
        cbSO = new javax.swing.JCheckBox();
        cbTCRate = new javax.swing.JCheckBox();
        cbMethod = new javax.swing.JCheckBox();
        all = new javax.swing.JButton();
        none = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "<html>Choose which parts of the <b>OUTLIER</b> specification should be written to the XLSX.</html>");

        org.openide.awt.Mnemonics.setLocalizedText(cbSpan, "Span");

        org.openide.awt.Mnemonics.setLocalizedText(cbCriticalValue, "Critical Value");

        org.openide.awt.Mnemonics.setLocalizedText(cbAO, "AO");

        org.openide.awt.Mnemonics.setLocalizedText(cbLS, "LS");

        org.openide.awt.Mnemonics.setLocalizedText(cbTC, "TC");

        org.openide.awt.Mnemonics.setLocalizedText(cbSO, "SO");

        org.openide.awt.Mnemonics.setLocalizedText(cbTCRate, "TC rate");

        org.openide.awt.Mnemonics.setLocalizedText(cbMethod, "Method");

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
                            .addComponent(cbCriticalValue)
                            .addComponent(cbSpan)
                            .addComponent(cbAO)
                            .addComponent(cbLS))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbMethod)
                            .addComponent(cbTC)
                            .addComponent(cbSO)
                            .addComponent(cbTCRate)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(all, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(none)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbSpan)
                    .addComponent(cbTC))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbCriticalValue)
                    .addComponent(cbSO))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbAO)
                    .addComponent(cbTCRate))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbLS)
                    .addComponent(cbMethod))
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
    private javax.swing.JCheckBox cbAO;
    private javax.swing.JCheckBox cbCriticalValue;
    private javax.swing.JCheckBox cbLS;
    private javax.swing.JCheckBox cbMethod;
    private javax.swing.JCheckBox cbSO;
    private javax.swing.JCheckBox cbSpan;
    private javax.swing.JCheckBox cbTC;
    private javax.swing.JCheckBox cbTCRate;
    private javax.swing.JButton none;
    // End of variables declaration//GEN-END:variables

    private void changeAllCheckboxes(boolean selected) {
        cbSpan.setSelected(selected);
        cbCriticalValue.setSelected(selected);
        cbAO.setSelected(selected);
        cbLS.setSelected(selected);
        cbTC.setSelected(selected);
        cbSO.setSelected(selected);
        cbTCRate.setSelected(selected);
        cbMethod.setSelected(selected);
    }
}
