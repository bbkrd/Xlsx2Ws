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
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.xlsx2ws.ImportFromXlsx"
)
@ActionRegistration(
        displayName = "#CTL_ImportFromXlsx"
)
@ActionReference(path = "Menu/File", position = 2)
@Messages("CTL_ImportFromXlsx=Import from Xlsx file")
public final class ImportFromXlsxAction implements ActionListener {

    private final FileChooserBuilder wsFileChooser = new FileChooserBuilder(ImportFromXlsxAction.class)
            .setFileFilter(new FileNameExtensionFilter("Spreadsheet file", "xlsx"));

    @Override
    public void actionPerformed(ActionEvent e) {
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
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

        File selectedFile = wsFileChooser.showOpenDialog();
        if (selectedFile != null) {
            new Creator().createWorkspace(selectedFile);
        }
    }
}
