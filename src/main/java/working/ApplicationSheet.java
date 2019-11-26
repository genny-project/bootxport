package working;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.area.XlsArea;
import org.jxls.command.GridCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import life.genny.bootxport.data.Export;

public class ApplicationSheet {
  
  static String[] header = {"${header}"};
  static String[] records = {"${cell}"};

  static String baseEntity = "BaseEntity";

  public String getPathModule(String realm) {
    String outputTemplateModule = System.getProperty("user.home").concat("/.genny/multitenancy/"
        + realm + "/modules.xlsx");
    return outputTemplateModule;
  }
  public String getPathTemplateModule(String realm) {
    String templateModule =  System.getProperty("user.home").concat("/.genny/multitenancy/"
        + realm + "/modules-template.xlsx");
    return templateModule;
  }

  public static void createWorkSheets(List<String> sheetNames,
      String name) throws IOException {

    Workbook workbook = new XSSFWorkbook();

    sheetNames.stream().forEach(act -> {
      Sheet contacts = workbook.createSheet(act);
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerFont.setFontHeightInPoints((short) 14);
      headerFont.setColor(IndexedColors.RED.getIndex());

      Row headerRow = contacts.createRow(0);
      Row recordRows = contacts.createRow(1);

      for (int i = 0; i < header.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(header[i]);
      }
      for (int i = 0; i < records.length; i++) {
        Cell cell = recordRows.createCell(i);
        cell.setCellValue(records[i]);
      }
    });
    FileOutputStream fileOut = new FileOutputStream(name);
    workbook.write(fileOut);
    workbook.close();
  }
  
  
  
  public static <T> void applyToWorksheet(String worksheetName,
      List<T> objects, List<String> header, Transformer transformer)
      throws MalformedURLException, IOException,
      InvalidFormatException {

    final String headers = "headers";
    final String data = "data";

    String props = header.stream().collect(Collectors.joining(","));

    List<String> capitalizedHeaders = header.stream()
        .map(ApplicationSheet::lastFromSplit).map(StringUtils::capitalize)
        .collect(Collectors.toList());

    Context context = new Context();
    context.putVar(headers, capitalizedHeaders);
    context.putVar(data, objects);


    XlsArea wholeArea =
        new XlsArea(worksheetName.concat("!A1:A1"), transformer);
    XlsArea subArea =
        new XlsArea(worksheetName.concat("!A2:A2"), transformer);

    GridCommand gs = new GridCommand();
    gs.addArea(wholeArea);
    gs.addArea(subArea);
    gs.setHeaders(headers);
    gs.setData(data);
    gs.setProps(props);

    gs.applyAt(new CellRef(worksheetName.concat("!A1")), context);

  }

  public static String lastFromSplit(String str) {
    String[] split = str.split("(\\s*(\\.|\\s)\\s*)");
    int lenght = split.length - 1;
    return split[lenght];
  }
}
