/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderReader;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderReaderFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationReader;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationReaderFactory;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.sa.SaItem;
import ec.tstoolkit.MetaData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    Map<String, Set<String>> map = new HashMap<>();

    public void createWorkspace(File selectedFile) {
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        String fileName = selectedFile.getName();
        ws.setName(fileName.substring(0, fileName.length() - 5));
        ws.sort();
        List<AllRowInfo> list = readSaItemSheet(selectedFile);

        list.stream().forEach(information -> {
            String multiDocumentName = information.getMultidocName();
            String saItemName = information.getSaItemName();

            if (map.containsKey(multiDocumentName)
                    && map.get(multiDocumentName).contains(saItemName.toUpperCase(Locale.ENGLISH))) {
                //TODO Log
                return;
            }
            Ts ts = readTs(information);
            if (ts == null) {
                //TODO Log
                return;
            }

            ISaSpecification specification = readSpecification(information);
            if (specification == null) {
                //TODO Log
                return;
            }

            SaItem item = new SaItem(specification, ts);
            item.setName(saItemName);
            item.setMetaData(new MetaData(information.getMetaData()));

            MultiProcessingDocument document = createAbsentMultiDoc(multiDocumentName);
            map.get(multiDocumentName).add(saItemName.toUpperCase(Locale.ENGLISH));
            document.getCurrent().add(item);
        });
    }

    private List<AllRowInfo> readSaItemSheet(File selectedFile) {
        List<AllRowInfo> list = new ArrayList<>();
        try (FileInputStream excelFile = new FileInputStream(selectedFile);) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            Map<Integer, InformationDTO> headers = readHeaders(iterator.next());

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                AllRowInfo allRowInfos = new AllRowInfo();
                for (Iterator<Cell> cellIterator = currentRow.cellIterator(); cellIterator.hasNext();) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (headers.containsKey(columnIndex)) {
                        InformationDTO informationDTO = headers.get(columnIndex);
                        String information = "";
                        switch (cell.getCellTypeEnum()) {
                            case NUMERIC:
                                information = Double.toString(cell.getNumericCellValue());
                                break;
                            case STRING:
                                information = cell.getStringCellValue();
                                break;
                        }
                        switch (informationDTO.getType()) {
                            case MULTIDOCUMENT_NAME:
                                allRowInfos.setMultidocName(information);
                                break;
                            case SAITEM_NAME:
                                allRowInfos.setSaItemName(information);
                                break;
                            case PROVIDER_NAME:
                                allRowInfos.setProviderName(information);
                                break;
                            case SPECIFICATION_NAME:
                                allRowInfos.setSpecificationName(information);
                                break;
                            case PROVIDER_INFO:
                                allRowInfos.addProviderInfo(informationDTO.getName(), information);
                                break;
                            case SPECIFICATION_INFO:
                                allRowInfos.addSpecificationInfos(informationDTO.getName(), information);
                                break;
                            case METADATA:
                            //TODO: Different view on none Metadata?
                            default:
                                allRowInfos.addMetaData(informationDTO.getName(), information);
                                break;
                        }
                    }

                }
                if (allRowInfos.isValid()) {
                    list.add(allRowInfos);
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, e);
            return new ArrayList<>();
        }
        return list;
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

    private Ts readTs(AllRowInfo information) {
        String providerName = information.getProviderName();
        Optional<? extends IProviderReaderFactory> optionalProvider = Lookup.getDefault().lookupAll(IProviderReaderFactory.class).stream().filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName)).findFirst();
        if (!optionalProvider.isPresent()) {
            //TODO Log
            return null;
        }

        IProviderReader provider = optionalProvider.get().getNewInstance();
        information.getProviderInfos().entrySet().forEach((entry) -> {
            provider.putInformation(entry.getKey(), entry.getValue());
        });

        Ts ts = provider.readTs();
        return ts;
    }

    private ISaSpecification readSpecification(AllRowInfo information) {
        String specificationName = information.getSpecificationName();
        Optional<? extends ISpecificationReaderFactory> optionalSpecificationReader = Lookup.getDefault().lookupAll(ISpecificationReaderFactory.class).stream().filter(spec -> spec.getSpecificationName().equalsIgnoreCase(specificationName)).findFirst();
        if (!optionalSpecificationReader.isPresent()) {
            return null;
        }
        ISpecificationReader specificationReader = optionalSpecificationReader.get().getNewInstance();
        information.getSpecificationInfos().entrySet().forEach((entry) -> {
            specificationReader.putInformation(entry.getKey(), entry.getValue());
        });

        ISaSpecification specification = specificationReader.readSpecification();
        return specification;
    }
}
