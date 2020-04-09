/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;

/**
 *
 * @author s4504tw
 */
@lombok.Value
@lombok.AllArgsConstructor
public class X13SpecificationSetting implements ISetting {

    boolean series;
    boolean estimate;
    boolean transform;
    boolean regression;
    boolean outlier;
    boolean arima;
    boolean x11;

    public X13SpecificationSetting() {
        this.series = true;
        this.estimate = true;
        this.transform = true;
        this.regression = true;
        this.outlier = true;
        this.arima = true;
        this.x11 = true;
    }

}
