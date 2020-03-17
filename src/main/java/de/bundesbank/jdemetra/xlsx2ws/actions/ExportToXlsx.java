/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.actions;

import de.bundesbank.jdemetra.xlsx2ws.Writer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.xlsx2ws.ExportToXlsx"
)
@ActionRegistration(
        displayName = "#CTL_ExportToXlsx"
)
@ActionReference(path = "Menu/File", position = 402, separatorAfter = 403)
@Messages("CTL_ExportToXlsx=Export to Xlsx file")
public final class ExportToXlsx implements ActionListener {

    private final FileChooser fileChooser;

    public ExportToXlsx() {
        this.fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spreadsheet file", "*.xlsx"));
        new JFXPanel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Platform.runLater(() -> {
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                new Writer().writeWorkspace(selectedFile);
            }
        });
    }
}
