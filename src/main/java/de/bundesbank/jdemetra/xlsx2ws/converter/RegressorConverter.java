package de.bundesbank.jdemetra.xlsx2ws.converter;

import de.bundesbank.jdemetra.xlsx2ws.Creator;
import static de.bundesbank.jdemetra.xlsx2ws.Creator.readHeaders;
import static de.bundesbank.jdemetra.xlsx2ws.Creator.readTs;
import static de.bundesbank.jdemetra.xlsx2ws.Writer.createHeaders;
import static de.bundesbank.jdemetra.xlsx2ws.Writer.writeProviderInfo;
import static de.bundesbank.jdemetra.xlsx2ws.Writer.writePureDataInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.dto.InformationDTO;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.DOCUMENT_NAME;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.ITEM_NAME;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.PROVIDER_INFO;
import static de.bundesbank.jdemetra.xlsx2ws.dto.InformationType.PROVIDER_NAME;
import de.bundesbank.jdemetra.xlsx2ws.dto.Message;
import de.bundesbank.jdemetra.xlsx2ws.dto.RegressorInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.ReportItem;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import ec.nbdemetra.ui.variables.VariablesDocumentManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.nbdemetra.ws.WorkspaceItem;
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tss.TsMoniker;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    @ServiceProvider(service = SheetReader.class, position = 2_000_000),
    @ServiceProvider(service = SheetWriter.class, position = 2_000_000)
})
public class RegressorConverter implements SheetReader, SheetWriter {

    private static final String REGRESSORS = "regressors";

    @Override
    public void writeSheet(XSSFWorkbook workbook, Map<String, ISetting> settings) {
        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();

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

        XSSFSheet regressors = createSheet(workbook, headersRegressors);
        int counter = 1;
        for (RegressorInfo regressorInfo : regressorInfos) {
            XSSFRow row = regressors.createRow(counter++);
            row.createCell(0).setCellValue(regressorInfo.getDocumentName());
            row.createCell(1).setCellValue(regressorInfo.getName());
            row.createCell(2).setCellValue(regressorInfo.getProviderName());
            regressorInfo.getProviderInfos().forEach((key, value) -> row.createCell(headersRegressors.get(key).getPosition()).setCellValue(value));
        }
    }

    private XSSFSheet createSheet(XSSFWorkbook workbook, HashMap<String, Header> headersRegressors) {
        //Regressor sheet
        XSSFSheet regressors = workbook.createSheet(REGRESSORS);
        XSSFRow headerRow = regressors.createRow(0);
        headerRow.createCell(0).setCellValue("variables");
        headerRow.createCell(1).setCellValue("name");
        headerRow.createCell(2).setCellValue("providername");
        for (Header value : headersRegressors.values()) {
            XSSFCell cell = headerRow.createCell(value.getPosition());
            cell.setCellValue(value.getName());
        }
        return regressors;
    }

    private final Map<String, Set<String>> variablesMap = new HashMap<>();

    @Override
    public List<ReportItem> read(File file) {
        List<ReportItem> reportItems = new ArrayList<>();
        variablesMap.clear();

        Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();
        List<WorkspaceItem<?>> existingTsVariables = ws.searchDocuments(VariablesDocumentManager.ID);
        existingTsVariables.forEach((existingTsVariable) -> {
            Object element = existingTsVariable.getElement();
            if (element instanceof TsVariables) {
                String name = existingTsVariable.getDisplayName();
                Set<String> variables = new HashSet<>(Arrays.asList(((TsVariables) element).getNames()));
                variablesMap.put(name, variables);
            }
        });

        List<RegressorInfo> regressors = readRegressorSheet(file, reportItems);

        regressors.stream().map(this::processRegressorInformation).forEach(reportItems::add);
        return reportItems;
    }

    private List<RegressorInfo> readRegressorSheet(File selectedFile, List<ReportItem> reportItems) {
        try (FileInputStream excelFile = new FileInputStream(selectedFile)) {
            Workbook workbook = new XSSFWorkbook(excelFile);
            if (workbook.getNumberOfSheets() < 2) {
                return new ArrayList<>();
            }
            Sheet datatypeSheet = workbook.getSheet(REGRESSORS);
            if (datatypeSheet == null) {
                datatypeSheet = workbook.getSheetAt(1);
            }
            Iterator<Row> iterator = datatypeSheet.iterator();

            if (!iterator.hasNext()) {
                return new ArrayList<>();
            }

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
                    reportItems.add(new ReportItem("", info.getName() != null ? info.getName() : "", messages, "Error"));
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
            if (ts.getMoniker().getId() == null || ts.getMoniker().getSource() == null) {
                document.set(itemName, new TsVariable(ts.getRawName(), ts.getTsData()));
            } else {
                document.set(itemName, new DynamicTsVariable(ts.getRawName(), ts.getMoniker(), ts.getTsData()));
            }

        }
        return new ReportItem(variablesListName, itemName, messageList, "TsVariables");
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

    @Override
    public void writeHeaderSheet(XSSFWorkbook workbook) {
        TreeSet<String> providerInfoHeaderRegressors = new TreeSet<>();

        Lookup.getDefault().lookupAll(IProviderFactory.class).stream()
                .map(IProviderFactory::getNewInstance)
                .map(IProvider::getAllHeaders)
                .forEach(providerInfoHeaderRegressors::addAll);

        HashMap<String, Header> headersRegressors = createHeaders(3, new PrefixSet("prov_", providerInfoHeaderRegressors));
        createSheet(workbook, headersRegressors);
    }

}
