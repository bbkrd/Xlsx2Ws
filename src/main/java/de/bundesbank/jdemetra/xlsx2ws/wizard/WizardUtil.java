/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard;

import org.openide.WizardDescriptor;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.experimental.UtilityClass
public class WizardUtil {

    public void countIndex(WizardDescriptor wiz, int index) {
        if (wiz.getValue() == WizardDescriptor.NEXT_OPTION) {
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, ++index);
        } else if (wiz.getValue() == WizardDescriptor.PREVIOUS_OPTION) {
            wiz.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, --index);
        }
    }
}
