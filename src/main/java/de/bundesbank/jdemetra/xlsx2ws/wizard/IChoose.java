/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard;

import ec.tstoolkit.design.ServiceDefinition;
import org.openide.WizardDescriptor;

/**
 *
 * @author s4504tw
 */
@ServiceDefinition
public interface IChoose {

    WizardDescriptor.Iterator<WizardDescriptor> createIterator();
}
