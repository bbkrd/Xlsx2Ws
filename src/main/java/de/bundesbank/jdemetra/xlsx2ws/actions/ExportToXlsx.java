/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.actions;

import de.bundesbank.jdemetra.xlsx2ws.Writer;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.wizard.WizardIterator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

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
    private final ProgressHandle progressHandle = ProgressHandle.createHandle("Exporting to Xlsx");

    public ExportToXlsx() {
        this.fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spreadsheet file", "*.xlsx"));
        new JFXPanel();
    }

    public static final String SETTINGS = "settings_map";

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardDescriptor wiz = new WizardDescriptor(new WizardIterator());
        final HashMap<String, ISetting> hashMap = new HashMap<>();
        wiz.putProperty(SETTINGS, hashMap);

        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.CTL_ExportToXlsx());
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            Platform.runLater(() -> {
                try {
                    progressHandle.start();
                    Preferences preferences = NbPreferences.forModule(ActionUtil.class);
                    File startingDirectory = new File(preferences.get(ActionUtil.LAST_FOLDER, System.getProperty("user.home")));
                    fileChooser.setInitialDirectory(startingDirectory);
                    File file = fileChooser.showSaveDialog(null);
                    if (file != null) {
                        preferences.put(ActionUtil.LAST_FOLDER, file.getParent());
                        new Writer().writeWorkspace(file, hashMap);
                    }
                } finally {
                    progressHandle.finish();
                }
            });
        }
    }
}
