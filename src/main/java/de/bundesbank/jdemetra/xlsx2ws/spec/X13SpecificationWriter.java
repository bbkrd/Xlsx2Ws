/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.PositionInfo;
import static de.bundesbank.jdemetra.xlsx2ws.spec.X13SpecificationReader.*;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.ArimaSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.EstimateSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.OutlierSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.RegressionSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.SeriesSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.TransformSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.X11Setting;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.*;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Thomas Witthohn
 */
public class X13SpecificationWriter implements ISpecificationWriter<X13Specification, X13SpecificationSetting> {

    public static final int INITIAL_POS_SERIES_SPAN = 111000;
    public static final int INITIAL_POS_ESTIMATE_SPAN = 121000;
    public static final int INITIAL_POS_OUTLIER = 150000;
    public static final int INITIAL_POS_REGRESSOR = 160000;
    public static final int INITIAL_POS_FIXED = 165000;
    public static final int INITIAL_POS_OUTLIER_SPAN = 170000;
    public static final int INITIAL_POS_P = 184000;
    public static final int INITIAL_POS_Q = 185000;
    public static final int INITIAL_POS_BP = 186000;
    public static final int INITIAL_POS_BQ = 187000;
    public static final int INITIAL_POS_SEASONALFILTERS = 194100;
    public static final int INITIAL_POS_SIGMA_VECTOR = 196100;

    public static final int MODIFIER_START = 100;
    public static final int MODIFIER_END = 101;
    public static final int MODIFIER_FIRST = 102;
    public static final int MODIFIER_LAST = 103;

    public static final PositionInfo POS_BASE = new PositionInfo(100000, BASE);
    public static final PositionInfo POS_PRELIMINARY_CHECK = new PositionInfo(110000, PRELIMINARY_CHECK);
    public static final PositionInfo POS_TOLERANCE = new PositionInfo(120000, TOLERANCE);
    public static final PositionInfo POS_TRANSFORM = new PositionInfo(130000, TRANSFORM);
    public static final PositionInfo POS_AIC_DIFFERENCE = new PositionInfo(131000, AIC_DIFFERENCE);
    public static final PositionInfo POS_ADJUST = new PositionInfo(132000, ADJUST);
    public static final PositionInfo POS_HOLIDAYS = new PositionInfo(140000, HOLIDAYS);
    public static final PositionInfo POS_TRADING_DAYS_TYPE = new PositionInfo(141000, TRADING_DAYS_TYPE);
    public static final PositionInfo POS_AUTO_ADJUST = new PositionInfo(142000, AUTO_ADJUST);
    public static final PositionInfo POS_LEAP_YEAR = new PositionInfo(143000, LEAP_YEAR);
    public static final PositionInfo POS_W = new PositionInfo(144000, W);
    public static final PositionInfo POS_TEST = new PositionInfo(145000, TEST);
    public static final PositionInfo POS_EASTER = new PositionInfo(146000, EASTER);
    public static final PositionInfo POS_EASTER_JULIAN = new PositionInfo(147000, EASTER_JULIAN);
    public static final PositionInfo POS_PRE_TEST = new PositionInfo(148000, PRE_TEST);
    public static final PositionInfo POS_DURATION = new PositionInfo(149000, DURATION);
    public static final PositionInfo POS_CRITICAL_VALUE = new PositionInfo(171000, CRITICAL_VALUE);
    public static final PositionInfo POS_AO = new PositionInfo(172000, AO);
    public static final PositionInfo POS_LS = new PositionInfo(173000, LS);
    public static final PositionInfo POS_TC = new PositionInfo(174000, TC);
    public static final PositionInfo POS_SO = new PositionInfo(175000, SO);
    public static final PositionInfo POS_TC_RATE = new PositionInfo(176000, TC_RATE);
    public static final PositionInfo POS_METHOD = new PositionInfo(177000, METHOD);
    public static final PositionInfo POS_ACCEPT_DEFAULT = new PositionInfo(181000, ACCEPT_DEFAULT);
    public static final PositionInfo POS_CANCELATION_LIMIT = new PositionInfo(181100, CANCELATION_LIMIT);
    public static final PositionInfo POS_INITIAL_UR = new PositionInfo(181200, INITIAL_UR);
    public static final PositionInfo POS_FINAL_UR = new PositionInfo(181300, FINAL_UR);
    public static final PositionInfo POS_MIXED = new PositionInfo(181400, MIXED);
    public static final PositionInfo POS_BALANCED = new PositionInfo(181500, BALANCED);
    public static final PositionInfo POS_ARMALIMIT = new PositionInfo(181600, ARMALIMIT);
    public static final PositionInfo POS_REDUCE_CV = new PositionInfo(181700, REDUCE_CV);
    public static final PositionInfo POS_LJUNGBOX_LIMIT = new PositionInfo(181800, LJUNGBOX_LIMIT);
    public static final PositionInfo POS_URFINAL = new PositionInfo(181900, URFINAL);
    public static final PositionInfo POS_AUTOMODEL = new PositionInfo(180000, AUTOMODEL);
    public static final PositionInfo POS_MEAN = new PositionInfo(182000, MEAN);
    public static final PositionInfo POS_ARIMA = new PositionInfo(183000, ARIMA);
    public static final PositionInfo POS_MODE = new PositionInfo(190000, MODE);
    public static final PositionInfo POS_SEASONAL = new PositionInfo(191000, SEASONAL);
    public static final PositionInfo POS_LOWER_SIGMA = new PositionInfo(192000, LOWER_SIGMA);
    public static final PositionInfo POS_UPPER_SIGMA = new PositionInfo(193000, UPPER_SIGMA);
    public static final PositionInfo POS_SEASONALFILTER = new PositionInfo(194000, SEASONALFILTER);
    public static final PositionInfo POS_HENDERSON = new PositionInfo(195000, HENDERSON);
    public static final PositionInfo POS_CALENDARSIGMA = new PositionInfo(196000, CALENDARSIGMA);
    public static final PositionInfo POS_EXCLUDE_FORECAST = new PositionInfo(197000, EXCLUDEFORECAST);
    public static final PositionInfo POS_BIAS_CORRECTION = new PositionInfo(198000, BIAS_CORRECTION);
    public static final PositionInfo POS_MAXLEAD = new PositionInfo(199000, MAXLEAD);
    public static final PositionInfo POS_MAXBACK = new PositionInfo(199500, MAXBACK);

