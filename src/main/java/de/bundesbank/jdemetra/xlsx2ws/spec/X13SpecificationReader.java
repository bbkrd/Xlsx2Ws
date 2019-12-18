package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.DayBuilder;
import de.bundesbank.jdemetra.xlsx2ws.dto.Message;
import de.bundesbank.jdemetra.xlsx2ws.dto.SpecificationDTO;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.ISaSpecification;
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
import java.text.Collator;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
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
    private static final LocalDate START_EXCEL = LocalDate.of(1900, Month.JANUARY, 1);

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
    private final List<Message> messages = new ArrayList<>();

    private final String ALL_FINE = "Everything is fine!";

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

        if (messages.isEmpty()) {
            messages.add(new Message(Level.FINE, ALL_FINE));
        }
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
    }

    private void readCalendar(RegressionSpec regressionSpec) {
        //TD
        TradingDaysSpec tradingDaysSpec = regressionSpec.getTradingDays();
        consumeInt(W, tradingDaysSpec::setStockTradingDays);
        tradingDaysSpec.setHolidays(information.get(HOLIDAYS));

        consumeEnum(TEST, RegressionTestSpec::valueOf, tradingDaysSpec::setTest);
        consumeEnum(TRADINGDAYSTYPE, TradingDaysType::valueOf, tradingDaysSpec::setTradingDaysType);
        consumeEnum(LEAP_YEAR, LengthOfPeriodType::valueOf, tradingDaysSpec::setLengthOfPeriod);

        consumeBoolean(AUTOADJUST, tradingDaysSpec::setAutoAdjust);

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
        information.entrySet().stream()
                .filter(x -> x.getKey().startsWith(OUTLIER))
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
                .filter(x -> x.getKey().startsWith(REGRESSOR))
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
            messages.add(new Message(Level.INFO, "Userdefined trading day variables were defined. All other trading day setting were overridden."));
            regressionSpec.getTradingDays().setUserVariables(userDefinedCalendarEffects.toArray(new String[userDefinedCalendarEffects.size()]));
        }
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
        if (span.getType() == PeriodSelectorType.All && basicSpec.isPreliminaryCheck()) {
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
        regressionCounter = writeTradingDays(regressionSpec, regressionCounter);
        writeEaster(regressionSpec);
        writePreSpecifiedOutliers(regressionSpec);
        //INTERVENTION (TODO)
        //RAMP (TODO)
        writeUserDefinedVariables(regressionSpec, regressionCounter);
        //FIXED REGRESSION COEFFICIENTS (TODO)
    }

    private int writeTradingDays(RegressionSpec regressionSpec, int regressionCounter) {
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
        return regressionCounter;
    }

    private void writeEaster(RegressionSpec regressionSpec) {
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
    }

    private void writePreSpecifiedOutliers(RegressionSpec regressionSpec) {
        if (regressionSpec.getOutliersCount() > 0) {
            OutlierDefinition[] outliers = regressionSpec.getOutliers();
            for (int i = 0; i < outliers.length; i++) {
                information.put(OUTLIER + (i + 1), outliers[i].getCode() + outliers[i].getPosition().toString());
            }
        }
    }

    private void writeUserDefinedVariables(RegressionSpec regressionSpec, int regressionCounter) throws AssertionError {
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
        information.put(TC_RATE, Double.toString(outlierSpec.getMonthlyTCRate()));
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
        information.put(SEASONAL, Boolean.toString(x11.isSeasonal()));
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
            if (sigmavec != null) {
                for (int i = 0; i < sigmavec.length; i++) {
                    information.put(SIGMA_VECTOR + (i + 1), sigmavec[i].toString());
                }
            }
        }
        //Excludeforecast
        boolean excludefcst = x11.isExcludefcst();
        information.put(EXCLUDEFORECAST, Boolean.toString(excludefcst));
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
        Collator instance = Collator.getInstance();
        instance.setStrength(Collator.PRIMARY);
        list.sort(instance);
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
            double value;
            ParameterType type;

            if (get.contains("*")) {
                String[] split = get.split("\\*", 2);
                value = Double.parseDouble(split[0]);
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
                        type = ParameterType.Fixed;
                }
            } else {
                value = Double.parseDouble(get);
                type = ParameterType.Fixed;
            }
            parameter.get()[i - 1] = new Parameter(value, type);
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
}
