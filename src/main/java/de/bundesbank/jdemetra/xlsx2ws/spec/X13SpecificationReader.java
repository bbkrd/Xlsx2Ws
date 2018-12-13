/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.DayBuilder;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.BasicSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author Thomas Witthohn
 */
public class X13SpecificationReader implements ISpecificationReader<X13Specification> {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}(\\.(0[1-9]|1[0-2])(\\.(0[1-9]|[12]\\d|3[01]))?)?");
    private static final Pattern ARIMA_PATTERN = Pattern.compile("\\(([0-6])\\s*([0-2])\\s*([0-6])\\)\\s*\\(([01])\\s*([01])\\s*([01])\\)");
    private static final Pattern REGRESSOR_PATTERN = Pattern.compile(".*\\..+?(\\*)([cituy]|sa?)", Pattern.CASE_INSENSITIVE);

    public static final String BASE = "base", MAXLEAD = "maxlead", OUTLIER = "outlier_", CRITICAL_VALUE = "critical_value",
            HENDERSON = "henderson", TRANSFORM = "transform", UPPER_SIGMA = "usigma", LOWER_SIGMA = "lsigma",
            SERIES_START = "series_start", SERIES_END = "series_end", ARIMA_AUTO = "automdl",
            ARIMA = "arima", P = "p_", BP = "bp_", Q = "q_", BQ = "bq_", SEASONALFILTER = "seasonalfilters_",
            MEAN = "mean", REGRESSOR = "regressor_";

    private final Map<String, String> information = new HashMap<>();

    @Override
    public X13Specification readSpecification() {
        X13Specification specification = X13Specification.fromString(information.containsKey(BASE) ? information.get(BASE) : "").clone();
        readOutliers(specification.getRegArimaSpecification().getRegression());
        readRegressors(specification.getRegArimaSpecification().getRegression());

        if (information.containsKey(MAXLEAD) && !"X11".equals(information.get(BASE))) {
            int forecastHorizon = (int) Double.parseDouble(information.get(MAXLEAD));
            specification.getX11Specification().setForecastHorizon(forecastHorizon);
        }

        if (information.containsKey(HENDERSON)) {
            int henderson = (int) Double.parseDouble(information.get(HENDERSON));
            specification.getX11Specification().setHendersonFilterLength(henderson);
        }

        if (information.containsKey(TRANSFORM)) {
            try {
                DefaultTransformationType transform = DefaultTransformationType.valueOf(information.get(TRANSFORM));
                specification.getRegArimaSpecification().getTransform().setFunction(transform);
            } catch (IllegalArgumentException ex) {
                //TODO log
                System.out.println(ex.getMessage());
            }
        }
        readSigmaLimit(specification.getX11Specification());

        if (information.containsKey(CRITICAL_VALUE)) {
            double cv = Double.parseDouble(information.get(CRITICAL_VALUE));
            specification.getRegArimaSpecification().getOutliers().setDefaultCriticalValue(cv);
        }

        readARIMA(specification.getRegArimaSpecification());
        readSeriesSpan(specification.getRegArimaSpecification().getBasic());
        readSeasonalFilter(specification.getX11Specification());

        return specification;
    }

    @Override
    public void putInformation(String key, String value) {
        information.put(key, value);
    }

    private void readOutliers(RegressionSpec regressionSpec) {
        information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(OUTLIER))
                .map(x -> x.getValue())
                .forEach(outlierInfo -> {
                    Day day = parseDay(outlierInfo.substring(2));
                    if (day == null) {
                        return;
                    }
                    IOutlierVariable outlier;
                    switch (outlierInfo.substring(0, 2).toUpperCase()) {
                        case AdditiveOutlier.CODE:
                            outlier = new AdditiveOutlier(day);
                            break;
                        case LevelShift.CODE:
                            outlier = new LevelShift(day);
                            break;
                        case TransitoryChange.CODE:
                            outlier = new TransitoryChange(day);
                            break;
                        case SeasonalOutlier.CODE:
                            outlier = new SeasonalOutlier(day);
                            break;
                        default:
                            return;
                    }
                    regressionSpec.add(outlier);
                });
    }

    /**
     * Examples:<br/>
     * 1989 -> Day for 1989.01.01<br/>
     * 1989.03 -> Day for 1989.03.01<br/>
     * 1989.03.15 -> Day for 1989.03.15<br/>
     *
     * @param dayInfo String in the form YYYY[.MM[.DD]]
     * @return Day or null if not parsable
     */
    private Day parseDay(String dayInfo) {
        if (dayInfo != null && DATE_PATTERN.matcher(dayInfo).matches()) {
            DayBuilder builder = new DayBuilder();
            switch (dayInfo.length()) {
                case 10:
                    builder.day(Integer.parseInt(dayInfo.substring(8, 10)));
                case 7:
                    builder.month(Integer.parseInt(dayInfo.substring(5, 7)));
                case 4:
                    return builder.year(Integer.parseInt(dayInfo.substring(0, 4)))
                            .build();
            }
        }
        //throw new IllegalArgumentException("");
        return null;
    }

    private void readARIMA(RegArimaSpecification regArimaSpecification) {
        if (information.containsKey(ARIMA_AUTO)) {
            boolean usingAutoModel = information.get(ARIMA_AUTO).equalsIgnoreCase("true");
            regArimaSpecification.setUsingAutoModel(usingAutoModel);
            if (usingAutoModel) {
                //TODO Log
                return;
            }
        }

        if (information.containsKey(MEAN)) {
            boolean usingMean = information.get(MEAN).equalsIgnoreCase("true");
            regArimaSpecification.getArima().setMean(usingMean);
        }

        if (information.containsKey(ARIMA)) {
            String arimaModel = information.get(ARIMA);
            Matcher matcher = ARIMA_PATTERN.matcher(arimaModel);
            if (matcher.matches()) {
                ArimaSpec arimaSpec = regArimaSpecification.getArima();
                int p = Integer.parseInt(matcher.group(1));
                int d = Integer.parseInt(matcher.group(2));
                int q = Integer.parseInt(matcher.group(3));
                int bp = Integer.parseInt(matcher.group(4));
                int bd = Integer.parseInt(matcher.group(5));
                int bq = Integer.parseInt(matcher.group(6));

                arimaSpec.setP(p);
                arimaSpec.setD(d);
                arimaSpec.setQ(q);
                arimaSpec.setBP(bp);
                arimaSpec.setBD(bd);
                arimaSpec.setBQ(bq);
                for (int i = 1; i <= p; i++) {
                    if (information.containsKey(P + i)) {
                        double pParameter = Double.parseDouble(information.get(P + i));
                        arimaSpec.getPhi()[i - 1] = new Parameter(pParameter, ParameterType.Fixed);
                    }
                }
                for (int i = 1; i <= q; i++) {
                    if (information.containsKey(Q + i)) {
                        double qParameter = Double.parseDouble(information.get(Q + i));
                        arimaSpec.getTheta()[i - 1] = new Parameter(qParameter, ParameterType.Fixed);
                    }
                }
                for (int i = 1; i <= bp; i++) {
                    if (information.containsKey(BP + i)) {
                        double bpParameter = Double.parseDouble(information.get(BP + i));
                        arimaSpec.getBPhi()[i - 1] = new Parameter(bpParameter, ParameterType.Fixed);
                    }
                }
                for (int i = 1; i <= bq; i++) {
                    if (information.containsKey(BQ + i)) {
                        double bqParameter = Double.parseDouble(information.get(BQ + i));
                        arimaSpec.getBTheta()[i - 1] = new Parameter(bqParameter, ParameterType.Fixed);
                    }
                }

            }
        }
    }

    private void readSeriesSpan(BasicSpec basic) {
        TsPeriodSelector tsPeriodSelector = new TsPeriodSelector();

        Day seriesStart = parseDay(information.get(SERIES_START));
        Day seriesEnd = parseDay(information.get(SERIES_END));

        if (seriesStart != null && seriesEnd == null) {
            tsPeriodSelector.from(seriesStart);
        }
        if (seriesStart == null && seriesEnd != null) {
            tsPeriodSelector.to(seriesEnd);
        }
        if (seriesStart != null && seriesEnd != null) {
            tsPeriodSelector.between(seriesStart, seriesEnd);
        }

        basic.setSpan(tsPeriodSelector);

    }

    private void readSeasonalFilter(X11Specification x11Specification) {
        List<Map.Entry<String, String>> list = information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(SEASONALFILTER) && x.getKey().matches(SEASONALFILTER + "\\d+"))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return;
        }
        int max = list.stream().mapToInt(x -> Integer.parseInt(x.getKey().substring(SEASONALFILTER.length()))).max().getAsInt();
        if (max != list.size()) {
            //TODO LOG
            throw new RuntimeException("The maximal seasonal filter(" + max + ") isn't the same as the number of seasonal filters specified(" + list.size() + ").");
        }

        SeasonalFilterOption[] options = new SeasonalFilterOption[max];

        list.forEach((entry) -> {
            int position = Integer.parseInt(entry.getKey().substring(SEASONALFILTER.length()));
            SeasonalFilterOption option = SeasonalFilterOption.valueOf(entry.getValue());
            options[position - 1] = option;
        });
        x11Specification.setSeasonalFilters(options);
    }

    private void readSigmaLimit(X11Specification x11Specification) {
        Double usigma = null, lsigma = null;
        if (information.containsKey(UPPER_SIGMA)) {
            usigma = Double.valueOf(information.get(UPPER_SIGMA));
        }

        if (information.containsKey(LOWER_SIGMA)) {
            lsigma = Double.valueOf(information.get(LOWER_SIGMA));
        }

        if (usigma == null && lsigma != null) {
            //TODO Log if lsigma > default usigma?
            x11Specification.setLowerSigma(lsigma);
        }
        if (usigma != null && lsigma == null) {
            //TODO Log if usigma < default lsigma?
            x11Specification.setUpperSigma(usigma);
        }
        if (usigma != null && lsigma != null) {
            x11Specification.setSigma(lsigma, usigma);
        }
    }

    private void readRegressors(RegressionSpec regressionSpec) {
        List<TsVariableDescriptor> variableDescriptors = new ArrayList<>();
        List<String> userDefinedCalendarEffects = new ArrayList<>();
        information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(REGRESSOR) && REGRESSOR_PATTERN.matcher(x.getValue()).matches())
                .map(x -> x.getValue())
                .forEach(regressorInfo -> {
                    int lastStar = regressorInfo.lastIndexOf('*');
                    String variable, typ;
                    if (lastStar == -1) {
                        variable = regressorInfo;
                        typ = "m";
                    } else {
                        variable = regressorInfo.substring(0, lastStar);
                        typ = regressorInfo.substring(lastStar + 1).toLowerCase(Locale.ENGLISH);
                    }

                    if (typ.equals("c")) {
                        userDefinedCalendarEffects.add(variable);
                        return;
                    }

                    TsVariableDescriptor regressor = new TsVariableDescriptor(variable);

                    switch (typ) {
                        case "i":
                            regressor.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
                            break;
                        case "t":
                            regressor.setEffect(TsVariableDescriptor.UserComponentType.Trend);
                            break;
                        case "y":
                            regressor.setEffect(TsVariableDescriptor.UserComponentType.Series);
                            break;
                        case "s":
                            regressor.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
                            break;
                        case "sa":
                            regressor.setEffect(TsVariableDescriptor.UserComponentType.SeasonallyAdjusted);
                            break;
                        case "u":
                            regressor.setEffect(TsVariableDescriptor.UserComponentType.Undefined);
                            break;
                        default:
                            //TODO Log missing or wrong typ
                            regressor.setEffect(TsVariableDescriptor.UserComponentType.Undefined);
                            break;
                    }
                    variableDescriptors.add(regressor);
                });
        if (!variableDescriptors.isEmpty()) {
            regressionSpec.setUserDefinedVariables(variableDescriptors.toArray(new TsVariableDescriptor[variableDescriptors.size()]));
        }
        if (!userDefinedCalendarEffects.isEmpty()) {
            regressionSpec.getTradingDays().setUserVariables(userDefinedCalendarEffects.toArray(new String[userDefinedCalendarEffects.size()]));
        }
    }
}
