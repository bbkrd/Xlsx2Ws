/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.actions.ExportToXlsx;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.X13SpecificationFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.X13SpecificationSetting;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class ArimaSettingWizard implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private ArimaSettingVisual component;
    private X13SpecificationSetting setting;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public ArimaSettingVisual getComponent() {
        if (component == null) {
            component = new ArimaSettingVisual();
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
            ISetting settingTemp = ((HashMap<String, ISetting>) property).get(X13SpecificationFactory.SUPPORTED_CLASS.getName());
            if (settingTemp instanceof X13SpecificationSetting) {
                this.setting = (X13SpecificationSetting) settingTemp;
            }
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        setting.setArimaSetting(component.createSetting());
    }

}
