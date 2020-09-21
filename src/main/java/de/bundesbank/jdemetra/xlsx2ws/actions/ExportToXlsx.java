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
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
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

    public static final String SETTINGS = "settings_map";

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardDescriptor wiz = new WizardDescriptor(new WizardIterator());
        final HashMap<String, ISetting> hashMap = new HashMap<>();
        wiz.putProperty(SETTINGS, hashMap);

        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.CTL_ExportToXlsx());
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            ActionUtil.save("Exporting to Xlsx", hashMap, this::definingAction);
        }
    }

    private void definingAction(File file, HashMap<String, ISetting> hashMap) {
        new Writer().writeWorkspace(file, hashMap);
    }
}