    private final Map<PositionInfo, String> information = new HashMap<>();

    public Map<PositionInfo, String> writeSpecification(X13Specification spec) {
        return writeSpecification(spec, null);
    }

    @Override
    public Map<PositionInfo, String> writeSpecification(X13Specification spec, X13SpecificationSetting settings) {
        if (settings == null) {
            settings = new X13SpecificationSetting();
        }
        String base = spec.toString();
        if (!base.equals("X13")) {
            information.put(POS_BASE, base);
            return information;
        }
        RegArimaSpecification regArimaSpecification = spec.getRegArimaSpecification();

        //SERIES
        if (settings.isSeries()) {
            SeriesSetting seriesSetting = settings.getSeriesSetting();
            if (seriesSetting == null) {
                seriesSetting = new SeriesSetting();
            }
            writeSeriesInformation(regArimaSpecification.getBasic(), seriesSetting);
        }

        if (regArimaSpecification.equals(RegArimaSpecification.RGDISABLED)) {
            if (settings.isX11()) {
                //just X11
                information.put(POS_BASE, X13Specification.RSAX11.toString());
                //X11
                X11Setting x11Setting = settings.getX11Setting();
                if (x11Setting == null) {
                    x11Setting = new X11Setting();
                }
                writeX11Information(spec.getX11Specification(), x11Setting, true);
            }
        } else {
            //ESTIMATE
            if (settings.isEstimate()) {
                EstimateSetting estimateSetting = settings.getEstimateSetting();
                if (estimateSetting == null) {
                    estimateSetting = new EstimateSetting();
                }
                writeEstimateInformation(regArimaSpecification.getEstimate(), estimateSetting);
            }
            //TRANSFORMATION
            if (settings.isTransform()) {
                TransformSetting transformSetting = settings.getTransformSetting();
                if (transformSetting == null) {
                    transformSetting = new TransformSetting();
                }
                writeTransformationInformation(regArimaSpecification.getTransform(), transformSetting);
            }
            //REGRESSION
            if (settings.isRegression()) {
                RegressionSetting regressionSetting = settings.getRegressionSetting();
                if (regressionSetting == null) {
                    regressionSetting = new RegressionSetting();
                }
                writeRegressionInformation(regArimaSpecification.getRegression(), regressionSetting);
            }
            //OUTLIERS
            if (settings.isOutlier()) {
                OutlierSetting outlierSetting = settings.getOutlierSetting();
                if (outlierSetting == null) {
                    outlierSetting = new OutlierSetting();
                }
                writeOutlierInformation(regArimaSpecification.getOutliers(), outlierSetting);
            }
            //ARIMA
            if (settings.isArima()) {
                ArimaSetting arimaSetting = settings.getArimaSetting();
                if (arimaSetting == null) {
                    arimaSetting = new ArimaSetting();
                }
                if (regArimaSpecification.isUsingAutoModel()) {
                    writeAutoModelInformation(regArimaSpecification.getAutoModel(), arimaSetting);
                } else {
                    writeARIMAInformation(regArimaSpecification.getArima(), arimaSetting);
                }
            }
            //X11
            if (settings.isX11()) {
                X11Setting x11Setting = settings.getX11Setting();
                if (x11Setting == null) {
                    x11Setting = new X11Setting();
                }
                writeX11Information(spec.getX11Specification(), x11Setting, false);
            }
        }
        return information;
    }

