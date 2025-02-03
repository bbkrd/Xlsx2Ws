package de.bundesbank.jdemetra.xlsx2ws.converter;

import de.bundesbank.jdemetra.xlsx2ws.dto.ISetting;
import ec.tstoolkit.design.ServiceDefinition;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@ServiceDefinition
public interface SheetWriter {

    public void writeSheet(XSSFWorkbook workbook, Map<String, ISetting> settings);

    public void writeHeaderSheet(XSSFWorkbook workbook);

}
