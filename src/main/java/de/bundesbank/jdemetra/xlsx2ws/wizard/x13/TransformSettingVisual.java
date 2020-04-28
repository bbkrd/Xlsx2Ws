/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.spec.x13.TransformSetting;
import javax.swing.JPanel;

public final class TransformSettingVisual extends JPanel {

    public TransformSettingVisual() {
        initComponents();
        changeAllCheckboxes(true);
    }

    @Override
    public String getName() {
        return "Transform Settings (X13)";
    }

    public TransformSetting createSetting() {
        return new TransformSetting(
                cbTransform.isSelected(),
                cbAicDiff.isSelected(),
                cbAdjust.isSelected());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        cbTransform = new javax.swing.JCheckBox();
        cbAicDiff = new javax.swing.JCheckBox();
        cbAdjust = new javax.swing.JCheckBox();
        all = new javax.swing.JButton();
        none = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(430, 312));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "<html>Choose which parts of the <b>TRANSFORM</b> specification should be written to the XLSX.</html>");

        org.openide.awt.Mnemonics.setLocalizedText(cbTransform, "Transform");

        org.openide.awt.Mnemonics.setLocalizedText(cbAicDiff, "AIC diff");

        org.openide.awt.Mnemonics.setLocalizedText(cbAdjust, "Adjust");

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
                    .addComponent(cbAicDiff)
                    .addComponent(cbTransform)
                    .addComponent(cbAdjust)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(all, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(none)))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbTransform)
                .addGap(18, 18, 18)
                .addComponent(cbAicDiff)
                .addGap(18, 18, 18)
                .addComponent(cbAdjust)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 141, Short.MAX_VALUE)
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
    private javax.swing.JCheckBox cbAdjust;
    private javax.swing.JCheckBox cbAicDiff;
    private javax.swing.JCheckBox cbTransform;
    private javax.swing.JButton none;
    // End of variables declaration//GEN-END:variables

    private void changeAllCheckboxes(boolean selected) {
        cbTransform.setSelected(selected);
        cbAicDiff.setSelected(selected);
        cbAdjust.setSelected(selected);
    }

}