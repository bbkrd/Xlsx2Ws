/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.csv2ws;

import de.bundesbank.webservice.BubaWebBean;
import de.bundesbank.webservice.BubaWebProvider;
import de.bundesbank.webservice.SystemSource;
import de.bundesbank.webservice.saintegration.ProductionTier;
import de.bundesbank.webservice.saintegration.UpdateMeta;
import de.bundesbank.webservice.saintegration.UpdateTyp;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.x13.X13Specification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.sa.SaItem;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.TsProviders;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.information.InformationSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author Thomas Witthohn
 */
public class Creater {

    public void createWorkspace(File selectedFile) {
        WorkspaceFactory.getInstance().newWorkspace();
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        String fileName = selectedFile.getName();
        ws.setName(fileName.substring(0, fileName.length() - 4));
        ws.sort();

        try (Stream<String> stream = Files.lines(selectedFile.toPath())) {
            stream.skip(1)
                    .map(line -> line.split(";", -1))
                    .forEach(split -> {
                        String multiDocumentName = split[0];
                        String saItemName = split[1];
                        String tsName = split[2];

                        Ts ts = createTs(tsName);
                        if (ts == null) {
                            return;
                        }

                        SaItem item = new SaItem(X13Specification.RSA5, ts);
                        item.setName(saItemName);
                        item.setMetaData(createMetaData(split));

                        MultiProcessingDocument document = createAbsentMultiDoc(multiDocumentName);
                        document.getCurrent().add(item);
                    });
        } catch (IOException ex) {
            Logger.getLogger(Creater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private MultiProcessingDocument createAbsentMultiDoc(String name) {
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        WorkspaceItem<?> doc = ws.searchDocumentByName(MultiProcessingManager.ID, name);
        MultiProcessingManager mgr = WorkspaceFactory.getInstance().getManager(MultiProcessingManager.class);

        if (doc == null) {
            doc = mgr.create(ws);
            doc.setDisplayName(name);
        }
        if (doc.getElement() instanceof MultiProcessingDocument) {
            return (MultiProcessingDocument) doc.getElement();
        }
        return null;
    }

    private Ts createTs(String name) {
        BubaWebBean bean = new BubaWebBean();
        bean.setDb(SystemSource.PRODUKTION);
        bean.setId(name);
        bean.setName(name);

        BubaWebProvider provider = TsProviders.lookup(BubaWebProvider.class, BubaWebProvider.SOURCE).get();
        DataSource dataSource = provider.encodeBean(bean);
        try {
            TsMoniker moniker = provider.toMoniker(provider.children(dataSource).get(0));
            return TsFactory.instance.createTs(name, moniker, TsInformationType.All);
        } catch (IllegalArgumentException | IOException ex) {
            Logger.getLogger(Creater.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private MetaData createMetaData(String[] information) {
        MetaData metaData = new MetaData();
        int i = 3;
        List<String> keys = new ArrayList<>();

        for (ProductionTier tier : ProductionTier.values()) {
            keys.add(tier + InformationSet.STRSEP + UpdateTyp.SEASONALFACTOR + InformationSet.STRSEP);
            keys.add(tier + InformationSet.STRSEP + UpdateTyp.CALENDARFACTOR + InformationSet.STRSEP);
            keys.add(tier + InformationSet.STRSEP + UpdateTyp.FORECAST + InformationSet.STRSEP);
            if (tier.equals(ProductionTier.TEST_TIER)) {
                keys.add(tier + InformationSet.STRSEP + UpdateTyp.SEASONALLY_ADJUSTED + InformationSet.STRSEP);
            }
        }

        for (String key : keys) {
            for (UpdateMeta value : UpdateMeta.values()) {
                metaData.put(key + value, information[i]);
                i++;
            }
        }

        metaData.put("prodebene" + InformationSet.STRSEP + UpdateTyp.SEASONALFACTOR + InformationSet.STRSEP + "loadid", information[i]);
        metaData.put("prodebene" + InformationSet.STRSEP + UpdateTyp.CALENDARFACTOR + InformationSet.STRSEP + "loadid", information[i + 1]);
        return metaData;
    }
}
