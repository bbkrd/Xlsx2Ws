/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec.x13;

/**
 *
 * @author s4504tw
 */
@lombok.Value
@lombok.AllArgsConstructor
public class SeriesSetting {

    boolean span;
    boolean preCheck;

    public SeriesSetting() {
        this.span = true;
        this.preCheck = true;
    }

}