    private void writeSeriesInformation(BasicSpec basicSpec, SeriesSetting seriesSetting) {
        TsPeriodSelector span = basicSpec.getSpan();
        if (span.getType() == PeriodSelectorType.All && basicSpec.isPreliminaryCheck()) {
            return;
        }
        if (seriesSetting.isPreCheck()) {
            information.put(POS_PRELIMINARY_CHECK, Boolean.toString(basicSpec.isPreliminaryCheck()));
        }
        if (seriesSetting.isSpan()) {
            writeSpan(span, SERIES, INITIAL_POS_SERIES_SPAN);
        }

    }

    private void writeEstimateInformation(EstimateSpec estimateSpec, EstimateSetting estimateSetting) {
        if (estimateSpec.isDefault()) {
            return;
        }
        if (estimateSetting.isTolerance()) {
            information.put(POS_TOLERANCE, Double.toString(estimateSpec.getTol()));
        }
        if (estimateSetting.isSpan()) {
            writeSpan(estimateSpec.getSpan(), ESTIMATE, INITIAL_POS_ESTIMATE_SPAN);
        }
    }

    private void writeTransformationInformation(TransformSpec transformSpec, TransformSetting transformSetting) {
        DefaultTransformationType function = transformSpec.getFunction();
        if (transformSetting.isTransform()) {
            information.put(POS_TRANSFORM, function.toString());
        }
        switch (function) {
            case None:
                break;
            case Auto:
                if (transformSetting.isAicDiff()) {
                    information.put(POS_AIC_DIFFERENCE, Double.toString(transformSpec.getAICDiff()));
                }
                break;
            case Log:
                if (transformSetting.isAdjust()) {
                    information.put(POS_ADJUST, transformSpec.getAdjust().toString());
                }
                break;
            default:
                throw new AssertionError();
        }
    }

    private void writeRegressionInformation(RegressionSpec regressionSpec, RegressionSetting regressionSetting) {
        int regressionCounter = 0;
        regressionCounter = writeTradingDays(regressionSpec, regressionSetting, regressionCounter);
        if (regressionSetting.isEaster()) {
            writeEaster(regressionSpec);
        }
        if (regressionSetting.isPrespecifiedOutliers()) {
            writePreSpecifiedOutliers(regressionSpec);
        }
        //INTERVENTION (TODO)
        //RAMP (TODO)
        if (regressionSetting.isUserDefinedVariables()) {
            writeUserDefinedVariables(regressionSpec, regressionCounter);
        }
        if (regressionSetting.isFixedRegressionCoefficients()) {
            writeFixedRegressionCoefficients(regressionSpec);
        }
    }

