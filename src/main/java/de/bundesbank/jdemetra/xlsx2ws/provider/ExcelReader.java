/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.spreadsheet.SpreadSheetBean;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProvider;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.openide.util.Lookup;

/**
 *
 * @author Thomas Witthohn
 */
@Log
public class ExcelReader implements IProvider {

    private static final String FILE_NOT_SUPPORTED = "File not supported";
    public static final String TIMESERIES_KEY = "timeserieskey";
    public static final String PATH = "path";
    public static final String SHEET = "sheet";
    private final Map<String, String> informations = new HashMap<>();

    @Override
    public Optional<Ts> readTs() {
        if (!informations.containsKey(TIMESERIES_KEY) || !informations.containsKey(PATH) || !informations.containsKey(SHEET)) {
            return Optional.empty();
        }
        String seriesName = informations.get(TIMESERIES_KEY);
        String path = informations.get(PATH);
        String sheetName = informations.get(SHEET);

        try {
            File file = new File(path);

            SpreadSheetProvider provider = Lookup.getDefault().lookup(SpreadSheetProvider.class);
            if (!provider.accept(file)) {
                return createTsWithInvalidDataCause(FILE_NOT_SUPPORTED);
            }

            SpreadSheetBean bean = new SpreadSheetBean();
            bean.setFile(file);
            DataSource spreadsheet = bean.toDataSource(SpreadSheetProvider.SOURCE, SpreadSheetProvider.VERSION);

            List<DataSet> sheets = provider.children(spreadsheet);
            Optional<DataSet> specifiedSheet = sheets.stream().filter(i -> i.getParam("sheetName").get().equals(sheetName)).findFirst();
            if (!specifiedSheet.isPresent()) {
                String error = "Sheet " + sheetName + " doesn't exist.";
                return createTsWithInvalidDataCause(error);
            }

            List<DataSet> series = provider.children(specifiedSheet.get());
            Optional<DataSet> specifiedSeries = series.stream().filter(i -> i.getParam("seriesName").get().equals(seriesName)).findFirst();
            if (!specifiedSeries.isPresent()) {
                String error = "Series " + seriesName + " doesn't exist in sheet " + sheetName + ".";
                return createTsWithInvalidDataCause(error);
            }

            TsMoniker moniker = provider.toMoniker(specifiedSeries.get());
            return Optional.of(TsFactory.instance.createTs(null, moniker, TsInformationType.All));
        } catch (IllegalArgumentException | IOException ex) {
            log.log(Level.SEVERE, null, ex);
            return Optional.empty();
        }
    }

    private Optional<Ts> createTsWithInvalidDataCause(String error) {
        Ts createTs = TsFactory.instance.createTs();
        createTs.setInvalidDataCause(error);
        return Optional.of(createTs);
    }

    @Override
    public void putInformation(String key, String value) {
        informations.put(key, value);
    }

    @Override
    public Map<String, String> writeTs(TsMoniker moniker) {
        SpreadSheetProvider provider = Lookup.getDefault().lookup(SpreadSheetProvider.class);
        DataSet dataSet = provider.toDataSet(moniker);

        String timeSeriesId = dataSet.getParam("seriesName").get();
        informations.put(TIMESERIES_KEY, timeSeriesId);

        String sheet = dataSet.getParam("sheetName").get();
        informations.put(SHEET, sheet);

        String path = dataSet.getDataSource().getParam("file").get();
        informations.put(PATH, path);
        return informations;
    }

}
