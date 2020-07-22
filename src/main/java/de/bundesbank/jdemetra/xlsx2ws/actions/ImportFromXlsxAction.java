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
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

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

    private final FileChooser fileChooser;

    public ImportFromXlsxAction() {
        this.fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spreadsheet file", "*.xlsx"));
        new JFXPanel();
    }

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

        Platform.runLater(() -> {
            ProgressHandle progressHandle = ProgressHandle.createHandle("Import from Xlsx");
            try {
                progressHandle.start();
                Preferences preferences = NbPreferences.forModule(ActionUtil.class);
                File startingDirectory = new File(preferences.get(ActionUtil.LAST_FOLDER, System.getProperty("user.home")));
                fileChooser.setInitialDirectory(startingDirectory);
                File file = fileChooser.showOpenDialog(null);
                if (file != null) {
                    ws.closeOpenDocuments();
                    preferences.put(ActionUtil.LAST_FOLDER, file.getParent());
                    new Creator().createWorkspace(file);
                }
            } finally {
                progressHandle.finish();
            }

        });
    }
}