    private int writeTradingDays(RegressionSpec regressionSpec, RegressionSetting regressionSetting, int regressionCounter) {
        TradingDaysSpec tradingDays = regressionSpec.getTradingDays();
        if (tradingDays != null) {
            String holidays = tradingDays.getHolidays();
            if (holidays != null && regressionSetting.isHolidays()) {
                information.put(POS_HOLIDAYS, holidays);
            }

            TradingDaysType tradingDaysType = tradingDays.getTradingDaysType();
            if (regressionSetting.isTradingDaysType()) {
                information.put(POS_TRADING_DAYS_TYPE, tradingDaysType.toString());
            }

            if (holidays != null || !tradingDaysType.equals(TradingDaysType.None)) {
                boolean autoAdjust = tradingDays.isAutoAdjust();
                if (regressionSetting.isAutoAdjust()) {
                    information.put(POS_AUTO_ADJUST, Boolean.toString(autoAdjust));
                }
                if (!autoAdjust && regressionSetting.isLeapYear()) {
                    LengthOfPeriodType lengthOfPeriod = tradingDays.getLengthOfPeriod();
                    information.put(POS_LEAP_YEAR, lengthOfPeriod.toString());
                }
            }

            if (regressionSetting.isW()) {
                int stockTradingDays = tradingDays.getStockTradingDays();
                if (stockTradingDays != 0) {
                    information.put(POS_W, Integer.toString(stockTradingDays));
                }
            }

            String[] userVariables = tradingDays.getUserVariables();
            if (userVariables != null && regressionSetting.isUserDefinedVariables()) {
                for (int i = 0; i < userVariables.length; i++) {
                    information.put(new PositionInfo(INITIAL_POS_REGRESSOR + regressionCounter + i, REGRESSOR + (regressionCounter + i + 1)), userVariables[i] + "*c");
                    regressionCounter++;
                }
            }

            if (regressionSetting.isTradingDaysTest()) {
                information.put(POS_TEST, tradingDays.getTest().toString());
            }
        }
        return regressionCounter;
    }

    private void writeEaster(RegressionSpec regressionSpec) {
        //EASTER
        MovingHolidaySpec easter = regressionSpec.getEaster();
        if (easter == null) {
            information.put(POS_EASTER, Boolean.toString(false));
        } else {
            information.put(POS_EASTER, Boolean.toString(true));
            information.put(POS_EASTER_JULIAN, Boolean.toString(easter.getType() == MovingHolidaySpec.Type.JulianEaster));
            information.put(POS_PRE_TEST, easter.getTest().toString());
            information.put(POS_DURATION, Integer.toString(easter.getW()));
        }
    }

    private void writePreSpecifiedOutliers(RegressionSpec regressionSpec) {
        if (regressionSpec.getOutliersCount() > 0) {
            OutlierDefinition[] outliers = regressionSpec.getOutliers();
            for (int i = 0; i < outliers.length; i++) {
                information.put(new PositionInfo(INITIAL_POS_OUTLIER + i, OUTLIER + (i + 1)), outliers[i].getCode() + outliers[i].getPosition().toString());
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
                information.put(new PositionInfo(INITIAL_POS_REGRESSOR + regressionCounter + i, REGRESSOR + (regressionCounter + i + 1)), info);
                regressionCounter++;
            }
        }
    }

    private void writeFixedRegressionCoefficients(RegressionSpec regressionSpec) {
        Map<String, double[]> allFixedCoefficients = regressionSpec.getAllFixedCoefficients();
        int counter = 0;
        for (Map.Entry<String, double[]> entry : allFixedCoefficients.entrySet()) {
            String key = entry.getKey();
            double[] value = entry.getValue();
            String info = key + "*" + Arrays.stream(value).mapToObj(d -> Double.toString(d)).collect(Collectors.joining(";"));
            information.put(new PositionInfo(INITIAL_POS_FIXED + counter, FIXED_COEFFICIENT + (counter + 1)), info);
            counter++;
        }
    }

    private void writeOutlierInformation(OutlierSpec outlierSpec, OutlierSetting outlierSetting) {
        if (!outlierSpec.isUsed()) {
            return;
        }
        if (outlierSetting.isSpan()) {
            writeSpan(outlierSpec.getSpan(), OUTLIER, INITIAL_POS_OUTLIER_SPAN);
        }
        if (outlierSetting.isCriticalValue()) {
            information.put(POS_CRITICAL_VALUE, Double.toString(outlierSpec.getDefaultCriticalValue()));
        }
        if (outlierSetting.isAo()) {
            information.put(POS_AO, Boolean.toString(outlierSpec.search(OutlierType.AO) != null));
        }
        if (outlierSetting.isLs()) {
            information.put(POS_LS, Boolean.toString(outlierSpec.search(OutlierType.LS) != null));
        }
        if (outlierSetting.isTc()) {
            information.put(POS_TC, Boolean.toString(outlierSpec.search(OutlierType.TC) != null));
        }
        if (outlierSetting.isSo()) {
            information.put(POS_SO, Boolean.toString(outlierSpec.search(OutlierType.SO) != null));
        }
        if (outlierSetting.isTcRate()) {
            information.put(POS_TC_RATE, Double.toString(outlierSpec.getMonthlyTCRate()));
        }
        if (outlierSetting.isMethod()) {
            information.put(POS_METHOD, outlierSpec.getMethod().toString());
        }
    }

