/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard.x13;

import de.bundesbank.jdemetra.xlsx2ws.spec.x13.X13MainSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.X13MainSettingDTO;
import de.bundesbank.jdemetra.xlsx2ws.wizard.X13SettingWizard;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;

/**
 *
 * @author s4504tw
 */
public class X13WizardIterator implements WizardDescriptor.Iterator<WizardDescriptor> {

    int index = 0;

    private final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();

    public X13WizardIterator() {
        X13SettingWizard x13SettingWizard = new X13SettingWizard();
        x13SettingWizard.addChangeListener((ChangeEvent e) -> {
            Object source = e.getSource();
            if (source instanceof X13MainSettingDTO) {
                X13MainSetting mainSetting = ((X13MainSettingDTO) source).getMainSetting();
                boolean active = ((X13MainSettingDTO) source).isActive();
                if (active) {
                    WizardDescriptor.Panel<WizardDescriptor> wizard = mainSetting.createWizard();
                    int position = mainSetting.getPosition();
                    if (panels.size() < position) {
                        panels.add(wizard);
                    } else {
                        panels.add(position, wizard);
                    }
                } else {
                    panels.removeIf(mainSetting.getWizardClass()::isInstance);
                }
            }
        });
        panels.add(x13SettingWizard);
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels.get(index);
    }

    @Override
    public String name() {
        //TODO?
        return null;
    }

    @Override
    public boolean hasNext() {
        return index < panels.size() - 1;
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
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

}
