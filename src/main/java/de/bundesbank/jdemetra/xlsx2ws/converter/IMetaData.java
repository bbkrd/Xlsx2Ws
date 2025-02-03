package de.bundesbank.jdemetra.xlsx2ws.converter;

import ec.tstoolkit.design.ServiceDefinition;
import java.util.List;
import java.util.Set;

@ServiceDefinition
public interface IMetaData {

    @lombok.NonNull
    List<Class> getSupportedClass();

    @lombok.NonNull
    Set<String> getAllHeaders();

}
