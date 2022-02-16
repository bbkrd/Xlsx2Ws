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
public class OutlierSetting {

    boolean span;
    boolean criticalValue;
    boolean ao;
    boolean ls;
    boolean tc;
    boolean so;
    boolean tcRate;
    boolean method;

    public OutlierSetting() {
        this.span = true;
        this.criticalValue = true;
        this.ao = true;
        this.ls = true;
        this.tc = true;
        this.so = true;
        this.tcRate = true;
        this.method = true;
    }

}
