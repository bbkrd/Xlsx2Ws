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
public class TransformSetting {

    boolean transform;
    boolean aicDiff;
    boolean adjust;

    public TransformSetting() {
        this.transform = true;
        this.aicDiff = true;
        this.adjust = true;
    }

}
