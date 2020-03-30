/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.PositionInfo;
import ec.satoolkit.ISaSpecification;
import java.util.Map;

/**
 *
 * @author s4504tw
 */
public interface ISpecificationWriter<T extends ISaSpecification> {

    Map<PositionInfo, String> writeSpecification(T spec);
}