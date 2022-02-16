/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec.x13;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Value
@lombok.AllArgsConstructor
public class RegressionSetting {

    boolean holidays;
    boolean tradingDaysType;
    boolean autoAdjust;
    boolean leapYear;
    boolean w;
    boolean userVariables;
    boolean tradingDaysTest;
    boolean easter;
    boolean prespecifiedOutliers;
    boolean userDefinedVariables;
    boolean fixedRegressionCoefficients;

    public RegressionSetting() {
        this.holidays = true;
        this.tradingDaysType = true;
        this.autoAdjust = true;
        this.leapYear = true;
        this.w = true;
        this.userVariables = true;
        this.tradingDaysTest = true;
        this.easter = true;
        this.prespecifiedOutliers = true;
        this.userDefinedVariables = true;
        this.fixedRegressionCoefficients = true;
    }

}