    private void writeAutoModelInformation(AutoModelSpec autoModelSpec, ArimaSetting arimaSetting) {
        information.put(POS_AUTOMODEL, Boolean.toString(true));
        if (arimaSetting.isAcceptDefault()) {
            information.put(POS_ACCEPT_DEFAULT, Boolean.toString(autoModelSpec.isAcceptDefault()));
        }
        if (arimaSetting.isCancelationLimit()) {
            information.put(POS_CANCELATION_LIMIT, Double.toString(autoModelSpec.getCancelationLimit()));
        }
        if (arimaSetting.isInitialUR()) {
            information.put(POS_INITIAL_UR, Double.toString(autoModelSpec.getInitialUnitRootLimit()));
        }
        if (arimaSetting.isFinalUR()) {
            information.put(POS_FINAL_UR, Double.toString(autoModelSpec.getFinalUnitRootLimit()));
        }
        if (arimaSetting.isMixed()) {
            information.put(POS_MIXED, Boolean.toString(autoModelSpec.isMixed()));
        }
        if (arimaSetting.isBalanced()) {
            information.put(POS_BALANCED, Boolean.toString(autoModelSpec.isBalanced()));
        }
        if (arimaSetting.isArmaLimit()) {
            information.put(POS_ARMALIMIT, Double.toString(autoModelSpec.getArmaSignificance()));
        }
        if (arimaSetting.isReduceCV()) {
            information.put(POS_REDUCE_CV, Double.toString(autoModelSpec.getPercentReductionCV()));
        }
        if (arimaSetting.isLjungboxLimit()) {
            information.put(POS_LJUNGBOX_LIMIT, Double.toString(autoModelSpec.getLjungBoxLimit()));
        }
        if (arimaSetting.isUrFinal()) {
            information.put(POS_URFINAL, Double.toString(autoModelSpec.getUnitRootLimit()));
        }
    }

    private void writeARIMAInformation(ArimaSpec arimaSpec, ArimaSetting arimaSetting) {
        information.put(POS_AUTOMODEL, Boolean.toString(false));
        if (arimaSetting.isMean()) {
            information.put(POS_MEAN, Boolean.toString(arimaSpec.isMean()));
        }
        if (arimaSetting.isArimaModel()) {
            StringBuilder arima = new StringBuilder("(");
            arima.append(arimaSpec.getP()).append(" ")
                    .append(arimaSpec.getD()).append(" ")
                    .append(arimaSpec.getQ()).append(")(")
                    .append(arimaSpec.getBP()).append(" ")
                    .append(arimaSpec.getBD()).append(" ")
                    .append(arimaSpec.getBQ()).append(")");
            information.put(POS_ARIMA, arima.toString());
        }
        if (arimaSetting.isP()) {
            for (int i = 0; i < arimaSpec.getP(); i++) {
                Parameter parameter = arimaSpec.getPhi()[i];
                String info = Double.toString(parameter.getValue()) + translateType(parameter);
                information.put(new PositionInfo(INITIAL_POS_P + i, P + (i + 1)), info);
            }
        }
        if (arimaSetting.isQ()) {
            for (int i = 0; i < arimaSpec.getQ(); i++) {
                Parameter parameter = arimaSpec.getTheta()[i];
                String info = Double.toString(parameter.getValue()) + translateType(parameter);
                information.put(new PositionInfo(INITIAL_POS_Q + i, Q + (i + 1)), info);
            }
        }
        if (arimaSetting.isBp()) {
            for (int i = 0; i < arimaSpec.getBP(); i++) {
                Parameter parameter = arimaSpec.getBPhi()[i];
                String info = Double.toString(parameter.getValue()) + translateType(parameter);
                information.put(new PositionInfo(INITIAL_POS_BP + i, BP + (i + 1)), info);
            }
        }
        if (arimaSetting.isBq()) {
            for (int i = 0; i < arimaSpec.getBQ(); i++) {
                Parameter parameter = arimaSpec.getBTheta()[i];
                String info = Double.toString(parameter.getValue()) + translateType(parameter);
                information.put(new PositionInfo(INITIAL_POS_BQ + i, BQ + (i + 1)), info);
            }
        }
    }

