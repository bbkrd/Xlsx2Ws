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
public class ArimaSetting {

    boolean mean;
    boolean arimaModel;
    boolean p, q, bp, bq;
    boolean acceptDefault;
    boolean cancelationLimit;
    boolean initialUR;
    boolean finalUR;
    boolean mixed;
    boolean balanced;
    boolean armaLimit;
    boolean reduceCV;
    boolean ljungboxLimit;
    boolean urFinal;

    public ArimaSetting() {
        this.mean = true;
        this.arimaModel = true;
        this.p = true;
        this.q = true;
        this.bp = true;
        this.bq = true;
        this.acceptDefault = true;
        this.cancelationLimit = true;
        this.initialUR = true;
        this.finalUR = true;
        this.mixed = true;
        this.balanced = true;
        this.armaLimit = true;
        this.reduceCV = true;
        this.ljungboxLimit = true;
        this.urFinal = true;
    }

}
