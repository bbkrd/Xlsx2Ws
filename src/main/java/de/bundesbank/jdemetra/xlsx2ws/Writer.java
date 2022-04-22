/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.dto.IProviderInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.MetaDataSetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.MultiDocSetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.PositionInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.RegressorInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.SaItemInfo;
import de.bundesbank.jdemetra.xlsx2ws.provider.GenericProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.GenericProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.provider.PureDataProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.PureDataProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationWriter;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.ui.calendars.CalendarDocumentManager;
import ec.nbdemetra.ui.variables.VariablesDocumentManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsMoniker;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.swing.JOptionPane;
import lombok.extern.java.Log;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Lookup;

@Log
public class Writer {

    public void writeWorkspace(File file) {
        writeWorkspace(file, null);
    }

    public void writeWorkspace(File file, Map<String, ISetting> settings) {
        try (final FileOutputStream fileOut = new FileOutputStream(file)) {
            TreeSet<String> providerInfoHeaderSaItems = new TreeSet<>();
            TreeSet<PositionInfo> specificationInfoHeader = new TreeSet<>();
            TreeSet<String> metaInfoHeader = new TreeSet<>();

            List<SaItemInfo> saItemInfos = new ArrayList();
            MultiDocSetting multiDocSetting = getMultiDocSetting(settings);
            MetaDataSetting metaDataSetting = getMetaDataSetting(settings);

            Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
            List<WorkspaceItem<MultiProcessingDocument>> existingDocuments = ws.searchDocuments(MultiProcessingDocument.class);
            existingDocuments.stream()
                    .filter(x -> multiDocSetting == null || multiDocSetting.contains(x.getDisplayName()))
                    .forEach(existingDocument -> {
                        String name = existingDocument.getDisplayName();
                        final SaProcessing processing = existingDocument.getElement().getCurrent();
                        Set<String> control = new HashSet<>(processing.size());

                        processing.stream()
                                .filter(item -> !item.getRawName().equals(""))
                                .map(item -> item.getRawName())
                                .forEach(control::add);

                        processing.forEach(item -> {
                            SaItemInfo saItemInfo = new SaItemInfo();
                            saItemInfo.setMultidocName(name);
                            saItemInfo.setSaItemName(item.getRawName().equals("") ? findNewName(item, control) : item.getRawName());
                            if (item.getMetaData() != null) {
                                item.getMetaData().entrySet().stream()
                                        .filter(e -> metaDataSetting == null || metaDataSetting.contains(e.getKey()))
                                        .forEach((entry) -> {
                                            saItemInfo.getMetaData().put(entry.getKey(), entry.getValue());
                                            metaInfoHeader.add(entry.getKey());
                                        });

                            }
                            TsMoniker originalMoniker = getOriginalMoniker(item.getTs().getMoniker());
                            if (originalMoniker == null) {
                                writePureDataInfo(item.getName(), item.getTs().getTsData(), saItemInfo, providerInfoHeaderSaItems);
                            } else {
                                writeProviderInfo(originalMoniker, saItemInfo, providerInfoHeaderSaItems);
                            }
                            writeSpecificationInfo(item, saItemInfo, specificationInfoHeader, settings);
                            saItemInfos.add(saItemInfo);
                        });
                    });

            HashMap<String, Header> headersSaItems = createHeaders(4,
                    new PrefixSet("prov_", providerInfoHeaderSaItems),
                    new PrefixSet("spec_", specificationInfoHeader),
                    new PrefixSet("meta_", metaInfoHeader));

            List<RegressorInfo> regressorInfos = new ArrayList<>();
            TreeSet<String> providerInfoHeaderRegressors = new TreeSet<>();
            List<WorkspaceItem<?>> existingTsVariables = ws.searchDocuments(VariablesDocumentManager.ID);
            existingTsVariables.stream().forEach((existingTsVariable) -> {
                Object element = existingTsVariable.getElement();
                if (element instanceof TsVariables) {
                    String documentName = existingTsVariable.getDisplayName();
                    Collection<ITsVariable> variables = ((TsVariables) element).variables();
                    for (ITsVariable variable : variables) {
                        RegressorInfo regressorInfo = new RegressorInfo();
                        regressorInfo.setDocumentName(documentName);
                        regressorInfo.setName(variable.getName());
                        if (variable instanceof DynamicTsVariable) {
                            TsMoniker moniker = ((DynamicTsVariable) variable).getMoniker();
                            if (moniker.getSource() == null || moniker.getId() == null) {
                                writePureDataInfo(variable.getDescription(null), ((DynamicTsVariable) variable).getTsData(), regressorInfo, providerInfoHeaderRegressors);
                            } else {
                                writeProviderInfo(moniker, regressorInfo, providerInfoHeaderRegressors);
                            }
                        } else if (variable instanceof TsVariable) {
                            writePureDataInfo(variable.getDescription(null), ((TsVariable) variable).getTsData(), regressorInfo, providerInfoHeaderRegressors);
                        }
                        regressorInfos.add(regressorInfo);
                    }
                }
            });
            HashMap<String, Header> headersRegressors = createHeaders(3, new PrefixSet("prov_", providerInfoHeaderRegressors));

            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                //SAItems sheet
                XSSFSheet sheet = workbook.createSheet("multidocs");
                XSSFRow headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("multidoc");
                headerRow.createCell(1).setCellValue("saitem");
                headerRow.createCell(2).setCellValue("providername");
                headerRow.createCell(3).setCellValue("specificationname");
                for (Header value : headersSaItems.values()) {
                    XSSFCell cell = headerRow.createCell(value.getPosition());
                    cell.setCellValue(value.getName());
                }

                int counter = 1;
                for (SaItemInfo saItemInfo : saItemInfos) {
                    XSSFRow row = sheet.createRow(counter++);
                    row.createCell(0).setCellValue(saItemInfo.getMultidocName());
                    row.createCell(1).setCellValue(saItemInfo.getSaItemName());
                    row.createCell(2).setCellValue(saItemInfo.getProviderName());
                    row.createCell(3).setCellValue(saItemInfo.getSpecificationName());
                    saItemInfo.getProviderInfos().forEach((key, value) -> row.createCell(headersSaItems.get(key).getPosition()).setCellValue(value));
                    saItemInfo.getSpecificationInfos().forEach((key, value) -> row.createCell(headersSaItems.get(key).getPosition()).setCellValue(value));
                    saItemInfo.getMetaData().forEach((key, value) -> row.createCell(headersSaItems.get(key).getPosition()).setCellValue(value));
                }

                //Regressor sheet
                XSSFSheet regressors = workbook.createSheet("regressors");
                headerRow = regressors.createRow(0);
                headerRow.createCell(0).setCellValue("variables");
                headerRow.createCell(1).setCellValue("name");
                headerRow.createCell(2).setCellValue("providername");
                for (Header value : headersRegressors.values()) {
                    XSSFCell cell = headerRow.createCell(value.getPosition());
                    cell.setCellValue(value.getName());
                }
                counter = 1;
                for (RegressorInfo regressorInfo : regressorInfos) {
                    XSSFRow row = regressors.createRow(counter++);
                    row.createCell(0).setCellValue(regressorInfo.getDocumentName());
                    row.createCell(1).setCellValue(regressorInfo.getName());
                    row.createCell(2).setCellValue(regressorInfo.getProviderName());
                    regressorInfo.getProviderInfos().forEach((key, value) -> row.createCell(headersRegressors.get(key).getPosition()).setCellValue(value));
                }

                workbook.write(fileOut);
            }
            if (ws.searchCompatibleDocuments(CalendarDocumentManager.ID).size() > 1) {
                JOptionPane.showMessageDialog(null, "User defined calendars will not be saved!", "Warning", JOptionPane.WARNING_MESSAGE);

            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeProviderInfo(TsMoniker moniker, IProviderInfo iProviderInfo, TreeSet<String> providerInfoHeader) {
        String source = moniker.getSource();
        Optional<? extends IProviderFactory> optionalProvider = Lookup.getDefault().lookupAll(IProviderFactory.class).stream().filter(provider -> provider.getSourceName().equalsIgnoreCase(source)).findFirst();
        IProvider provider;
        if (!optionalProvider.isPresent()) {
            provider = new GenericProvider();
            iProviderInfo.setProviderName(GenericProviderFactory.NAME);
        } else {
            provider = optionalProvider.get().getNewInstance();
            iProviderInfo.setProviderName(optionalProvider.get().getProviderName());
        }

        Map<String, String> providerInfo = provider.writeTs(moniker);
        providerInfo.forEach((key, value) -> {
            iProviderInfo.addProviderInfo(key, value);
            providerInfoHeader.add(key);
        });
    }

    private void writePureDataInfo(String name, TsData tsData, IProviderInfo iProviderInfo, TreeSet<String> providerInfoHeader) {
        PureDataProvider provider = new PureDataProvider();
        iProviderInfo.setProviderName(PureDataProviderFactory.NAME);
        Map<String, String> providerInfo = provider.writeData(name, tsData);
        providerInfo.forEach((key, value) -> {
            iProviderInfo.addProviderInfo(key, value);
            providerInfoHeader.add(key);
        });
    }

    private void writeSpecificationInfo(SaItem item, SaItemInfo saItemInfo, TreeSet<PositionInfo> specificationInfoHeader, Map<String, ISetting> settings) {
        ISaSpecification domainSpecification = item.getDomainSpecification();
        String specName = domainSpecification.getClass().getName();

        Optional<? extends ISpecificationFactory> optionalSpec = Lookup.getDefault().lookupAll(ISpecificationFactory.class).stream().filter(specReader -> specReader.getSupportedClass().equalsIgnoreCase(specName)).findFirst();
        if (!optionalSpec.isPresent()) {
            saItemInfo.setSpecificationName(domainSpecification.toLongString());
            saItemInfo.addSpecificationInfo("ERROR", "No available specwriter");
            specificationInfoHeader.add(new PositionInfo(0, "ERROR"));
            return;
        }
        ISpecificationWriter specWriter = optionalSpec.get().getNewWriterInstance();
        saItemInfo.setSpecificationName(optionalSpec.get().getSpecificationName());

        ISetting setting = settings != null ? settings.get(specName) : null;
        Map<PositionInfo, String> specInfo = specWriter.writeSpecification(domainSpecification, setting);
        specInfo.forEach((key, value) -> {
            saItemInfo.addSpecificationInfo(key.getName(), value);
            specificationInfoHeader.add(key);
        });
    }

    private TsMoniker getOriginalMoniker(TsMoniker moniker) {
        if (moniker.getId() != null) {
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
        return TsMoniker.createProvidedMoniker(source, id);
    }

    private String getSource(@Nonnull MetaData md) {
        String result = md.get(MetaData.SOURCE);
        return result != null ? result : md.get(Ts.SOURCE_OLD);
    }

    private String getId(@Nonnull MetaData md) {
        String result = md.get(MetaData.ID);
        return result != null ? result : md.get(Ts.ID_OLD);
    }

    private HashMap<String, Header> createHeaders(int offset, PrefixSet... prefixSets) {
        int counter = offset;
        HashMap<String, Header> headers = new HashMap<>();
        for (PrefixSet prefixSet : prefixSets) {
            for (Object o : prefixSet.getSet()) {
                headers.put(o.toString(), new Header(prefixSet.getPrefix() + o.toString(), counter++));
            }
        }
        return headers;
    }

    private MultiDocSetting getMultiDocSetting(Map<String, ISetting> settings) {
        ISetting multidocSetting = settings != null ? settings.get(MultiDocSetting.MULTIDOC_SETTING) : null;
        if (multidocSetting instanceof MultiDocSetting) {
            return (MultiDocSetting) multidocSetting;
        }
        return null;
    }

    private MetaDataSetting getMetaDataSetting(Map<String, ISetting> settings) {
        ISetting setting = settings != null ? settings.get(MetaDataSetting.META_DATA_SETTING) : null;
        if (setting instanceof MetaDataSetting) {
            return (MetaDataSetting) setting;
        }
        return null;
    }

    private String findNewName(SaItem item, Set<String> control) {
        String newName = item.getTs().getRawName();
        int counter = 1;
        if (!control.add(newName)) {
            while (!control.add(newName + "(" + counter + ")")) {
                counter++;
            }
            newName = newName + "(" + counter + ")";
        }
        return newName;
    }

    @lombok.Value
    private static class Header {

        private final String name;
        private final int position;

    }

    @lombok.Value
    private static class PrefixSet<T> {

        private final String prefix;
        private final TreeSet<T> set;
    }
}
