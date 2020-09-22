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
import de.bundesbank.jdemetra.xlsx2ws.dto.ReportItem;
import de.bundesbank.jdemetra.xlsx2ws.dto.SaItemInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.SpecificationDTO;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationFactory;
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
import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Lookup;

/**
 *
 * @author Thomas Witthohn
 */
@Log
public class Creator {

    private final Map<String, Set<String>> map = new HashMap<>();
    private final Map<String, Set<String>> variablesMap = new HashMap<>();
    private Workspace ws;
    List<ReportItem> reportItems;

    public void createWorkspace(File selectedFile) {
        ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        readExistingWorkspace(ws);
        reportItems = new ArrayList<>();

        List<RegressorInfo> regressors = readRegressorSheet(selectedFile);

        List<SaItemInfo> list = readSaItemSheet(selectedFile);

        list.stream().map(this::processSaItemInformation).forEach(reportItems::add);
        regressors.stream().map(this::processRegressorInformation).forEach(reportItems::add);

        Report report = new Report(reportItems);
        report.setVisible(true);
    }

    private ReportItem processSaItemInformation(SaItemInfo information) {
        final String multiDocumentName = information.getMultidocName();
        final String saItemName = information.getSaItemName();
        SaItem old = null;
        MultiProcessingDocument document = null;

        ArrayList<Message> messageList = new ArrayList<>();

        if (map.containsKey(multiDocumentName)) {
            WorkspaceItem<?> doc = ws.searchDocumentByName(MultiProcessingManager.ID, multiDocumentName);
            document = (MultiProcessingDocument) doc.getElement();
            if (map.get(multiDocumentName).contains(saItemName)) {
                old = document.getCurrent().stream().filter(item -> item.getRawName().equals(saItemName)).findFirst().orElse(null);
            }
        }

        Optional<Ts> readTs = readTs(information);
        SpecificationDTO specificationDTO = readSpecification(information, old);

        Message[] messages = specificationDTO.getMessages();
        Arrays.stream(messages).forEach(messageList::add);

        ISaSpecification specification = specificationDTO.getSpecification();

        Ts ts = null;
        if (readTs.isPresent()) {
            ts = readTs.get();
            String invalidDataCause = ts.getInvalidDataCause();
            if (invalidDataCause != null && !invalidDataCause.isEmpty()) {
                messageList.add(new Message(Level.SEVERE, "Timeseries has an error \"" + invalidDataCause + "\"!"));
            }
        }
        MetaData meta = null;
        if (old != null) {
            ts = ts == null ? old.getTs() : ts;
            meta = old.getMetaData().clone();
        }

        if (ts == null) {
            messageList.add(new Message(Level.SEVERE, "No timeseries found!"));
        }

        if (meta == null) {
            meta = new MetaData(information.getMetaData());
        } else {
            meta.putAll(information.getMetaData());
        }
        ReportItem reportItem = new ReportItem(multiDocumentName, saItemName, messageList, false);
        if (reportItem.getHighestLevel() <= Level.WARNING.intValue()) {
            SaItem item = new SaItem(specification, ts);
            item.setName(saItemName);
            item.setMetaData(meta);
            if (document == null) {
                document = createAbsentMultiDoc(multiDocumentName);
            }
            if (old != null) {
                document.getCurrent().replace(old, item);
            } else {
                map.get(multiDocumentName).add(saItemName);
                document.getCurrent().add(item);
            }
        }
        return reportItem;

    }

    private void readExistingWorkspace(Workspace ws) {
        List<WorkspaceItem<MultiProcessingDocument>> existingDocuments = ws.searchDocuments(MultiProcessingDocument.class);
        existingDocuments.forEach((existingDocument) -> {
            String name = existingDocument.getDisplayName();
            Set<String> saItems = existingDocument.getElement().getCurrent().stream().map(SaItem::getRawName).collect(Collectors.toSet());
            map.put(name, saItems);
        });
        List<WorkspaceItem<?>> existingTsVariables = ws.searchDocuments(VariablesDocumentManager.ID);

        existingTsVariables.forEach((existingTsVariable) -> {
            Object element = existingTsVariable.getElement();
            if (element instanceof TsVariables) {
                String name = existingTsVariable.getDisplayName();
                Set<String> variables = new HashSet<>(Arrays.asList(((TsVariables) element).getNames()));
                variablesMap.put(name, variables);
            }
        });
    }

