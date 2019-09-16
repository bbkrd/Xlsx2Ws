/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import de.bundesbank.webservice.BubaWebBean;
import de.bundesbank.webservice.BubaWebProvider;
import de.bundesbank.webservice.SystemSource;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.tsproviders.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.openide.util.Lookup;

/**
 *
 * @author Thomas Witthohn
 */
@Log
public class ZISProvider implements IProvider {

    public static final String TIMESERIES_KEY = "timeserieskey";
    private final Map<String, String> informations = new HashMap<>();

    @Override
    public Ts readTs() {
        if (!informations.containsKey(TIMESERIES_KEY)) {
            return null;
        }
        String timeseriesKey = informations.get(TIMESERIES_KEY);
        BubaWebBean bean = new BubaWebBean();
        bean.setDb(SystemSource.PRODUKTION);
        bean.setId(timeseriesKey);
        bean.setName(timeseriesKey);

        BubaWebProvider provider = Lookup.getDefault().lookup(BubaWebProvider.class);
        DataSource dataSource = provider.encodeBean(bean);
        try {
            TsMoniker moniker = provider.toMoniker(provider.children(dataSource).get(0));
            return TsFactory.instance.createTs(null, moniker, TsInformationType.All);
        } catch (IllegalArgumentException | IOException ex) {
            log.log(Level.SEVERE, "Error while creating Ts {0}: {1}", new Object[]{timeseriesKey, ex});
            return null;
        }
    }

    @Override
    public Map<String, String> writeTs(TsMoniker moniker) {
        BubaWebProvider provider = Lookup.getDefault().lookup(BubaWebProvider.class);
        String timeSeriesId = provider.toDataSet(moniker).getParam("seriesName").get();
        informations.put(TIMESERIES_KEY, timeSeriesId);
        return informations;
    }

    @Override
    public void putInformation(String key, String value) {
        informations.put(key, value);
    }

}
