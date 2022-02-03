/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.provider;

import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsMoniker;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsObservation;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.openide.util.Exceptions;

public class PureDataProvider implements IProvider {

    public static final String DATA = "data",
            START = "start",
            FREQUENCY = "frequency",
            NAME = "name";
    private final Map<String, String> informations = new HashMap<>();
    private static final NumberFormat FORMATTER = DecimalFormat.getNumberInstance(Locale.US);

    static {
        FORMATTER.setMaximumFractionDigits(9);
    }

    @Override
    public Optional<Ts> readTs() {
        if (informations.containsKey(DATA) && informations.containsKey(START) && informations.containsKey(FREQUENCY)) {

            try {
                String dataString = informations.get(DATA);
                double[] data = Arrays.stream(dataString.split(";")).mapToDouble(x -> Double.parseDouble(x)).toArray();

                String startString = informations.get(START);
                Day startDay = Day.fromString(startString);

                String frequencyString = informations.get(FREQUENCY);
                TsFrequency frequency = TsFrequency.valueOf(Integer.parseInt(frequencyString));

                TsData tsData = new TsData(new TsPeriod(frequency, startDay), data, false);

                String name = informations.get(NAME);
                if (name == null) {
                    name = "No Name";
                }
                return Optional.of(TsFactory.instance.createTs(name, null, tsData));
            } catch (ParseException | NumberFormatException ex) {
                Exceptions.printStackTrace(ex);
                return Optional.empty();
            }

        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<String, String> writeTs(TsMoniker moniker) {
        throw new IllegalArgumentException("This is a special case provider and this method should not be called for it.");
    }

    public Map<String, String> writeData(String name, TsData tsData) {
        if (tsData != null) {
            String start = tsData.getStart().firstday().toString();
            String frequency = String.valueOf(tsData.getFrequency().intValue());

            StringBuilder builder = new StringBuilder();
            for (TsObservation obs : tsData) {
                builder.append(FORMATTER.format(obs.getValue())).append(";");
            }
            String data = builder.toString();
            informations.put(DATA, data);
            informations.put(START, start);
            informations.put(FREQUENCY, frequency);
        }
        informations.put(NAME, name);
        return informations;
    }

    @Override
    public void putInformation(String key, String value) {
        informations.put(key, value);
    }

}
