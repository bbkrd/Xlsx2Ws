/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.dto.SaItemInfo;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationReader;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationReaderFactory;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsMoniker;
import ec.tss.sa.SaItem;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.regression.TsVariables;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.extern.java.Log;
import org.openide.util.Lookup;

@Log
public class Writer {

    public void writeWorkspace(File file) {
        Map<String, Set<String>> variablesMap = new HashMap<>();

        List<SaItemInfo> list = new ArrayList();

        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        List<WorkspaceItem<MultiProcessingDocument>> existingDocuments = ws.searchDocuments(MultiProcessingDocument.class);
        existingDocuments.forEach(existingDocument -> {
            String name = existingDocument.getDisplayName();
            existingDocument.getElement().getCurrent().forEach(item -> {
                SaItemInfo saItemInfo = new SaItemInfo();
                saItemInfo.setMultidocName(name);
                saItemInfo.setSaItemName(item.getRawName().equals("") ? Integer.toString(list.size()) : item.getRawName());
                if (item.getMetaData() != null) {
                    saItemInfo.getMetaData().putAll(item.getMetaData());
                }
                writeProviderInfo(item, saItemInfo);

                ISaSpecification domainSpecification = item.getDomainSpecification();
                String specName = domainSpecification.getClass().getName();

                Optional<? extends ISpecificationReaderFactory> optionalSpec = Lookup.getDefault().lookupAll(ISpecificationReaderFactory.class).stream().filter(specReader -> specReader.getSupportedClass().equalsIgnoreCase(specName)).findFirst();
                if (!optionalSpec.isPresent()) {
                    //TODO Log
                    return;
                }
                ISpecificationReader specReader = optionalSpec.get().getNewInstance();
                Map<String, String> specInfo = specReader.writeSpecification(domainSpecification);
                saItemInfo.setSpecificationName(optionalSpec.get().getSpecificationName());
                specInfo.forEach((key, value) -> saItemInfo.addSpecificationInfo(key, value));
                list.add(saItemInfo);
            });
        });

        List<WorkspaceItem<TsVariables>> existingTsVariables = ws.searchDocuments(TsVariables.class);
        existingTsVariables.forEach((existingTsVariable) -> {
            String name = existingTsVariable.getDisplayName();
            Set<String> variables = new HashSet<>(Arrays.asList(existingTsVariable.getElement().getNames()));
            variablesMap.put(name, variables);
        });
    }

    private void writeProviderInfo(SaItem item, SaItemInfo saItemInfo) {
        TsMoniker originalMoniker = getOriginalMoniker(item.getTs().getMoniker());
        if (originalMoniker == null) {
            return;
        }
        String source = originalMoniker.getSource();
        Optional<? extends IProviderFactory> optionalProvider = Lookup.getDefault().lookupAll(IProviderFactory.class).stream().filter(provider -> provider.getSourceName().equalsIgnoreCase(source)).findFirst();
        if (!optionalProvider.isPresent()) {
            //TODO Log
            return;
        }
        IProvider provider = optionalProvider.get().getNewInstance();
        Map<String, String> providerInfo = provider.writeTs(originalMoniker);
        saItemInfo.setProviderName(optionalProvider.get().getProviderName());
        providerInfo.forEach((key, value) -> saItemInfo.addProviderInfo(key, value));
    }

    private TsMoniker getOriginalMoniker(TsMoniker moniker) {
        if (!moniker.isAnonymous()) {
            return moniker;
        }
        Ts ts = TsFactory.instance.getTs(moniker);
        if (ts == null) {
            return null;
        }
        MetaData md = ts.getMetaData();
        if (md == null) {
            return null;
        }
        String source = getSource(md);
        if (source == null) {
            return null;
        }
        String id = getId(md);
        if (id == null) {
            return null;
        }
        return new TsMoniker(source, id);
    }

    private String getSource(@Nonnull MetaData md) {
        String result = md.get(MetaData.SOURCE);
        return result != null ? result : md.get(Ts.SOURCE_OLD);
    }

    private String getId(@Nonnull MetaData md) {
        String result = md.get(MetaData.ID);
        return result != null ? result : md.get(Ts.ID_OLD);
    }
}
