package life.genny.bootxport.importation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.validation.Validation;

public class XlsImportOnline {

  private final String RANGE = "!A1:Z";

  private String sheetId;

  private final JsonFactory JSON_FACTORY =
      JacksonFactory.getDefaultInstance();

  private HttpTransport HTTP_TRANSPORT;

  private final List<String> SCOPES =
      Arrays.asList(SheetsScopes.SPREADSHEETS);

  private Sheets service;

  private XlsImportOnline() {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      service = getSheetsService();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  private static volatile XlsImportOnline instance = null;

  public static XlsImportOnline getInstance() {
    if (instance == null) {
      synchronized (XlsImportOnline.class) {
        if (instance == null) {
          instance = new XlsImportOnline();
        }
      }
    }
    return instance;
  }


  public Sheets getSheetsService() throws Exception {
    final Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        credential).build();
  }

  public Credential authorize() throws Exception {
    Optional<String> path =
        Optional.ofNullable(System.getenv("GOOGLE_SVC_ACC_PATH"));

    GoogleCredential credential =
        GoogleCredential.fromStream(new FileInputStream(path.get()),
            HTTP_TRANSPORT, JSON_FACTORY).createScoped(SCOPES);

    System.out.println("Spreadsheets being read with user id: "
        + credential.getServiceAccountId());
    System.out.println(credential.getTokenServerEncodedUrl());
    return credential;
  }

  public List<Map<String, String>> toTableFormat(
      final List<List<Object>> values) {

    final List<String> keys = new ArrayList<String>();
    final List<Map<String, String>> k = new ArrayList<>();
    for (final Object key : values.get(0)) {
      keys.add((String) key);
    }
    values.remove(0);
    for (final List row : values) {
      final Map<String, String> mapper =
          new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(keys.get(counter), row.get(counter).toString());
      }
      k.add(mapper);
    }
    return k;
  }

  public Map<String, Map<String, String>> toTableFormatInKey(
      final List<List<Object>> values, Set<String> keyColumns) {

    final List<String> keys = new ArrayList<String>();
    final Map<String, Map<String, String>> k = new HashMap<>();
    for (final Object key : values.get(0)) {
      keys.add((String) key);
    }
    values.remove(0);
    for (final List row : values) {
      final Map<String, String> mapper =
          new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(keys.get(counter), row.get(counter).toString());
      }
      String join = mapper.keySet().stream()
          .filter(keyColumns::contains).collect(Collectors.joining());

      k.put(mapper.get(join), mapper);
    }
    return k;
  }

  private List<Map<String, String>> table(final String sheetId,
      final String sheetName) {
    final String absoluteRange = sheetName + RANGE;
    ValueRange response = null;
    // try {
    try {
      response = service.spreadsheets().values()
          .get(sheetId, absoluteRange).execute();
    } catch (IOException e) {
      System.out.println("Does not exist");
    }
    List<List<Object>> data = null;
    try {
      data = response.getValues();
    } catch (NullPointerException e) {
      System.out.println("Looks like you dont have internet");
    }
    return toTableFormat(data);

  };

  private Map<String, Map<String, String>> tableInKey(
      final String sheetId, final String sheetName,
      Set<String> keyColumns) {
    final String absoluteRange = sheetName + RANGE;
    ValueRange response = null;
    // try {
    try {
      response = service.spreadsheets().values()
          .get(sheetId, absoluteRange).execute();
    } catch (IOException e) {
    }
    List<List<Object>> data = null;
    try {
      data = response.getValues();
    } catch (NullPointerException e) {
    }
    return toTableFormatInKey(data, keyColumns);

  };

  public Function2<String, String, List<Map<String, String>>> getInTableFormat =
      Function2.of(this::table).memoized();

  public Function3<String, String, Set<String>, Map<String, Map<String, String>>> getInTableFormatInKey =
      Function3.of(this::tableInKey).memoized();


  public void update() {

    getInTableFormat = Function2.of(this::table).memoized();
    getInTableFormatInKey = Function3.of(this::tableInKey).memoized();
  }
  // public static void main(String... args) throws IOException {
  // RootSheet r =
  // new RootSheet("1zzz6bYXuryASR09Tsyok4_qiJI9n81DBsxD4oFBk5mw");
  //
  // XlsImportOnline factoryMain = XlsImportOnline.getFactoryMain();
  // try {
  // List<Map<String, String>> list = factoryMain.table("Projects");
  // List<RealmSheet> realms = list.stream().map(fd -> {
  // RealmSheet realm = new RealmSheet();
  // realm.setId(fd.get("sheetID"));
  // realm.setName(fd.get("name"));
  // return realm;
  // }).collect(Collectors.toList());
  //
  // realms.forEach(d -> {
  //
  // factoryMain.setSheetId(d.getId());
  // List<Map<String, String>> table;
  //
  // try {
  // System.out.println("Realm: " + d.getName());
  // try {
  //
  // table = factoryMain.table("Modules");
  // table.forEach(System.out::println);
  //
  //
  // } catch (GoogleJsonResponseException e) {
  //
  // System.out.println("null");
  // }
  //
  // } catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // });
  //
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  // }



  public static void main(String... args)
      throws GoogleJsonResponseException {


    XlsImportOnline xlsOnline = XlsImportOnline.getInstance();

    // Function3<String, Map<String, String>, Set<String>, Map<String, Map<String, String>>> ap =
    // (name, d, keys) -> {
    //
    // Map<String, Map<String, String>> map =
    // xlsOnline.getInTableFormat
    // .apply(d.get("sheetID"), "Modules").stream()
    // .map(d1 -> {
    //
    // System.out.println();
    // System.out.println("Module: " + d1.get("name")
    // + " " + d1.get("module"));
    // System.out.println();
    // Map<String, Map<String, String>> datas =
    // new HashMap<>();
    // try {
    //
    // datas = xlsOnline.getInTableFormatInKey
    // .apply(d1.get("sheetID"), name, keys);
    //
    // } catch (Exception e1) {
    // }
    // return datas;
    // }).reduce((ac, acc) -> {
    // ac.putAll(acc);
    // return ac;
    // }).get();;
    //
    // return map;
    // };
    //
    //

    long timeBefore = 0;
    long timeAfter = 0;

    timeBefore = System.currentTimeMillis();
    List<RealmX> realms = xlsOnline.getInTableFormat
        .apply("1zzz6bYXuryASR09Tsyok4_qiJI9n81DBsxD4oFBk5mw",
            "Projects")
        .stream().limit(1).map(d -> {

          RealmX mx = new RealmX(d.get("name"), d);
          return mx;
        }).collect(Collectors.toList());

    realms.forEach(d -> System.out.println(d.getName()));
    // timeAfter = System.currentTimeMillis();
    // System.out.println(timeAfter - timeBefore);


  }

}



