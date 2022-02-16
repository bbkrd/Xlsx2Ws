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
public class X11Setting {

    boolean mode;
    boolean seasonal;
    boolean lowerSigma;
    boolean upperSigma;
    boolean seasonalfilter;
    boolean henderson;
    boolean calendarsigma;
    boolean excludeforecast;
    boolean biascorrection;
    boolean maxlead;
    boolean maxback;

    public X11Setting() {
        this.mode = true;
        this.seasonal = true;
        this.lowerSigma = true;
        this.upperSigma = true;
        this.seasonalfilter = true;
        this.henderson = true;
        this.calendarsigma = true;
        this.excludeforecast = true;
        this.biascorrection = true;
        this.maxlead = true;
        this.maxback = true;
    }

}
