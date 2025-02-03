package de.bundesbank.jdemetra.xlsx2ws.converter;

import de.bundesbank.jdemetra.xlsx2ws.dto.ReportItem;
import ec.tstoolkit.design.ServiceDefinition;
import java.io.File;
import java.util.List;

@ServiceDefinition
public interface SheetReader {

    public List<ReportItem> read(File file);

}
