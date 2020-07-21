package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.DayBuilder;
import de.bundesbank.jdemetra.xlsx2ws.dto.Message;
import de.bundesbank.jdemetra.xlsx2ws.dto.SpecificationDTO;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.x11.BiasCorrection;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Exception;
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
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import java.text.Collator;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class X13SpecificationReader implements ISpecificationReader<X13Specification> {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}(.(0[1-9]|1[0-2])(.(0[1-9]|[12]\\d|3[01]))?)?");
    private static final Pattern ARIMA_PATTERN = Pattern.compile("\\(([0-6])\\s*([0-2])\\s*([0-6])\\)\\s*\\(([01])\\s*([01])\\s*([01])\\)");
    private static final Pattern REGRESSOR_PATTERN = Pattern.compile(".*\\..+?((\\*).*)?", Pattern.CASE_INSENSITIVE);
    private static final String DOUBLE = "([-+]?([0-9]+\\.?[0-9]*|[0-9]*\\.?[0-9]+)([eE][-+]?[0-9]+)?)";
    private static final Pattern FIXED_COEFFICIENT_PATTERN = Pattern.compile(".*(\\*)(" + DOUBLE + ";)*" + DOUBLE, Pattern.CASE_INSENSITIVE);
    private static final LocalDate START_EXCEL = LocalDate.of(1900, Month.JANUARY, 1);

    public static final String BASE = "base", SPAN_NONE_VALUE = "X", START = "start", END = "end", FIRST = "first", LAST = "last",
            //SERIES
            SERIES = "series_", PRELIMINARY_CHECK = "preliminary_check",
            //ESTIMATE
            ESTIMATE = "estimate_", TOLERANCE = "tolerance",
            //TRANSFORMATION
            TRANSFORM = "transform", AIC_DIFFERENCE = "aicdiff", ADJUST = "adjust",
            //REGRESSION
            REGRESSOR = "regressor_", OUTLIER = "outlier_", FIXED_COEFFICIENT = "fixed_coefficient_",
            //CALENDAR
            //TRADINGDAYS
            HOLIDAYS = "holidays", TRADING_DAYS_TYPE = "td", LEAP_YEAR = "leap_year", AUTO_ADJUST = "auto_adjust", W = "w", TEST = "td_test",
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
    private final List<Message> messages = new ArrayList<>();

    @Override
    public void putInformation(String key, String value) {
        information.put(key, value);
    }

    @Override
    public SpecificationDTO<X13Specification> readSpecification(ISaSpecification old) {
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

        return new SpecificationDTO<>(specification, messages.toArray(new Message[messages.size()]));
    }

    private void readSeries(BasicSpec basicSpec) {
        consumeSpan(SERIES, basicSpec::setSpan);
        consumeBoolean(PRELIMINARY_CHECK, basicSpec::setPreliminaryCheck);
    }

    private void readEstimate(EstimateSpec estimateSpec) {
        consumeSpan(ESTIMATE, estimateSpec::setSpan);
        consumeDouble(TOLERANCE, estimateSpec::setTol);
    }

    private void readTransformation(TransformSpec transformSpec) {
        consumeEnum(TRANSFORM, DefaultTransformationType::valueOf, transformSpec::setFunction);
        consumeDouble(AIC_DIFFERENCE, transformSpec::setAICDiff);
        consumeEnum(ADJUST, LengthOfPeriodType::valueOf, transformSpec::setAdjust);
    }

    private void readRegression(RegressionSpec regressionSpec) {
        readCalendar(regressionSpec);
        //TODO Intervention Variables
        //TODO Ramps
        readPreSpecifiedOutliers(regressionSpec);
        readUserDefinedVariables(regressionSpec);
        readFixedCoefficients(regressionSpec);
    }

    private void readCalendar(RegressionSpec regressionSpec) {
        //TD
        TradingDaysSpec tradingDaysSpec = regressionSpec.getTradingDays();
        consumeInt(W, tradingDaysSpec::setStockTradingDays);
        tradingDaysSpec.setHolidays(information.get(HOLIDAYS));

        consumeEnum(TEST, RegressionTestSpec::valueOf, tradingDaysSpec::setTest);
        consumeEnum(TRADING_DAYS_TYPE, TradingDaysType::valueOf, tradingDaysSpec::setTradingDaysType);
        consumeEnum(LEAP_YEAR, LengthOfPeriodType::valueOf, tradingDaysSpec::setLengthOfPeriod);

        consumeBoolean(AUTO_ADJUST, tradingDaysSpec::setAutoAdjust);

        //EASTER
        if (information.containsKey(EASTER)) {
            Easter easter = new Easter();
            regressionSpec.removeMovingHolidays(regressionSpec.getEaster());

            consumeBoolean(EASTER, easter::setEaster);
            consumeBoolean(EASTER_JULIAN, easter::setJulian);
            consumeInt(DURATION, easter::setW);
            consumeEnum(PRE_TEST, RegressionTestSpec::valueOf, easter::setRegressionTestSpec);
            easter.fill(regressionSpec);

        }
    }

    private void readPreSpecifiedOutliers(RegressionSpec regressionSpec) {
        if (information.containsKey(OUTLIER + 1)) {
            regressionSpec.clearOutliers();
        }

        information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(OUTLIER))
                .sorted(STRING_MAP_COMPARATOR)
                .forEach(x -> {
                    String outlierInfo = x.getValue();
                    if (outlierInfo.length() < 2) {
                        messages.add(new Message(Level.SEVERE, "The information in " + x.getKey() + " doesn't follow the outlier syntax."));
                        return;
                    }
                    Day day = parseDay(outlierInfo.substring(2), x.getKey());
                    if (day == null) {
                        return;
                    }
                    OutlierDefinition outlier;
                    switch (outlierInfo.substring(0, 2).toUpperCase()) {
                        case AdditiveOutlier.CODE:
                            outlier = new OutlierDefinition(day, OutlierType.AO);
                            break;
                        case LevelShift.CODE:
                            outlier = new OutlierDefinition(day, OutlierType.LS);
                            break;
                        case TransitoryChange.CODE:
                            outlier = new OutlierDefinition(day, OutlierType.TC);
                            break;
                        case SeasonalOutlier.CODE:
                            outlier = new OutlierDefinition(day, OutlierType.SO);
                            break;
                        default:
                            return;
                    }
                    if (!regressionSpec.contains(outlier)) {
                        regressionSpec.add(outlier);
                    }
                });
    }

    private void readUserDefinedVariables(RegressionSpec regressionSpec) {
        List<TsVariableDescriptor> variableDescriptors = new ArrayList<>();
        List<String> userDefinedCalendarEffects = new ArrayList<>();
        information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(REGRESSOR))
                .sorted(STRING_MAP_COMPARATOR)
                .forEach(x -> {
                    if (REGRESSOR_PATTERN.matcher(x.getValue()).matches()) {

                        String regressorInfo = x.getValue();
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
                                messages.add(new Message(Level.WARNING, x.getKey() + " has no typ and will be marked as Undefined."));
                                regressor.setEffect(TsVariableDescriptor.UserComponentType.Undefined);
                                break;
                        }
                        variableDescriptors.add(regressor);
                    } else {
                        messages.add(new Message(Level.SEVERE, x.getKey() + " has an invalid syntax."));
                    }
                });
        if (!variableDescriptors.isEmpty()) {
            regressionSpec.setUserDefinedVariables(variableDescriptors.toArray(new TsVariableDescriptor[variableDescriptors.size()]));
        }
        if (!userDefinedCalendarEffects.isEmpty()) {
            messages.add(new Message(Level.INFO, "Userdefined trading day variables were defined. All other trading day settings were overridden."));
            regressionSpec.getTradingDays().setUserVariables(userDefinedCalendarEffects.toArray(new String[userDefinedCalendarEffects.size()]));
        }
    }

    private void readFixedCoefficients(RegressionSpec regressionSpec) {
        information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(FIXED_COEFFICIENT))
                .sorted(STRING_MAP_COMPARATOR)
                .forEach((x) -> {
                    if (FIXED_COEFFICIENT_PATTERN.matcher(x.getValue()).matches()) {
                        String[] split = x.getValue().split("\\*");
                        String name = split[0];
                        double[] c = Arrays.stream(split[1].split(";")).mapToDouble(s -> Double.parseDouble(s)).toArray();
                        regressionSpec.setFixedCoefficients(name, c);
                    } else {
                        messages.add(new Message(Level.SEVERE, x.getKey() + " has an invalid syntax."));
                    }
                });
    }

    private void readOutliers(OutlierSpec outlierSpec) {
        consumeSpan(OUTLIER, outlierSpec::setSpan);
        consumeDouble(CRITICAL_VALUE, outlierSpec::setDefaultCriticalValue);

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
        consumeDouble(TC_RATE, outlierSpec::setMonthlyTCRate);
        consumeEnum(METHOD, OutlierSpec.Method::valueOf, outlierSpec::setMethod);

    }

    private void readAutoModel(AutoModelSpec autoModelSpec) {
        consumeBoolean(ACCEPT_DEFAULT, autoModelSpec::setAcceptDefault);
        consumeDouble(CANCELATION_LIMIT, autoModelSpec::setCancelationLimit);
        consumeDouble(INITIAL_UR, autoModelSpec::setInitialUnitRootLimit);
        consumeDouble(FINAL_UR, autoModelSpec::setFinalUnitRootLimit);
        consumeBoolean(MIXED, autoModelSpec::setMixed);
        consumeBoolean(BALANCED, autoModelSpec::setBalanced);
        consumeDouble(ARMALIMIT, autoModelSpec::setArmaSignificance);
        consumeDouble(REDUCE_CV, autoModelSpec::setPercentReductionCV);
        consumeDouble(LJUNGBOX_LIMIT, autoModelSpec::setLjungBoxLimit);
        consumeDouble(URFINAL, autoModelSpec::setUnitRootLimit);
    }

    private void readARIMA(ArimaSpec arimaSpec) {
        consumeBoolean(MEAN, arimaSpec::setMean);

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

                consumeParameters(P, p, arimaSpec::getPhi);
                consumeParameters(Q, q, arimaSpec::getTheta);
                consumeParameters(BP, bp, arimaSpec::getBPhi);
                consumeParameters(BQ, bq, arimaSpec::getBTheta);

            } else {
                messages.add(new Message(Level.SEVERE, "The information ARIMA doesn't contain a valid ARIMA model"));
            }
        }
    }

    private void readX11(X11Specification x11Specification, boolean onlyX11) {
        consumeEnum(MODE, DecompositionMode::valueOf, x11Specification::setMode);
        consumeBoolean(SEASONAL, x11Specification::setSeasonal);

        if (!onlyX11) {
            consumeInt(MAXLEAD, x11Specification::setForecastHorizon);
            consumeInt(MAXBACK, x11Specification::setBackcastHorizon);
        }

        consumeInt(HENDERSON, x11Specification::setHendersonFilterLength);

        readSigmaLimit(x11Specification);
        if (x11Specification.isSeasonal()) {
            readSeasonalFilter(x11Specification);
        }

        consumeBoolean(EXCLUDEFORECAST, x11Specification::setExcludefcst);
        consumeEnum(CALENDARSIGMA, CalendarSigma::valueOf, x11Specification::setCalendarSigma);

        if (x11Specification.getCalendarSigma() == CalendarSigma.Select && information.containsKey(SIGMA_VECTOR + 1)) {
            readSigmaVec(x11Specification);
        }

        consumeEnum(BIAS_CORRECTION, BiasCorrection::valueOf, x11Specification::setBiasCorrection);
    }

    private void readSeasonalFilter(X11Specification x11Specification) {
        //DOCUMENTATION MISSING!!!!!
        String seasonalfilter = information.get(SEASONALFILTER);
        if (seasonalfilter != null) {
            x11Specification.setSeasonalFilter(SeasonalFilterOption.valueOf(seasonalfilter));
            return;
        }
        List<SeasonalFilterOption> options = prepareEnumList(SEASONALFILTERS, SeasonalFilterOption::valueOf);
        if (options != null) {
            x11Specification.setSeasonalFilters(options.toArray(new SeasonalFilterOption[options.size()]));
        }
    }

    private void readSigmaVec(X11Specification x11Specification) {
        List<SigmavecOption> options = prepareEnumList(SIGMA_VECTOR, SigmavecOption::valueOf);
        if (options != null) {
            x11Specification.setSigmavec(options.toArray(new SigmavecOption[options.size()]));
        }
    }

    private void readSigmaLimit(X11Specification x11Specification) {
        SigmaLimit sigmaLimit = new SigmaLimit();
        consumeDouble(UPPER_SIGMA, sigmaLimit::setUpperSigma);
        consumeDouble(LOWER_SIGMA, sigmaLimit::setLowerSigma);
        sigmaLimit.fill(x11Specification);
    }

    private void consumeSpan(String part, Consumer<TsPeriodSelector> consumer) {
        if (!information.containsKey(part + START) && !information.containsKey(part + END) && !information.containsKey(part + FIRST) && !information.containsKey(part + LAST)) {
            return;
        }
        String start = information.get(part + START);
        String end = information.get(part + END);
        if (SPAN_NONE_VALUE.equalsIgnoreCase(start) || SPAN_NONE_VALUE.equalsIgnoreCase(end)) {
            TsPeriodSelector tsPeriodSelector = new TsPeriodSelector();
            tsPeriodSelector.none();
            consumer.accept(tsPeriodSelector);
            return;
        }
        Day startDay = parseDay(start, part + START);
        Day endDay = parseDay(end, part + END);
        TsPeriodSelector tsPeriodSelector = new TsPeriodSelector();
        if (startDay == null && endDay == null) {
            String first = information.get(part + FIRST);
            String last = information.get(part + LAST);
            OptionalInt firstOpt = tryParseInteger(first), lastOpt = tryParseInteger(last);
            if (firstOpt.isPresent() && lastOpt.isPresent()) {
                tsPeriodSelector.excluding(firstOpt.getAsInt(), lastOpt.getAsInt());
            } else if (!firstOpt.isPresent() && lastOpt.isPresent()) {
                tsPeriodSelector.last(lastOpt.getAsInt());
            } else if (firstOpt.isPresent()) {//Implied !lastOpt.isPresent()
                tsPeriodSelector.first(firstOpt.getAsInt());
            }
            consumer.accept(tsPeriodSelector);
            return;
        } else if (startDay != null && endDay == null) {
            tsPeriodSelector.from(startDay);
        } else if (startDay == null) { //Implied endDay !=null
            tsPeriodSelector.to(endDay);
        } else { //Implied startDay != null && endDay != null
            if (startDay.isNotBefore(endDay)) {
                messages.add(new Message(Level.SEVERE, "A mismatch between the starting and ending dates of the " + part.substring(0, part.length() - 1) + " was detected. Please correct it."));
            }
            tsPeriodSelector.between(startDay, endDay);
        }
        consumer.accept(tsPeriodSelector);
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
    private Day parseDay(String dayInfo, String key) {
        if (dayInfo != null) {
            try {
                return Day.fromString(dayInfo);
            } catch (ParseException e) {
            }
            if (DATE_PATTERN.matcher(dayInfo).matches()) {
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
            } else if (dayInfo.matches("\\d+")) {
                long day = Long.parseLong(dayInfo);
                day -= day > 59 ? 2 : 1;
                LocalDate x = START_EXCEL.plusDays(day);
                return new DayBuilder().year(x.getYear()).month(x.getMonthValue()).day(x.getDayOfMonth()).build();
            }
            messages.add(new Message(Level.SEVERE, "Unparseable Date format in " + key + "."));
        }
        return null;
    }

    private void consumeInt(String key, IntConsumer consumer) {
        if (!information.containsKey(key)) {
            return;
        }
        OptionalInt optionalInt = tryParseInteger(information.get(key));
        if (optionalInt.isPresent()) {
            try {
                consumer.accept(optionalInt.getAsInt());
            } catch (X11Exception e) {
                messages.add(new Message(Level.SEVERE, "The value " + optionalInt.getAsInt() + " is no valid input for " + key + ". (" + e.getMessage() + ")"));
            }
        } else {
            messages.add(new Message(Level.SEVERE, "The information " + key + " doesn't contain a parseble integer value."));
        }
    }

    private void consumeDouble(String key, DoubleConsumer consumer) {
        if (!information.containsKey(key)) {
            return;
        }
        try {
            double parsedDouble = Double.parseDouble(information.get(key));
            consumer.accept(parsedDouble);
        } catch (NumberFormatException e) {
            messages.add(new Message(Level.SEVERE, "The information " + key + " doesn't contain a parseble floating point value."));
        }
    }

    private <R extends Enum> void consumeEnum(String key, Function<String, R> function, Consumer<R> consumer) {
        if (!information.containsKey(key)) {
            return;
        }
        try {
            R apply = function.apply(information.get(key));
            consumer.accept(apply);
        } catch (IllegalArgumentException e) {
            messages.add(new Message(Level.SEVERE, "The information " + key + " doesn't contain a valid argument."));
        }
    }

    private <R extends Enum> List<R> prepareEnumList(String key, Function<String, R> function) {
        List<String> list = information.keySet().stream()
                .filter(x -> x.startsWith(key) && x.matches(key + "\\d+"))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        int max = list.stream().mapToInt(x -> Integer.parseInt(x.substring(key.length()))).max().getAsInt();
        if (max != list.size()) {
            messages.add(new Message(Level.SEVERE, "The maximal " + key + "(" + max + ") isn't the same as the number of " + key + "(" + list.size() + ") specified."));
            return null;
        }
        List<R> options = new ArrayList<>(max);
        list.sort(STRING_COMPARATOR);
        list.forEach((entry) -> {
            consumeEnum(entry, function, options::add);
        });

        return options.size() != max ? null : options;
    }

    private void consumeBoolean(String key, Consumer<Boolean> consumer) {
        if (!information.containsKey(key)) {
            return;
        }

        String value = information.get(key);
        if (value == null || !(value.equalsIgnoreCase(Boolean.TRUE.toString()) || value.equalsIgnoreCase(Boolean.FALSE.toString()))) {
            messages.add(new Message(Level.WARNING, "The information " + key + " doesn't contain \"true\" or \"false\". It will be set to false."));
        }
        boolean parsedBoolean = Boolean.parseBoolean(information.get(key));
        consumer.accept(parsedBoolean);
    }

    private void consumeParameters(String key, int lastPosition, Supplier<Parameter[]> parameter) {
        for (int i = 1; i <= lastPosition; i++) {
            if (!information.containsKey(key + i)) {
                continue;
            }
            final String get = information.get(key + i);
            String valueString;
            double value;
            ParameterType type = ParameterType.Fixed;

            if (get.contains("*")) {
                String[] split = get.split("\\*", 2);
                valueString = split[0];
                switch (split[1].toLowerCase()) {
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
                        messages.add(new Message(Level.INFO, "Parameter " + key + i + " has no known type declared. It will be assumed to be fixed."));
                }
            } else {
                valueString = get;
            }

            try {
                value = Double.parseDouble(valueString);
            } catch (NumberFormatException e) {
                messages.add(new Message(Level.SEVERE, "The information " + key + i + " doesn't contain a parseble double value."));
                continue;
            }
            parameter.get()[i - 1] = new Parameter(value, type);
        }
        for (int i = lastPosition + 1; i <= 6; i++) {
            if (information.containsKey(key + i)) {
                messages.add(new Message(Level.WARNING, "Parameter " + key + i + " is declared but does not fit the declared ARIMA model."));
            }
        }
    }

    private OptionalInt tryParseInteger(String value) {
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    @lombok.Setter
    private static final class Easter {

        private boolean easter = false;
        private boolean julian = false;
        private int w = MovingHolidaySpec.DEF_EASTERDUR;
        private RegressionTestSpec regressionTestSpec = null;

        public void fill(RegressionSpec regressionSpec) {
            if (easter) {
                MovingHolidaySpec easterSpec = MovingHolidaySpec.easterSpec(true, julian);
                easterSpec.setW(w);
                if (regressionTestSpec != null) {
                    easterSpec.setTest(regressionTestSpec);
                }
                regressionSpec.add(easterSpec);
            }
        }
    }

    @lombok.Setter
    private static final class SigmaLimit {

        private Double upperSigma = null;
        private Double lowerSigma = null;

        public void fill(X11Specification x11Specification) {
            if (upperSigma == null && lowerSigma != null) {
                //TODO Log if lsigma > default usigma?
                x11Specification.setLowerSigma(lowerSigma);
            }
            if (upperSigma != null && lowerSigma == null) {
                //TODO Log if usigma < default lsigma?
                x11Specification.setUpperSigma(upperSigma);
            }
            if (upperSigma != null && lowerSigma != null) {
                x11Specification.setSigma(lowerSigma, upperSigma);
            }
        }
    }

    private static final StringMapComparator STRING_MAP_COMPARATOR = new StringMapComparator();

    private static class StringMapComparator implements Comparator<Map.Entry<String, String>> {

        @Override
        public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
            String key1 = o1.getKey();
            String key2 = o2.getKey();
            return STRING_COMPARATOR.compare(key1, key2);
        }
    }

    private static final StringComparator STRING_COMPARATOR = new StringComparator();

    private static class StringComparator implements Comparator<String> {

        @Override
        public int compare(String x, String y) {
            int i = Integer.compare(x.length(), y.length());
            if (i == 0) {
                return Collator.getInstance().compare(x, y);
            } else {
                return i;
            }
        }
    }
}
