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
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.xlsx2ws.ExcelToWorkspaceAction"
)
@ActionRegistration(
        displayName = "#CTL_Excel2Workspace"
)
@ActionReference(path = "Menu/File", position = 400, separatorBefore = 399)
@Messages("CTL_Excel2Workspace=Create new Workspace from XLSX file")
public final class ExcelToWorkspaceAction implements ActionListener {

    private final FileChooser fileChooser;

    public ExcelToWorkspaceAction() {
        this.fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spreadsheet file", "*.xlsx"));
        new JFXPanel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!WorkspaceFactory.getInstance().closeWorkspace(true)) {
            return;
        }

        Platform.runLater(() -> {
            ProgressHandle progressHandle = ProgressHandle.createHandle("Creating workspace from Xlsx");
            try {
                progressHandle.start();
                Preferences preferences = NbPreferences.forModule(ActionUtil.class);
                File startingDirectory = new File(preferences.get(ActionUtil.LAST_FOLDER, System.getProperty("user.home")));
                fileChooser.setInitialDirectory(startingDirectory);
                File file = fileChooser.showOpenDialog(null);
                if (file != null) {
                    WorkspaceFactory.getInstance().newWorkspace();
                    Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
                    preferences.put(ActionUtil.LAST_FOLDER, file.getParent());
                    String fileName = file.getName();
                    ws.setName(fileName.substring(0, fileName.length() - 5));
                    ws.sort();
                    new Creator().createWorkspace(file);
                }
            } finally {
                progressHandle.finish();
            }
        });

    }
}
