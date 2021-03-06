/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard;

import de.bundesbank.jdemetra.xlsx2ws.actions.ExportToXlsx;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.MetaDataSetting;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class MetaDataWizard implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private MetaDataVisual component;
    private HashMap<String, ISetting> settings;
    private int index;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public MetaDataVisual getComponent() {
        if (component == null) {
            component = new MetaDataVisual();
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
        index = (int) wiz.getProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX);
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        settings.put(MetaDataSetting.META_DATA_SETTING, component.createSetting());
        WizardUtil.countIndex(wiz, index);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

}
