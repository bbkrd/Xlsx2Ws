/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.Message;
import de.bundesbank.jdemetra.xlsx2ws.dto.SpecificationDTO;
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
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class X13SpecificationReaderTest {

    private static final Message ALL_FINE = new Message(Level.FINE, "Everything is fine!");

    public X13SpecificationReaderTest() {
    }

    @Test
    public void testReadSpecification_RSA0() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.BASE, "RSA0");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(X13Specification.RSA0, specification);
    }

    @Test
    public void testReadSpecification_Start1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "1970");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.from(new Day(1970, Month.January, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_StartWrongFormat() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "1970.04A"); //instead of 1970.04
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsPeriodSelector expected = new TsPeriodSelector();

        Assert.assertEquals(new Message(Level.SEVERE, "Unparseable Date format in series_start."), messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_StartNone() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "X");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.none();

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_EndNone() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.END, "X");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.none();

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_First10() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.FIRST, "10");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.first(10);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Last10() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.LAST, "10");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.last(10);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_ExcludingFirst5Last10() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.FIRST, "5");
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.LAST, "10");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.excluding(5, 10);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Start197004() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "1970.04");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.from(new Day(1970, Month.April, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_End1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.END, "1970");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.to(new Day(1970, Month.January, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Between1970_1980() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "1970");
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.END, "1980");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.between(new Day(1970, Month.January, 0), new Day(1980, Month.January, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Between19701105_19800228() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "1970.11.05");
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.END, "1980.02.28");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.between(new Day(1970, Month.November, 4), new Day(1980, Month.February, 27));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Between19000228_19000301ExcelDate() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "59");
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.END, "61");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.between(new Day(1900, Month.February, 27), new Day(1900, Month.March, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Between19701105_19800228ExcelDate() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.START, "25877");
        instance.putInformation(X13SpecificationReader.SERIES + X13SpecificationReader.END, "29279");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.between(new Day(1970, Month.November, 4), new Day(1980, Month.February, 27));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_MaxleadMinus2() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MAXLEAD, "-2");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(-2, specification.getX11Specification().getForecastHorizon());
    }

    @Test
    public void testReadSpecification_Maxlead24() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MAXLEAD, "24");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(24, specification.getX11Specification().getForecastHorizon());
    }

    @Test
    public void testReadSpecification_BaseX11MaxleadMinus2() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.BASE, "X11");
        instance.putInformation(X13SpecificationReader.MAXLEAD, "-2");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(X13Specification.RSAX11, specification);
        Assert.assertEquals(0, specification.getX11Specification().getForecastHorizon());
    }

    @Test
    public void testReadSpecification_Henderson101() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "101");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(101, specification.getX11Specification().getHendersonFilterLength());
    }

    @Test
    public void testReadSpecification_Henderson3() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "3");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(3, specification.getX11Specification().getHendersonFilterLength());
    }

    @Test
    public void testReadSpecification_Henderson3Point5() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "3.5");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();

        Assert.assertEquals(new Message(Level.SEVERE, "The information henderson doesn't contain a parseble integer value."), readSpecification.getMessages()[0]);
        Assert.assertEquals(0, specification.getX11Specification().getHendersonFilterLength());
    }

    @Test
    public void testReadSpecification_Henderson28() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "28");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);

        Assert.assertEquals(new Message(Level.SEVERE, "The value 28 is no valid input for henderson. (Invalid henderson length)"), readSpecification.getMessages()[0]);
        Assert.assertEquals(X13Specification.RSA0, readSpecification.getSpecification());
    }

    @Test
    public void testReadSpecification_Henderson103() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "103");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);

        Assert.assertEquals(new Message(Level.SEVERE, "The value 103 is no valid input for henderson. (Invalid henderson length)"), readSpecification.getMessages()[0]);
        Assert.assertEquals(X13Specification.RSA0, readSpecification.getSpecification());
    }

    @Test
    public void testReadSpecification_HendersonAbc() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "Abc");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        Assert.assertEquals(new Message(Level.SEVERE, "The information henderson doesn't contain a parseble integer value."), readSpecification.getMessages()[0]);
    }

    @Test
    public void testReadSpecification_TransformLog() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "Log");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(DefaultTransformationType.Log, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_TransformAuto() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "Auto");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(DefaultTransformationType.Auto, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_TransformNone() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "None");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(DefaultTransformationType.None, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_TransformWrong() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "Wrong");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(DefaultTransformationType.None, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_CriticalValue5Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.CRITICAL_VALUE, "5.4");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(5.4, specification.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), 10e-15);
    }

    @Test
    public void testReadSpecification_UpperSigma5Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.UPPER_SIGMA, "5.4");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(5.4, specification.getX11Specification().getUpperSigma(), 10e-15);
    }

    @Test
    public void testReadSpecification_LowerSigma5Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "5.4");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(5.4, specification.getX11Specification().getLowerSigma(), 10e-15);
    }

    @Test(expected = X11Exception.class)
    public void testReadSpecification_LowerSigma0Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "0.4");
        instance.readSpecification(null).getSpecification();
    }

    @Test
    public void testReadSpecification_LowerSigma5Point4AndUpperSigma5Point5() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "5.4");
        instance.putInformation(X13SpecificationReader.UPPER_SIGMA, "5.5");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(5.4, specification.getX11Specification().getLowerSigma(), 10e-15);
        Assert.assertEquals(5.5, specification.getX11Specification().getUpperSigma(), 10e-15);
    }

    @Test(expected = X11Exception.class)
    public void testReadSpecification_LowerSigma5Point4AndUpperSigma5Point3() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "5.4");
        instance.putInformation(X13SpecificationReader.UPPER_SIGMA, "5.3");
        instance.readSpecification(null).getSpecification();
    }

    @Test
    public void testReadSpecification_Arima312111() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(312)(111)");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(3, specification.getRegArimaSpecification().getArima().getP());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getD());
        Assert.assertEquals(2, specification.getRegArimaSpecification().getArima().getQ());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBP());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBD());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());

    }

    @Test
    public void testReadSpecification_Arima312111WithSomeFixedParameters() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(312)(111)");
        instance.putInformation(X13SpecificationReader.P + 1, "0.1");
        instance.putInformation(X13SpecificationReader.P + 3, "0.2");
        instance.putInformation(X13SpecificationReader.Q + 2, "0.3");
        instance.putInformation(X13SpecificationReader.BP + 1, "0.4");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.5");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(3, specification.getRegArimaSpecification().getArima().getP());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getD());
        Assert.assertEquals(2, specification.getRegArimaSpecification().getArima().getQ());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBP());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBD());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertEquals(new Parameter(0.1, ParameterType.Fixed), specification.getRegArimaSpecification().getArima().getPhi()[0]);
        Assert.assertEquals(new Parameter(), specification.getRegArimaSpecification().getArima().getPhi()[1]);
        Assert.assertEquals(new Parameter(0.2, ParameterType.Fixed), specification.getRegArimaSpecification().getArima().getPhi()[2]);
        Assert.assertEquals(new Parameter(), specification.getRegArimaSpecification().getArima().getTheta()[0]);
        Assert.assertEquals(new Parameter(0.3, ParameterType.Fixed), specification.getRegArimaSpecification().getArima().getTheta()[1]);
        Assert.assertEquals(new Parameter(0.4, ParameterType.Fixed), specification.getRegArimaSpecification().getArima().getBPhi()[0]);
        Assert.assertEquals(new Parameter(0.5, ParameterType.Fixed), specification.getRegArimaSpecification().getArima().getBTheta()[0]);

    }

    @Test
    public void testReadSpecification_ArimaWrongFormat() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(32)(111)");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(new ArimaSpec(), specification.getRegArimaSpecification().getArima());

    }

    @Test
    public void testReadSpecification_AutoTrue() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.AUTOMODEL, "true");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(true, specification.getRegArimaSpecification().isUsingAutoModel());

    }

    @Test
    public void testReadSpecification_AutoFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.AUTOMODEL, "false");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(false, specification.getRegArimaSpecification().isUsingAutoModel());

    }

    @Test
    public void testReadSpecification_MeanTrue() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MEAN, "true");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(true, specification.getRegArimaSpecification().getArima().isMean());

    }

    @Test
    public void testReadSpecification_MeanFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MEAN, "false");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(false, specification.getRegArimaSpecification().getArima().isMean());

    }

    @Test
    public void testReadSpecification_OutlierAO1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "AO1970");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(1, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new OutlierDefinition(new Day(1970, Month.January, 0), AdditiveOutlier.CODE), specification.getRegArimaSpecification().getRegression().getOutliers()[0]);
    }

    @Test
    public void testReadSpecification_OutlierAOWrongDate() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "AO1970A00");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(new Message(Level.SEVERE, "Unparseable Date format in outlier_1."), messages[0]);
        Assert.assertEquals(0, specification.getRegArimaSpecification().getRegression().getOutliersCount());
    }

    @Test
    public void testReadSpecification_OutlierLS1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "LS1970");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(1, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new OutlierDefinition(new Day(1970, Month.January, 0), LevelShift.CODE), specification.getRegArimaSpecification().getRegression().getOutliers()[0]);
    }

    @Test
    public void testReadSpecification_OutlierTC1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "TC1970");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(1, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new OutlierDefinition(new Day(1970, Month.January, 0), TransitoryChange.CODE), specification.getRegArimaSpecification().getRegression().getOutliers()[0]);
    }

    @Test
    public void testReadSpecification_OutlierSO1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "SO1970");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(1, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new OutlierDefinition(new Day(1970, Month.January, 0), SeasonalOutlier.CODE), specification.getRegArimaSpecification().getRegression().getOutliers()[0]);
    }

    @Test
    public void testReadSpecification_OutlierAllTypes() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "AO1970.02.05");
        instance.putInformation(X13SpecificationReader.OUTLIER + 2, "LS1980.04.10");
        instance.putInformation(X13SpecificationReader.OUTLIER + 3, "TC1990.06.15");
        instance.putInformation(X13SpecificationReader.OUTLIER + 4, "SO2000.08.20");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Set<OutlierDefinition> expectedOutliers = new HashSet<>();
        expectedOutliers.add(new OutlierDefinition(new Day(1970, Month.February, 4), AdditiveOutlier.CODE));
        expectedOutliers.add(new OutlierDefinition(new Day(1980, Month.April, 9), LevelShift.CODE));
        expectedOutliers.add(new OutlierDefinition(new Day(1990, Month.June, 14), TransitoryChange.CODE));
        expectedOutliers.add(new OutlierDefinition(new Day(2000, Month.August, 19), SeasonalOutlier.CODE));

        Assert.assertEquals(4, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        for (OutlierDefinition outlier : specification.getRegArimaSpecification().getRegression().getOutliers()) {
            Assert.assertTrue(expectedOutliers.contains(outlier));
        }
    }

    @Test
    public void testReadSpecification_OutlierUnknownOutlier() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "OO1970");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(0, specification.getRegArimaSpecification().getRegression().getOutliersCount());
    }

    @Test
    public void testReadSpecification_OutlierWrongFormat() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        String expectedMessage = "The information in outlier_1 doesn't follow the outlier syntax.";

        Assert.assertEquals(0, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new Message(Level.SEVERE, expectedMessage), messages[0]);
    }

    @Test
    public void testReadSpecification_SeasonalFilterS3X15() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + 1, "S3X15");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertArrayEquals(new SeasonalFilterOption[]{SeasonalFilterOption.S3X15}, specification.getX11Specification().getSeasonalFilters());
    }

    @Test
    public void testReadSpecification_SeasonalFilterA() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + "a", "S3X9");
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + 1, "S3X15");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertArrayEquals(new SeasonalFilterOption[]{SeasonalFilterOption.S3X15}, specification.getX11Specification().getSeasonalFilters());
    }

    @Test
    public void testReadSpecification_SeasonalFilterTooFewFilters() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + 12, "S3X9");
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + 1, "S3X15");
        String expected = "The maximal seasonalfilters_(12) isn't the same as the number of seasonalfilters_(2) specified.";
        Assert.assertEquals(new Message(Level.SEVERE, expected), instance.readSpecification(null).getMessages()[0]);
    }

    @Test
    public void testReadSpecification_Tolerance() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TOLERANCE, "0.1");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertEquals(0.1, specification.getRegArimaSpecification().getEstimate().getTol(), 0);
    }

    @Test
    public void testReadSpecification_ToleranceNonDoubleInput() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TOLERANCE, "abc");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(1.0E-7, specification.getRegArimaSpecification().getEstimate().getTol(), 0);
        Assert.assertEquals(new Message(Level.SEVERE, "The information tolerance doesn't contain a parseble floating point value."), messages[0]);
    }

    @Test
    public void testReadSpecification_OldSpec() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification old = X13Specification.RSA4.clone();
        X13Specification specification = instance.readSpecification(old).getSpecification();

        Assert.assertEquals(X13Specification.RSA4, specification);
    }

    @Test
    public void testReadSpecification_PreCheckTrue() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.PRELIMINARY_CHECK, "true");
        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(true, specification.getRegArimaSpecification().getBasic().isPreliminaryCheck());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_PreCheckFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.PRELIMINARY_CHECK, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(false, specification.getRegArimaSpecification().getBasic().isPreliminaryCheck());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_PreCheckNonBooleanInput() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.PRELIMINARY_CHECK, "0.1");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(false, specification.getRegArimaSpecification().getBasic().isPreliminaryCheck());
        Assert.assertEquals(new Message(Level.WARNING, "The information preliminary_check doesn't contain \"true\" or \"false\". It will be set to false."), messages[0]);
    }

    @Test
    public void testReadSpecification_PreCheckNullInput() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.PRELIMINARY_CHECK, null);

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(false, specification.getRegArimaSpecification().getBasic().isPreliminaryCheck());
        Assert.assertEquals(new Message(Level.WARNING, "The information preliminary_check doesn't contain \"true\" or \"false\". It will be set to false."), messages[0]);
    }

    @Test
    public void testReadSpecification_Seasonalfilter() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONALFILTER, "S3X5");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(1, specification.getX11Specification().getSeasonalFilters().length);
        Assert.assertEquals(SeasonalFilterOption.S3X5, specification.getX11Specification().getSeasonalFilters()[0]);
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_SeasonalFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONAL, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(null, specification.getX11Specification().getSeasonalFilters());
        Assert.assertEquals(false, specification.getX11Specification().isSeasonal());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA100000_PStarFixed() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(100)(000)");
        instance.putInformation(X13SpecificationReader.P + 1, "0.1*f");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getPhi());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getP());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA100000_PDefault() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(100)(000)");
        instance.putInformation(X13SpecificationReader.P + 1, "0.1");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getPhi());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getP());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA000100_BPStarFixed() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(100)");
        instance.putInformation(X13SpecificationReader.BP + 1, "0.1*f");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getBPhi());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBP());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA100000_QStarFixed() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(001)(000)");
        instance.putInformation(X13SpecificationReader.Q + 1, "0.1*f");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getQ());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA100000_BQStarFixed() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*f");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA100000_BQStarUndefined() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*u");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Undefined)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA100000_BQStarInitial() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*i");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Initial)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertEquals(ALL_FINE, messages[0]);
    }

    @Test
    public void testReadSpecification_ARIMA100000_BQStarFixedDefault() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*a");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertEquals(new Message(Level.INFO, "Parameter bq_1 has no known type declared. It will be assumed to be fixed."), messages[0]);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierAO_True() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.AO, "true");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.AO)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(exists);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierAO_False() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.AO, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.AO)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(!exists);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierLS_True() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LS, "true");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.LS)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(exists);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierLS_False() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LS, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.LS)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(!exists);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierTC_True() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TC, "true");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.TC)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(exists);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierTC_False() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TC, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.TC)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(!exists);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierSO_True() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SO, "true");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.SO)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(exists);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierSO_False() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SO, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        boolean exists = false;
        for (SingleOutlierSpec type : specification.getRegArimaSpecification().getOutliers().getTypes()) {
            if (type.getType().equals(OutlierType.SO)) {
                exists = true;
                break;
            }
        }
        Assert.assertTrue(!exists);
    }

    @Test
    public void testReadSpecification_EasterFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.EASTER, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(null, specification.getRegArimaSpecification().getRegression().getEaster());
    }

    @Test
    public void testReadSpecification_EasterABC() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.EASTER, "ABC");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(new Message(Level.WARNING, "The information easter doesn't contain \"true\" or \"false\". It will be set to false."), messages[0]);
        Assert.assertEquals(null, specification.getRegArimaSpecification().getRegression().getEaster());
    }

    @Test
    public void testReadSpecification_EasterTrue() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.EASTER, "true");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(MovingHolidaySpec.easterSpec(true), specification.getRegArimaSpecification().getRegression().getEaster());
    }

    @Test
    public void testReadSpecification_EasterJulianTrue() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.EASTER, "true");
        instance.putInformation(X13SpecificationReader.EASTER_JULIAN, "true");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(MovingHolidaySpec.easterSpec(true, true), specification.getRegArimaSpecification().getRegression().getEaster());
    }

    @Test
    public void testReadSpecification_EasterJulianFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.EASTER, "true");
        instance.putInformation(X13SpecificationReader.EASTER_JULIAN, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(MovingHolidaySpec.easterSpec(true, false), specification.getRegArimaSpecification().getRegression().getEaster());
    }

    @Test
    public void testReadSpecification_EasterJulianABC() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.EASTER, "true");
        instance.putInformation(X13SpecificationReader.EASTER_JULIAN, "ABC");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(new Message(Level.WARNING, "The information easter_julian doesn't contain \"true\" or \"false\". It will be set to false."), messages[0]);
        Assert.assertEquals(MovingHolidaySpec.easterSpec(true, false), specification.getRegArimaSpecification().getRegression().getEaster());
    }

    @Test
    public void testReadSpecification_EasterRegressionTestNone() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.EASTER, "true");
        instance.putInformation(X13SpecificationReader.PRE_TEST, "None");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        MovingHolidaySpec expected = MovingHolidaySpec.easterSpec(true, false);
        expected.setTest(RegressionTestSpec.None);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getEaster());
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_Irregular() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*i");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.Irregular);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_Trend() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*t");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.Trend);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_Series() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*y");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.Series);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_Seasonal() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*s");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_SeasonallyAdjusted() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*sa");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.SeasonallyAdjusted);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_Undefined() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*u");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.Undefined);

        Assert.assertEquals(ALL_FINE, messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_UndefinedDefault() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*a");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.Undefined);

        Assert.assertEquals(new Message(Level.WARNING, "regressor_1 has no typ and will be marked as Undefined."), messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_UndefinedNoStar() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        TsVariableDescriptor expected = new TsVariableDescriptor("O.O");
        expected.setEffect(TsVariableDescriptor.UserComponentType.Undefined);

        Assert.assertEquals(new Message(Level.WARNING, "regressor_1 has no typ and will be marked as Undefined."), messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getUserDefinedVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_Calendar() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O*c");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        String expected = "O.O";

        Assert.assertEquals(new Message(Level.INFO, "Userdefined trading day variables were defined. All other trading day setting were overridden."), messages[0]);
        Assert.assertEquals(expected, specification.getRegArimaSpecification().getRegression().getTradingDays().getUserVariables()[0]);
    }

    @Test
    public void testReadSpecification_UserDefinedVariable_Calendar2() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "O.O1*c");
        instance.putInformation(X13SpecificationReader.REGRESSOR + 2, "O.O2*c");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        String expected1 = "O.O1";
        String expected2 = "O.O2";

        Assert.assertEquals(new Message(Level.INFO, "Userdefined trading day variables were defined. All other trading day setting were overridden."), messages[0]);
        String[] result = specification.getRegArimaSpecification().getRegression().getTradingDays().getUserVariables();

        //Regressor read in hash order soooooooo it should be "O.O2" in result[0] and "O.O1" in result[1]
        //but as long as both are in the array it is fine with me
        Assert.assertEquals(2, result.length);
        Assert.assertTrue(result[0].equals(expected1) || result[1].equals(expected1));
        Assert.assertTrue(result[0].equals(expected2) || result[1].equals(expected2));

    }

    @Test
    public void testReadSpecification_UserDefinedVariable_FalseInput() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.REGRESSOR + 1, "OO1*c");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(new Message(Level.SEVERE, "regressor_1 has an invalid syntax."), messages[0]);

        String[] result = specification.getRegArimaSpecification().getRegression().getTradingDays().getUserVariables();
        Assert.assertTrue(result == null);
    }

    @Test
    public void testReadSpecification_CalendarSigmaSelect_SigmaVecGroups() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.CALENDARSIGMA, "Select");
        instance.putInformation(X13SpecificationReader.SIGMA_VECTOR + 1, "Group1");
        instance.putInformation(X13SpecificationReader.SIGMA_VECTOR + 2, "Group2");
        instance.putInformation(X13SpecificationReader.SIGMA_VECTOR + 3, "Group1");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);

        X11Specification result = specification.getX11Specification();

        Assert.assertEquals(CalendarSigma.Select, result.getCalendarSigma());
        Assert.assertArrayEquals(new SigmavecOption[]{SigmavecOption.Group1, SigmavecOption.Group2, SigmavecOption.Group1}, result.getSigmavec());
    }

    @Test
    public void testReadSpecification_CalendarSigmaSelect_NoSigmaVec() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.CALENDARSIGMA, "Select");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(ALL_FINE, messages[0]);

        X11Specification result = specification.getX11Specification();

        Assert.assertEquals(CalendarSigma.Select, result.getCalendarSigma());
        Assert.assertArrayEquals(null, result.getSigmavec());
    }

    @Test
    public void testReadSpecification_CalendarSigmaSelect_SigmaVecGroupsError() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.CALENDARSIGMA, "Select");
        instance.putInformation(X13SpecificationReader.SIGMA_VECTOR + 1, "Group4");
        instance.putInformation(X13SpecificationReader.SIGMA_VECTOR + 2, "Group2");
        instance.putInformation(X13SpecificationReader.SIGMA_VECTOR + 3, "Group3");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(new Message(Level.SEVERE, "The information sigma_vector_1 doesn't contain a valid argument."), messages[0]);
        Assert.assertEquals(new Message(Level.SEVERE, "The information sigma_vector_3 doesn't contain a valid argument."), messages[1]);

        X11Specification result = specification.getX11Specification();

        Assert.assertEquals(CalendarSigma.Select, result.getCalendarSigma());
        Assert.assertArrayEquals(null, result.getSigmavec());
    }

    @Test
    public void testWriteSpecification_Rsa5c() {
        X13SpecificationReader instance = new X13SpecificationReader();
        Map<String, String> result = instance.writeSpecification(X13Specification.RSA5);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BASE));
        Assert.assertEquals(X13Specification.RSA5.toString(), result.get(X13SpecificationReader.BASE));
    }

    @Test
    public void testWriteSpecification_Rsa5cNoPreCheck() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().getBasic().setPreliminaryCheck(false);
        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TC_RATE));
        Assert.assertEquals("0.7", result.get(X13SpecificationReader.TC_RATE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CANCELATION_LIMIT));
        Assert.assertEquals("0.1", result.get(X13SpecificationReader.CANCELATION_LIMIT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.EXCLUDEFORECAST));
        Assert.assertEquals("false", result.get(X13SpecificationReader.EXCLUDEFORECAST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.DURATION));
        Assert.assertEquals("8", result.get(X13SpecificationReader.DURATION));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONAL));
        Assert.assertEquals("true", result.get(X13SpecificationReader.SEASONAL));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.LS));
        Assert.assertEquals("true", result.get(X13SpecificationReader.LS));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.HENDERSON));
        Assert.assertEquals("0", result.get(X13SpecificationReader.HENDERSON));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MIXED));
        Assert.assertEquals("true", result.get(X13SpecificationReader.MIXED));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AIC_DIFFERENCE));
        Assert.assertEquals("-2.0", result.get(X13SpecificationReader.AIC_DIFFERENCE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.EASTER));
        Assert.assertEquals("true", result.get(X13SpecificationReader.EASTER));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BALANCED));
        Assert.assertEquals("false", result.get(X13SpecificationReader.BALANCED));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MODE));
        Assert.assertEquals("Undefined", result.get(X13SpecificationReader.MODE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRANSFORM));
        Assert.assertEquals("Auto", result.get(X13SpecificationReader.TRANSFORM));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.INITIAL_UR));
        Assert.assertEquals("1.0416666666666667", result.get(X13SpecificationReader.INITIAL_UR));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("false", result.get(X13SpecificationReader.PRELIMINARY_CHECK));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.EASTER_JULIAN));
        Assert.assertEquals("false", result.get(X13SpecificationReader.EASTER_JULIAN));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AUTOADJUST));
        Assert.assertEquals("true", result.get(X13SpecificationReader.AUTOADJUST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MAXLEAD));
        Assert.assertEquals("-1", result.get(X13SpecificationReader.MAXLEAD));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SO));
        Assert.assertEquals("false", result.get(X13SpecificationReader.SO));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AUTOMODEL));
        Assert.assertEquals("true", result.get(X13SpecificationReader.AUTOMODEL));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONALFILTER));
        Assert.assertEquals("Msr", result.get(X13SpecificationReader.SEASONALFILTER));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MAXBACK));
        Assert.assertEquals("0", result.get(X13SpecificationReader.MAXBACK));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.LJUNGBOX_LIMIT));
        Assert.assertEquals("0.95", result.get(X13SpecificationReader.LJUNGBOX_LIMIT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.FINAL_UR));
        Assert.assertEquals("0.88", result.get(X13SpecificationReader.FINAL_UR));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.ACCEPT_DEFAULT));
        Assert.assertEquals("false", result.get(X13SpecificationReader.ACCEPT_DEFAULT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.METHOD));
        Assert.assertEquals("AddOne", result.get(X13SpecificationReader.METHOD));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REDUCE_CV));
        Assert.assertEquals("0.14286", result.get(X13SpecificationReader.REDUCE_CV));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.LOWER_SIGMA));
        Assert.assertEquals("1.5", result.get(X13SpecificationReader.LOWER_SIGMA));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TEST));
        Assert.assertEquals("Remove", result.get(X13SpecificationReader.TEST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.URFINAL));
        Assert.assertEquals("1.05", result.get(X13SpecificationReader.URFINAL));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AO));
        Assert.assertEquals("true", result.get(X13SpecificationReader.AO));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TC));
        Assert.assertEquals("true", result.get(X13SpecificationReader.TC));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertEquals("TradingDays", result.get(X13SpecificationReader.TRADINGDAYSTYPE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.ARMALIMIT));
        Assert.assertEquals("1.0", result.get(X13SpecificationReader.ARMALIMIT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CRITICAL_VALUE));
        Assert.assertEquals("0.0", result.get(X13SpecificationReader.CRITICAL_VALUE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.UPPER_SIGMA));
        Assert.assertEquals("2.5", result.get(X13SpecificationReader.UPPER_SIGMA));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRE_TEST));
        Assert.assertEquals("Add", result.get(X13SpecificationReader.PRE_TEST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CALENDARSIGMA));
        Assert.assertEquals("None", result.get(X13SpecificationReader.CALENDARSIGMA));
    }

    @Test
    public void testWriteSpecification_Rsa5cTolerance0Point5() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().getEstimate().setTol(0.5);
        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TC_RATE));
        Assert.assertEquals("0.7", result.get(X13SpecificationReader.TC_RATE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TOLERANCE));
        Assert.assertEquals("0.5", result.get(X13SpecificationReader.TOLERANCE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CANCELATION_LIMIT));
        Assert.assertEquals("0.1", result.get(X13SpecificationReader.CANCELATION_LIMIT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.EXCLUDEFORECAST));
        Assert.assertEquals("false", result.get(X13SpecificationReader.EXCLUDEFORECAST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.DURATION));
        Assert.assertEquals("8", result.get(X13SpecificationReader.DURATION));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONAL));
        Assert.assertEquals("true", result.get(X13SpecificationReader.SEASONAL));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.LS));
        Assert.assertEquals("true", result.get(X13SpecificationReader.LS));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.HENDERSON));
        Assert.assertEquals("0", result.get(X13SpecificationReader.HENDERSON));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MIXED));
        Assert.assertEquals("true", result.get(X13SpecificationReader.MIXED));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AIC_DIFFERENCE));
        Assert.assertEquals("-2.0", result.get(X13SpecificationReader.AIC_DIFFERENCE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.EASTER));
        Assert.assertEquals("true", result.get(X13SpecificationReader.EASTER));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BALANCED));
        Assert.assertEquals("false", result.get(X13SpecificationReader.BALANCED));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MODE));
        Assert.assertEquals("Undefined", result.get(X13SpecificationReader.MODE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRANSFORM));
        Assert.assertEquals("Auto", result.get(X13SpecificationReader.TRANSFORM));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.INITIAL_UR));
        Assert.assertEquals("1.0416666666666667", result.get(X13SpecificationReader.INITIAL_UR));

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.EASTER_JULIAN));
        Assert.assertEquals("false", result.get(X13SpecificationReader.EASTER_JULIAN));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AUTOADJUST));
        Assert.assertEquals("true", result.get(X13SpecificationReader.AUTOADJUST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MAXLEAD));
        Assert.assertEquals("-1", result.get(X13SpecificationReader.MAXLEAD));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SO));
        Assert.assertEquals("false", result.get(X13SpecificationReader.SO));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AUTOMODEL));
        Assert.assertEquals("true", result.get(X13SpecificationReader.AUTOMODEL));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONALFILTER));
        Assert.assertEquals("Msr", result.get(X13SpecificationReader.SEASONALFILTER));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.MAXBACK));
        Assert.assertEquals("0", result.get(X13SpecificationReader.MAXBACK));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.LJUNGBOX_LIMIT));
        Assert.assertEquals("0.95", result.get(X13SpecificationReader.LJUNGBOX_LIMIT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.FINAL_UR));
        Assert.assertEquals("0.88", result.get(X13SpecificationReader.FINAL_UR));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.ACCEPT_DEFAULT));
        Assert.assertEquals("false", result.get(X13SpecificationReader.ACCEPT_DEFAULT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.METHOD));
        Assert.assertEquals("AddOne", result.get(X13SpecificationReader.METHOD));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REDUCE_CV));
        Assert.assertEquals("0.14286", result.get(X13SpecificationReader.REDUCE_CV));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.LOWER_SIGMA));
        Assert.assertEquals("1.5", result.get(X13SpecificationReader.LOWER_SIGMA));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TEST));
        Assert.assertEquals("Remove", result.get(X13SpecificationReader.TEST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.URFINAL));
        Assert.assertEquals("1.05", result.get(X13SpecificationReader.URFINAL));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AO));
        Assert.assertEquals("true", result.get(X13SpecificationReader.AO));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TC));
        Assert.assertEquals("true", result.get(X13SpecificationReader.TC));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertEquals("TradingDays", result.get(X13SpecificationReader.TRADINGDAYSTYPE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.ARMALIMIT));
        Assert.assertEquals("1.0", result.get(X13SpecificationReader.ARMALIMIT));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CRITICAL_VALUE));
        Assert.assertEquals("0.0", result.get(X13SpecificationReader.CRITICAL_VALUE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.UPPER_SIGMA));
        Assert.assertEquals("2.5", result.get(X13SpecificationReader.UPPER_SIGMA));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRE_TEST));
        Assert.assertEquals("Add", result.get(X13SpecificationReader.PRE_TEST));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CALENDARSIGMA));
        Assert.assertEquals("None", result.get(X13SpecificationReader.CALENDARSIGMA));
    }

    @Test
    public void testWriteSpecification_ARIMA111111() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().setUsingAutoModel(false);
        ArimaSpec arima = spec.getRegArimaSpecification().getArima();
        arima.setP(1);
        arima.setD(1);
        arima.setQ(1);
        arima.setBP(1);
        arima.setBD(1);
        arima.setBQ(1);
        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.ARIMA));
        Assert.assertEquals("(1 1 1)(1 1 1)", result.get(X13SpecificationReader.ARIMA));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.P + 1));
        Assert.assertEquals("0.0*u", result.get(X13SpecificationReader.P + 1));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.Q + 1));
        Assert.assertEquals("0.0*u", result.get(X13SpecificationReader.P + 1));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BP + 1));
        Assert.assertEquals("0.0*u", result.get(X13SpecificationReader.BP + 1));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BQ + 1));
        Assert.assertEquals("0.0*u", result.get(X13SpecificationReader.BQ + 1));
    }

    @Test
    public void testWriteSpecification_ARIMA300001() {
        X13SpecificationReader instance = new X13SpecificationReader();
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
        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.ARIMA));
        Assert.assertEquals("(3 0 0)(0 0 1)", result.get(X13SpecificationReader.ARIMA));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.P + 1));
        Assert.assertEquals("1.0*f", result.get(X13SpecificationReader.P + 1));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.P + 2));
        Assert.assertEquals("0.0*u", result.get(X13SpecificationReader.P + 2));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.P + 3));
        Assert.assertEquals("0.5*i", result.get(X13SpecificationReader.P + 3));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BQ + 1));
        Assert.assertEquals("2.0*f", result.get(X13SpecificationReader.BQ + 1));
    }

    @Test
    public void testWriteSpecification_OutliersSwitched() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().setUsingAutoModel(false);
        OutlierSpec outlierSpec = spec.getRegArimaSpecification().getOutliers();
        outlierSpec.clearTypes();
        outlierSpec.add(OutlierType.SO);
        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.AO));
        Assert.assertEquals("false", result.get(X13SpecificationReader.AO));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.LS));
        Assert.assertEquals("false", result.get(X13SpecificationReader.LS));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TC));
        Assert.assertEquals("false", result.get(X13SpecificationReader.TC));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SO));
        Assert.assertEquals("true", result.get(X13SpecificationReader.SO));
    }

    @Test
    public void testWriteSpecification_OutliersNotUsed() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();
        spec.getRegArimaSpecification().setUsingAutoModel(false);
        OutlierSpec outlierSpec = spec.getRegArimaSpecification().getOutliers();
        outlierSpec.clearTypes();
        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.AO));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.LS));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.TC));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.SO));
    }

    @Test
    public void testWriteSpecification_X11Only() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setSeasonalFilter(SeasonalFilterOption.S3X15);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BASE));
        Assert.assertEquals("X11", result.get(X13SpecificationReader.BASE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONALFILTER));
        Assert.assertEquals("S3X15", result.get(X13SpecificationReader.SEASONALFILTER));

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXLEAD));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXBACK));
    }

    @Test
    public void testWriteSpecification_X11OnlyMultipleFilters() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setSeasonalFilters(new SeasonalFilterOption[]{SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X3, SeasonalFilterOption.S3X5, SeasonalFilterOption.S3X9});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BASE));
        Assert.assertEquals("X11", result.get(X13SpecificationReader.BASE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONALFILTERS + 1));
        Assert.assertEquals("S3X15", result.get(X13SpecificationReader.SEASONALFILTERS + 1));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONALFILTERS + 2));
        Assert.assertEquals("S3X3", result.get(X13SpecificationReader.SEASONALFILTERS + 2));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONALFILTERS + 3));
        Assert.assertEquals("S3X5", result.get(X13SpecificationReader.SEASONALFILTERS + 3));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SEASONALFILTERS + 4));
        Assert.assertEquals("S3X9", result.get(X13SpecificationReader.SEASONALFILTERS + 4));

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXLEAD));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXBACK));
    }

    @Test
    public void testWriteSpecification_X11OnlyCalendarSigmaSelect() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setCalendarSigma(CalendarSigma.Select);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BASE));
        Assert.assertEquals("X11", result.get(X13SpecificationReader.BASE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CALENDARSIGMA));
        Assert.assertEquals("Select", result.get(X13SpecificationReader.CALENDARSIGMA));

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXLEAD));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXBACK));
    }

    @Test
    public void testWriteSpecification_X11OnlyCalendarSigmaSelectSigmaVecGroups() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSAX11.clone();
        spec.getX11Specification().setCalendarSigma(CalendarSigma.Select);
        spec.getX11Specification().setSigmavec(new SigmavecOption[]{SigmavecOption.Group1, SigmavecOption.Group2, SigmavecOption.Group2, SigmavecOption.Group1});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.BASE));
        Assert.assertEquals("X11", result.get(X13SpecificationReader.BASE));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.CALENDARSIGMA));
        Assert.assertEquals("Select", result.get(X13SpecificationReader.CALENDARSIGMA));

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SIGMA_VECTOR + 1));
        Assert.assertEquals("Group1", result.get(X13SpecificationReader.SIGMA_VECTOR + 1));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SIGMA_VECTOR + 2));
        Assert.assertEquals("Group2", result.get(X13SpecificationReader.SIGMA_VECTOR + 2));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SIGMA_VECTOR + 3));
        Assert.assertEquals("Group2", result.get(X13SpecificationReader.SIGMA_VECTOR + 3));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SIGMA_VECTOR + 4));
        Assert.assertEquals("Group1", result.get(X13SpecificationReader.SIGMA_VECTOR + 4));

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXLEAD));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.MAXBACK));
    }

    @Test
    public void testWriteSpecification_SeriesFrom1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.from(new Day(1970, Month.January, 0));

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.START));
        Assert.assertEquals("1970-01-01", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.START));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(X13SpecificationReader.PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesTo1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.to(new Day(1970, Month.January, 0));

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.END));
        Assert.assertEquals("1970-01-01", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.END));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(X13SpecificationReader.PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesBetween1960_1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.between(new Day(1960, Month.January, 0), new Day(1970, Month.January, 0));

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.START));
        Assert.assertEquals("1960-01-01", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.START));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.END));
        Assert.assertEquals("1970-01-01", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.END));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(X13SpecificationReader.PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesFirst10() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.first(10);

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.FIRST));
        Assert.assertEquals("10", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.FIRST));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(X13SpecificationReader.PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesLast10() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.last(10);

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.LAST));
        Assert.assertEquals("10", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.LAST));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(X13SpecificationReader.PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesExcluding1020() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.excluding(10, 20);

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.FIRST));
        Assert.assertEquals("10", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.FIRST));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.LAST));
        Assert.assertEquals("20", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.LAST));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(X13SpecificationReader.PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_SeriesNone() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsPeriodSelector selector = new TsPeriodSelector();
        selector.none();

        spec.getRegArimaSpecification().getBasic().setSpan(selector);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.START));
        Assert.assertEquals("X", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.START));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.SERIES + X13SpecificationReader.END));
        Assert.assertEquals("X", result.get(X13SpecificationReader.SERIES + X13SpecificationReader.END));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRELIMINARY_CHECK));
        Assert.assertEquals("true", result.get(X13SpecificationReader.PRELIMINARY_CHECK));
    }

    @Test
    public void testWriteSpecification_TransformNone() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRANSFORM));
        Assert.assertEquals("None", result.get(X13SpecificationReader.TRANSFORM));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.AIC_DIFFERENCE));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.ADJUST));
    }

    @Test
    public void testWriteSpecification_TransformLog() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRANSFORM));
        Assert.assertEquals("Log", result.get(X13SpecificationReader.TRANSFORM));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.AIC_DIFFERENCE));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.ADJUST));
        Assert.assertEquals("None", result.get(X13SpecificationReader.ADJUST));
    }

    @Test
    public void testWriteSpecification_RegressionTradingDaysAndHolidays() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();
        TradingDaysSpec tradingDays = spec.getRegArimaSpecification().getRegression().getTradingDays();

        tradingDays.setHolidays("X.X");
        tradingDays.setAutoAdjust(false);
        tradingDays.setLengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.HOLIDAYS));
        Assert.assertEquals("X.X", result.get(X13SpecificationReader.HOLIDAYS));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertEquals("TradingDays", result.get(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.AUTOADJUST));
        Assert.assertEquals("false", result.get(X13SpecificationReader.AUTOADJUST));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.LEAP_YEAR));
        Assert.assertEquals("LengthOfPeriod", result.get(X13SpecificationReader.LEAP_YEAR));
    }

    @Test
    public void testWriteSpecification_RegressionStockTradingDays() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();
        TradingDaysSpec tradingDays = spec.getRegArimaSpecification().getRegression().getTradingDays();

        tradingDays.setStockTradingDays(30);
        tradingDays.setAutoAdjust(false);
        tradingDays.setLengthOfPeriod(LengthOfPeriodType.LengthOfPeriod);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.HOLIDAYS));
        Assert.assertEquals("30", result.get(X13SpecificationReader.W));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertEquals("None", result.get(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.AUTOADJUST));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.LEAP_YEAR));
    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedCalendar() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getRegression().getTradingDays().setUserVariables(new String[]{"X.X"});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.HOLIDAYS));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.W));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertEquals("None", result.get(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.AUTOADJUST));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.LEAP_YEAR));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.REGRESSOR + 1));
        Assert.assertEquals("X.X*c", result.get(X13SpecificationReader.REGRESSOR + 1));
    }

    @Test
    public void testWriteSpecification_RegressionNoTradingDaysNoEaster() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA0.clone();
        spec.getRegArimaSpecification().getBasic().setPreliminaryCheck(false);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(!result.containsKey(X13SpecificationReader.HOLIDAYS));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.W));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertEquals("None", result.get(X13SpecificationReader.TRADINGDAYSTYPE));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.AUTOADJUST));
        Assert.assertTrue(!result.containsKey(X13SpecificationReader.LEAP_YEAR));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.EASTER));
        Assert.assertEquals("false", result.get(X13SpecificationReader.EASTER));
    }

    @Test
    public void testWriteSpecification_RegressionJulianEaster() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA0.clone();

        MovingHolidaySpec easter = MovingHolidaySpec.easterSpec(true, true);
        spec.getRegArimaSpecification().getRegression().add(easter);

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.EASTER));
        Assert.assertEquals("true", result.get(X13SpecificationReader.EASTER));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.EASTER_JULIAN));
        Assert.assertEquals("true", result.get(X13SpecificationReader.EASTER_JULIAN));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.PRE_TEST));
        Assert.assertEquals("Add", result.get(X13SpecificationReader.PRE_TEST));
        Assert.assertTrue(result.containsKey(X13SpecificationReader.DURATION));
        Assert.assertEquals("8", result.get(X13SpecificationReader.DURATION));
    }

    @Test
    public void testWriteSpecification_RegressionPreSpecifiedOutlierAO19700601() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        spec.getRegArimaSpecification().getRegression().setOutliers(new OutlierDefinition[]{new OutlierDefinition(new TsPeriod(TsFrequency.Monthly, 1970, 5), OutlierType.AO)});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.OUTLIER + 1));
        Assert.assertEquals("AO1970-06-01", result.get(X13SpecificationReader.OUTLIER + 1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesUndefined() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Undefined);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REGRESSOR + 1));
        Assert.assertEquals("X.X*u", result.get(X13SpecificationReader.REGRESSOR + 1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesSeries() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Series);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REGRESSOR + 1));
        Assert.assertEquals("X.X*y", result.get(X13SpecificationReader.REGRESSOR + 1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesTrend() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Trend);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REGRESSOR + 1));
        Assert.assertEquals("X.X*t", result.get(X13SpecificationReader.REGRESSOR + 1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesSeasonal() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Seasonal);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REGRESSOR + 1));
        Assert.assertEquals("X.X*s", result.get(X13SpecificationReader.REGRESSOR + 1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesSeasonallyAdjusted() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.SeasonallyAdjusted);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REGRESSOR + 1));
        Assert.assertEquals("X.X*sa", result.get(X13SpecificationReader.REGRESSOR + 1));

    }

    @Test
    public void testWriteSpecification_RegressionUserDefinedVariablesIrregular() {
        X13SpecificationReader instance = new X13SpecificationReader();
        X13Specification spec = X13Specification.RSA5.clone();

        TsVariableDescriptor tsVariableDescriptor = new TsVariableDescriptor("X.X");
        tsVariableDescriptor.setEffect(TsVariableDescriptor.UserComponentType.Irregular);
        spec.getRegArimaSpecification().getRegression().setUserDefinedVariables(new TsVariableDescriptor[]{tsVariableDescriptor});

        Map<String, String> result = instance.writeSpecification(spec);

        Assert.assertTrue(result.containsKey(X13SpecificationReader.REGRESSOR + 1));
        Assert.assertEquals("X.X*i", result.get(X13SpecificationReader.REGRESSOR + 1));

    }

}
