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
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.TsVariableDescriptor;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Thomas Witthohn
 */
public class X13SpecificationReaderTest {

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

        Assert.assertEquals(new Message(Level.SEVERE, "Unparsable Date format in series_start."), messages[0]);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertEquals(new Message(Level.SEVERE, "The information henderson doesn't contain a parsable integer value."), readSpecification.getMessages()[0]);
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
        Assert.assertEquals(new Message(Level.SEVERE, "The information henderson doesn't contain a parsable integer value."), readSpecification.getMessages()[0]);
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
        String expected = "Lower sigma is greater than the default upper sigma. Upper sigma will be set to 5.9";
        Assert.assertEquals(new Message(Level.INFO, expected), instance.readSpecification(null).getMessages()[0]);
        Assert.assertEquals(5.4, specification.getX11Specification().getLowerSigma(), 10e-15);
    }

    @Test
    public void testReadSpecification_LowerSigma0Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "0.4");
        instance.readSpecification(null).getSpecification();
        String expected = "Invalid sigma options";
        Assert.assertEquals(new Message(Level.SEVERE, expected), instance.readSpecification(null).getMessages()[0]);
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

    @Test
    public void testReadSpecification_LowerSigma5Point4AndUpperSigma5Point3() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "5.4");
        instance.putInformation(X13SpecificationReader.UPPER_SIGMA, "5.3");
        String expected = "Invalid sigma options";
        Assert.assertEquals(new Message(Level.SEVERE, expected), instance.readSpecification(null).getMessages()[0]);
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
    public void testReadSpecification_NoAutoModelButNoArima() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.AUTOMODEL, "false");
        SpecificationDTO<X13Specification> specificationDTO = instance.readSpecification(null);

        Assert.assertEquals(new Message(Level.WARNING, "No ARIMA model specified, airline model will be used."), specificationDTO.getMessages()[0]);
        Assert.assertEquals(new ArimaSpec(), specificationDTO.getSpecification().getRegArimaSpecification().getArima());
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

        Assert.assertEquals(new Message(Level.SEVERE, "Unparsable Date format in outlier_1."), messages[0]);
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
    public void testReadSpecification_SeasonalFilterOrdering() {
        X13SpecificationReader instance = new X13SpecificationReader();
        for (int i = 1; i < 10; i++) {
            instance.putInformation(X13SpecificationReader.SEASONALFILTERS + i, "S3X15");
        }
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + 10, "S3X9");
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + 11, "S3X9");
        instance.putInformation(X13SpecificationReader.SEASONALFILTERS + 12, "S3X9");
        X13Specification specification = instance.readSpecification(null).getSpecification();

        Assert.assertArrayEquals(new SeasonalFilterOption[]{SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X15,
            SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X15,
            SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X15, SeasonalFilterOption.S3X15,
            SeasonalFilterOption.S3X9, SeasonalFilterOption.S3X9, SeasonalFilterOption.S3X9},
                specification.getX11Specification().getSeasonalFilters());
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
        Assert.assertEquals(new Message(Level.SEVERE, "The information tolerance doesn't contain a parsable floating point value."), messages[0]);
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
        Assert.assertTrue(messages.length == 0);
    }

    @Test
    public void testReadSpecification_PreCheckFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.PRELIMINARY_CHECK, "false");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(false, specification.getRegArimaSpecification().getBasic().isPreliminaryCheck());
        Assert.assertTrue(messages.length == 0);
    }

    @Test
    public void testReadSpecification_PreCheckNonBooleanInput() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.PRELIMINARY_CHECK, "0.1");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(true, specification.getRegArimaSpecification().getBasic().isPreliminaryCheck());
        Assert.assertEquals(new Message(Level.WARNING, "The information preliminary_check doesn't contain \"true\" or \"false\". It will be set to true."), messages[0]);
    }

    @Test
    public void testReadSpecification_PreCheckNullInput() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.PRELIMINARY_CHECK, null);

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertEquals(true, specification.getRegArimaSpecification().getBasic().isPreliminaryCheck());
        Assert.assertEquals(new Message(Level.WARNING, "The information preliminary_check doesn't contain \"true\" or \"false\". It will be set to true."), messages[0]);
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
        Assert.assertTrue(messages.length == 0);
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
        Assert.assertTrue(messages.length == 0);
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
        Assert.assertTrue(messages.length == 0);
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
        Assert.assertTrue(messages.length == 0);
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
        Assert.assertTrue(messages.length == 0);
    }

    @Test
    public void testReadSpecification_ARIMA001000_QStarFixed() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(001)(000)");
        instance.putInformation(X13SpecificationReader.Q + 1, "0.1*f");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getQ());
        Assert.assertTrue(messages.length == 0);
    }

    @Test
    public void testReadSpecification_ARIMA000001_BQStarFixed() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*f");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertTrue(messages.length == 0);
    }

    @Test
    public void testReadSpecification_ARIMA000001_BQStarUndefined() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*u");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Undefined)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertTrue(messages.length == 0);
    }

    @Test
    public void testReadSpecification_ARIMA000001_BQStarInitial() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*i");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Initial)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertTrue(messages.length == 0);
    }

    @Test
    public void testReadSpecification_ARIMA000001_BQStarFixedDefault() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(000)(001)");
        instance.putInformation(X13SpecificationReader.BQ + 1, "0.1*a");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertArrayEquals(new Parameter[]{new Parameter(0.1, ParameterType.Fixed)}, specification.getRegArimaSpecification().getArima().getBTheta());
        Assert.assertEquals(1, specification.getRegArimaSpecification().getArima().getBQ());
        Assert.assertEquals(new Message(Level.WARNING, "Parameter bq_1 has no known type declared. It will be assumed to be fixed."), messages[0]);
    }

    @Test
    public void testReadSpecification_AutomaticOutlierAO_True() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.AO, "true");

        SpecificationDTO<X13Specification> readSpecification = instance.readSpecification(null);
        X13Specification specification = readSpecification.getSpecification();
        Message[] messages = readSpecification.getMessages();

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertTrue(messages.length == 0);
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

        Assert.assertEquals(new Message(Level.INFO, "Userdefined trading day variables were defined. All other trading day settings were overridden."), messages[0]);
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

        Assert.assertEquals(new Message(Level.INFO, "Userdefined trading day variables were defined. All other trading day settings were overridden."), messages[0]);
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

        Assert.assertTrue(messages.length == 0);

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

        Assert.assertTrue(messages.length == 0);

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
}
