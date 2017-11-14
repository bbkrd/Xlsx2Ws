/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

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
        id = "de.bundesbank.jdemetra.xlsx2ws.ExcelToWorkspaceAction"
)
@ActionRegistration(
        displayName = "#CTL_Excel2Workspace"
)
@ActionReference(path = "Menu/File", position = 1)
@Messages("CTL_Excel2Workspace=Create new Workspace from XLSX file")
public final class ExcelToWorkspaceAction implements ActionListener {

    private final FileChooserBuilder wsFileChooser = new FileChooserBuilder(ExcelToWorkspaceAction.class)
            .setFileFilter(new FileNameExtensionFilter("Spreadsheet file", "xlsx"));

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!WorkspaceFactory.getInstance().closeWorkspace(true)) {
            return;
        }
        WorkspaceFactory.getInstance().newWorkspace();
        File selectedFile = wsFileChooser.showOpenDialog();
        if (selectedFile != null) {
            new Creater().createWorkspace(selectedFile);
            NotifyDescriptor nd = new NotifyDescriptor.Message("DONE?");
            DialogDisplayer.getDefault().notify(nd);
        }

    }
}
