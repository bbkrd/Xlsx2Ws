/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.actions;

import de.bundesbank.jdemetra.xlsx2ws.Writer;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Help",
        id = "de.bundesbank.jdemetra.xlsx2ws.CreateBlankXlsxAction"
)
@ActionRegistration(
        displayName = "#CTL_CreateBlankXlsx"
)
@ActionReference(path = "Menu/Help", position = 400, separatorBefore = 399)
@Messages("CTL_CreateBlankXlsx=Create blank XLSX file with all headers")
public final class CreateBlankXlsxAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        ActionUtil.save(Bundle.CTL_CreateBlankXlsx(), null, this::definingAction);

    }

    private void definingAction(File file, HashMap<String, ISetting> hashMap) {
        new Writer().writeHeaderXlsx(file);
    }
}
