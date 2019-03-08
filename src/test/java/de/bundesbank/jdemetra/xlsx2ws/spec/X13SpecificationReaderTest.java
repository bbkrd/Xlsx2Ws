/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import ec.satoolkit.x11.SeasonalFilterOption;
import ec.satoolkit.x11.X11Exception;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.ParameterType;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.x13.ArimaSpec;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.Month;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.regression.AdditiveOutlier;
import ec.tstoolkit.timeseries.regression.LevelShift;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.regression.SeasonalOutlier;
import ec.tstoolkit.timeseries.regression.TransitoryChange;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Ignore;
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
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(X13Specification.RSA0, specification);
    }

    @Test
    public void testReadSpecification_Start1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES_START, "1970");
        X13Specification specification = instance.readSpecification(null);

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.from(new Day(1970, Month.January, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_StartWrongFormat() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES_START, "197004"); //instead of 1970.04
        X13Specification specification = instance.readSpecification(null);

        TsPeriodSelector expected = new TsPeriodSelector();

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Start197004() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES_START, "1970.04");
        X13Specification specification = instance.readSpecification(null);

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.from(new Day(1970, Month.April, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_End1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES_END, "1970");
        X13Specification specification = instance.readSpecification(null);

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.to(new Day(1970, Month.January, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Between1970_1980() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES_START, "1970");
        instance.putInformation(X13SpecificationReader.SERIES_END, "1980");
        X13Specification specification = instance.readSpecification(null);

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.between(new Day(1970, Month.January, 0), new Day(1980, Month.January, 0));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_Between19701105_19800228() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SERIES_START, "1970.11.05");
        instance.putInformation(X13SpecificationReader.SERIES_END, "1980.02.28");
        X13Specification specification = instance.readSpecification(null);

        TsPeriodSelector expected = new TsPeriodSelector();
        expected.between(new Day(1970, Month.November, 4), new Day(1980, Month.February, 27));

        Assert.assertEquals(expected, specification.getRegArimaSpecification().getBasic().getSpan());
    }

    @Test
    public void testReadSpecification_MaxleadMinus2() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MAXLEAD, "-2");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(-2, specification.getX11Specification().getForecastHorizon());
    }

    @Test
    public void testReadSpecification_Maxlead24() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MAXLEAD, "24");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(24, specification.getX11Specification().getForecastHorizon());
    }

    @Test
    public void testReadSpecification_BaseX11MaxleadMinus2() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.BASE, "X11");
        instance.putInformation(X13SpecificationReader.MAXLEAD, "-2");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(X13Specification.RSAX11, specification);
        Assert.assertEquals(0, specification.getX11Specification().getForecastHorizon());
    }

    @Test
    public void testReadSpecification_Henderson101() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "101");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(101, specification.getX11Specification().getHendersonFilterLength());
    }

    @Test
    public void testReadSpecification_Henderson3() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "3");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(3, specification.getX11Specification().getHendersonFilterLength());
    }

    @Test
    public void testReadSpecification_Henderson3Point5() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "3.5");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(3, specification.getX11Specification().getHendersonFilterLength());
    }

    @Test(expected = X11Exception.class)
    public void testReadSpecification_Henderson28() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "28");
        instance.readSpecification(null);
    }

    @Test(expected = X11Exception.class)
    public void testReadSpecification_Henderson103() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "103");
        instance.readSpecification(null);
    }

    @Test(expected = NumberFormatException.class)
    public void testReadSpecification_HendersonAbc() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.HENDERSON, "Abc");
        instance.readSpecification(null);
    }

    @Test
    public void testReadSpecification_TransformLog() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "Log");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(DefaultTransformationType.Log, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_TransformAuto() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "Auto");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(DefaultTransformationType.Auto, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_TransformNone() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "None");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(DefaultTransformationType.None, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_TransformWrong() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.TRANSFORM, "Wrong");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(DefaultTransformationType.None, specification.getRegArimaSpecification().getTransform().getFunction());
    }

    @Test
    public void testReadSpecification_CriticalValue5Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.CRITICAL_VALUE, "5.4");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(5.4, specification.getRegArimaSpecification().getOutliers().getDefaultCriticalValue(), 10e-15);
    }

    @Test
    public void testReadSpecification_UpperSigma5Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.UPPER_SIGMA, "5.4");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(5.4, specification.getX11Specification().getUpperSigma(), 10e-15);
    }

    @Test
    public void testReadSpecification_LowerSigma5Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "5.4");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(5.4, specification.getX11Specification().getLowerSigma(), 10e-15);
    }

    @Test(expected = X11Exception.class)
    public void testReadSpecification_LowerSigma0Point4() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "0.4");
        instance.readSpecification(null);
    }

    @Test
    public void testReadSpecification_LowerSigma5Point4AndUpperSigma5Point5() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "5.4");
        instance.putInformation(X13SpecificationReader.UPPER_SIGMA, "5.5");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(5.4, specification.getX11Specification().getLowerSigma(), 10e-15);
        Assert.assertEquals(5.5, specification.getX11Specification().getUpperSigma(), 10e-15);
    }

    @Test(expected = X11Exception.class)
    public void testReadSpecification_LowerSigma5Point4AndUpperSigma5Point3() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.LOWER_SIGMA, "5.4");
        instance.putInformation(X13SpecificationReader.UPPER_SIGMA, "5.3");
        instance.readSpecification(null);
    }

    @Test
    public void testReadSpecification_Arima312111() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA, "(312)(111)");
        X13Specification specification = instance.readSpecification(null);

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
        X13Specification specification = instance.readSpecification(null);

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
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(new ArimaSpec(), specification.getRegArimaSpecification().getArima());

    }

    @Test
    public void testReadSpecification_AutoTrue() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA_AUTO, "true");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(true, specification.getRegArimaSpecification().isUsingAutoModel());

    }

    @Test
    public void testReadSpecification_AutoFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.ARIMA_AUTO, "false");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(false, specification.getRegArimaSpecification().isUsingAutoModel());

    }

    @Test
    public void testReadSpecification_MeanTrue() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MEAN, "true");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(true, specification.getRegArimaSpecification().getArima().isMean());

    }

    @Test
    public void testReadSpecification_MeanFalse() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.MEAN, "false");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(false, specification.getRegArimaSpecification().getArima().isMean());

    }

    @Test
    public void testReadSpecification_OutlierAO1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "AO1970");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(1, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new OutlierDefinition(new Day(1970, Month.January, 0), AdditiveOutlier.CODE), specification.getRegArimaSpecification().getRegression().getOutliers()[0]);
    }

    @Test
    public void testReadSpecification_OutlierAOWrongDate() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "AO197000");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(0, specification.getRegArimaSpecification().getRegression().getOutliersCount());
    }

    @Test
    public void testReadSpecification_OutlierLS1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "LS1970");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(1, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new OutlierDefinition(new Day(1970, Month.January, 0), LevelShift.CODE), specification.getRegArimaSpecification().getRegression().getOutliers()[0]);
    }

    @Test
    public void testReadSpecification_OutlierTC1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "TC1970");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(1, specification.getRegArimaSpecification().getRegression().getOutliersCount());
        Assert.assertEquals(new OutlierDefinition(new Day(1970, Month.January, 0), TransitoryChange.CODE), specification.getRegArimaSpecification().getRegression().getOutliers()[0]);
    }

    @Test
    public void testReadSpecification_OutlierSO1970() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "SO1970");
        X13Specification specification = instance.readSpecification(null);

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
        X13Specification specification = instance.readSpecification(null);

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
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(0, specification.getRegArimaSpecification().getRegression().getOutliersCount());
    }

    @Ignore
    @Test
    public void testReadSpecification_OutlierWrongFormat() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.OUTLIER + 1, "");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertEquals(0, specification.getRegArimaSpecification().getRegression().getOutliersCount());
    }

    @Test
    public void testReadSpecification_SeasonalFilterS3X15() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONALFILTER + 1, "S3X15");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertArrayEquals(new SeasonalFilterOption[]{SeasonalFilterOption.S3X15}, specification.getX11Specification().getSeasonalFilters());
    }

    @Test
    public void testReadSpecification_SeasonalFilterA() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONALFILTER + "a", "S3X9");
        instance.putInformation(X13SpecificationReader.SEASONALFILTER + 1, "S3X15");
        X13Specification specification = instance.readSpecification(null);

        Assert.assertArrayEquals(new SeasonalFilterOption[]{SeasonalFilterOption.S3X15}, specification.getX11Specification().getSeasonalFilters());
    }

    @Test(expected = RuntimeException.class)
    public void testReadSpecification_SeasonalFilterTooFewFilters() {
        X13SpecificationReader instance = new X13SpecificationReader();
        instance.putInformation(X13SpecificationReader.SEASONALFILTER + 12, "S3X9");
        instance.putInformation(X13SpecificationReader.SEASONALFILTER + 1, "S3X15");
        instance.readSpecification(null);
    }

}