class TheWorkBook {

  static Set<String> codeKey = new HashSet<>();
  static Set<String> baseEntityCodeattributeCodeKey = new HashSet<>();
  static Set<String> targetCodeParentCodeLinkCodeKey =
      new HashSet<>();
  static Set<String> targetCodeParentCodeKey = new HashSet<>();
  static Set<String> questionCodeSourceCodeTargetCode =
      new HashSet<>();


  static {
    codeKey.add("code");
    baseEntityCodeattributeCodeKey.add("baseEntityCode");
    baseEntityCodeattributeCodeKey.add("attributeCode");
    targetCodeParentCodeLinkCodeKey.add("targetCode");
    targetCodeParentCodeLinkCodeKey.add("parentCode");
    targetCodeParentCodeLinkCodeKey.add("linkCode");
    targetCodeParentCodeKey.add("targetCode");
    targetCodeParentCodeKey.add("parentCode");
    targetCodeParentCodeKey.add("linkCode");
    questionCodeSourceCodeTargetCode.add("question_code");
    questionCodeSourceCodeTargetCode.add("sourceCode");
    questionCodeSourceCodeTargetCode.add("targetCode");
  }

  XlsImportOnline xlsOnline = XlsImportOnline.getInstance();

  String moduleName;
  String sheetId;


  String baseEntityName = "BaseEntity";
  String attributeName = "Attribute";
  String questionName = "Question";
  String questionQuestionName = "QuestionQuestion";
  String entityEntityName = "EntityEntity";
  String entityAttributeName = "EntityAttribute";
  String askName = "Ask";
  String notificationsName = "Notifications";
  String validationName = "Validation";
  String dataTypeName = "DataType";
  String attributeLinkName = "AttributeLink";

