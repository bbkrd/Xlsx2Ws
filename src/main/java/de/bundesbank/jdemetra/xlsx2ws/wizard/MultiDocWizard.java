/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard;

import de.bundesbank.jdemetra.xlsx2ws.actions.ExportToXlsx;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.MultiDocSetting;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class MultiDocWizard implements WizardDescriptor.ValidatingPanel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private MultiDocVisual component;
    private HashMap<String, ISetting> settings;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public MultiDocVisual getComponent() {
        if (component == null) {
            component = new MultiDocVisual();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        Object property = wiz.getProperty(ExportToXlsx.SETTINGS);
        if (property instanceof HashMap) {
            settings = (HashMap<String, ISetting>) property;
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (wiz.getValue() == WizardDescriptor.FINISH_OPTION) {
            settings.clear();
        }
        settings.put(MultiDocSetting.MULTIDOC_SETTING, component.createSetting());
    }

    @Override
    public void validate() throws WizardValidationException {
        if (component.createSetting().isEmpty()) {
            throw new WizardValidationException(null, "Please select at least one multi-document to continue!", null);
        }
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
