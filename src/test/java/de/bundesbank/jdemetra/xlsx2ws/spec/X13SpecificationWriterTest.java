/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.PositionInfo;
import static de.bundesbank.jdemetra.xlsx2ws.spec.X13SpecificationReader.*;
import static de.bundesbank.jdemetra.xlsx2ws.spec.X13SpecificationWriter.*;
import ec.satoolkit.x11.CalendarSigma;
import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.SigmavecOption;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author s4504tw
 */
public class X13SpecificationWriterTest {

    public X13SpecificationWriterTest() {
    }

    @Test
    public void testWriteSpecification_Rsa5c() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        Map<PositionInfo, String> result = instance.writeSpecification(X13Specification.RSA5);

        Assert.assertTrue(result.containsKey(POS_BASE));
        Assert.assertEquals(X13Specification.RSA5.toString(), result.get(POS_BASE));
    }

    @Test
    public void testWriteSpecification_Rsa5cNoPreCheck() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().getBasic().setPreliminaryCheck(false);
        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_TC_RATE));
        Assert.assertEquals("0.7", result.get(POS_TC_RATE));

        Assert.assertTrue(result.containsKey(POS_CANCELATION_LIMIT));
        Assert.assertEquals("0.1", result.get(POS_CANCELATION_LIMIT));

        Assert.assertTrue(result.containsKey(POS_EXCLUDE_FORECAST));
        Assert.assertEquals("false", result.get(POS_EXCLUDE_FORECAST));

        Assert.assertTrue(result.containsKey(POS_DURATION));
        Assert.assertEquals("8", result.get(POS_DURATION));

        Assert.assertTrue(result.containsKey(POS_SEASONAL));
        Assert.assertEquals("true", result.get(POS_SEASONAL));

        Assert.assertTrue(result.containsKey(POS_LS));
        Assert.assertEquals("true", result.get(POS_LS));

        Assert.assertTrue(result.containsKey(POS_HENDERSON));
        Assert.assertEquals("0", result.get(POS_HENDERSON));

        Assert.assertTrue(result.containsKey(POS_MIXED));
        Assert.assertEquals("true", result.get(POS_MIXED));

        Assert.assertTrue(result.containsKey(POS_AIC_DIFFERENCE));
        Assert.assertEquals("-2.0", result.get(POS_AIC_DIFFERENCE));

        Assert.assertTrue(result.containsKey(POS_EASTER));
        Assert.assertEquals("true", result.get(POS_EASTER));

        Assert.assertTrue(result.containsKey(POS_BALANCED));
        Assert.assertEquals("false", result.get(POS_BALANCED));

        Assert.assertTrue(result.containsKey(POS_MODE));
        Assert.assertEquals("Undefined", result.get(POS_MODE));

        Assert.assertTrue(result.containsKey(POS_TRANSFORM));
        Assert.assertEquals("Auto", result.get(POS_TRANSFORM));

        Assert.assertTrue(result.containsKey(POS_INITIAL_UR));
        Assert.assertEquals("1.0416666666666667", result.get(POS_INITIAL_UR));

        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("false", result.get(POS_PRELIMINARY_CHECK));

        Assert.assertTrue(result.containsKey(POS_EASTER_JULIAN));
        Assert.assertEquals("false", result.get(POS_EASTER_JULIAN));

        Assert.assertTrue(result.containsKey(POS_AUTO_ADJUST));
        Assert.assertEquals("true", result.get(POS_AUTO_ADJUST));

        Assert.assertTrue(result.containsKey(POS_MAXLEAD));
        Assert.assertEquals("-1", result.get(POS_MAXLEAD));

        Assert.assertTrue(result.containsKey(POS_SO));
        Assert.assertEquals("false", result.get(POS_SO));

        Assert.assertTrue(result.containsKey(POS_AUTOMODEL));
        Assert.assertEquals("true", result.get(POS_AUTOMODEL));

        Assert.assertTrue(result.containsKey(POS_SEASONALFILTER));
        Assert.assertEquals("Msr", result.get(POS_SEASONALFILTER));

        Assert.assertTrue(result.containsKey(POS_MAXBACK));
        Assert.assertEquals("0", result.get(POS_MAXBACK));

        Assert.assertTrue(result.containsKey(POS_LJUNGBOX_LIMIT));
        Assert.assertEquals("0.95", result.get(POS_LJUNGBOX_LIMIT));

        Assert.assertTrue(result.containsKey(POS_FINAL_UR));
        Assert.assertEquals("0.88", result.get(POS_FINAL_UR));

        Assert.assertTrue(result.containsKey(POS_ACCEPT_DEFAULT));
        Assert.assertEquals("false", result.get(POS_ACCEPT_DEFAULT));

        Assert.assertTrue(result.containsKey(POS_METHOD));
        Assert.assertEquals("AddOne", result.get(POS_METHOD));

        Assert.assertTrue(result.containsKey(POS_REDUCE_CV));
        Assert.assertEquals("0.14286", result.get(POS_REDUCE_CV));

        Assert.assertTrue(result.containsKey(POS_LOWER_SIGMA));
        Assert.assertEquals("1.5", result.get(POS_LOWER_SIGMA));

        Assert.assertTrue(result.containsKey(POS_TEST));
        Assert.assertEquals("Remove", result.get(POS_TEST));

        Assert.assertTrue(result.containsKey(POS_URFINAL));
        Assert.assertEquals("1.05", result.get(POS_URFINAL));

        Assert.assertTrue(result.containsKey(POS_AO));
        Assert.assertEquals("true", result.get(POS_AO));

        Assert.assertTrue(result.containsKey(POS_TC));
        Assert.assertEquals("true", result.get(POS_TC));

        Assert.assertTrue(result.containsKey(POS_TRADING_DAYS_TYPE));
        Assert.assertEquals("TradingDays", result.get(POS_TRADING_DAYS_TYPE));

        Assert.assertTrue(result.containsKey(POS_ARMALIMIT));
        Assert.assertEquals("1.0", result.get(POS_ARMALIMIT));

        Assert.assertTrue(result.containsKey(POS_CRITICAL_VALUE));
        Assert.assertEquals("0.0", result.get(POS_CRITICAL_VALUE));

        Assert.assertTrue(result.containsKey(POS_UPPER_SIGMA));
        Assert.assertEquals("2.5", result.get(POS_UPPER_SIGMA));

        Assert.assertTrue(result.containsKey(POS_PRE_TEST));
        Assert.assertEquals("Add", result.get(POS_PRE_TEST));

        Assert.assertTrue(result.containsKey(POS_CALENDARSIGMA));
        Assert.assertEquals("None", result.get(POS_CALENDARSIGMA));
    }

    @Test
    public void testWriteSpecification_Rsa5cTolerance0Point5() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().getEstimate().setTol(0.5);
        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_TC_RATE));
        Assert.assertEquals("0.7", result.get(POS_TC_RATE));

        Assert.assertTrue(result.containsKey(POS_TOLERANCE));
        Assert.assertEquals("0.5", result.get(POS_TOLERANCE));

        Assert.assertTrue(result.containsKey(POS_CANCELATION_LIMIT));
        Assert.assertEquals("0.1", result.get(POS_CANCELATION_LIMIT));

        Assert.assertTrue(result.containsKey(POS_EXCLUDE_FORECAST));
        Assert.assertEquals("false", result.get(POS_EXCLUDE_FORECAST));

        Assert.assertTrue(result.containsKey(POS_DURATION));
        Assert.assertEquals("8", result.get(POS_DURATION));

        Assert.assertTrue(result.containsKey(POS_SEASONAL));
        Assert.assertEquals("true", result.get(POS_SEASONAL));

        Assert.assertTrue(result.containsKey(POS_LS));
        Assert.assertEquals("true", result.get(POS_LS));

        Assert.assertTrue(result.containsKey(POS_HENDERSON));
        Assert.assertEquals("0", result.get(POS_HENDERSON));

        Assert.assertTrue(result.containsKey(POS_MIXED));
        Assert.assertEquals("true", result.get(POS_MIXED));

        Assert.assertTrue(result.containsKey(POS_AIC_DIFFERENCE));
        Assert.assertEquals("-2.0", result.get(POS_AIC_DIFFERENCE));

        Assert.assertTrue(result.containsKey(POS_EASTER));
        Assert.assertEquals("true", result.get(POS_EASTER));

        Assert.assertTrue(result.containsKey(POS_BALANCED));
        Assert.assertEquals("false", result.get(POS_BALANCED));

        Assert.assertTrue(result.containsKey(POS_MODE));
        Assert.assertEquals("Undefined", result.get(POS_MODE));

        Assert.assertTrue(result.containsKey(POS_TRANSFORM));
        Assert.assertEquals("Auto", result.get(POS_TRANSFORM));

        Assert.assertTrue(result.containsKey(POS_INITIAL_UR));
        Assert.assertEquals("1.0416666666666667", result.get(POS_INITIAL_UR));

        Assert.assertTrue(!result.containsKey(POS_PRELIMINARY_CHECK));

        Assert.assertTrue(result.containsKey(POS_EASTER_JULIAN));
        Assert.assertEquals("false", result.get(POS_EASTER_JULIAN));

        Assert.assertTrue(result.containsKey(POS_AUTO_ADJUST));
        Assert.assertEquals("true", result.get(POS_AUTO_ADJUST));

        Assert.assertTrue(result.containsKey(POS_MAXLEAD));
        Assert.assertEquals("-1", result.get(POS_MAXLEAD));

        Assert.assertTrue(result.containsKey(POS_SO));
        Assert.assertEquals("false", result.get(POS_SO));

        Assert.assertTrue(result.containsKey(POS_AUTOMODEL));
        Assert.assertEquals("true", result.get(POS_AUTOMODEL));

        Assert.assertTrue(result.containsKey(POS_SEASONALFILTER));
        Assert.assertEquals("Msr", result.get(POS_SEASONALFILTER));

        Assert.assertTrue(result.containsKey(POS_MAXBACK));
        Assert.assertEquals("0", result.get(POS_MAXBACK));

        Assert.assertTrue(result.containsKey(POS_LJUNGBOX_LIMIT));
        Assert.assertEquals("0.95", result.get(POS_LJUNGBOX_LIMIT));

        Assert.assertTrue(result.containsKey(POS_FINAL_UR));
        Assert.assertEquals("0.88", result.get(POS_FINAL_UR));

        Assert.assertTrue(result.containsKey(POS_ACCEPT_DEFAULT));
        Assert.assertEquals("false", result.get(POS_ACCEPT_DEFAULT));

        Assert.assertTrue(result.containsKey(POS_METHOD));
        Assert.assertEquals("AddOne", result.get(POS_METHOD));

        Assert.assertTrue(result.containsKey(POS_REDUCE_CV));
        Assert.assertEquals("0.14286", result.get(POS_REDUCE_CV));

        Assert.assertTrue(result.containsKey(POS_LOWER_SIGMA));
        Assert.assertEquals("1.5", result.get(POS_LOWER_SIGMA));

        Assert.assertTrue(result.containsKey(POS_TEST));
        Assert.assertEquals("Remove", result.get(POS_TEST));

        Assert.assertTrue(result.containsKey(POS_URFINAL));
        Assert.assertEquals("1.05", result.get(POS_URFINAL));

        Assert.assertTrue(result.containsKey(POS_AO));
        Assert.assertEquals("true", result.get(POS_AO));

        Assert.assertTrue(result.containsKey(POS_TC));
        Assert.assertEquals("true", result.get(POS_TC));

        Assert.assertTrue(result.containsKey(POS_TRADING_DAYS_TYPE));
        Assert.assertEquals("TradingDays", result.get(POS_TRADING_DAYS_TYPE));

        Assert.assertTrue(result.containsKey(POS_ARMALIMIT));
        Assert.assertEquals("1.0", result.get(POS_ARMALIMIT));

        Assert.assertTrue(result.containsKey(POS_CRITICAL_VALUE));
        Assert.assertEquals("0.0", result.get(POS_CRITICAL_VALUE));

        Assert.assertTrue(result.containsKey(POS_UPPER_SIGMA));
        Assert.assertEquals("2.5", result.get(POS_UPPER_SIGMA));

        Assert.assertTrue(result.containsKey(POS_PRE_TEST));
        Assert.assertEquals("Add", result.get(POS_PRE_TEST));

        Assert.assertTrue(result.containsKey(POS_CALENDARSIGMA));
        Assert.assertEquals("None", result.get(POS_CALENDARSIGMA));
    }

    @Test
    public void testWriteSpecification_ARIMA111111() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().setUsingAutoModel(false);
        ArimaSpec arima = spec.getRegArimaSpecification().getArima();
        arima.setP(1);
        arima.setD(1);
        arima.setQ(1);
        arima.setBP(1);
        arima.setBD(1);
        arima.setBQ(1);
        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_ARIMA));
        Assert.assertEquals("(1 1 1)(1 1 1)", result.get(POS_ARIMA));

        PositionInfo p = new PositionInfo(INITIAL_POS_P, P + 1);
        Assert.assertTrue(result.containsKey(p));
        Assert.assertEquals("0.0*u", result.get(p));

        PositionInfo q = new PositionInfo(INITIAL_POS_Q, Q + 1);
        Assert.assertTrue(result.containsKey(q));
        Assert.assertEquals("0.0*u", result.get(q));

        PositionInfo bp = new PositionInfo(INITIAL_POS_BP, BP + 1);
        Assert.assertTrue(result.containsKey(bp));
        Assert.assertEquals("0.0*u", result.get(bp));

        PositionInfo bq = new PositionInfo(INITIAL_POS_BQ, BQ + 1);
        Assert.assertTrue(result.containsKey(bq));
        Assert.assertEquals("0.0*u", result.get(bq));
    }

    @Test
    public void testWriteSpecification_ARIMA300001() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().setUsingAutoModel(false);
        ArimaSpec arima = spec.getRegArimaSpecification().getArima();
        arima.setP(3);
        arima.setD(0);
        arima.setQ(0);
        arima.setBP(0);
        arima.setBD(0);
        arima.setBQ(1);

        arima.setPhi(new Parameter[]{new Parameter(1, ParameterType.Fixed), new Parameter(0, ParameterType.Undefined), new Parameter(0.5, ParameterType.Initial)});
        arima.setBTheta(new Parameter[]{new Parameter(2, ParameterType.Fixed)});
        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_ARIMA));
        Assert.assertEquals("(3 0 0)(0 0 1)", result.get(POS_ARIMA));

        PositionInfo p1 = new PositionInfo(INITIAL_POS_P, P + 1);
        Assert.assertTrue(result.containsKey(p1));
        Assert.assertEquals("1.0*f", result.get(p1));

        PositionInfo p2 = new PositionInfo(INITIAL_POS_P + 1, P + 2);
        Assert.assertTrue(result.containsKey(p2));
        Assert.assertEquals("0.0*u", result.get(p2));

        PositionInfo p3 = new PositionInfo(INITIAL_POS_P + 2, P + 3);
        Assert.assertTrue(result.containsKey(p3));
        Assert.assertEquals("0.5*i", result.get(p3));

        PositionInfo bq = new PositionInfo(INITIAL_POS_BQ, BQ + 1);
        Assert.assertTrue(result.containsKey(bq));
        Assert.assertEquals("2.0*f", result.get(bq));
    }

    @Test
    public void testWriteSpecification_OutliersSwitched() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().setUsingAutoModel(false);
        OutlierSpec outlierSpec = spec.getRegArimaSpecification().getOutliers();
        outlierSpec.clearTypes();
        outlierSpec.add(OutlierType.SO);
        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_AO));
        Assert.assertEquals("false", result.get(POS_AO));

        Assert.assertTrue(result.containsKey(POS_LS));
        Assert.assertEquals("false", result.get(POS_LS));

        Assert.assertTrue(result.containsKey(POS_TC));
        Assert.assertEquals("false", result.get(POS_TC));

        Assert.assertTrue(result.containsKey(POS_SO));
        Assert.assertEquals("true", result.get(POS_SO));
    }

    @Test
    public void testWriteSpecification_OutliersNotUsed() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().setUsingAutoModel(false);
        OutlierSpec outlierSpec = spec.getRegArimaSpecification().getOutliers();
        outlierSpec.clearTypes();
        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(POS_AO));
        Assert.assertTrue(!result.containsKey(POS_LS));
        Assert.assertTrue(!result.containsKey(POS_TC));
        Assert.assertTrue(!result.containsKey(POS_SO));
    }

    @Test
    public void testWriteSpecification_X11Only() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setSeasonalFilter(SeasonalFilterOption.S3X15);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_BASE));
        Assert.assertEquals("X11", result.get(POS_BASE));

        Assert.assertTrue(result.containsKey(POS_SEASONALFILTER));
        Assert.assertEquals("S3X15", result.get(POS_SEASONALFILTER));

        Assert.assertTrue(!result.containsKey(POS_MAXLEAD));
        Assert.assertTrue(!result.containsKey(POS_MAXBACK));
    }

    @Test
    public void testWriteSpecification_X11OnlyMultipleFilters() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setSeasonalFilters(new SeasonalFilterOption[]{SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X3, SeasonalFilterOption.S3X5, SeasonalFilterOption.S3X9});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_BASE));
        Assert.assertEquals("X11", result.get(POS_BASE));

        PositionInfo seasonalFilter1 = new PositionInfo(INITIAL_POS_SEASONALFILTERS, SEASONALFILTERS + 1);
        Assert.assertTrue(result.containsKey(seasonalFilter1));
        Assert.assertEquals("S3X15", result.get(seasonalFilter1));

        PositionInfo seasonalFilter2 = new PositionInfo(INITIAL_POS_SEASONALFILTERS + 1, SEASONALFILTERS + 2);
        Assert.assertTrue(result.containsKey(seasonalFilter2));
        Assert.assertEquals("S3X3", result.get(seasonalFilter2));

        PositionInfo seasonalFilter3 = new PositionInfo(INITIAL_POS_SEASONALFILTERS + 2, SEASONALFILTERS + 3);
        Assert.assertTrue(result.containsKey(seasonalFilter3));
        Assert.assertEquals("S3X5", result.get(seasonalFilter3));

        PositionInfo seasonalFilter4 = new PositionInfo(INITIAL_POS_SEASONALFILTERS + 3, SEASONALFILTERS + 4);
        Assert.assertTrue(result.containsKey(seasonalFilter4));
        Assert.assertEquals("S3X9", result.get(seasonalFilter4));

        Assert.assertTrue(!result.containsKey(POS_MAXLEAD));
        Assert.assertTrue(!result.containsKey(POS_MAXBACK));
    }

    @Test
    public void testWriteSpecification_X11OnlyCalendarSigmaSelect() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setCalendarSigma(CalendarSigma.Select);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_BASE));
        Assert.assertEquals("X11", result.get(POS_BASE));

        Assert.assertTrue(result.containsKey(POS_CALENDARSIGMA));
        Assert.assertEquals("Select", result.get(POS_CALENDARSIGMA));

        Assert.assertTrue(!result.containsKey(POS_MAXLEAD));
        Assert.assertTrue(!result.containsKey(POS_MAXBACK));
    }

    @Test
    public void testWriteSpecification_X11OnlyCalendarSigmaSelectSigmaVecGroups() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        spec.getX11Specification().setSigmavec(new SigmavecOption[]{SigmavecOption.Group1, SigmavecOption.Group2, SigmavecOption.Group2, SigmavecOption.Group1});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_BASE));
        Assert.assertEquals("X11", result.get(POS_BASE));

        Assert.assertTrue(result.containsKey(POS_CALENDARSIGMA));
        Assert.assertEquals("Select", result.get(POS_CALENDARSIGMA));

        PositionInfo sigmaVector1 = new PositionInfo(INITIAL_POS_SIGMA_VECTOR, SIGMA_VECTOR + 1);
        Assert.assertTrue(result.containsKey(sigmaVector1));
        Assert.assertEquals("Group1", result.get(sigmaVector1));

        PositionInfo sigmaVector2 = new PositionInfo(INITIAL_POS_SIGMA_VECTOR + 1, SIGMA_VECTOR + 2);
        Assert.assertTrue(result.containsKey(sigmaVector2));
        Assert.assertEquals("Group2", result.get(sigmaVector2));

        PositionInfo sigmaVector3 = new PositionInfo(INITIAL_POS_SIGMA_VECTOR + 2, SIGMA_VECTOR + 3);
        Assert.assertTrue(result.containsKey(sigmaVector3));
        Assert.assertEquals("Group2", result.get(sigmaVector3));

        PositionInfo sigmaVector4 = new PositionInfo(INITIAL_POS_SIGMA_VECTOR + 3, SIGMA_VECTOR + 4);
        Assert.assertTrue(result.containsKey(sigmaVector4));
        Assert.assertEquals("Group1", result.get(sigmaVector4));

        Assert.assertTrue(!result.containsKey(POS_MAXLEAD));
        Assert.assertTrue(!result.containsKey(POS_MAXBACK));
    }

    @Test
    public void testWriteSpecification_SeriesFrom1970() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.from(new Day(1970, Month.January, 0));

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(SERIES_START));
        Assert.assertEquals("1970-01-01", result.get(SERIES_START));
        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(POS_PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesTo1970() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.to(new Day(1970, Month.January, 0));

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(SERIES_END));
        Assert.assertEquals("1970-01-01", result.get(SERIES_END));
        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(POS_PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesBetween1960_1970() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.between(new Day(1960, Month.January, 0), new Day(1970, Month.January, 0));

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(SERIES_START));
        Assert.assertEquals("1960-01-01", result.get(SERIES_START));
        Assert.assertTrue(result.containsKey(SERIES_END));
        Assert.assertEquals("1970-01-01", result.get(SERIES_END));
        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(POS_PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesFirst10() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.first(10);

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(SERIES_FIRST));
        Assert.assertEquals("10", result.get(SERIES_FIRST));
        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(POS_PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesLast10() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.last(10);

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(SERIES_LAST));
        Assert.assertEquals("10", result.get(SERIES_LAST));
        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(POS_PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesExcluding1020() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.excluding(10, 20);

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(SERIES_FIRST));
        Assert.assertEquals("10", result.get(SERIES_FIRST));
        Assert.assertTrue(result.containsKey(SERIES_LAST));
        Assert.assertEquals("20", result.get(SERIES_LAST));
        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(POS_PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesNone() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.none();

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(SERIES_START));
        Assert.assertEquals("X", result.get(SERIES_START));
        Assert.assertTrue(result.containsKey(SERIES_END));
        Assert.assertEquals("X", result.get(SERIES_END));
        Assert.assertTrue(result.containsKey(POS_PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(POS_PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_TransformNone() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_TRANSFORM));
        Assert.assertEquals("None", result.get(POS_TRANSFORM));
        Assert.assertTrue(!result.containsKey(POS_AIC_DIFFERENCE));
        Assert.assertTrue(!result.containsKey(POS_ADJUST));
    }

    @Test
    public void testWriteSpecification_TransformLog() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_TRANSFORM));
        Assert.assertEquals("Log", result.get(POS_TRANSFORM));
        Assert.assertTrue(!result.containsKey(POS_AIC_DIFFERENCE));
        Assert.assertTrue(result.containsKey(POS_ADJUST));
        Assert.assertEquals("None", result.get(POS_ADJUST));
    }

    @Test
    public void testWriteSpecification_RegressionTradingDaysAndHolidays() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        TradingDaysSpec tradingDays = spec.getRegArimaSpecification().getRegression().getTradingDays();

        tradingDays.setHolidays("X.X");
        tradingDays.setAutoAdjust(false);
        tradingDays.setLengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_HOLIDAYS));
        Assert.assertEquals("X.X", result.get(POS_HOLIDAYS));
        Assert.assertTrue(result.containsKey(POS_TRADING_DAYS_TYPE));
        Assert.assertEquals("TradingDays", result.get(POS_TRADING_DAYS_TYPE));
        Assert.assertTrue(result.containsKey(POS_AUTO_ADJUST));
        Assert.assertEquals("false", result.get(POS_AUTO_ADJUST));
        Assert.assertTrue(result.containsKey(POS_LEAP_YEAR));
        Assert.assertEquals("LengthOfPeriod", result.get(POS_LEAP_YEAR));
    }

    @Test
    public void testWriteSpecification_RegressionStockTradingDays() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();
        TradingDaysSpec tradingDays = spec.getRegArimaSpecification().getRegression().getTradingDays();

        tradingDays.setStockTradingDays(30);
        tradingDays.setAutoAdjust(false);
        tradingDays.setLengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(POS_HOLIDAYS));
        Assert.assertEquals("30", result.get(POS_W));
        Assert.assertTrue(result.containsKey(POS_TRADING_DAYS_TYPE));
        Assert.assertEquals("None", result.get(POS_TRADING_DAYS_TYPE));
        Assert.assertTrue(!result.containsKey(POS_AUTO_ADJUST));
        Assert.assertTrue(!result.containsKey(POS_LEAP_YEAR));
    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedCalendar() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getRegression().getTradingDays().setUserVariables(new String[]{"X.X"});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(POS_HOLIDAYS));
        Assert.assertTrue(!result.containsKey(POS_W));
        Assert.assertTrue(result.containsKey(POS_TRADING_DAYS_TYPE));
        Assert.assertEquals("None", result.get(POS_TRADING_DAYS_TYPE));
        Assert.assertTrue(!result.containsKey(POS_AUTO_ADJUST));
        Assert.assertTrue(!result.containsKey(POS_LEAP_YEAR));
        Assert.assertTrue(result.containsKey(REGRESSOR1));
        Assert.assertEquals("X.X*c", result.get(REGRESSOR1));
    }

    @Test
    public void testWriteSpecification_RegressionNoTradingDaysNoEaster() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA0.clone();
        spec.getRegArimaSpecification().getBasic().setPreliminaryCheck(false);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(POS_HOLIDAYS));
        Assert.assertTrue(!result.containsKey(POS_W));
        Assert.assertTrue(result.containsKey(POS_TRADING_DAYS_TYPE));
        Assert.assertEquals("None", result.get(POS_TRADING_DAYS_TYPE));
        Assert.assertTrue(!result.containsKey(POS_AUTO_ADJUST));
        Assert.assertTrue(!result.containsKey(POS_LEAP_YEAR));
        Assert.assertTrue(result.containsKey(POS_EASTER));
        Assert.assertEquals("false", result.get(POS_EASTER));
    }

    @Test
    public void testWriteSpecification_RegressionJulianEaster() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA0.clone();

        MovingHolidaySpec easter = MovingHolidaySpec.easterSpec(true, true);
        spec.getRegArimaSpecification().getRegression().add(easter);

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(POS_EASTER));
        Assert.assertEquals("true", result.get(POS_EASTER));
        Assert.assertTrue(result.containsKey(POS_EASTER_JULIAN));
        Assert.assertEquals("true", result.get(POS_EASTER_JULIAN));
        Assert.assertTrue(result.containsKey(POS_PRE_TEST));
        Assert.assertEquals("Add", result.get(POS_PRE_TEST));
        Assert.assertTrue(result.containsKey(POS_DURATION));
        Assert.assertEquals("8", result.get(POS_DURATION));
    }

    @Test
    public void testWriteSpecification_RegressionPreSpecifiedOutlierAO19700601() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getRegression().setOutliers(new OutlierDefinition[]{new OutlierDefinition(new TsPeriod(TsFrequency.Monthly, 1970, 5), OutlierType.AO)});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        PositionInfo outlier = new PositionInfo(INITIAL_POS_OUTLIER, OUTLIER + 1);
        Assert.assertTrue(result.containsKey(outlier));
        Assert.assertEquals("AO1970-06-01", result.get(outlier));
    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesUndefined() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Undefined);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(REGRESSOR1));
        Assert.assertEquals("X.X*u", result.get(REGRESSOR1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesSeries() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Series);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(REGRESSOR1));
        Assert.assertEquals("X.X*y", result.get(REGRESSOR1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesTrend() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Trend);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(REGRESSOR1));
        Assert.assertEquals("X.X*t", result.get(REGRESSOR1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesSeasonal() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(REGRESSOR1));
        Assert.assertEquals("X.X*s", result.get(REGRESSOR1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesSeasonallyAdjusted() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.SeasonallyAdjusted);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(REGRESSOR1));
        Assert.assertEquals("X.X*sa", result.get(REGRESSOR1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesIrregular() {
        X13SpecificationWriter instance = new X13SpecificationWriter();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<PositionInfo, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(REGRESSOR1));
        Assert.assertEquals("X.X*i", result.get(REGRESSOR1));

    }
    private static final PositionInfo REGRESSOR1 = new PositionInfo(INITIAL_POS_REGRESSOR, REGRESSOR + 1);
    private static final PositionInfo SERIES_START = new PositionInfo(INITIAL_POS_SERIES_SPAN + MODIFIER_START, SERIES + START);
    private static final PositionInfo SERIES_END = new PositionInfo(INITIAL_POS_SERIES_SPAN + MODIFIER_END, SERIES + END);
    private static final PositionInfo SERIES_FIRST = new PositionInfo(INITIAL_POS_SERIES_SPAN + MODIFIER_FIRST, SERIES + FIRST);
    private static final PositionInfo SERIES_LAST = new PositionInfo(INITIAL_POS_SERIES_SPAN + MODIFIER_LAST, SERIES + LAST);

}