  Map<String, Map<String, String>> baseEntitys;
  Map<String, Map<String, String>> attribute;
  Map<String, Map<String, String>> attributeLink;
  Map<String, Map<String, String>> questionQuestion;
  Map<String, Map<String, String>> validation;
  Map<String, Map<String, String>> dataType;
  Map<String, Map<String, String>> question;
  Map<String, Map<String, String>> ask;
  Map<String, Map<String, String>> notifications;
  Map<String, Map<String, String>> entityAttribute;
  Map<String, Map<String, String>> entityEntity;

  public TheWorkBook(String sheetId, String moduleName) {
    System.out.println("here:" + sheetId);
    System.out.println("heremmm:" + moduleName);
    this.sheetId = sheetId;
    this.moduleName = moduleName;
    try {

      setBaseEntity();

    } catch (Exception e1) {
      baseEntitys = new HashMap<>();
    }
    try {

      setAttribute();

    } catch (Exception e1) {
      attribute = new HashMap<>();
    }
    try {


      setAttributeLink();

    } catch (Exception e1) {
      attributeLink = new HashMap<>();
    }
    try {

      setQuestionQuestion();

    } catch (Exception e1) {
      questionQuestion = new HashMap<>();
    }
    try {

      setValidation();

    } catch (Exception e1) {
      validation = new HashMap<>();
    }
    try {

      setDataType();

    } catch (Exception e1) {
      dataType = new HashMap<>();
    }
    try {

      setQuestion();

    } catch (Exception e1) {
      question = new HashMap<>();
    }
    try {

      setAsk();

    } catch (Exception e1) {
      ask = new HashMap<>();
    }
    try {

      setNotifications();

    } catch (Exception e1) {
      notifications = new HashMap<>();
    }
    try {

      setEntityAttribute();

    } catch (Exception e1) {
      entityAttribute = new HashMap<>();
    }
    try {

      setEntityEntity();

    } catch (Exception e1) {
      entityEntity = new HashMap<>();
    }

  }

  public void setBaseEntity() {
    this.baseEntitys = xlsOnline.getInTableFormatInKey.apply(sheetId,
        baseEntityName, codeKey);
  }

  public void setAttribute() {
    System.out.println(moduleName);
    this.attribute = xlsOnline.getInTableFormatInKey.apply(sheetId,
        attributeName, codeKey);
  }

  public void setAttributeLink() {
    this.attributeLink = xlsOnline.getInTableFormatInKey
        .apply(sheetId, attributeLinkName, codeKey);
  }

  public void setQuestionQuestion() {
    this.questionQuestion = xlsOnline.getInTableFormatInKey.apply(
        sheetId, questionQuestionName, targetCodeParentCodeKey);
  }

  public void setValidation() {
    this.validation = xlsOnline.getInTableFormatInKey.apply(sheetId,
        validationName, codeKey);
  }

  public void setDataType() {
    this.dataType = xlsOnline.getInTableFormatInKey.apply(sheetId,
        dataTypeName, codeKey);
  }

  public void setQuestion() {
    this.question = xlsOnline.getInTableFormatInKey.apply(sheetId,
        questionName, codeKey);
  }

  public void setAsk() {
    this.ask = xlsOnline.getInTableFormatInKey.apply(sheetId, askName,
        questionCodeSourceCodeTargetCode);
  }

  public void setNotifications() {
    this.notifications = xlsOnline.getInTableFormatInKey
        .apply(sheetId, notificationsName, codeKey);
  }

  public void setEntityAttribute() {
    this.entityAttribute = xlsOnline.getInTableFormatInKey.apply(
        sheetId, entityAttributeName, baseEntityCodeattributeCodeKey);
  }

  public void setEntityEntity() {
    this.entityEntity = xlsOnline.getInTableFormatInKey.apply(sheetId,
        entityEntityName, targetCodeParentCodeLinkCodeKey);
  }

}


class ModuleX {


  String sheetID;
  final String worksheetName = "Modules";


  String spreadsheet;

  List<TheWorkBook> geta;

  public ModuleX(String sheetID) {
    this.sheetID = sheetID;
    geta = xlsOnline.getInTableFormat
        .apply(this.sheetID, worksheetName).stream().map(d1 -> {
          System.out.println(d1);

          TheWorkBook tw =
              new TheWorkBook(d1.get("sheetID"), d1.get("module"));


          return tw;

        }).collect(Collectors.toList());

  }

  XlsImportOnline xlsOnline = XlsImportOnline.getInstance();



