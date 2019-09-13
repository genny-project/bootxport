package life.genny.bootxport.data;

public class DataHeader {

  public static String[] tableNames = new String[] {"Ask",
      "BaseEntity", "Attribute", "AttributeLink", "Validation",
      "QuestionQuestion", "Messages", "EntityEntity",
      "EntityAttribute", "DataType", "Question", "Test",};

  public static String[] askH = new String[] {"questionCode", "name",
      "sourceCode", "targetCode", "attributeCode", "weight",
      "readonly", "oneshot", "mandatory", "hidden", "disabled",};

  public static String[] baseEntityH = new String[] {"name", "code",};

  public static String[] entityEntityH = new String[] {
      "link.targetCode", "link.sourceCode", "pk.attribute.code",
      "link.childColor", "link.linkValue", "link.parentColor",
      "link.rule", "link.weight", "valueBoolean", "valueDate",
      "valueDateTime", "valueDouble", "valueInteger", "valueLong",
      "valueMoney", "valueString", "valueTime", "version", "weight",};
  public static String[] entityAttributeH = new String[] {
      "baseEntityCode", "attributeCode", "inferred", "privacyFlag",
      "readonly", "valueBoolean", "valueDate", "valueDateRange",
      "valueDateTime", "valueDouble", "valueInteger", "valueLong",
      "valueMoney", "valueString", "valueTime", "weight",};

  public static String[] attributeH =
      new String[] {"code", "name", "datatype",};

  public static String[] dataTypeH = new String[] {"validationList",
      "typeName", "className", "inputmask", "code", "name",};

  public static String[] validationH =
      new String[] {"name", "realm", "code", "multiAllowed",
          "recursiveGroup", "regex", "selectionBaseEntityGroupList",};

  public static String[] questionQuestionH = new String[] {
      "pk.sourceCode", "pk.targetCode", "weight", "mandatory",};

  public static String[] questionH =
      new String[] {"code", "name", "attributeCode",};

  public static String[] messagesH =
      new String[] {"code", "name", "description", "subject",
          "email_templateId", "toast_template", "sms_template",};

}
