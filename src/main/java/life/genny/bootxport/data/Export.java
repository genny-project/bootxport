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
import java.util.Optional;
import java.util.UUID;
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
import life.genny.bootxport.xport.Processor;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.validation.Validation;

public class Export {

  static String[] header = {"${header}"};
  static String[] records = {"${cell}"};
  static String baseEntity = "BaseEntity";


  public void createDirectory(String path) {
    File fi = new File(path);
    fi.mkdir();

  };

  public String getPathTemplateModule(String realm) {
    String templateModule = "/Users/helios/.genny/multitenancy/"
        + realm + "/modules-template.xlsx";
    return templateModule;
  }

  public String getPathModule(String realm) {
    String outputTemplateModule = "/Users/helios/.genny/multitenancy/"
        + realm + "/modules.xlsx";
    return outputTemplateModule;
  }

  public static void main(String... args) {
    List<Realm> multitenancy2 =
        Processor.getProcessor().multitenancy;

    //#######################################################################################
    //# 
    //# 1.  Create directory for multi-tenancy
    //# 2.  Extract Realms
    //# 3.  Prepare for headers and properties
    //# 4.  Prepared template path
    //# 5.  Output multi-tenancy sheet path
    //# 6.  Create array of sheet names for multi-tenancy
    //# 7.  Create WorkSheet for Multi-tenancy
    //# 8.  Create templateFile for multitenancy
    //# 9.  Create input stream from the template file
    //# 10. Create output stream for the Multi-tenancy Worksheet 
    //# 11. Create transformer 
    //# 12. Extract Multi-tenancy sheet Headers
    //# 13. Extract Multi-tenancy sheet Properties
    //# 14. Apply props and headers to Muli-tenancy Worsheet
    //# 15. Write the file from the output stream 
    //# 16. Delete Multi-tenancy file
    //# 
    //# #####################################################################################

    Export x = new Export();
    x.createDirectory("/Users/helios/.genny/multitenancy/");                                          //1

    List<String> realms =
        io.vavr.collection.List.ofAll(multitenancy2).map(r -> r.getName()).toJavaList();                                             //2

    Map<String, Map<String, String>> realmWithProps =
        realms.stream().map(r -> {

          Map<String, Map<String, String>> realmConf =
              new HashMap<>();
          Map<String, String> props = new HashMap<>();

          props.put("sheetID",
              "/Users/helios/.genny/multitenancy/" + r + "/" + "modules.xlsx");
          props.put("code", r);

          realmConf.put(r, props);

          return realmConf;
        }).reduce((acc, next) -> {
          acc.putAll(next);
          return acc;
        }).get();                                                                                      //3

    String templateMultitenancy = "/Users/helios/.genny/multitenancy/"
        + "multitenancy" + "-template.xlsx";                                                           //4
    String outputMultitenancy = "/Users/helios/.genny/multitenancy/"
        + "multitenancy" + ".xlsx";                                                                    //5
    List<String> multitenancySheetNames = new ArrayList<String>();
    multitenancySheetNames.add("Projects");                                                            //6

    try {
      createWorkSheets(multitenancySheetNames, templateMultitenancy);                                  //7
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    File multitenancyTemplateFile = new File(templateMultitenancy);                                    //8
    Transformer transformer = null;
    try {
      InputStream is = multitenancyTemplateFile.toURL().openStream();                                  //9
      OutputStream os = new FileOutputStream(outputMultitenancy);                                      //10
      transformer = PoiTransformer.createTransformer(is, os);                                          //11
    } catch (IOException e1) {
      e1.printStackTrace();
    } catch (InvalidFormatException e) {
      e.printStackTrace();
    }

    List<String> multitenancySheetHeader = realmWithProps.entrySet().stream()
        .flatMap(m -> m.getValue().keySet().stream())
        .distinct()
        .collect(Collectors.toList());                                                                 //12 

    List<Map<String, String>> multitenancySheetProps = realmWithProps.entrySet().stream()
        .map(g -> g.getValue()).collect(Collectors.toList());                                          //13


    try {
      x.applyToWorksheet("Projects", multitenancySheetProps,
          multitenancySheetHeader, transformer);                                                       //14
      transformer.write();                                                                             //15
      multitenancyTemplateFile.delete();                                                               //16
    } catch (InvalidFormatException | IOException e) {
      e.printStackTrace();
    }


    //#######################################################################################
    //# 
    //# 1. Create realm folders within multi-tenancy
    //# 2. Prepare for module headers and properties
    //# 3. Prepared template path
    //# 4. Output multi-tenancy sheet path
    //# 5. Create array of sheet names for modules
    //# 
    //# #####################################################################################

    realms.forEach(realm -> x
        .createDirectory("/Users/helios/.genny/multitenancy/" + realm));                               //1


    Map<String,String>  moduleTemplatePaths  = realms.stream()
        .collect(Collectors.toMap(
            realm->realm,
            realm->x.getPathTemplateModule(realm)
            ));

    Map<String,String>  modulePaths  = realms.stream()
        .collect(Collectors.toMap(
            realm->realm,
            realm->x.getPathModule(realm)
            ));
     
    List<String> moduleSheetNames = new ArrayList<String>();
    moduleSheetNames.add("Modules");                                                                        //
    
    moduleTemplatePaths.entrySet().forEach(realmWithPath ->{
      try {
        createWorkSheets(moduleSheetNames, realmWithPath.getValue());                                  //7
      } catch (IOException e) {
        e.printStackTrace();
      }

      File moduleTemplateFile = new File(realmWithPath.getValue());                                    //8
      Transformer transmer = null;
      try {
        InputStream is = moduleTemplateFile.toURL().openStream();                                  //9
        OutputStream os = new FileOutputStream(modulePaths.get(realmWithPath.getKey()));                                      //10
        transmer = PoiTransformer.createTransformer(is, os);                                          //11
      } catch (IOException e1) {
        e1.printStackTrace();
      } catch (InvalidFormatException e) {
        e.printStackTrace();
      }
      
      Map<String, Map<String, String>> moduleConf = new HashMap<>();
      Map<String, String> props = new HashMap<>();

      x.createDirectory("/Users/helios/.genny/multitenancy/" + realmWithPath.getKey()+"/modules");
      props.put("sheetID",
          "/Users/helios/.genny/multitenancy/" + realmWithPath.getKey() + "/" + "modules/"+ realmWithPath.getKey() + ".xlsx");

      props.put("name", realmWithPath.getKey());

      moduleConf.put(realmWithPath.getKey(), props);

      List<String> moduleSheetHeader = moduleConf.entrySet().stream()
          .flatMap(m -> m.getValue().keySet().stream())
          .distinct()
          .collect(Collectors.toList());                                                                 //12 

      List<Map<String, String>> moduleSheetProps = moduleConf.entrySet().stream()
          .map(g -> g.getValue()).collect(Collectors.toList());                                          //13


      try {

        x.applyToWorksheet("Modules", moduleSheetProps,
            moduleSheetHeader, transmer);                                                       //14

        transmer.write();                                                                            //15
        moduleTemplateFile.delete();                                                               //16
      } catch (InvalidFormatException | IOException e) {
        e.printStackTrace();
      }
    });
    
    
     multitenancy2.forEach(d -> {
       System.out.println(d);
       d.getAsks();
       d.getBaseEntitys();
       d.getEntityEntitys();
       d.getQuestionQuestions();
       d.getValidations();
       d.getMessages();
       List<Attribute> attributes =
       d.getAttributes();
       io.vavr.collection.List<DataType> dataTypes =
       io.vavr.collection.List.ofAll(attributes).map(at -> at.dataType).toList();
    
       ObjectMapper oMapper = new ObjectMapper();

       
//       List<Attribute> attributeLinkList = io.vavr.collection.List.ofAll(attributes)
//           .filter(a -> a.getCode().startsWith("LNK")).toJavaList();
       

//       List<Attribute> attributeList = io.vavr.collection.List.ofAll(attributes)
//       .removeAll(a -> a.getCode().startsWith("LNK")).toJavaList();
    
    
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
       askHeader.add("weight");
       askHeader.add("readonly");
       askHeader.add("oneshot");
       askHeader.add("mandatory");
       askHeader.add("hidden");
       askHeader.add("disabled");
    
       List<String> baseEntityHeader = new ArrayList<String>();
       baseEntityHeader.add("name");
       baseEntityHeader.add("code");
    
       List<String> entityEntityHeader = new ArrayList<String>();
       entityEntityHeader.add("link.targetCode");
       entityEntityHeader.add("link.sourceCode");
       entityEntityHeader.add("pk.attribute.code");
       entityEntityHeader.add("link.childColor");
       entityEntityHeader.add("link.linkValue");
       entityEntityHeader.add("link.parentColor");
       entityEntityHeader.add("link.rule");
       entityEntityHeader.add("link.weight");
       entityEntityHeader.add("valueBoolean");
       entityEntityHeader.add("valueDate");
       entityEntityHeader.add("valueDateTime");
       entityEntityHeader.add("valueDouble");
       entityEntityHeader.add("valueInteger");
       entityEntityHeader.add("valueLong");
       entityEntityHeader.add("valueMoney");
       entityEntityHeader.add("valueString");
       entityEntityHeader.add("valueTime");
       entityEntityHeader.add("version");
       entityEntityHeader.add("weight");
    
       List<String> entityAttributeHeader = new ArrayList<String>();
       entityAttributeHeader.add("baseEntityCode");
       entityAttributeHeader.add("attributeCode");
       entityAttributeHeader.add("inferred");
       entityAttributeHeader.add("privacyFlag");
       entityAttributeHeader.add("readonly");
       entityAttributeHeader.add("valueBoolean");
       entityAttributeHeader.add("valueDate");
       entityAttributeHeader.add("valueDateRange");
       entityAttributeHeader.add("valueDateTime");
       entityAttributeHeader.add("valueDouble");
       entityAttributeHeader.add("valueInteger");
       entityAttributeHeader.add("valueLong");
       entityAttributeHeader.add("valueMoney");
       entityAttributeHeader.add("valueString");
       entityAttributeHeader.add("valueTime");
       entityAttributeHeader.add("weight");

    
       List<String> attributeHeader = new ArrayList<String>();
       attributeHeader.add("code");
       attributeHeader.add("name");
       attributeHeader.add("datatype");
    
       List<String> dataTypeHeader = new ArrayList<String>();
       dataTypeHeader.add("typeName");
       dataTypeHeader.add("className");
       dataTypeHeader.add("inputmask");
       dataTypeHeader.add("code");
       dataTypeHeader.add("name");
    
       List<String> validationHeader = new ArrayList<String>();
       validationHeader.add("name");
       validationHeader.add("realm");
       validationHeader.add("code");
       validationHeader.add("multiAllowed");
       validationHeader.add("recursiveGroup");
       validationHeader.add("regex");
       validationHeader.add("selectionBaseEntityGroupList");
    
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
       messagesHeader.add("email_templateId");
       messagesHeader.add("toast_template");
       messagesHeader.add("sms_template");
       

//       List<Map> list = dataTypes.map(type -> 
//         {
//            Map<String,String> obj =  oMapper.convertValue(type, Map.class);
//             
//            Optional<Validation> findFirst = type.getValidationList().stream().findFirst();
//
//            if(findFirst.isPresent())
//              obj.put("validationList", findFirst.get().getCode());
//
//            return  obj;
//         }
//       ).collect(Collectors.toList());
      
       List<Tuple2<Map<String,String>,Map<String,String>>> list2 = attributes.stream().map(type -> {
            Map<String,String> attributeMap =  oMapper.convertValue(type, Map.class);
            Map<String,String> dataTypeMap =  oMapper.convertValue(type.dataType, Map.class);
            String dataTypeCode = UUID.randomUUID().toString(); 

            Optional<Validation> findFirst = type.getDataType().getValidationList().stream().findFirst();
  
            attributeMap.put("datatype", dataTypeCode);

            if(findFirst.isPresent())
              dataTypeMap.put("validationList", findFirst.get().getCode());
  
              dataTypeMap.put("code", dataTypeCode);
              dataTypeMap.put("name", dataTypeCode);
              
                 
            return  Tuple.of(attributeMap,dataTypeMap);
         }
       ).collect(Collectors.toList());

       
       List<Map<String, String>> attributesMap = list2.stream().map(attributeAndDataType -> attributeAndDataType._1).collect(Collectors.toList());
       List<Map<String, String>> dataTypesMap = list2.stream().map(attributeAndDataType -> attributeAndDataType._2).collect(Collectors.toList());

       List<Map<String,String>> attributesLinkMap = attributesMap.stream()
           .filter(a -> a.get("code").startsWith("LNK")).collect(Collectors.toList());

       
       try {
          dataTypeHeader.add("validationList");
       } catch (Exception e2) {
       }
       String template =
       "/Users/helios/.genny/" + d.getName() + "-template.xlsx";
       String output = "/Users/helios/.genny/multitenancy/" + d.getName() +"/modules/"+ d.getName()+ ".xlsx";
    
       try {
       createWorkSheets(names, template);
       } catch (IOException e) {
       e.printStackTrace();
       }
    
       File moduleDomain = new File(template);
       Transformer tm = null;
       try {
         InputStream is = moduleDomain.toURL().openStream();
         OutputStream os = new FileOutputStream(output);
         tm = PoiTransformer.createTransformer(is, os);
         } catch (IOException e1) {
         e1.printStackTrace();
         } catch (InvalidFormatException e) {
         e.printStackTrace();
       }
       try {
         x.applyToWorksheet("Ask", d.getAsks(), askHeader,
         tm);
         x.applyToWorksheet("BaseEntity",
         d.getBaseEntitys(), baseEntityHeader,
         tm);
         x.applyToWorksheet("EntityEntity",
         d.getEntityEntitys(), entityEntityHeader,
         tm);
         x.applyToWorksheet("EntityAttribute",
         d.getEntityAttributes(),
         entityAttributeHeader, tm);
         x.applyToWorksheet("Attribute", attributesMap,
         attributeHeader, tm);
         x.applyToWorksheet("AttributeLink", attributesLinkMap,
         attributeHeader, tm);
         x.applyToWorksheet("DataType", dataTypesMap,
         dataTypeHeader, tm);
         x.applyToWorksheet("Validation",
         d.getValidations(), validationHeader,
         tm);
         x.applyToWorksheet("QuestionQuestion",
         d.getQuestionQuestions(),
         questionQuestionHeader, tm);
         x.applyToWorksheet("Question", d.getQuestions(),
         questionHeader, tm);
         x.applyToWorksheet("Messages", d.getMessages(),
         messagesHeader, tm);
         tm.write();
         moduleDomain.delete();
       } catch (InvalidFormatException | IOException e) {
         System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
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
//    workbook.close();
    fileOut.close();
  }


  public <T> void applyToWorksheet(String worksheetName,
      List<T> objects, List<String> header, Transformer transformer)
      throws MalformedURLException, IOException,
      InvalidFormatException {

    final String headers = "headers";
    final String data = "data";

    String props = header.stream().collect(Collectors.joining(","));

    List<String> capitalizedHeaders = header.stream()
        .map(Export::lastFromSplit)
        .map(StringUtils::capitalize)
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

