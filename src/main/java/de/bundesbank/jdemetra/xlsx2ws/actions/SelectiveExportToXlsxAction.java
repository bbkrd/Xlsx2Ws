/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.actions;

import de.bundesbank.jdemetra.xlsx2ws.Writer;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.wizard.IChoose;
import de.bundesbank.jdemetra.xlsx2ws.wizard.IntroductionWizard;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javax.swing.JComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

@ActionID(
        category = "Tools",
        id = "de.bundesbank.jdemetra.xlsx2ws.SelectiveExportToXlsxAction"
)
@ActionRegistration(
        displayName = "#CTL_SelectiveExportToXlsx"
)
@ActionReference(path = "Menu/File", position = 403, separatorAfter = 404)
@NbBundle.Messages("CTL_SelectiveExportToXlsx=Selective export to Xlsx file")
public final class SelectiveExportToXlsxAction implements ActionListener {

    private final FileChooser fileChooser;
    private final ProgressHandle progressHandle = ProgressHandle.createHandle("Selective exporting to Xlsx");

    public SelectiveExportToXlsxAction() {
        this.fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spreadsheet file", "*.xlsx"));
        new JFXPanel();
    }

    public static final String SETTINGS = "settings_map";

    @Override
    public void actionPerformed(ActionEvent e) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();

        panels.add(new IntroductionWizard());
//        panels.add(new ProviderWizard());

        Lookup.getDefault().lookupAll(IChoose.class).forEach((t) -> panels.add(t.createPanel()));

        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<>(panels));
        final HashMap<String, ISetting> hashMap = new HashMap<>();
        wiz.putProperty(SETTINGS, hashMap);

        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.CTL_SelectiveExportToXlsx());
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
