/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.ArimaSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.EstimateSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.OutlierSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.RegressionSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.SeriesSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.TransformSetting;
import de.bundesbank.jdemetra.xlsx2ws.spec.x13.X11Setting;

/**
 *
 * @author s4504tw
 */
@lombok.Data
@lombok.RequiredArgsConstructor
public class X13SpecificationSetting implements ISetting {

    final boolean series;
    final boolean estimate;
    final boolean transform;
    final boolean regression;
    final boolean outlier;
    final boolean arima;
    final boolean x11;

    SeriesSetting seriesSetting = new SeriesSetting();
    EstimateSetting estimateSetting = new EstimateSetting();
    TransformSetting transformSetting = new TransformSetting();
    RegressionSetting regressionSetting = new RegressionSetting();
    OutlierSetting outlierSetting = new OutlierSetting();
    ArimaSetting arimaSetting = new ArimaSetting();
    X11Setting x11Setting = new X11Setting();

    public X13SpecificationSetting() {
        series = true;
        estimate = true;
        transform = true;
        regression = true;
        outlier = true;
        arima = true;
        x11 = true;
    }

}
