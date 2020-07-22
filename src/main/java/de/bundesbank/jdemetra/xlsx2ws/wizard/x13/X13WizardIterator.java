/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.spec.x13.X13MainSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.X13MainSettingDTO;
import de.bundesbank.jdemetra.xlsx2ws.wizard.X13SettingWizard;
import java.util.NoSuchElementException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

/**
 *
 * @author s4504tw
 */
public class X13WizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    private int index = 0;
    private final WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[8];

    public X13WizardIterator() {
        X13SettingWizard x13SettingWizard = new X13SettingWizard();
        x13SettingWizard.addChangeListener((ChangeEvent e) -> {
            Object source = e.getSource();
            if (source instanceof X13MainSettingDTO) {
                X13MainSetting mainSetting = ((X13MainSettingDTO) source).getMainSetting();
                boolean active = ((X13MainSettingDTO) source).isActive();
                int position = mainSetting.getPosition();
                if (active) {
                    WizardDescriptor.Panel<WizardDescriptor> wizard = mainSetting.createWizard();
                    panels[position] = wizard;
                } else {
                    panels[position] = null;
                }
            }
        });
        panels[0] = x13SettingWizard;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return "X13";
    }

    @Override
    public boolean hasNext() {
        for (int i = index + 1; i < panels.length; i++) {
            if (panels[i] != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        for (int i = index + 1; i < panels.length; i++) {
            if (panels[i] != null) {
                index = i;
                return;
            }
        }
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        for (int i = index - 1; i >= 0; i--) {
            if (panels[i] != null) {
                index = i;
                return;
            }
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

}