    private List<SaItemInfo> readSaItemSheet(File selectedFile) {
        try (FileInputStream excelFile = new FileInputStream(selectedFile);) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            Map<Integer, InformationDTO> headers = readHeaders(iterator.next());

            Set<String> names = new HashSet<>();
            List<SaItemInfo> list = new ArrayList<>();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                SaItemInfo saItemInfo = new SaItemInfo();
                for (Iterator<Cell> cellIterator = currentRow.cellIterator(); cellIterator.hasNext();) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (headers.containsKey(columnIndex)) {
                        InformationDTO informationDTO = headers.get(columnIndex);
                        String information;
                        switch (cell.getCellType()) {
                            case NUMERIC:
                                information = Double.toString(cell.getNumericCellValue());
                                if (information.endsWith(".0")) {
                                    information = information.substring(0, information.length() - 2);
                                }
                                break;
                            case STRING:
                                information = cell.getStringCellValue();
                                break;
                            default:
                                information = "";
                        }
                        if (information.trim().isEmpty()) {
                            continue;
                        }
                        switch (informationDTO.getType()) {
                            case DOCUMENT_NAME:
                                saItemInfo.setMultidocName(information.trim());
                                break;
                            case ITEM_NAME:
                                saItemInfo.setSaItemName(information.trim());
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
                if (saItemInfo.isValid() && names.add(saItemInfo.getSaItemName())) {
                    list.add(saItemInfo);
                } else {
                    ArrayList<Message> messages = new ArrayList<>();
                    messages.add(new Message(Level.SEVERE, "Each item needs a unique name!"));
                    reportItems.add(new ReportItem(saItemInfo.getMultidocName(), saItemInfo.getSaItemName() != null ? saItemInfo.getSaItemName() : "", messages, false));
                }
            }
            return list;
        } catch (IOException e) {
            Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, e);
            return new ArrayList<>();
        }
    }

    private MultiProcessingDocument createAbsentMultiDoc(String name) {
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
            if (cell.getCellType() == CellType.STRING) {
                int columnIndex = cell.getColumnIndex();
                String information = cell.getStringCellValue();
                headers.put(columnIndex, new InformationDTO(information));
            }
        }
        return headers;
    }

    private Optional<Ts> readTs(IProviderInfo information) {
        String providerName = information.getProviderName();
        Optional<? extends IProviderFactory> optionalProvider = Lookup.getDefault().lookupAll(IProviderFactory.class).stream().filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName)).findFirst();
        if (!optionalProvider.isPresent()) {
            //TODO Log
            return Optional.empty();
        }

        IProvider provider = optionalProvider.get().getNewInstance();
        information.getProviderInfos().entrySet().forEach((entry) -> {
            provider.putInformation(entry.getKey(), entry.getValue());
        });

        Optional<Ts> ts = provider.readTs();
        return ts;
    }

    private SpecificationDTO readSpecification(SaItemInfo information, SaItem old) {
        String specificationName = information.getSpecificationName();
        ISaSpecification oldSpec = null;
        if (old != null) {
            oldSpec = old.getDomainSpecification();
        }

        if (specificationName == null) {
            if (oldSpec != null) {
                return new SpecificationDTO<>(oldSpec, new Message[]{new Message(Level.INFO, "No specification name declared, old specficition will be used.")});
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

        SpecificationDTO specificationDTO = specificationReader.readSpecification(oldSpec);
        return specificationDTO;
    }

    private List<RegressorInfo> readRegressorSheet(File selectedFile) {
        try (FileInputStream excelFile = new FileInputStream(selectedFile);) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            if (workbook.getNumberOfSheets() < 2) {
                return new ArrayList<>();
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
                        String information;
                        switch (cell.getCellType()) {
                            case NUMERIC:
                                information = Double.toString(cell.getNumericCellValue());
                                if (information.endsWith(".0")) {
                                    information = information.substring(0, information.length() - 2);
                                }
                                break;
                            case STRING:
                                information = cell.getStringCellValue();
                                break;
                            default:
                                information = "";
                        }
                        switch (informationDTO.getType()) {
                            case DOCUMENT_NAME:
                                info.setDocumentName(information.trim());
                                break;
                            case ITEM_NAME:
                                info.setName(information.trim());
                                break;
                            case PROVIDER_NAME:
                                info.setProviderName(information);
                                break;
                            case PROVIDER_INFO:
                                info.addProviderInfo(informationDTO.getName(), information);
                                break;
                            default:
                            //Ignore other input
                        }
                    }
                }
                if (info.isValid()) {
                    regressorInfos.add(info);
                } else {
                    ArrayList<Message> messages = new ArrayList<>();
                    messages.add(new Message(Level.SEVERE, "Each regressor needs a valid variables input!"));
                    reportItems.add(new ReportItem("", info.getName() != null ? info.getName() : "", messages, false));
                }

            }
            return regressorInfos;
        } catch (IOException e) {
            Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, e);
            return new ArrayList<>();
        }

    }

    private ReportItem processRegressorInformation(RegressorInfo regressorInfo) {
        String variablesListName = regressorInfo.getDocumentName();
        String itemName = regressorInfo.getName();
        boolean alreadyExists = false;

        ArrayList<Message> messageList = new ArrayList<>();

        Optional<Ts> readTs = readTs(regressorInfo);

        Ts ts = null;
        if (readTs.isPresent()) {
            ts = readTs.get();
            String invalidDataCause = ts.getInvalidDataCause();
            if (invalidDataCause != null && !invalidDataCause.isEmpty()) {
                messageList.add(new Message(Level.SEVERE, "Timeseries has an error \"" + invalidDataCause + "\"!"));
            }
        } else {
            messageList.add(new Message(Level.SEVERE, "No timeseries found!"));
        }

        if (ts != null) {
            if (variablesMap.containsKey(variablesListName)
                    && variablesMap.get(variablesListName).contains(itemName)) {
                messageList.add(new Message(Level.INFO, itemName + " in " + variablesListName + " was replaced."));
                alreadyExists = true;
            }
            TsVariables document = createAbsentVariablesList(variablesListName);
            variablesMap.get(variablesListName).add(itemName);
            if (alreadyExists) {
                document.remove(itemName);
            }
            document.set(itemName, new DynamicTsVariable(ts.getRawName(), ts.getMoniker(), ts.getTsData()));

        }
        return new ReportItem(variablesListName, itemName, messageList, true);
    }

    private TsVariables createAbsentVariablesList(String name) {
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
