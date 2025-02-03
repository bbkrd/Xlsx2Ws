/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.dto.IProviderInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.InformationDTO;
import de.bundesbank.jdemetra.xlsx2ws.dto.ReportItem;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.converter.SheetReader;
import ec.tss.Ts;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.extern.java.Log;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.openide.util.Lookup;

/**
 *
 * @author Thomas Witthohn
 */
@Log
public class Creator {

    List<ReportItem> reportItems;

    public void createWorkspace(File selectedFile) {
        reportItems = new ArrayList<>();

        //Add Lookup
        Lookup.getDefault().lookupAll(SheetReader.class).forEach((t) -> {
            reportItems.addAll(t.read(selectedFile));
        });

        Report report = new Report(reportItems);
        report.setVisible(true);
    }

    public static Map<Integer, InformationDTO> readHeaders(Row headerRow) {
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

    public static Optional<Ts> readTs(IProviderInfo information) {
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

}
