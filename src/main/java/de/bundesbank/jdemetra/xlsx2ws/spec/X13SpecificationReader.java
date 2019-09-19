package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.DayBuilder;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.AutoModelSpec;
import ec.tstoolkit.modelling.arima.x13.BasicSpec;
import ec.tstoolkit.modelling.arima.x13.EstimateSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import ec.tstoolkit.modelling.arima.x13.RegressionSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.modelling.arima.x13.TransformSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class X13SpecificationReader implements ISpecificationReader<X13Specification> {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}(\\.(0[1-9]|1[0-2])(\\.(0[1-9]|[12]\\d|3[01]))?)?");
    private static final Pattern ARIMA_PATTERN = Pattern.compile("\\(([0-6])\\s*([0-2])\\s*([0-6])\\)\\s*\\(([01])\\s*([01])\\s*([01])\\)");
    private static final Pattern REGRESSOR_PATTERN = Pattern.compile(".*\\..+?(\\*)([cituy]|sa?)", Pattern.CASE_INSENSITIVE);

    public static final String BASE = "base", SPAN_NONE_VALUE = "X", START = "start", END = "end", FIRST = "first", LAST = "last",
            //SERIES
            SERIES = "series_", PRELIMINARY_CHECK = "preliminary_check",
            //ESTIMATE
            ESTIMATE = "estimate_", TOLERANCE = "tolerance",
            //TRANSFORMATION
            TRANSFORM = "transform", AIC_DIFFERENCE = "aicdiff", ADJUST = "adjust",
            //REGRESSION
            REGRESSOR = "regressor_", OUTLIER = "outlier_",
            //CALENDAR
            //TRADINGDAYS
            HOLIDAYS = "holidays", TRADINGDAYSTYPE = "td", LEAP_YEAR = "leap_year", AUTOADJUST = "auto_adjust", W = "w", TEST = "td_test",
            //EASTER
            EASTER = "easter", EASTER_JULIAN = "easter_julian", PRE_TEST = "easter_pre_test", DURATION = "easter_duration",
            //OUTLIERS
            CRITICAL_VALUE = "critical_value", AO = "ao", LS = "ls", TC = "tc", SO = "so", TC_RATE = "tc_rate", METHOD = "method",
            //AUTOMODEL
            AUTOMODEL = "automdl", ACCEPT_DEFAULT = "accept_default", CANCELATION_LIMIT = "cancelation_limit", INITIAL_UR = "initial_ur", FINAL_UR = "final_ur",
            MIXED = "mixed", BALANCED = "balanced", ARMALIMIT = "armalimit", REDUCE_CV = "reduce_cv", LJUNGBOX_LIMIT = "ljungboxlimit", URFINAL = "urfinal",
            //ARIMA
            ARIMA = "arima", P = "p_", BP = "bp_", Q = "q_", BQ = "bq_", MEAN = "mean",
            //X11
            MODE = "mode", SEASONAL = "seasonal", MAXLEAD = "maxlead", MAXBACK = "maxback", UPPER_SIGMA = "usigma",
            LOWER_SIGMA = "lsigma", SEASONALFILTER = "seasonalfilter", SEASONALFILTERS = "seasonalfilters_", HENDERSON = "henderson",
            CALENDARSIGMA = "calendarsigma", SIGMA_VECTOR = "sigma_vector_", EXCLUDEFORECAST = "excludefcst", BIAS_CORRECTION = "bias_correction";

    private final Map<String, String> information = new HashMap<>();

    @Override
    public void putInformation(String key, String value) {
        information.put(key, value);
    }

    @Override
    public X13Specification readSpecification(ISaSpecification old) {
        X13Specification specification;
        if (information.containsKey(BASE)) {
            specification = X13Specification.fromString(information.get(BASE)).clone();
        } else if (old instanceof X13Specification) {
            specification = (X13Specification) old.clone();
        } else {
            specification = new X13Specification();
        }
        RegArimaSpecification regArimaSpecification = specification.getRegArimaSpecification();
        boolean onlyX11 = regArimaSpecification.equals(RegArimaSpecification.RGDISABLED);

        readSeries(regArimaSpecification.getBasic());
        if (!onlyX11) {
            readEstimate(regArimaSpecification.getEstimate());
            readTransformation(regArimaSpecification.getTransform());
            readRegression(regArimaSpecification.getRegression());
            readOutliers(regArimaSpecification.getOutliers());

            if (information.containsKey(AUTOMODEL)) {
                boolean usingAutoModel = information.get(AUTOMODEL).equalsIgnoreCase("true");
                regArimaSpecification.setUsingAutoModel(usingAutoModel);
            }
            if (regArimaSpecification.isUsingAutoModel()) {
                readAutoModel(regArimaSpecification.getAutoModel());
            } else {
                readARIMA(regArimaSpecification.getArima());
            }
        }

        readX11(specification.getX11Specification(), onlyX11);

        return specification;
    }

    private void readSeries(BasicSpec basicSpec) {
        TsPeriodSelector span = readSpan(SERIES);
        if (span != null) {
            basicSpec.setSpan(span);
        }
        if (information.containsKey(PRELIMINARY_CHECK)) {
            basicSpec.setPreliminaryCheck(Boolean.parseBoolean(information.get(PRELIMINARY_CHECK)));
        }
    }

    private void readEstimate(EstimateSpec estimateSpec) {
        TsPeriodSelector span = readSpan(ESTIMATE);
        if (span != null) {
            estimateSpec.setSpan(span);
        }
        if (information.containsKey(TOLERANCE)) {
            try {
                estimateSpec.setTol(Double.parseDouble(information.get(TOLERANCE)));
            } catch (NumberFormatException e) {
                //TODO LOG
            }

        }
    }

    private void readTransformation(TransformSpec transformSpec) {
        if (information.containsKey(TRANSFORM)) {
            try {
                DefaultTransformationType transform = DefaultTransformationType.valueOf(information.get(TRANSFORM));
                transformSpec.setFunction(transform);
            } catch (IllegalArgumentException ex) {
                //TODO log
            }
        }
        if (information.containsKey(AIC_DIFFERENCE)) {
            try {
                double aicDiff = Double.parseDouble(information.get(TRANSFORM));
                transformSpec.setAICDiff(aicDiff);
            } catch (NumberFormatException ex) {
                //TODO log
            }
        }
        if (information.containsKey(ADJUST)) {
            try {
                LengthOfPeriodType lengthOfPeriodType = LengthOfPeriodType.valueOf(information.get(ADJUST));
                transformSpec.setAdjust(lengthOfPeriodType);
            } catch (IllegalArgumentException ex) {
                //TODO log
            }
        }

    }

    private void readRegression(RegressionSpec regressionSpec) {
        readCalendar(regressionSpec);
        //TODO Intervention Variables
        //TODO Ramps
        readPreSpecifiedOutliers(regressionSpec);
        readUserDefinedVariables(regressionSpec);
    }

    private void readCalendar(RegressionSpec regressionSpec) {
        //TD
        TradingDaysSpec tradingDaysSpec = regressionSpec.getTradingDays();
        OptionalInt stockTradingDays = tryParseInteger(information.get(W));
        stockTradingDays.ifPresent(tradingDaysSpec::setStockTradingDays);
        tradingDaysSpec.setHolidays(information.get(HOLIDAYS));

        if (information.containsKey(TEST)) {
            String test = information.get(TEST);
            try {
                tradingDaysSpec.setTest(RegressionTestSpec.valueOf(test));
            } catch (IllegalArgumentException e) {
                //TODO LOG
            }
        }
        if (information.containsKey(TRADINGDAYSTYPE)) {
            String tradingDaysType = information.get(TRADINGDAYSTYPE);
            try {
                tradingDaysSpec.setTradingDaysType(TradingDaysType.valueOf(tradingDaysType));
            } catch (IllegalArgumentException e) {
                //TODO LOG
            }
        }
        if (information.containsKey(LEAP_YEAR)) {
            String leap_year = information.get(LEAP_YEAR);
            try {
                tradingDaysSpec.setLengthOfPeriod(LengthOfPeriodType.valueOf(leap_year));
            } catch (IllegalArgumentException e) {
                //TODO LOG
            }
        }
        if (information.containsKey(AUTOADJUST)) {
            tradingDaysSpec.setAutoAdjust(Boolean.parseBoolean(information.get(AUTOADJUST)));
        }

        //EASTER
        if (information.containsKey(EASTER)) {
            regressionSpec.removeMovingHolidays(regressionSpec.getEaster());
            if (Boolean.parseBoolean(information.get(EASTER))) {
                boolean pretest = Boolean.parseBoolean(information.getOrDefault(PRE_TEST, "true"));
                boolean julian = Boolean.parseBoolean(information.getOrDefault(EASTER_JULIAN, "false"));
                MovingHolidaySpec easterSpec = MovingHolidaySpec.easterSpec(pretest, julian);
                OptionalInt duration = tryParseInteger(information.get(DURATION));
                duration.ifPresent(easterSpec::setW);
                regressionSpec.add(easterSpec);
            }

        }
    }

    private void readPreSpecifiedOutliers(RegressionSpec regressionSpec) {
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

    private void readUserDefinedVariables(RegressionSpec regressionSpec) {
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
            //Overwrites different Calendar Settings maybe log?
            regressionSpec.getTradingDays().setUserVariables(userDefinedCalendarEffects.toArray(new String[userDefinedCalendarEffects.size()]));
        }
    }

    private void readOutliers(OutlierSpec outlierSpec) {
        TsPeriodSelector span = readSpan(OUTLIER);
        if (span != null) {
            outlierSpec.setSpan(span);
        }
        if (information.containsKey(CRITICAL_VALUE)) {
            double criticalValue = Double.parseDouble(information.get(CRITICAL_VALUE));
            outlierSpec.setDefaultCriticalValue(criticalValue);
        }
        if (information.containsKey(AO)) {
            boolean ao = Boolean.parseBoolean(information.get(AO));
            if (ao) {
                outlierSpec.add(OutlierType.AO);
            } else {
                outlierSpec.remove(OutlierType.AO);
            }
        }
        if (information.containsKey(LS)) {
            boolean ls = Boolean.parseBoolean(information.get(LS));
            if (ls) {
                outlierSpec.add(OutlierType.LS);
            } else {
                outlierSpec.remove(OutlierType.LS);
            }
        }
        if (information.containsKey(TC)) {
            boolean tc = Boolean.parseBoolean(information.get(TC));
            if (tc) {
                outlierSpec.add(OutlierType.TC);
            } else {
                outlierSpec.remove(OutlierType.TC);
            }
        }
        if (information.containsKey(SO)) {
            boolean so = Boolean.parseBoolean(information.get(SO));
            if (so) {
                outlierSpec.add(OutlierType.SO);
            } else {
                outlierSpec.remove(OutlierType.SO);
            }
        }
        if (information.containsKey(TC_RATE)) {
            double tcRate = Double.parseDouble(information.get(TC_RATE));
            outlierSpec.setMonthlyTCRate(tcRate);
        }
        if (information.containsKey(METHOD)) {
            OutlierSpec.Method method = OutlierSpec.Method.valueOf(information.get(METHOD));
            outlierSpec.setMethod(method);
        }

    }

    private void readAutoModel(AutoModelSpec autoModelSpec) {
        if (information.containsKey(ACCEPT_DEFAULT)) {
            boolean acceptDefault = Boolean.parseBoolean(information.get(ACCEPT_DEFAULT));
            autoModelSpec.setAcceptDefault(acceptDefault);
        }
        if (information.containsKey(CANCELATION_LIMIT)) {
            double cancelationLimit = Double.parseDouble(information.get(CANCELATION_LIMIT));
            autoModelSpec.setCancelationLimit(cancelationLimit);
        }
        if (information.containsKey(INITIAL_UR)) {
            double initialUr = Double.parseDouble(information.get(INITIAL_UR));
            autoModelSpec.setInitialUnitRootLimit(initialUr);
        }
        if (information.containsKey(FINAL_UR)) {
            double finalUr = Double.parseDouble(information.get(FINAL_UR));
            autoModelSpec.setFinalUnitRootLimit(finalUr);
        }
        if (information.containsKey(MIXED)) {
            boolean mixed = Boolean.parseBoolean(information.get(MIXED));
            autoModelSpec.setMixed(mixed);
        }
        if (information.containsKey(BALANCED)) {
            boolean balanced = Boolean.parseBoolean(information.get(BALANCED));
            autoModelSpec.setBalanced(balanced);
        }
        if (information.containsKey(ARMALIMIT)) {
            double armaLimit = Double.parseDouble(information.get(ARMALIMIT));
            autoModelSpec.setArmaSignificance(armaLimit);
        }
        if (information.containsKey(REDUCE_CV)) {
            double reduceCV = Double.parseDouble(information.get(REDUCE_CV));
            autoModelSpec.setPercentReductionCV(reduceCV);
        }
        if (information.containsKey(LJUNGBOX_LIMIT)) {
            double ljungboxLimit = Double.parseDouble(information.get(LJUNGBOX_LIMIT));
            autoModelSpec.setLjungBoxLimit(ljungboxLimit);
        }
        if (information.containsKey(URFINAL)) {
            double urFinal = Double.parseDouble(information.get(URFINAL));
            autoModelSpec.setUnitRootLimit(urFinal);
        }
    }

    private void readARIMA(ArimaSpec arimaSpec) {
        if (information.containsKey(MEAN)) {
            boolean usingMean = Boolean.parseBoolean(information.get(MEAN));
            arimaSpec.setMean(usingMean);
        }

        if (information.containsKey(ARIMA)) {
            String arimaModel = information.get(ARIMA);
            Matcher matcher = ARIMA_PATTERN.matcher(arimaModel);
            if (matcher.matches()) {
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
                        arimaSpec.getPhi()[i - 1] = readParameter(P + i);
                    }
                }
                for (int i = 1; i <= q; i++) {
                    if (information.containsKey(Q + i)) {
                        arimaSpec.getTheta()[i - 1] = readParameter(Q + i);
                    }
                }
                for (int i = 1; i <= bp; i++) {
                    if (information.containsKey(BP + i)) {
                        arimaSpec.getBPhi()[i - 1] = readParameter(BP + i);
                    }
                }
                for (int i = 1; i <= bq; i++) {
                    if (information.containsKey(BQ + i)) {
                        arimaSpec.getBTheta()[i - 1] = readParameter(BQ + i);
                    }
                }

            }
        }
    }

    private Parameter readParameter(String text) {
        final String get = information.get(text);
        double value;
        ParameterType type;

        if (get.contains("*")) {
            String[] split = get.split("\\*", 2);
            value = Double.parseDouble(split[0]);
            switch (split[1]) {
                case "i":
                    type = ParameterType.Initial;
                    break;
                case "u":
                    type = ParameterType.Undefined;
                    break;
                case "f":
                    type = ParameterType.Fixed;
                    break;
                default:
                    //TODO Log
                    type = ParameterType.Fixed;
            }
        } else {
            value = Double.parseDouble(get);
            type = ParameterType.Fixed;
        }

        return new Parameter(value, type);
    }

    private void readX11(X11Specification x11Specification, boolean onlyX11) {
        if (information.containsKey(MODE)) {
            DecompositionMode mode = DecompositionMode.valueOf(information.get(MODE));
            x11Specification.setMode(mode);
        }
        if (information.containsKey(SEASONAL)) {
            boolean seasonal = Boolean.valueOf(information.get(SEASONAL));
            x11Specification.setSeasonal(seasonal);
        }

        if (information.containsKey(MAXLEAD) && !onlyX11) {
            int forecastHorizon = (int) Double.parseDouble(information.get(MAXLEAD));
            x11Specification.setForecastHorizon(forecastHorizon);
        }

        if (information.containsKey(MAXBACK) && !onlyX11) {
            int forecastHorizon = (int) Double.parseDouble(information.get(MAXBACK));
            x11Specification.setForecastHorizon(forecastHorizon);
        }

        if (information.containsKey(HENDERSON)) {
            int henderson = (int) Double.parseDouble(information.get(HENDERSON));
            x11Specification.setHendersonFilterLength(henderson);
        }
        readSigmaLimit(x11Specification);
        if (x11Specification.isSeasonal()) {
            readSeasonalFilter(x11Specification);
        }

        if (information.containsKey(EXCLUDEFORECAST)) {
            boolean excludefcst = Boolean.valueOf(information.get(EXCLUDEFORECAST));
            x11Specification.setExcludefcst(excludefcst);
        }

        if (information.containsKey(CALENDARSIGMA)) {
            CalendarSigma calendarSigma = CalendarSigma.valueOf(information.get(CALENDARSIGMA));
            x11Specification.setCalendarSigma(calendarSigma);
        }

        if (x11Specification.getCalendarSigma() == CalendarSigma.Select && information.containsKey(SIGMA_VECTOR + 1)) {
            readSigmaVec(x11Specification);
        }

    }

    private void readSeasonalFilter(X11Specification x11Specification) {
        //DOCUMENTATION MISSING!!!!!
        String seasonalfilter = information.get(SEASONALFILTER);
        if (seasonalfilter != null) {
            x11Specification.setSeasonalFilter(SeasonalFilterOption.valueOf(seasonalfilter));
            return;
        }

        List<Map.Entry<String, String>> list = information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(SEASONALFILTERS) && x.getKey().matches(SEASONALFILTERS + "\\d+"))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return;
        }
        int max = list.stream().mapToInt(x -> Integer.parseInt(x.getKey().substring(SEASONALFILTERS.length()))).max().getAsInt();
        if (max != list.size()) {
            //TODO LOG
            throw new RuntimeException("The maximal seasonal filter(" + max + ") isn't the same as the number of seasonal filters specified(" + list.size() + ").");
        }

        SeasonalFilterOption[] options = new SeasonalFilterOption[max];

        list.forEach((entry) -> {
            int position = Integer.parseInt(entry.getKey().substring(SEASONALFILTERS.length()));
            SeasonalFilterOption option = SeasonalFilterOption.valueOf(entry.getValue());
            options[position - 1] = option;
        });
        x11Specification.setSeasonalFilters(options);
    }

    private void readSigmaVec(X11Specification x11Specification) {
        List<Map.Entry<String, String>> list = information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(SIGMA_VECTOR) && x.getKey().matches(SIGMA_VECTOR + "\\d+"))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            return;
        }
        int max = list.stream().mapToInt(x -> Integer.parseInt(x.getKey().substring(SIGMA_VECTOR.length()))).max().getAsInt();
        if (max != list.size()) {
            //TODO LOG
            throw new RuntimeException("The maximal SigmavecOption(" + max + ") isn't the same as the number of SigmavecOptions specified(" + list.size() + ").");
        }

        SigmavecOption[] options = new SigmavecOption[max];

        list.forEach((entry) -> {
            int position = Integer.parseInt(entry.getKey().substring(SIGMA_VECTOR.length()));
            SigmavecOption option = SigmavecOption.valueOf(entry.getValue());
            options[position - 1] = option;
        });
        x11Specification.setSigmavec(options);
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

    @Override
    public Map<String, String> writeSpecification(X13Specification spec) {
        String base = spec.toString();
        if (!base.equals("X13")) {
            information.put(BASE, base);
            return information;
        }
        RegArimaSpecification regArimaSpecification = spec.getRegArimaSpecification();

        //SERIES
        writeSeriesInformation(regArimaSpecification.getBasic());

        if (regArimaSpecification.equals(RegArimaSpecification.RGDISABLED)) {
            //just X11
            information.put(BASE, X13Specification.RSAX11.toString());
            //X11
            writeX11Information(spec.getX11Specification(), true);
        } else {
            //ESTIMATE
            writeEstimateInformation(regArimaSpecification.getEstimate());
            //TRANSFORMATION
            writeTransformationInformation(regArimaSpecification.getTransform());
            //REGRESSION
            writeRegressionInformation(regArimaSpecification.getRegression());
            //OUTLIERS
            writeOutlierInformation(regArimaSpecification.getOutliers());
            //ARIMA
            if (regArimaSpecification.isUsingAutoModel()) {
                writeAutoModelInformation(regArimaSpecification.getAutoModel());
            } else {
                writeARIMAInformation(regArimaSpecification.getArima());
            }
            //X11
            writeX11Information(spec.getX11Specification(), false);
        }
        return information;
    }

    private void writeSeriesInformation(BasicSpec basicSpec) {
        TsPeriodSelector span = basicSpec.getSpan();
        if (span.getType() != PeriodSelectorType.All && basicSpec.isPreliminaryCheck()) {
            return;
        }
        information.put(PRELIMINARY_CHECK, Boolean.toString(basicSpec.isPreliminaryCheck()));
        writeSpan(span, SERIES);

    }

    private void writeEstimateInformation(EstimateSpec estimateSpec) {
        if (estimateSpec.isDefault()) {
            return;
        }
        TsPeriodSelector span = estimateSpec.getSpan();
        information.put(TOLERANCE, Double.toString(estimateSpec.getTol()));
        writeSpan(span, ESTIMATE);

    }

    private void writeTransformationInformation(TransformSpec transformSpec) {
        DefaultTransformationType function = transformSpec.getFunction();
        information.put(TRANSFORM, function.toString());
        switch (function) {
            case None:
                break;
            case Auto:
                information.put(AIC_DIFFERENCE, Double.toString(transformSpec.getAICDiff()));
                break;
            case Log:
                information.put(ADJUST, transformSpec.getAdjust().toString());
                break;
            default:
                throw new AssertionError();
        }
    }

    private void writeRegressionInformation(RegressionSpec regressionSpec) {
        int regressionCounter = 0;
        //TRADINGDAYS
        TradingDaysSpec tradingDays = regressionSpec.getTradingDays();
        if (tradingDays != null) {
            String holidays = tradingDays.getHolidays();
            if (holidays != null) {
                information.put(HOLIDAYS, holidays);
            }

            TradingDaysType tradingDaysType = tradingDays.getTradingDaysType();
            information.put(TRADINGDAYSTYPE, tradingDaysType.toString());

            if (holidays != null || !tradingDaysType.equals(TradingDaysType.None)) {
                boolean autoAdjust = tradingDays.isAutoAdjust();
                information.put(AUTOADJUST, Boolean.toString(autoAdjust));
                if (!autoAdjust) {
                    LengthOfPeriodType lengthOfPeriod = tradingDays.getLengthOfPeriod();
                    information.put(LEAP_YEAR, lengthOfPeriod.toString());
                }
            }

            int stockTradingDays = tradingDays.getStockTradingDays();
            if (stockTradingDays != 0) {
                information.put(W, Integer.toString(stockTradingDays));
            }

            String[] userVariables = tradingDays.getUserVariables();
            if (userVariables != null) {
                for (int i = 0; i < userVariables.length; i++) {
                    information.put(REGRESSOR + (regressionCounter + i + 1), userVariables[i] + "*c");
                    regressionCounter++;
                }
            }
            RegressionTestSpec test = tradingDays.getTest();
            information.put(TEST, test.toString());
        }
        //EASTER
        MovingHolidaySpec easter = regressionSpec.getEaster();
        if (easter == null) {
            information.put(EASTER, Boolean.toString(false));
        } else {
            information.put(EASTER, Boolean.toString(true));
            information.put(EASTER_JULIAN, Boolean.toString(easter.getType() == MovingHolidaySpec.Type.JulianEaster));
            information.put(PRE_TEST, easter.getTest().toString());
            information.put(DURATION, Integer.toString(easter.getW()));
        }
        //PRE-SPECIFIED OUTLIERS
        if (regressionSpec.getOutliersCount() > 0) {
            OutlierDefinition[] outliers = regressionSpec.getOutliers();
            for (int i = 0; i < outliers.length; i++) {
                information.put(OUTLIER + (i + 1), outliers[i].getCode() + outliers[i].getPosition().toString());
            }
        }

        //INTERVENTION (TODO)
        //RAMP (TODO)
        //USER-DEFINED VARIABLES
        if (regressionSpec.getUserDefinedVariablesCount() > 0) {
            TsVariableDescriptor[] userDefinedVariables = regressionSpec.getUserDefinedVariables();
            for (int i = 0; i < userDefinedVariables.length; i++) {
                TsVariableDescriptor userDefinedVariable = userDefinedVariables[i];
                String effect;
                switch (userDefinedVariable.getEffect()) {
                    case Undefined:
                        effect = "u";
                        break;
                    case Series:
                        effect = "y";
                        break;
                    case Trend:
                        effect = "t";
                        break;
                    case Seasonal:
                        effect = "s";
                        break;
                    case SeasonallyAdjusted:
                        effect = "sa";
                        break;
                    case Irregular:
                        effect = "i";
                        break;
                    default:
                        throw new AssertionError();
                }
                String info = userDefinedVariable.getName() + "*" + effect;
                information.put(REGRESSOR + (regressionCounter + i + 1), info);
                regressionCounter++;
            }
        }
        //FIXED REGRESSOION COEFFICIENTS (TODO)

    }

    private void writeOutlierInformation(OutlierSpec outlierSpec) {
        if (!outlierSpec.isUsed()) {
            return;
        }
        writeSpan(outlierSpec.getSpan(), OUTLIER);
        information.put(CRITICAL_VALUE, Double.toString(outlierSpec.getDefaultCriticalValue()));
        information.put(AO, Boolean.toString(outlierSpec.search(OutlierType.AO) != null));
        information.put(LS, Boolean.toString(outlierSpec.search(OutlierType.LS) != null));
        information.put(TC, Boolean.toString(outlierSpec.search(OutlierType.TC) != null));
        information.put(SO, Boolean.toString(outlierSpec.search(OutlierType.SO) != null));
        information.put(CRITICAL_VALUE, Double.toString(outlierSpec.getMonthlyTCRate()));
        information.put(METHOD, outlierSpec.getMethod().toString());
    }

    private void writeAutoModelInformation(AutoModelSpec autoModelSpec) {
        information.put(AUTOMODEL, Boolean.toString(true));
        information.put(ACCEPT_DEFAULT, Boolean.toString(autoModelSpec.isAcceptDefault()));
        information.put(CANCELATION_LIMIT, Double.toString(autoModelSpec.getCancelationLimit()));
        information.put(INITIAL_UR, Double.toString(autoModelSpec.getInitialUnitRootLimit()));
        information.put(FINAL_UR, Double.toString(autoModelSpec.getFinalUnitRootLimit()));
        information.put(MIXED, Boolean.toString(autoModelSpec.isMixed()));
        information.put(BALANCED, Boolean.toString(autoModelSpec.isBalanced()));
        information.put(ARMALIMIT, Double.toString(autoModelSpec.getArmaSignificance()));
        information.put(REDUCE_CV, Double.toString(autoModelSpec.getPercentReductionCV()));
        information.put(LJUNGBOX_LIMIT, Double.toString(autoModelSpec.getLjungBoxLimit()));
        information.put(URFINAL, Double.toString(autoModelSpec.getUnitRootLimit()));
    }

    private void writeARIMAInformation(ArimaSpec arimaSpec) {
        information.put(AUTOMODEL, Boolean.toString(false));
        information.put(MEAN, Boolean.toString(arimaSpec.isMean()));
        StringBuilder arima = new StringBuilder("(");
        arima.append(arimaSpec.getP()).append(" ")
                .append(arimaSpec.getD()).append(" ")
                .append(arimaSpec.getQ()).append(")(")
                .append(arimaSpec.getBP()).append(" ")
                .append(arimaSpec.getBD()).append(" ")
                .append(arimaSpec.getBQ()).append(")");
        information.put(ARIMA, arima.toString());
        for (int i = 0; i < arimaSpec.getP(); i++) {
            Parameter parameter = arimaSpec.getPhi()[i];
            String info = Double.toString(parameter.getValue()) + translateType(parameter);
            information.put(P + (i + 1), info);
        }
        for (int i = 0; i < arimaSpec.getQ(); i++) {
            Parameter parameter = arimaSpec.getTheta()[i];
            String info = Double.toString(parameter.getValue()) + translateType(parameter);
            information.put(Q + (i + 1), info);
        }
        for (int i = 0; i < arimaSpec.getBP(); i++) {
            Parameter parameter = arimaSpec.getBPhi()[i];
            String info = Double.toString(parameter.getValue()) + translateType(parameter);
            information.put(BP + (i + 1), info);
        }
        for (int i = 0; i < arimaSpec.getBQ(); i++) {
            Parameter parameter = arimaSpec.getBTheta()[i];
            String info = Double.toString(parameter.getValue()) + translateType(parameter);
            information.put(BQ + (i + 1), info);
        }

    }

    private void writeX11Information(X11Specification x11, boolean onlyX11) {
        //Mode
        DecompositionMode mode = x11.getMode();
        information.put(MODE, mode.toString());
        //Seasonal Component
        boolean seasonal = x11.isSeasonal();
        information.put(SEASONAL, Boolean.toString(seasonal));
        //LSigma
        information.put(LOWER_SIGMA, Double.toString(x11.getLowerSigma()));
        //USigma
        information.put(UPPER_SIGMA, Double.toString(x11.getUpperSigma()));
        //SeasonalFilter
        SeasonalFilterOption[] seasonalFilters = x11.getSeasonalFilters();
        if (seasonalFilters == null) {
            information.put(SEASONALFILTER, SeasonalFilterOption.Msr.toString());
        } else if (seasonalFilters.length == 1) {
            information.put(SEASONALFILTER, seasonalFilters[0].toString());
        } else {
            for (int i = 0; i < seasonalFilters.length; i++) {
                information.put(SEASONALFILTERS + (i + 1), seasonalFilters[i].toString());
            }
        }
        //Henderson
        information.put(HENDERSON, Integer.toString(x11.getHendersonFilterLength()));
        //Calendarsigma
        CalendarSigma calendarSigma = x11.getCalendarSigma();
        information.put(CALENDARSIGMA, calendarSigma.toString());
        if (calendarSigma.equals(CalendarSigma.Select)) {
            SigmavecOption[] sigmavec = x11.getSigmavec();
            for (int i = 0; i < sigmavec.length; i++) {
                information.put(SIGMA_VECTOR + (i + 1), sigmavec[i].toString());
            }
        }
        //Excludeforecast
        boolean excludefcst = x11.isExcludefcst();
        information.put(SEASONAL, Boolean.toString(excludefcst));
        //Bias correction (only for LogAdditive) with 2.2.2
        //if (mode.equals(DecompositionMode.LogAdditive)) {
        //}
        if (!onlyX11) {
            information.put(MAXLEAD, Integer.toString(x11.getForecastHorizon()));
            information.put(MAXBACK, Integer.toString(x11.getBackcastHorizon()));
        }
    }

    private OptionalInt tryParseInteger(String value) {
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    private String translateType(Parameter p) throws AssertionError {
        switch (p.getType()) {
            case Undefined:
                return "*u";
            case Initial:
                return "*i";
            case Fixed:
                return "*f";
            //Should not happen (TODO)
            case Estimated:
            case Derived:
            default:
                throw new AssertionError();
        }
    }

    private void writeSpan(TsPeriodSelector span, String part) throws AssertionError {
        switch (span.getType()) {
            case All:
                break;
            case From:
                information.put(part + START, span.getD0().toString());
                break;
            case Between:
                information.put(part + START, span.getD0().toString());
                information.put(part + END, span.getD1().toString());
                break;
            case To:
                information.put(part + END, span.getD1().toString());
                break;
            case First:
                information.put(part + FIRST, Integer.toString(span.getN0()));
                break;
            case Last:
                information.put(part + LAST, Integer.toString(span.getN1()));
                break;
            case Excluding:
                information.put(part + FIRST, Integer.toString(span.getN0()));
                information.put(part + LAST, Integer.toString(span.getN1()));
                break;
            case None:
                information.put(part + START, SPAN_NONE_VALUE);
                information.put(part + END, SPAN_NONE_VALUE);
                break;
            default:
                throw new AssertionError();
        }
    }

    private TsPeriodSelector readSpan(String part) {
        if (!information.containsKey(part + START) && !information.containsKey(part + END) && !information.containsKey(part + FIRST) && !information.containsKey(part + LAST)) {
            return null;
        }
        String start = information.get(part + START);
        String end = information.get(part + END);
        if (SPAN_NONE_VALUE.equalsIgnoreCase(start) || SPAN_NONE_VALUE.equalsIgnoreCase(start)) {
            TsPeriodSelector tsPeriodSelector = new TsPeriodSelector();
            tsPeriodSelector.none();
            return tsPeriodSelector;
        }
        Day startDay = parseDay(start);
        Day endDay = parseDay(end);
        if (startDay == null && endDay == null) {
            String first = information.get(part + FIRST);
            String last = information.get(part + LAST);
            OptionalInt firstOpt = tryParseInteger(first), lastOpt = tryParseInteger(last);
            TsPeriodSelector tsPeriodSelector = new TsPeriodSelector();
            if (firstOpt.isPresent() && !lastOpt.isPresent()) {
                tsPeriodSelector.first(firstOpt.getAsInt());
            }
            if (!firstOpt.isPresent() && lastOpt.isPresent()) {
                tsPeriodSelector.last(lastOpt.getAsInt());
            }
            if (firstOpt.isPresent() && lastOpt.isPresent()) {
                tsPeriodSelector.excluding(firstOpt.getAsInt(), lastOpt.getAsInt());
            }
            return tsPeriodSelector;
        }

        TsPeriodSelector tsPeriodSelector = new TsPeriodSelector();
        if (startDay != null && endDay == null) {
            tsPeriodSelector.from(startDay);
        }
        if (startDay == null && endDay != null) {
            tsPeriodSelector.to(endDay);
        }
        if (startDay != null && endDay != null) {
            tsPeriodSelector.between(startDay, endDay);
        }
        return tsPeriodSelector;
    }

    /**
     * Examples:<br/>
     * 1989 -> Day for 1989.01.01<br/>
     * 1989.03 -> Day for 1989.03.01<br/>
     * 1989.03.15 -> Day for 1989.03.15<br/>
     *
     * @param dayInfo String in the form YYYY[.MM[.DD]]
     *
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

}
