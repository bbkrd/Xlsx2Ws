package de.bundesbank.jdemetra.xlsx2ws.converter;

import de.bundesbank.jdemetra.xlsx2ws.Creator;
import de.bundesbank.jdemetra.xlsx2ws.Writer;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.InformationDTO;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.DOCUMENT_NAME;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.ITEM_NAME;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.METADATA;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.PROVIDER_INFO;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.PROVIDER_NAME;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.SPECIFICATION_INFO;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.SPECIFICATION_NAME;
import de.bundesbank.jdemetra.xlsx2ws.dto.Message;
import de.bundesbank.jdemetra.xlsx2ws.dto.MetaDataSetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.MultiDocSetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.PositionInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.ReportItem;
import de.bundesbank.jdemetra.xlsx2ws.dto.SaItemInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.SpecificationDTO;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationFactory;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationReader;
import de.bundesbank.jdemetra.xlsx2ws.spec.ISpecificationWriter;
import ec.nbdemetra.sa.MultiProcessingDocument;
import ec.nbdemetra.sa.MultiProcessingManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.satoolkit.ISaSpecification;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsMoniker;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tstoolkit.MetaData;
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
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.poi.ss.usermodel.Cell;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

@ServiceProviders({
    @ServiceProvider(service = SheetReader.class, position = 1_000_000),
    @ServiceProvider(service = SheetWriter.class, position = 1_000_000)
})
public class MultiDocumentConverter implements SheetReader, SheetWriter {

    private static final String MULTIDOCS = "multidocs";
    private static final String REPORT_ITEM_TYPE = "Multidocument";

    private final Map<String, Set<String>> map = new HashMap<>();

