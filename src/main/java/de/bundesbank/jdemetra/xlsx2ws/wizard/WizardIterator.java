/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author s4504tw
 */
public class WizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private List<WizardDescriptor.Iterator<WizardDescriptor>> iterators = new ArrayList<>();

    private final List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<>();
    private boolean isMultiDoc;
    private boolean isMetaData;
    private WizardDescriptor.Iterator<WizardDescriptor> currentIterator = null;
    private int currentIteratorIndex = -1;

    public WizardIterator() {
        panels.add(new MultiDocWizard());
        panels.add(new MetaDataWizard());
        Lookup.getDefault().lookupAll(IChoose.class).forEach((t) -> {
            WizardDescriptor.Iterator<WizardDescriptor> createIterator = t.createIterator();
            if (createIterator != null) {
                iterators.add(createIterator);
            }
        });

        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }

        isMultiDoc = true;
        isMetaData = false;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        if (isMultiDoc) {
            return panels.get(0);
        } else if (isMetaData) {
            return panels.get(1);
        } else {
            return currentIterator.current();
        }
    }

    @Override
    public String name() {
        //TODO?
        return null;
    }

    @Override
    public boolean hasNext() {
        return !isMetaData;
    }

    @Override
    public boolean hasPrevious() {
        return !isMultiDoc;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (isMultiDoc) {
            if (!iterators.isEmpty()) {
                currentIterator = iterators.get(0);
                currentIteratorIndex = 0;
            } else {
                isMetaData = true;
            }
            isMultiDoc = false;
            return;
        }
        if (currentIterator.hasNext()) {
            currentIterator.nextPanel();
            return;
        }
        if (currentIteratorIndex + 1 < iterators.size()) {
            currentIteratorIndex++;
            currentIterator = iterators.get(currentIteratorIndex);
            return;
        }
        isMetaData = true;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        if (isMetaData) {
            isMetaData = false;
            return;
        }

        if (currentIterator.hasPrevious()) {
            currentIterator.previousPanel();
            return;
        }
        if (currentIteratorIndex == 0) {
            currentIterator = null;
            isMultiDoc = true;
            return;
        }
        currentIteratorIndex--;
        currentIterator = iterators.get(currentIteratorIndex);
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then use
    // ChangeSupport to implement add/removeChangeListener and call fireChange
    // when needed

    @Override
    public Set instantiate() throws IOException {
        return null;
    }

    @Override
    public void initialize(WizardDescriptor wd) {
    }

    @Override
    public void uninitialize(WizardDescriptor wd) {
    }
}