    private void writeX11Information(X11Specification x11, X11Setting x11Setting, boolean onlyX11) {
        DecompositionMode mode = x11.getMode();
        //Mode
        if (x11Setting.isMode()) {
            information.put(POS_MODE, mode.toString());
        }
        //Seasonal Component
        if (x11Setting.isSeasonal()) {
            information.put(POS_SEASONAL, Boolean.toString(x11.isSeasonal()));
        }
        //LSigma
        if (x11Setting.isLowerSigma()) {
            information.put(POS_LOWER_SIGMA, Double.toString(x11.getLowerSigma()));
        }
        //USigma
        if (x11Setting.isUpperSigma()) {
            information.put(POS_UPPER_SIGMA, Double.toString(x11.getUpperSigma()));
        }
        //SeasonalFilter
        if (x11Setting.isSeasonalfilter()) {
            SeasonalFilterOption[] seasonalFilters = x11.getSeasonalFilters();
            if (seasonalFilters == null) {
                information.put(POS_SEASONALFILTER, SeasonalFilterOption.Msr.toString());
            } else if (seasonalFilters.length == 1) {
                information.put(POS_SEASONALFILTER, seasonalFilters[0].toString());
            } else {
                for (int i = 0; i < seasonalFilters.length; i++) {
                    information.put(new PositionInfo(INITIAL_POS_SEASONALFILTERS + i, SEASONALFILTERS + (i + 1)), seasonalFilters[i].toString());
                }
            }
        }
        //Henderson
        if (x11Setting.isHenderson()) {
            information.put(POS_HENDERSON, Integer.toString(x11.getHendersonFilterLength()));
        }
        //Calendarsigma
        if (x11Setting.isCalendarsigma()) {
            CalendarSigma calendarSigma = x11.getCalendarSigma();
            information.put(POS_CALENDARSIGMA, calendarSigma.toString());
            if (calendarSigma.equals(CalendarSigma.Select)) {
                SigmavecOption[] sigmavec = x11.getSigmavec();
                if (sigmavec != null) {
                    for (int i = 0; i < sigmavec.length; i++) {
                        information.put(new PositionInfo(INITIAL_POS_SIGMA_VECTOR + i, SIGMA_VECTOR + (i + 1)), sigmavec[i].toString());
                    }
                }
            }
        }
        //Excludeforecast
        if (x11Setting.isExcludeforecast()) {
            information.put(POS_EXCLUDE_FORECAST, Boolean.toString(x11.isExcludefcst()));
        }
        //Bias correction (only for LogAdditive)
        if (mode.equals(DecompositionMode.LogAdditive) && x11Setting.isBiascorrection()) {
            information.put(POS_BIAS_CORRECTION, x11.getBiasCorrection().toString());
        }
        if (!onlyX11) {
            if (x11Setting.isMaxlead()) {
                information.put(POS_MAXLEAD, Integer.toString(x11.getForecastHorizon()));
            }
            if (x11Setting.isMaxback()) {
                information.put(POS_MAXBACK, Integer.toString(x11.getBackcastHorizon()));
            }
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

    private void writeSpan(TsPeriodSelector span, String part, int position) throws AssertionError {
        switch (span.getType()) {
            case All:
                break;
            case From:
                information.put(new PositionInfo(position + MODIFIER_START, part + START), span.getD0().toString());
                break;
            case Between:
                information.put(new PositionInfo(position + MODIFIER_START, part + START), span.getD0().toString());
                information.put(new PositionInfo(position + MODIFIER_END, part + END), span.getD1().toString());
                break;
            case To:
                information.put(new PositionInfo(position + MODIFIER_END, part + END), span.getD1().toString());
                break;
            case First:
                information.put(new PositionInfo(position + MODIFIER_FIRST, part + FIRST), Integer.toString(span.getN0()));
                break;
            case Last:
                information.put(new PositionInfo(position + MODIFIER_LAST, part + LAST), Integer.toString(span.getN1()));
                break;
            case Excluding:
                information.put(new PositionInfo(position + MODIFIER_FIRST, part + FIRST), Integer.toString(span.getN0()));
                information.put(new PositionInfo(position + MODIFIER_LAST, part + LAST), Integer.toString(span.getN1()));
                break;
            case None:
                information.put(new PositionInfo(position + MODIFIER_START, part + START), SPAN_NONE_VALUE);
                information.put(new PositionInfo(position + MODIFIER_END, part + END), SPAN_NONE_VALUE);
                break;
            default:
                throw new AssertionError();
        }
    }

}
