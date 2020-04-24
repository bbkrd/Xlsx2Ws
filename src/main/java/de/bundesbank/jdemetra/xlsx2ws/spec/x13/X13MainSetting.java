/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec.x13;

import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.ArimaSettingWizard;
import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.EstimateSettingWizard;
import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.OutlierSettingWizard;
import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.RegressionSettingWizard;
import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.SeriesSettingWizard;
import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.TransformSettingWizard;
import de.bundesbank.jdemetra.xlsx2ws.wizard.x13.X11SettingWizard;
import org.openide.WizardDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author s4504tw
 */
public enum X13MainSetting {
    SERIES(SeriesSettingWizard.class, 1),
    ESTIMATE(EstimateSettingWizard.class, 2),
    TRANSFORM(TransformSettingWizard.class, 3),
    REGRESSION(RegressionSettingWizard.class, 4),
    OUTLIER(OutlierSettingWizard.class, 5),
    ARIMA(ArimaSettingWizard.class, 6),
    X11(X11SettingWizard.class, 7);

    private final Class<? super WizardDescriptor.Panel<WizardDescriptor>> wizardClass;
    private final int position;

    private X13MainSetting(Class wizardClass, int position) {
        this.wizardClass = wizardClass;
        this.position = position;
    }

    public WizardDescriptor.Panel<WizardDescriptor> createWizard() {
        try {
            return (WizardDescriptor.Panel<WizardDescriptor>) wizardClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Class<? super WizardDescriptor.Panel<WizardDescriptor>> getWizardClass() {
        return wizardClass;
    }

    public int getPosition() {
        return position;
    }
}
