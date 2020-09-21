/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.actions;

import de.bundesbank.jdemetra.xlsx2ws.Creator;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.xlsx2ws.ImportFromXlsx"
)
@ActionRegistration(
        displayName = "#CTL_ImportFromXlsx"
)
@ActionReference(path = "Menu/File", position = 401)
@Messages("CTL_ImportFromXlsx=Import from Xlsx file")
public final class ImportFromXlsxAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        if (ws.isDirty()) {
            NotifyDescriptor save = new NotifyDescriptor.Confirmation("Do you want to save before importing from Xlsx?");
            Object response = DialogDisplayer.getDefault().notify(save);
            if (response.equals(NotifyDescriptor.CANCEL_OPTION) || response.equals(NotifyDescriptor.CLOSED_OPTION)) {
                return;
            }
            if (response.equals(NotifyDescriptor.YES_OPTION)) {
                ws.save();
            }
        }

        ActionUtil.open("Import from Xlsx", this::definingAction);
    }

    private void definingAction(File file) {
        WorkspaceFactory.getInstance().getActiveWorkspace().closeOpenDocuments();
        new Creator().createWorkspace(file);
    }
}
