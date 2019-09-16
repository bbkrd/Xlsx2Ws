/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

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
        id = "de.bundesbank.jdemetra.xlsx2ws.ExportToXlsx"
)
@ActionRegistration(
        displayName = "#CTL_ExportToXlsx"
)
@ActionReference(path = "Menu/File", position = 2)
@Messages("CTL_ExportToXlsx=Export to Xlsx file")
public final class ExportToXlsx implements ActionListener {

    private final FileChooserBuilder wsFileChooser = new FileChooserBuilder(ExportToXlsx.class)
            .setFileFilter(new FileNameExtensionFilter("Spreadsheet file", "xlsx"));

    @Override
    public void actionPerformed(ActionEvent e) {
        //File selectedFile = wsFileChooser.showSaveDialog();//TODO Add xlsx if missing
        File selectedFile = new File("");
        if (selectedFile != null) {
            new Writer().writeWorkspace(selectedFile);
            NotifyDescriptor nd = new NotifyDescriptor.Message("DONE?");
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}
