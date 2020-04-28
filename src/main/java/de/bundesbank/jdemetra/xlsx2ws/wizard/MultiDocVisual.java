/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard;

import de.bundesbank.jdemetra.xlsx2ws.dto.MultiDocSetting;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.util.list.swing.JListSelection;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JPanel;

public final class MultiDocVisual extends JPanel {

    final JListSelection listSelection;

    public MultiDocVisual() {
        initComponents();
        listSelection = new JListSelection();
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        List<WorkspaceItem<MultiProcessingDocument>> existingDocuments = ws.searchDocuments(MultiProcessingDocument.class);
        existingDocuments.stream().map(x -> x.getDisplayName()).forEach(listSelection.getTargetModel()::addElement);

        jPanel1.add(listSelection, BorderLayout.CENTER);
    }

    @Override
    public String getName() {
        return "Multi-documents";
    }

    public MultiDocSetting createSetting() {
        Set<String> set = Arrays.stream(listSelection.getTargetModel().toArray()).map(x -> x.toString()).collect(Collectors.toSet());
        return new MultiDocSetting(set);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        setPreferredSize(new java.awt.Dimension(430, 312));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MultiDocVisual.class, "MultiDocVisual.jLabel1.text")); // NOI18N

        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 156, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