    @Override
    public void writeSheet(XSSFWorkbook workbook, Map<String, ISetting> settings) {
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
                            Writer.writePureDataInfo(item.getName(), item.getTs().getTsData(), saItemInfo, providerInfoHeaderSaItems);
                        } else {
                            Writer.writeProviderInfo(originalMoniker, saItemInfo, providerInfoHeaderSaItems);
                        }
                        writeSpecificationInfo(item, saItemInfo, specificationInfoHeader, settings);
                        saItemInfos.add(saItemInfo);
                    });
                });

        HashMap<String, Header> headersSaItems = Writer.createHeaders(4,
                new PrefixSet("prov_", providerInfoHeaderSaItems),
                new PrefixSet("spec_", specificationInfoHeader),
                new PrefixSet("meta_", metaInfoHeader));

        XSSFSheet sheet = createSheet(workbook, headersSaItems);

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
    }

    private XSSFSheet createSheet(XSSFWorkbook workbook, HashMap<String, Header> headersSaItems) {
        //SAItems sheet
        XSSFSheet sheet = workbook.createSheet(MULTIDOCS);
        XSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("multidoc");
        headerRow.createCell(1).setCellValue("saitem");
        headerRow.createCell(2).setCellValue("providername");
        headerRow.createCell(3).setCellValue("specificationname");
        for (Header value : headersSaItems.values()) {
            XSSFCell cell = headerRow.createCell(value.getPosition());
            cell.setCellValue(value.getName());
        }
        return sheet;
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

    @Override
    public List<ReportItem> read(File file) {
        List<ReportItem> reportItems = new ArrayList<>();
        map.clear();

        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        List<WorkspaceItem<MultiProcessingDocument>> existingDocuments = ws.searchDocuments(MultiProcessingDocument.class);
        existingDocuments.forEach((existingDocument) -> {
            String name = existingDocument.getDisplayName();
            Set<String> saItems = existingDocument.getElement().getCurrent().stream().map(SaItem::getRawName).collect(Collectors.toSet());
            map.put(name, saItems);
        });
        List<SaItemInfo> list = readSaItemSheet(file, reportItems);

        list.stream().map(this::processSaItemInformation).forEach(reportItems::add);
        return reportItems;
    }

    private List<SaItemInfo> readSaItemSheet(File selectedFile, List<ReportItem> reportItems) {
        try (FileInputStream excelFile = new FileInputStream(selectedFile);) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheet(MULTIDOCS);
            if (datatypeSheet == null) {
                //Fallback (old default)
                datatypeSheet = workbook.getSheetAt(0);
            }
            Iterator<Row> iterator = datatypeSheet.iterator();

            if (!iterator.hasNext()) {
                return new ArrayList<>();
            }

            Map<Integer, InformationDTO> headers = Creator.readHeaders(iterator.next());

            Set<String> names = new HashSet<>();
            List<SaItemInfo> list = new ArrayList<>();
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                SaItemInfo saItemInfo = new SaItemInfo(currentRow.getRowNum() + 1);
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
                String saItemName = saItemInfo.getSaItemName();
                if (!saItemInfo.isValid()) {
                    ArrayList<Message> messages = new ArrayList<>();
                    if (saItemName == null) {
                        messages.add(new Message(Level.SEVERE, String.format("Invalid item in row %1$s", saItemInfo.getRow())));
                        reportItems.add(new ReportItem(REPORT_ITEM_TYPE,
                                "Row " + saItemInfo.getRow(), messages, REPORT_ITEM_TYPE));
                    } else {
                        messages.add(new Message(Level.SEVERE, String.format("Invalid item %1$s in row %2$s",
                                saItemName, saItemInfo.getRow())));
                        reportItems.add(new ReportItem(saItemInfo.getMultidocName(), saItemName, messages, REPORT_ITEM_TYPE));
                    }
                    continue;

                }

                if (names.add(saItemInfo.getMultidocName() + "-" + saItemName)) {
                    list.add(saItemInfo);
                } else {
                    ArrayList<Message> messages = new ArrayList<>();
                    messages.add(new Message(Level.SEVERE, "Each item needs a unique name!"));
                    reportItems.add(new ReportItem(saItemInfo.getMultidocName(), saItemName != null ? saItemName : "", messages, REPORT_ITEM_TYPE));
                }
            }
            return list;
        } catch (IOException e) {
            Logger.getLogger(Creator.class.getName()).log(Level.SEVERE, null, e);
            return new ArrayList<>();
        }
    }

    private ReportItem processSaItemInformation(SaItemInfo information) {
        final String multiDocumentName = information.getMultidocName();
        final String saItemName = information.getSaItemName();
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
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

        Optional<Ts> readTs = Creator.readTs(information);
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
            if (old.getMetaData() != null) {
                meta = old.getMetaData().clone();
            }
        }

        if (ts == null) {
            messageList.add(new Message(Level.SEVERE, "No timeseries found!"));
        }

        if (meta == null) {
            meta = new MetaData(information.getMetaData());
        } else {
            meta.putAll(information.getMetaData());
        }
        ReportItem reportItem = new ReportItem(multiDocumentName, saItemName, messageList, REPORT_ITEM_TYPE);
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

    private SpecificationDTO readSpecification(SaItemInfo information, SaItem old) {
        String specificationName = information.getSpecificationName();
        ISaSpecification oldSpec = null;
        if (old != null) {
            oldSpec = old.getDomainSpecification();
        }

        if (specificationName == null) {
            if (oldSpec != null) {
                return new SpecificationDTO<>(oldSpec, new Message[]{new Message(Level.INFO, "No specification name declared, old specification will be used.")});
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

    @Override
    public void writeHeaderSheet(XSSFWorkbook workbook) {
        TreeSet<String> providerInfoHeaderSaItems = new TreeSet<>();
        TreeSet<PositionInfo> specificationInfoHeader = new TreeSet<>();
        TreeSet<String> metaInfoHeader = new TreeSet<>();

        Lookup.getDefault().lookupAll(IProviderFactory.class).stream()
                .map(IProviderFactory::getNewInstance)
                .map(IProvider::getAllHeaders)
                .forEach(providerInfoHeaderSaItems::addAll);

        Lookup.getDefault().lookupAll(ISpecificationFactory.class).stream()
                .map(ISpecificationFactory::getNewWriterInstance)
                .map(ISpecificationWriter::getAllHeaders)
                .forEach(specificationInfoHeader::addAll);

        Lookup.getDefault().lookupAll(IMetaData.class).stream()
                .filter(t -> t.getSupportedClass().contains(MultiProcessingDocument.class))
                .map(IMetaData::getAllHeaders)
                .forEach(metaInfoHeader::addAll);

        HashMap<String, Header> headersSaItems = Writer.createHeaders(4,
                new PrefixSet("prov_", providerInfoHeaderSaItems),
                new PrefixSet("spec_", specificationInfoHeader),
                new PrefixSet("meta_", metaInfoHeader));

        createSheet(workbook, headersSaItems);
    }

}
