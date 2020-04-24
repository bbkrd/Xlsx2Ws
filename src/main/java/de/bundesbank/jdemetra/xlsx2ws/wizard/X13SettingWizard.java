/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard;

import de.bundesbank.jdemetra.xlsx2ws.actions.ExportToXlsx;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.X13SpecificationFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class X13SettingWizard implements WizardDescriptor.Panel<WizardDescriptor> {

    private final List<ChangeListener> listener = new ArrayList<>();

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private X13SettingVisual component;
    private HashMap<String, ISetting> settings;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public X13SettingVisual getComponent() {
        if (component == null) {
            component = new X13SettingVisual();
            component.addChangeListener(x -> notifyListener(x));
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
        listener.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listener.remove(l);
    }

    private synchronized void notifyListener(ChangeEvent ev) {
        for (ChangeListener changeListener : listener) {
            changeListener.stateChanged(ev);
        }
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
        settings.put(X13SpecificationFactory.SUPPORTED_CLASS.getName(), component.createSetting());
    }

}
