/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.dto.IProviderInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.RegressorInfo;
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
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsMoniker;
import ec.tss.sa.SaItem;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import lombok.extern.java.Log;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

@Log
public class Writer {

    public void writeWorkspace(File file) {
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            TreeSet<String> providerInfoHeaderSaItems = new TreeSet<>();
            TreeSet<String> specificationInfoHeader = new TreeSet<>();
            TreeSet<String> metaInfoHeader = new TreeSet<>();

            List<SaItemInfo> saItemInfos = new ArrayList();

            Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
            List<WorkspaceItem<MultiProcessingDocument>> existingDocuments = ws.searchDocuments(MultiProcessingDocument.class);
            existingDocuments.forEach(existingDocument -> {
                String name = existingDocument.getDisplayName();
                existingDocument.getElement().getCurrent().forEach(item -> {
                    SaItemInfo saItemInfo = new SaItemInfo();
                    saItemInfo.setMultidocName(name);
                    saItemInfo.setSaItemName(item.getRawName().equals("") ? Integer.toString(saItemInfos.size() + 1) : item.getRawName());
                    if (item.getMetaData() != null) {
                        for (Map.Entry<String, String> entry : item.getMetaData().entrySet()) {
                            saItemInfo.getMetaData().put(entry.getKey(), entry.getValue());
                            metaInfoHeader.add(entry.getKey());
                        }
                    }
                    TsMoniker originalMoniker = getOriginalMoniker(item.getTs().getMoniker());
                    if (originalMoniker == null) {
                        return;
                    }
                    writeProviderInfo(originalMoniker, saItemInfo, providerInfoHeaderSaItems);
                    writeSpecificationInfo(item, saItemInfo, specificationInfoHeader);
                    saItemInfos.add(saItemInfo);
                });
            });

            HashMap<String, Header> headersSaItems = createHeaders(4, new Temp("p_", providerInfoHeaderSaItems), new Temp("s_", specificationInfoHeader), new Temp("m_", metaInfoHeader));

            List<RegressorInfo> regressorInfos = new ArrayList<>();
            TreeSet<String> providerInfoHeaderRegressors = new TreeSet<>();
            List<WorkspaceItem<TsVariables>> existingTsVariables = ws.searchDocuments(TsVariables.class);
            existingTsVariables.forEach((existingTsVariable) -> {
                String documentName = existingTsVariable.getDisplayName();
                Collection<ITsVariable> variables = existingTsVariable.getElement().variables();
                for (ITsVariable variable : variables) {
                    RegressorInfo regressorInfo = new RegressorInfo();
                    regressorInfo.setDocumentName(documentName);
                    regressorInfo.setName(variable.getName());
                    if (variable instanceof DynamicTsVariable) {
                        TsMoniker moniker = ((DynamicTsVariable) variable).getMoniker();
                        writeProviderInfo(moniker, regressorInfo, providerInfoHeaderRegressors);
                    }
                    regressorInfos.add(regressorInfo);
                }
            });
            HashMap<String, Header> headersRegressors = createHeaders(3, new Temp("p_", providerInfoHeaderRegressors));

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
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private void writeProviderInfo(TsMoniker moniker, IProviderInfo iProviderInfo, TreeSet<String> providerInfoHeader) {
        String source = moniker.getSource();
        Optional<? extends IProviderFactory> optionalProvider = Lookup.getDefault().lookupAll(IProviderFactory.class).stream().filter(provider -> provider.getSourceName().equalsIgnoreCase(source)).findFirst();
        if (!optionalProvider.isPresent()) {
            //TODO Log
            return;
        }
        IProvider provider = optionalProvider.get().getNewInstance();
        iProviderInfo.setProviderName(optionalProvider.get().getProviderName());
        Map<String, String> providerInfo = provider.writeTs(moniker);
        providerInfo.forEach((key, value) -> {
            iProviderInfo.addProviderInfo(key, value);
            providerInfoHeader.add(key);
        });
    }

    private void writeSpecificationInfo(SaItem item, SaItemInfo saItemInfo, TreeSet<String> specificationInfoHeader) {
        ISaSpecification domainSpecification = item.getDomainSpecification();
        String specName = domainSpecification.getClass().getName();

        Optional<? extends ISpecificationReaderFactory> optionalSpec = Lookup.getDefault().lookupAll(ISpecificationReaderFactory.class).stream().filter(specReader -> specReader.getSupportedClass().equalsIgnoreCase(specName)).findFirst();
        if (!optionalSpec.isPresent()) {
            //TODO Log
            return;
        }
        ISpecificationReader specReader = optionalSpec.get().getNewInstance();
        saItemInfo.setSpecificationName(optionalSpec.get().getSpecificationName());
        Map<String, String> specInfo = specReader.writeSpecification(domainSpecification);
        specInfo.forEach((key, value) -> {
            saItemInfo.addSpecificationInfo(key, value);
            specificationInfoHeader.add(key);
        });
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

    private HashMap<String, Header> createHeaders(int offset, Temp... temps) {
        int counter = offset;
        HashMap<String, Header> headers = new HashMap<>();
        for (Temp temp : temps) {
            for (String string : temp.getSet()) {
                headers.put(string, new Header(temp.getPrefix() + string, counter++));
            }
        }
        return headers;
    }

    @lombok.Value
    private class Header {

        private final String name;
        private final int position;

    }

    @lombok.Value
    private class Temp {

        private final String prefix;
        private final TreeSet<String> set;
    }
}
