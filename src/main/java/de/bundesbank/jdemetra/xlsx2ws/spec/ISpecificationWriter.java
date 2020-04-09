/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bundesbank.jdemetra.xlsx2ws.spec;

import de.bundesbank.jdemetra.xlsx2ws.dto.PositionInfo;
import ec.satoolkit.ISaSpecification;
import java.util.Map;
import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;

/**
 *
 * @author s4504tw
 */
public interface ISpecificationWriter<T extends ISaSpecification, S extends ISetting> {

    Map<PositionInfo, String> writeSpecification(T spec, S settings);
}
