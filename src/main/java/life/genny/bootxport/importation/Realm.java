package life.genny.bootxport.importation;

import java.util.Map;
import io.vavr.Function1;

class Realm {

  XlsImportOnline xlsOnline = XlsImportOnline.getInstance();

  private String name;

  private Map<String, Map<String, String>> attribute;
  private Map<String, Map<String, String>> attributeLink;
  private Map<String, Map<String, String>> questionQuestion;
  private Map<String, Map<String, String>> validation;
  private Map<String, Map<String, String>> dataType;
  private Map<String, Map<String, String>> question;
  private Map<String, Map<String, String>> ask;
  private Map<String, Map<String, String>> notifications;
  private Map<String, Map<String, String>> entityAttribute;
  private Map<String, Map<String, String>> entityEntity;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name.toLowerCase();
  }

  Function1<Map<String, String>, Module> ap = (d) -> {

    Module mx = new Module(d.get("sheetID"));

    return mx;
  };

  public Realm(String name, Map<String, String> d) {
    setName(name);
    Module m = ap.apply(d);
   
    baseEntity = m.geta.stream().map(mm -> mm.baseEntitys)
        .reduce((ac, acc) -> {
          ac.putAll(acc);
          return ac;
        }).get();
    attribute = m.geta.stream().map(mm -> {
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


}
