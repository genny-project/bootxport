package life.genny.bootxport.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.jxls.transform.poi.PoiTransformer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.datatype.DataType;

public class Export {

  static String[] header = {"${header}"};
  static String[] records = {"${cell}"};
  static String baseEntity = "BaseEntity";


  public static void main(String... args) {

    io.vavr.collection.List<Realm> multitenancy2 =
        Processor.getProcessor().multitenancy;

    multitenancy2.forEach(d -> {
      d.getAsks();
      d.getBaseEntitys();
      d.getEntityEntitys();
      d.getQuestionQuestions();
      d.getValidations();
      d.getMessages();
      io.vavr.collection.List<Attribute> attributes =
          d.getAttributes();
      io.vavr.collection.List<DataType> list =
          attributes.map(at -> at.dataType).toList();
      
//      attributes.map()
//      List<Attribute> attributeList = attributes.filter(a -> !a.getCode().startsWith("LNK")).toJavaList();
      List<Attribute> attributeLinkList = attributes.filter(a -> a.getCode().startsWith("LNK")).toJavaList();
      List<Attribute> attributeList = attributes.removeAll(a -> a.getCode().startsWith("LNK")).toJavaList();
      

      System.out.println(d.getEntityAttributes().length());

      List<String> names = new ArrayList<String>();
      names.add("Ask");
      names.add("BaseEntity");
      names.add("Attribute");
      names.add("AttributeLink");
      names.add("Validation");
      names.add("QuestionQuestion");
      names.add("Messages");
      names.add("EntityEntity");
      names.add("EntityAttribute");
      names.add("DataType");
      names.add("Question");
      names.add("Test");

      List<String> askHeader = new ArrayList<String>();
      askHeader.add("questionCode");
      askHeader.add("name");
      askHeader.add("sourceCode");
      askHeader.add("targetCode");
      askHeader.add("attributeCode");

      List<String> baseEntityHeader = new ArrayList<String>();
      baseEntityHeader.add("name");
      baseEntityHeader.add("code");

      List<String> entityEntityHeader = new ArrayList<String>();
      entityEntityHeader.add("link.targetCode");
      entityEntityHeader.add("link.sourceCode");
      entityEntityHeader.add("weight");
      entityEntityHeader.add("valueString");
      entityEntityHeader.add("valueDateTime");
      entityEntityHeader.add("valueLong");
      entityEntityHeader.add("valueInteger");
      entityEntityHeader.add("valueDouble");

      List<String> entityAttributeHeader = new ArrayList<String>();
      entityAttributeHeader.add("baseEntityCode");
      entityAttributeHeader.add("attributeCode");
      entityAttributeHeader.add("weight");
      entityAttributeHeader.add("valueString");
      entityAttributeHeader.add("valueDateTime");
      entityAttributeHeader.add("valueLong");
      entityAttributeHeader.add("valueInteger");
      entityAttributeHeader.add("valueDouble");

      List<String> attributeHeader = new ArrayList<String>();
      attributeHeader.add("code");
      attributeHeader.add("name");
      attributeHeader.add("dataType.typeName");

      List<String> dataTypeHeader = new ArrayList<String>();
      dataTypeHeader.add("typeName");
      dataTypeHeader.add("className");
      dataTypeHeader.add("validationList");

      List<String> validationHeader = new ArrayList<String>();
      validationHeader.add("code");
      validationHeader.add("name");
      validationHeader.add("regex");
      validationHeader.add("multiAllowed");
      validationHeader.add("recursiveGroup");

      List<String> questionQuestionHeader = new ArrayList<String>();
      questionQuestionHeader.add("pk.sourceCode");
      questionQuestionHeader.add("pk.targetCode");
      questionQuestionHeader.add("weight");
      questionQuestionHeader.add("mandatory");

      List<String> questionHeader = new ArrayList<String>();
      questionHeader.add("code");
      questionHeader.add("name");
      questionHeader.add("attributeCode");

      List<String> messagesHeader = new ArrayList<String>();
      messagesHeader.add("code");
      messagesHeader.add("name");
      messagesHeader.add("description");
      messagesHeader.add("subject");
//      messagesHeader.add("email");

      List<Map<String,String>> testMap = new ArrayList<Map<String,String>>();
      
      Map<String,String> pr = new HashMap<String,String>();
      pr.put("name", "andreds");
      pr.put("year", "89");
      
      testMap.add(pr);
      
      List<String> er = new ArrayList<String>();
      er.add("name");
      er.add("year");

      try {
        dataTypeHeader.add("validationList");
      } catch (Exception e2) {
        // TODO Auto-generated catch block
      }
      String template =
          "/Users/helios/.genny/" + d.getName() + "-template.xlsx";
      String output = "/Users/helios/.genny/" + d.getName() + ".xlsx";

      try {
        createWorkSheets(names, template);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Export x = new Export();
      File fl = new File(template);
      Transformer transformer = null;
      try {
        InputStream is = fl.toURL().openStream();
        OutputStream os = new FileOutputStream(output);
        transformer = PoiTransformer.createTransformer(is, os);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (InvalidFormatException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      try {
        x.applyToWorksheet("Ask", d.getAsks().toJavaList(), askHeader,
            transformer);
        x.applyToWorksheet("BaseEntity",
            d.getBaseEntitys().toJavaList(), baseEntityHeader,
            transformer);
        x.applyToWorksheet("EntityEntity",
            d.getEntityEntitys().toJavaList(), entityEntityHeader,
            transformer);
        x.applyToWorksheet("EntityAttribute",
            d.getEntityAttributes().toJavaList(),
            entityAttributeHeader, transformer);
        x.applyToWorksheet("Attribute",
            attributeList, attributeHeader,
            transformer);
        x.applyToWorksheet("AttributeLink",
            attributeLinkList, attributeHeader,
            transformer);
        x.applyToWorksheet("DataType", list.toJavaList(),
            dataTypeHeader, transformer);
        x.applyToWorksheet("Validation", d.getValidations().toJavaList(),
            validationHeader, transformer);
        x.applyToWorksheet("QuestionQuestion", d.getQuestionQuestions().toJavaList(),
            questionQuestionHeader, transformer);
        x.applyToWorksheet("Question", d.getQuestions().toJavaList(),
            questionHeader, transformer);
        x.applyToWorksheet("Messages", d.getMessages().toJavaList(),
            messagesHeader, transformer);
        x.applyToWorksheet("Test", testMap,
           er, transformer);
        transformer.write();
      } catch (InvalidFormatException | IOException e) {
        // TODO Auto-generated catch block
      }

    });

  }

  public static String lastFromSplit(String str) {
    String[] split = str.split("(\\s*(\\.|\\s)\\s*)");
    int lenght = split.length - 1;
    return split[lenght];
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
    fileOut.close();
  }

  // private static String output = "target/listener_demo_output.xls";

  public <T> void applyToWorksheet(String worksheetName,
      List<T> objects, List<String> header, Transformer transformer)
      throws MalformedURLException, IOException,
      InvalidFormatException {

    final String headers = "headers";
    final String data = "data";

    String props = header.stream().collect(Collectors.joining(","));

    List<String> capitalizedHeaders = header.stream()
        .map(Export::lastFromSplit).map(StringUtils::capitalize)
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

}
