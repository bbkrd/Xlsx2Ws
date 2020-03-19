/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.dto.IProviderInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.InformationDTO;
import de.bundesbank.jdemetra.xlsx2ws.dto.Message;
import de.bundesbank.jdemetra.xlsx2ws.dto.RegressorInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.SaItemInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.SpecificationDTO;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationReader;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ui.variables.VariablesDocumentManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tss.sa.SaItem;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.regression.TsVariables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Lookup;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationFactory;

/**
 *
 * @author Thomas Witthohn
 */
@Log
public class Creator {

    DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("dd. MMMM yyyy HH:mm:ss");
    String NEW_LINE = System.getProperty("line.separator");

    private final Map<String, Set<String>> map = new HashMap<>();
    private final Map<String, Set<String>> variablesMap = new HashMap<>();
    StringBuilder sb;

    public void createWorkspace(File selectedFile) {
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        readExistingWorkspace(ws);
        sb = new StringBuilder();
        sb.append("Please copy this protocol if you want to keep it!").append(NEW_LINE)
                .append("Protocol ").append(TIMESTAMP_FORMAT.format(System.currentTimeMillis()))
                .append('\t').append(System.getProperty("user.name")).append(NEW_LINE).append(NEW_LINE);
        String startingString = sb.toString();

        readRegressorSheet(selectedFile);

        List<SaItemInfo> list = readSaItemSheet(selectedFile);

        list.stream().forEach(information -> {
            String multiDocumentName = information.getMultidocName();
            String saItemName = information.getSaItemName();
            SaItem old = null;
            MultiProcessingDocument document = null;

            if (map.containsKey(multiDocumentName)) {
                WorkspaceItem<?> doc = ws.searchDocumentByName(MultiProcessingManager.ID, multiDocumentName);
                document = (MultiProcessingDocument) doc.getElement();
                if (map.get(multiDocumentName).contains(saItemName)) {
                    old = document.getCurrent().stream().filter(item -> item.getRawName().equals(saItemName)).findFirst().orElse(null);
                    //return;
                }
            }

            Ts ts = readTs(information);
            SpecificationDTO specificationDTO = readSpecification(information, old);

            Message[] messages = specificationDTO.getMessages();
            if (messages.length != 0) {
                sb.append("Messages for ").append(multiDocumentName).append("->").append(saItemName).append(":").append(NEW_LINE);
                for (Message message : messages) {
                    sb.append(message.getType()).append(" - ").append(message.getText()).append(NEW_LINE);
                }
            }

            ISaSpecification specification = specificationDTO.getSpecification();
            if (specification == null) {
                return;
            }

            if (old == null) {
                if (ts == null) {
                    //TODO LOG
                    return;
                }
                SaItem item = new SaItem(specification, ts);
                item.setName(saItemName);
                item.setMetaData(new MetaData(information.getMetaData()));
                if (document == null) {
                    document = createAbsentMultiDoc(multiDocumentName);
                }
                map.get(multiDocumentName).add(saItemName);
                document.getCurrent().add(item);
            } else {
                if (ts == null && information.getMetaData().isEmpty() && old.getDomainSpecification().equals(specification)) {
                    //TODO LOG (No change)
                    return;
                }
                ts = ts == null ? old.getTs() : ts;
                MetaData meta = old.getMetaData();
                if (meta == null) {
                    meta = new MetaData(information.getMetaData());
                } else {
                    meta = meta.clone();
                    meta.putAll(information.getMetaData());
                }
                SaItem item = new SaItem(specification, ts);
                item.setName(saItemName);
                item.setMetaData(meta);
                if (document == null) {
                    document = createAbsentMultiDoc(multiDocumentName);
                }
                document.getCurrent().replace(old, item);
            }

        });

        if (startingString.equals(sb.toString())) {
            sb.append("Everything is fine!");
        }
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private void readExistingWorkspace(Workspace ws) {
        List<WorkspaceItem<MultiProcessingDocument>> existingDocuments = ws.searchDocuments(MultiProcessingDocument.class);
        existingDocuments.forEach((existingDocument) -> {
            String name = existingDocument.getDisplayName();
            Set<String> saItems = existingDocument.getElement().getCurrent().stream().map(SaItem::getRawName).collect(Collectors.toSet());
            map.put(name, saItems);
        });

        List<WorkspaceItem<TsVariables>> existingTsVariables = ws.searchDocuments(TsVariables.class);
        existingTsVariables.forEach((existingTsVariable) -> {
            String name = existingTsVariable.getDisplayName();
            Set<String> variables = new HashSet<>(Arrays.asList(existingTsVariable.getElement().getNames()));
            variablesMap.put(name, variables);
        });
    }

    private List<SaItemInfo> readSaItemSheet(File selectedFile) {
        try (FileInputStream excelFile = new FileInputStream(selectedFile);) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            Map<Integer, InformationDTO> headers = readHeaders(iterator.next());

            List<SaItemInfo> list = new ArrayList<>();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                SaItemInfo saItemInfo = new SaItemInfo();
                for (Iterator<Cell> cellIterator = currentRow.cellIterator(); cellIterator.hasNext();) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (headers.containsKey(columnIndex)) {
                        InformationDTO informationDTO = headers.get(columnIndex);
                        String information = "";
                        switch (cell.getCellTypeEnum()) {
                            case NUMERIC:
                                information = Double.toString(cell.getNumericCellValue());
                                if (information.endsWith(".0")) {
                                    information = information.substring(0, information.length() - 2);
                                }
                                break;
                            case STRING:
                                information = cell.getStringCellValue();
                                break;
                        }
                        switch (informationDTO.getType()) {
                            case DOCUMENT_NAME:
                                saItemInfo.setMultidocName(information);
                                break;
                            case ITEM_NAME:
                                saItemInfo.setSaItemName(information);
                                break;
                            case PROVIDER_NAME:
                                saItemInfo.setProviderName(information);
                                break;
                            case SPECIFICATION_NAME:
                                saItemInfo.setSpecificationName(information);
                                break;
                            case PROVIDER_INFO:
                                saItemInfo.addProviderInfo(informationDTO.getName(), information);
                                break;
                            case SPECIFICATION_INFO:
                                saItemInfo.addSpecificationInfo(informationDTO.getName(), information);
                                break;
                            case METADATA:
                            //TODO: Different view on none Metadata?
                            default:
                                saItemInfo.addMetaData(informationDTO.getName(), information);
                                break;
                        }
                    }

                }
                if (saItemInfo.isValid()) {
                    list.add(saItemInfo);
                }
            }
            return list;
        } catch (IOException e) {
            Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, e);
            return new ArrayList<>();
        }
    }

    private MultiProcessingDocument createAbsentMultiDoc(String name) {
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        WorkspaceItem<?> doc = ws.searchDocumentByName(MultiProcessingManager.ID, name);

        if (doc == null) {
            MultiProcessingManager mgr = WorkspaceFactory.getInstance().getManager(MultiProcessingManager.class);
            doc = mgr.create(ws);
            doc.setDisplayName(name);
            map.put(name, new HashSet<>());
        }
        if (doc.getElement() instanceof MultiProcessingDocument) {
            return (MultiProcessingDocument) doc.getElement();
        }
        return null;
    }

    private Map<Integer, InformationDTO> readHeaders(Row headerRow) {
        Map<Integer, InformationDTO> headers = new TreeMap();
        for (Iterator<Cell> cellIterator = headerRow.cellIterator(); cellIterator.hasNext();) {
            Cell cell = cellIterator.next();
            if (cell.getCellTypeEnum() == CellType.STRING) {
                int columnIndex = cell.getColumnIndex();
                String information = cell.getStringCellValue();
                headers.put(columnIndex, new InformationDTO(information));
            }
        }
        return headers;
    }

    private Ts readTs(IProviderInfo information) {
        String providerName = information.getProviderName();
        Optional<? extends IProviderFactory> optionalProvider = Lookup.getDefault().lookupAll(IProviderFactory.class).stream().filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName)).findFirst();
        if (!optionalProvider.isPresent()) {
            //TODO Log
            return null;
        }

        IProvider provider = optionalProvider.get().getNewInstance();
        information.getProviderInfos().entrySet().forEach((entry) -> {
            provider.putInformation(entry.getKey(), entry.getValue());
        });

        Ts ts = provider.readTs();
        return ts;
    }

    private SpecificationDTO readSpecification(SaItemInfo information, SaItem old) {
        String specificationName = information.getSpecificationName();
        if (specificationName == null) {
            if (old != null) {
                return new SpecificationDTO<>(old.getDomainSpecification(), new Message[]{new Message(Level.INFO, "No specification name declared, old specficition will be used.")});
            } else {
                return new SpecificationDTO<>(null, new Message[]{new Message(Level.SEVERE, "Neither specification name was declared nor an old specification available as fallback.")});
            }
        }
        Optional<? extends ISpecificationFactory> optionalSpecificationReader = Lookup.getDefault().lookupAll(ISpecificationFactory.class).stream().filter(spec -> spec.getSpecificationName().equalsIgnoreCase(specificationName)).findFirst();
        if (!optionalSpecificationReader.isPresent()) {
            return new SpecificationDTO<>(null, new Message[]{new Message(Level.SEVERE, specificationName + " isn't a supported specification.")});
        }
        ISpecificationReader specificationReader = optionalSpecificationReader.get().getNewReaderInstance();
        information.getSpecificationInfos().entrySet().forEach((entry) -> {
            specificationReader.putInformation(entry.getKey(), entry.getValue());
        });

        ISaSpecification oldSpec = null;
        if (old != null) {
            oldSpec = old.getDomainSpecification();
        }
        SpecificationDTO specificationDTO = specificationReader.readSpecification(oldSpec);
        return specificationDTO;
    }

    private void readRegressorSheet(File selectedFile) {
        try (FileInputStream excelFile = new FileInputStream(selectedFile);) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            if (workbook.getNumberOfSheets() < 2) {
                return;
            }
            Sheet datatypeSheet = workbook.getSheet("regressor") == null ? workbook.getSheetAt(1) : workbook.getSheet("regressor");
            Iterator<Row> iterator = datatypeSheet.iterator();

            Map<Integer, InformationDTO> headers = readHeaders(iterator.next());

            List<RegressorInfo> regressorInfos = new ArrayList<>();

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                RegressorInfo info = new RegressorInfo();
                for (Iterator<Cell> cellIterator = currentRow.cellIterator(); cellIterator.hasNext();) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (headers.containsKey(columnIndex)) {
                        InformationDTO informationDTO = headers.get(columnIndex);
                        String information = "";
                        switch (cell.getCellTypeEnum()) {
                            case NUMERIC:
                                information = Double.toString(cell.getNumericCellValue());
                                if (information.endsWith(".0")) {
                                    information = information.substring(0, information.length() - 2);
                                }
                                break;
                            case STRING:
                                information = cell.getStringCellValue();
                                break;
                        }
                        switch (informationDTO.getType()) {
                            case DOCUMENT_NAME:
                                info.setDocumentName(information);
                                break;
                            case ITEM_NAME:
                                info.setName(information);
                                break;
                            case PROVIDER_NAME:
                                info.setProviderName(information);
                                break;
                            case PROVIDER_INFO:
                                info.addProviderInfo(informationDTO.getName(), information);
                                break;
                        }
                    }
                }
                regressorInfos.add(info);
            }
            if (!regressorInfos.isEmpty()) {
                sb.append("Messages for regressors:").append(NEW_LINE);
            }

            regressorInfos.forEach(information -> {
                String variablesListName = information.getDocumentName();
                String itemName = information.getName();
                boolean alreadyExists = false;

                Ts ts = readTs(information);
                if (ts == null) {
                    //TODO Log
                    return;
                }
                if (variablesMap.containsKey(variablesListName)
                        && variablesMap.get(variablesListName).contains(itemName)) {
                    sb.append(itemName).append(" in ").append(variablesListName).append(" was replaced.").append(NEW_LINE);
                    alreadyExists = true;
                }
                TsVariables document = createAbsentVariablesList(variablesListName);
                variablesMap.get(variablesListName).add(itemName);
                if (alreadyExists) {
                    document.remove(itemName);
                }
                document.set(itemName, new DynamicTsVariable(ts.getRawName(), ts.getMoniker(), ts.getTsData()));
            });

        } catch (IOException e) {
            Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private TsVariables createAbsentVariablesList(String name) {
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        WorkspaceItem<?> doc = ws.searchDocumentByName(VariablesDocumentManager.ID, name);
        if (doc == null) {
            VariablesDocumentManager mgr = WorkspaceFactory.getInstance().getManager(VariablesDocumentManager.class);
            doc = mgr.create(ws);
            doc.setDisplayName(name);
            ProcessingContext.getActiveContext().getTsVariableManagers().rename(doc.getIdentifier(), name);

            variablesMap.put(name, new HashSet<>());
        }
        if (doc.getElement() instanceof TsVariables) {
            return (TsVariables) doc.getElement();
        }
        return null;
    }
}
