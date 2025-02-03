/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws;

import de.bundesbank.jdemetra.xlsx2ws.converter.PrefixSet;
import de.bundesbank.jdemetra.xlsx2ws.converter.Header;
import de.bundesbank.jdemetra.xlsx2ws.dto.IProviderInfo;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.provider.GenericProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.GenericProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.IProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.provider.PureDataProvider;
import de.bundesbank.jdemetra.xlsx2ws.provider.PureDataProviderFactory;
import de.bundesbank.jdemetra.xlsx2ws.converter.SheetWriter;
import ec.nbdemetra.ui.calendars.CalendarDocumentManager;
import ec.nbdemetra.ws.Workspace;
import ec.nbdemetra.ws.WorkspaceFactory;
import ec.tss.TsMoniker;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import lombok.extern.java.Log;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Lookup;

@Log
public class Writer {

    public void writeWorkspace(File file) {
        writeWorkspace(file, null);
    }

    public void writeWorkspace(File file, Map<String, ISetting> settings) {
        try (final FileOutputStream fileOut = new FileOutputStream(file); XSSFWorkbook workbook = new XSSFWorkbook()) {
            Workspace ws = WorkspaceFactory.getInstance().getActiveWorkspace();

            Lookup.getDefault().lookupAll(SheetWriter.class).forEach(sheetWriter -> {
                sheetWriter.writeSheet(workbook, settings);
            });

            workbook.write(fileOut);
            if (ws.searchCompatibleDocuments(CalendarDocumentManager.ID).size() > 1) {
                JOptionPane.showMessageDialog(null, "User defined calendars will not be saved!", "Warning", JOptionPane.WARNING_MESSAGE);

            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void writeHeaderXlsx(File file) {
        try (final FileOutputStream fileOut = new FileOutputStream(file); XSSFWorkbook workbook = new XSSFWorkbook()) {
            Lookup.getDefault().lookupAll(SheetWriter.class).forEach(sheetWriter -> {
                sheetWriter.writeHeaderSheet(workbook);
            });
            workbook.write(fileOut);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void writeProviderInfo(TsMoniker moniker, IProviderInfo iProviderInfo, TreeSet<String> providerInfoHeader) {
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

    public static void writePureDataInfo(String name, TsData tsData, IProviderInfo iProviderInfo, TreeSet<String> providerInfoHeader) {
        PureDataProvider provider = new PureDataProvider();
        iProviderInfo.setProviderName(PureDataProviderFactory.NAME);
        Map<String, String> providerInfo = provider.writeData(name, tsData);
        providerInfo.forEach((key, value) -> {
            iProviderInfo.addProviderInfo(key, value);
            providerInfoHeader.add(key);
        });
    }

    public static HashMap<String, Header> createHeaders(int offset, PrefixSet... prefixSets) {
        int counter = offset;
        HashMap<String, Header> headers = new HashMap<>();
        for (PrefixSet prefixSet : prefixSets) {
            for (Object o : prefixSet.getSet()) {
                headers.put(o.toString(), new Header(prefixSet.getPrefix() + o.toString(), counter++));
            }
        }
        return headers;
    }
}
