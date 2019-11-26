package life.genny.bootxport.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.jxls.area.XlsArea;
import org.jxls.command.GridCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.bootxport.xport.Multitenancy;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class Export {
//
//  static String[] header = {"${header}"};
//  static String[] records = {"${cell}"};
//  static String baseEntity = "BaseEntity";
//
//
//
//  public String getPathTemplateModule(String realm) {
//    String templateModule =  System.getProperty("user.home").concat("/.genny/multitenancy/"
//        + realm + "/modules-template.xlsx");
//    return templateModule;
//  }
//
//  public String getPathModule(String realm) {
//    String outputTemplateModule = System.getProperty("user.home").concat("/.genny/multitenancy/"
//        + realm + "/modules.xlsx");
//    return outputTemplateModule;
//  }
//
//  public static void createModulesDirectories(
//      List<Realm> multitenancy, Export export) {
//    multitenancy.forEach(realm -> DirectoryStructure.createDirectory(
//        System.getProperty("user.home").concat("/.genny/multitenancy/") + realm.getName()));
//
//    Map<String, String> moduleTemplatePaths = multitenancy.stream()
//        .collect(Collectors.toMap(realm -> realm.getName(),
//            realm -> export.getPathTemplateModule(realm.getName())));
//
//    Map<String, String> modulePaths = multitenancy.stream()
//        .collect(Collectors.toMap(realm -> realm.getName(),
//            realm -> export.getPathModule(realm.getName())));
//    List<String> moduleSheetNames = new ArrayList<String>();
//    moduleSheetNames.add("Modules");
//
//    moduleTemplatePaths.entrySet().forEach(realmWithPath -> {
//      try {
//        createWorkSheets(moduleSheetNames, realmWithPath.getValue());
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//
//      File moduleTemplateFile = new File(realmWithPath.getValue());
//      Transformer transmer = null;
//      try {
//        InputStream is =
//            moduleTemplateFile.toURI().toURL().openStream();
//        OutputStream os = new FileOutputStream(
//            modulePaths.get(realmWithPath.getKey()));
//        transmer = PoiTransformer.createTransformer(is, os);
//      } catch (IOException e1) {
//        e1.printStackTrace();
//      } catch (InvalidFormatException e) {
//        e.printStackTrace();
//      }
//
//      Map<String, Map<String, String>> moduleConf = new HashMap<>();
//      Map<String, String> props = new HashMap<>();
//
//      DirectoryStructure
//          .createDirectory(System.getProperty("user.home").concat("/.genny/multitenancy/"
//              + realmWithPath.getKey() + "/modules"));
//      props.put("sheetID",
//          System.getProperty("user.home").concat("/.genny/multitenancy/"
//              + realmWithPath.getKey() + "/" + "modules/"
//              + realmWithPath.getKey() + ".xlsx"));
//
//      props.put("name", realmWithPath.getKey());
//
//      moduleConf.put(realmWithPath.getKey(), props);
//
//      List<String> moduleSheetHeader = moduleConf.entrySet().stream()
//          .flatMap(m -> m.getValue().keySet().stream()).distinct()
//          .collect(Collectors.toList());
//
//      List<Map<String, String>> moduleSheetProps =
//          moduleConf.entrySet().stream().map(g -> g.getValue())
//              .collect(Collectors.toList());
//
//      try {
//
//        export.applyToWorksheet("Modules", moduleSheetProps,
//            moduleSheetHeader, transmer);
//
//        transmer.write();
//        moduleTemplateFile.delete();
//      } catch (InvalidFormatException | IOException e) {
//        e.printStackTrace();
//      }
//    });
//  }
//
//
//  public static void createStructure(List<Realm> multitenancy,
//      Export export) {
//
//    /*
//     * Create multitenancy directory
//     */
//    DirectoryStructure
//        .createDirectory(System.getProperty("user.home").concat("/.genny/multitenancy/"));
//
//    String templateMultitenancy = System.getProperty("user.home").concat("/.genny/multitenancy/"
//        + "multitenancy" + "-template.xlsx");
//    String outputMultitenancy = System.getProperty("user.home").concat("/.genny/multitenancy/"
//        + "multitenancy" + ".xlsx");
//
//    List<String> multitenancySheetNames = new ArrayList<String>();
//    multitenancySheetNames.add("Projects");
//
//    try {
//      createWorkSheets(multitenancySheetNames, templateMultitenancy);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//
//    File multitenancyTemplateFile = new File(templateMultitenancy);
//    Transformer transformer = null;
//    try {
//      InputStream is =
//          multitenancyTemplateFile.toURI().toURL().openStream();
//      OutputStream os = new FileOutputStream(outputMultitenancy);
//      transformer = PoiTransformer.createTransformer(is, os);
//    } catch (IOException e1) {
//      e1.printStackTrace();
//    } catch (InvalidFormatException e) {
//      e.printStackTrace();
//    }
//
//    List<String> multitenancySheetHeader = multitenancy.stream()
//        .flatMap(m -> m.getModulePath().keySet().stream()).distinct()
//        .collect(Collectors.toList());
//
//    List<Map<String, String>> multitenancySheetProps =
//        multitenancy.stream().map(g -> g.getModulePath())
//            .collect(Collectors.toList());
//
//    try {
//      export.applyToWorksheet("Projects", multitenancySheetProps,
//          multitenancySheetHeader, transformer);
//      transformer.write();
//      multitenancyTemplateFile.delete();
//    } catch (InvalidFormatException | IOException e) {
//      e.printStackTrace();
//    }
//
//    createModulesDirectories(multitenancy, export);
//
//  }
//
// 
//
//  public static void main(String... args) {
//    List<Realm> multitenancy2 = new Multitenancy().realms;
//    Export x = new Export();
//    createStructure(multitenancy2, x);
//    exec(multitenancy2, x);
//  }
//
//  public static void exec(List<Realm> multitenancy, Export export) {
//    multitenancy.forEach(d -> {
//      export(export, d);
//    });
//  }
//
//  public static void export(Export export, Realm d) {
//    /*
//     * table names or work sheet names on the spreadsheet
//     */
//    List<String> tablesNames = Arrays.asList(DataHeader.tableNames);
//
//    String template =
//       System.getProperty("user.home").concat( "/.genny/" + d.getName() + "-template.xlsx");
//    String output =  System.getProperty("user.home").concat( "/.genny/multitenancy/" + d.getName()
//        + "/modules/" + d.getName() + ".xlsx");
//
//
//    try {
//      createWorkSheets(tablesNames, template);
//
//      File moduleDomain = new File(template);
//      Transformer tm = null;
//      try {
//        InputStream is = moduleDomain.toURI().toURL().openStream();
//        OutputStream os = new FileOutputStream(output);
//        tm = PoiTransformer.createTransformer(is, os);
//      } catch (InvalidFormatException | IOException e) {
//      }
//
//      PreExport preExport = new PreExport(d);
//      for (ModuleUnit<?> m : preExport.getModuleUnits()) {
//        try {
//          export.applyToWorksheet(m.getName(), m.getData(), m.getHeader(), tm);
//        } catch (InvalidFormatException | IOException e) {
//          e.printStackTrace();
//        }
//      }
//      tm.write();
//      moduleDomain.delete();
//    } catch (IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//  }
//
//
//  public static String lastFromSplit(String str) {
//    String[] split = str.split("(\\s*(\\.|\\s)\\s*)");
//    int lenght = split.length - 1;
//    return split[lenght];
//  }
//
//
//  public static void createWorkSheets(List<String> sheetNames,
//      String name) throws IOException {
//
//    Workbook workbook = new XSSFWorkbook();
//
//    sheetNames.stream().forEach(act -> {
//      Sheet contacts = workbook.createSheet(act);
//      Font headerFont = workbook.createFont();
//      headerFont.setBold(true);
//      headerFont.setFontHeightInPoints((short) 14);
//      headerFont.setColor(IndexedColors.RED.getIndex());
//
//      Row headerRow = contacts.createRow(0);
//      Row recordRows = contacts.createRow(1);
//
//      for (int i = 0; i < header.length; i++) {
//        Cell cell = headerRow.createCell(i);
//        cell.setCellValue(header[i]);
//      }
//      for (int i = 0; i < records.length; i++) {
//        Cell cell = recordRows.createCell(i);
//        cell.setCellValue(records[i]);
//      }
//    });
//    FileOutputStream fileOut = new FileOutputStream(name);
//    workbook.write(fileOut);
//    workbook.close();
//  }
//
//
//  public <T> void applyToWorksheet(String worksheetName,
//      List<T> objects, List<String> header, Transformer transformer)
//      throws MalformedURLException, IOException,
//      InvalidFormatException {
//
//    final String headers = "headers";
//    final String data = "data";
//
//    String props = header.stream().collect(Collectors.joining(","));
//
//    List<String> capitalizedHeaders = header.stream()
//        .map(Export::lastFromSplit).map(StringUtils::capitalize)
//        .collect(Collectors.toList());
//
//    Context context = new Context();
//    context.putVar(headers, capitalizedHeaders);
//    context.putVar(data, objects);
//
//
//    XlsArea wholeArea =
//        new XlsArea(worksheetName.concat("!A1:A1"), transformer);
//    XlsArea subArea =
//        new XlsArea(worksheetName.concat("!A2:A2"), transformer);
//
//    GridCommand gs = new GridCommand();
//    gs.addArea(wholeArea);
//    gs.addArea(subArea);
//    gs.setHeaders(headers);
//    gs.setData(data);
//    gs.setProps(props);
//
//    gs.applyAt(new CellRef(worksheetName.concat("!A1")), context);
//
//  }
//
}
//