  // Function3<String, Map<String, String>, Set<String>, Map<String, Map<String, String>>> ap =
  // (name, d, keys) -> {
  //
  // Map<String, Map<String, String>> map =
  //
  // xlsOnline.getInTableFormat
  // .apply(d.get("sheetID"), "Modules").stream()
  // .map(d1 -> {
  //
  // System.out.println();
  // System.out.println("Module: " + d1.get("name") + " "
  // + d1.get("module"));
  // System.out.println();
  // Map<String, Map<String, String>> datas =
  // new HashMap<>();
  // try {
  //
  // datas = xlsOnline.getInTableFormatInKey
  // .apply(d1.get("sheetID"), name, keys);
  //
  // } catch (Exception e1) {
  // }
  // return datas;
  // }).reduce((ac, acc) ->
  //
  // {
  // ac.putAll(acc);
  // return ac;
  // }).get();;
  //
  // return map;
  // };


}


class RealmX {

  XlsImportOnline xlsOnline = XlsImportOnline.getInstance();

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name.toLowerCase();
  }

  Function1<Map<String, String>, ModuleX> ap = (d) -> {

    ModuleX mx = new ModuleX(d.get("sheetID"));

    return mx;
  };
  // Function3<String, Map<String, String>, Set<String>, Map<String, Map<String, String>>> ap =
  // (name, d, keys) -> {
  //
  // ModuleX mx = new ModuleX();
  // mx.sheetID = d.get("sheetID");
  //
  // Map<String, Map<String, String>> map =
  // xlsOnline.getInTableFormat
  // .apply(d.get("sheetID"), "Modules").stream()
  // .map(d1 -> {
  //
  // Map<String, Map<String, String>> datas =
  // new HashMap<>();
  // try {
  //
  // datas = xlsOnline.getInTableFormatInKey
  // .apply(d1.get("sheetID"), name, keys);
  //
  // } catch (Exception e1) {
  // }
  // return datas;
  // }).reduce((ac, acc) ->
  //
  // {
  // ac.putAll(acc);
  // return ac;
  // }).get();;
  //
  // return map;
  // };


  public RealmX(String name, Map<String, String> d) {
    System.out.println("Realm: " + name);
    setName(name);
    ModuleX m = ap.apply(d);
    // setBaseEntity(d);
    // setAttribute(d);
    // setAttributeLink(d);
    // setQuestionQuestion(d);
    // setValidation(d);
    // setDataType(d);
    // setQuestion(d);
    // setAsk(d);
    // setNotifications(d);
    // setEntityAttribute(d);
    // setEntityEntity(d);
    baseEntity = m.geta.stream().map(mm -> mm.baseEntitys)
        .reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();
    attribute = m.geta.stream().map(mm -> {
      System.out.println("The Module name: " + mm.moduleName);;
      return mm.attribute;
    }).reduce((ac, acc) -> {

      ac.putAll(acc);
      return ac;
    }).get();
    

    attributeLink = m.geta.stream().map(mm -> mm.attributeLink)
        .reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();

    questionQuestion = m.geta.stream().map(mm -> mm.questionQuestion)
        .reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();

    validation =
        m.geta.stream().map(mm -> mm.validation).reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();

    dataType =
        m.geta.stream().map(mm -> mm.dataType).reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();

    System.out.println("helooooooooooooo "+dataType.get("DTT_UPLOAD"));

    question =
        m.geta.stream().map(mm -> mm.question).reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();
    ask = m.geta.stream().map(mm -> mm.ask).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
    notifications = m.geta.stream().map(mm -> mm.notifications)
        .reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();

    entityAttribute = m.geta.stream().map(mm -> mm.entityAttribute)
        .reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();

    entityEntity = m.geta.stream().map(mm -> mm.entityEntity)
        .reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();

  }

  Map<String, Map<String, String>> baseEntity;

  public Map<String, Map<String, String>> getBaseEntity() {
    return baseEntity;
  }

  public Map<String, Map<String, String>> getAttribute() {
    return attribute;
  }

  public Map<String, Map<String, String>> getNotifications() {
    return notifications;
  }

  public Map<String, Map<String, String>> getEntityEntity() {
    return entityEntity;
  }

  public Map<String, Map<String, String>> getQuestion() {
    return question;
  }

  public Map<String, Map<String, String>> getEntityAttribute() {
    return entityAttribute;
  }

  public Map<String, Map<String, String>> getAsk() {
    return ask;
  }

  public Map<String, Map<String, String>> getQuestionQuestion() {
    return questionQuestion;
  }

  public Map<String, Map<String, String>> getAttributeLink() {
    return attributeLink;
  }

  public Map<String, Map<String, String>> getValidation() {
    return validation;
  }

  public Map<String, Map<String, String>> getDataType() {
    return dataType;
  }



  // public void setBaseEntity(Map<String, String> baseEntity) {
  // this.baseEntity = ap.apply(BaseEntity.class.getSimpleName(),
  // baseEntity, codeKey);
  // }
  //
  // public void setAttribute(Map<String, String> attribute) {
  // this.attribute =
  // ap.apply(Attribute.class.getSimpleName(), attribute, codeKey);
  // }
  //
  // public void setAttributeLink(Map<String, String> attributeLink) {
  // this.attributeLink = ap.apply(AttributeLink.class.getSimpleName(),
  // attributeLink, codeKey);
  // }
  //
  // public void setQuestionQuestion(
  // Map<String, String> questionQuestion) {
  // this.questionQuestion =
  // ap.apply(QuestionQuestion.class.getSimpleName(),
  // questionQuestion, targetCodeParentCodeKey);
  // }
  //
  // public void setValidation(Map<String, String> validation) {
  // this.validation = ap.apply(Validation.class.getSimpleName(),
  // validation, codeKey);
  // }
  //
  // public void setDataType(Map<String, String> dataType) {
  // this.dataType =
  // ap.apply(DataType.class.getSimpleName(), dataType, codeKey);
  // }
  //
  // public void setQuestion(Map<String, String> question) {
  // this.question =
  // ap.apply(Question.class.getSimpleName(), question, codeKey);
  // }
  //
  // public void setAsk(Map<String, String> ask) {
  // this.ask = ap.apply(Ask.class.getSimpleName(), ask,
  // questionCodeSourceCodeTargetCode);
  // }
  //
  // public void setNotifications(Map<String, String> notifications) {
  // this.notifications =
  // ap.apply("Notifications", notifications, codeKey);
  // }
  //
  // public void setEntityAttribute(
  // Map<String, String> entityAttribute) {
  // this.entityAttribute =
  // ap.apply(EntityAttribute.class.getSimpleName(),
  // entityAttribute, baseEntityCodeattributeCodeKey);
  // }
  //
  // public void setEntityEntity(Map<String, String> entityEntity) {
  // this.entityEntity = ap.apply(EntityEntity.class.getSimpleName(),
  // entityEntity, targetCodeParentCodeLinkCodeKey);
  // }

  Map<String, Map<String, String>> attribute;
  Map<String, Map<String, String>> attributeLink;
  Map<String, Map<String, String>> questionQuestion;
  Map<String, Map<String, String>> validation;
  Map<String, Map<String, String>> dataType;
  Map<String, Map<String, String>> question;
  Map<String, Map<String, String>> ask;
  Map<String, Map<String, String>> notifications;
  Map<String, Map<String, String>> entityAttribute;
  Map<String, Map<String, String>> entityEntity;


  static Set<String> codeKey = new HashSet<>();
  static Set<String> baseEntityCodeattributeCodeKey = new HashSet<>();
  static Set<String> targetCodeParentCodeLinkCodeKey =
      new HashSet<>();
  static Set<String> targetCodeParentCodeKey = new HashSet<>();
  static Set<String> questionCodeSourceCodeTargetCode =
      new HashSet<>();


  static {
    codeKey.add("code");
    baseEntityCodeattributeCodeKey.add("baseEntityCode");
    baseEntityCodeattributeCodeKey.add("attributeCode");
    targetCodeParentCodeLinkCodeKey.add("targetCode");
    targetCodeParentCodeLinkCodeKey.add("parentCode");
    targetCodeParentCodeLinkCodeKey.add("linkCode");
    targetCodeParentCodeKey.add("targetCode");
    targetCodeParentCodeKey.add("parentCode");
    targetCodeParentCodeKey.add("linkCode");
    questionCodeSourceCodeTargetCode.add("question_code");
    questionCodeSourceCodeTargetCode.add("sourceCode");
    questionCodeSourceCodeTargetCode.add("targetCode");
  }
}
