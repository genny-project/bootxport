package life.genny.bootxport.xlsimport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.amazonaws.services.directory.model.Attribute;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.validation.Validation;

public class ModuleDataDomain {

  private static Set<String> codeKey = new HashSet<>();

  private static Set<String> baseEntityCodeattributeCodeKey =
      new HashSet<>();

  private static Set<String> targetCodeParentCodeLinkCodeKey =
      new HashSet<>();

  private static Set<String> targetCodeParentCodeKey = new HashSet<>();

  private static Set<String> questionCodeSourceCodeTargetCode =
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
  private String moduleName;
  private String sheetId;

  private final String notificationsName = "Notifications";
  private final String baseEntityName =
      BaseEntity.class.getSimpleName();
  private final String attributeName =
      Attribute.class.getSimpleName();
  private final String questionName = Question.class.getSimpleName();
  private final String questionQuestionName =
      QuestionQuestion.class.getSimpleName();
  private final String entityEntityName =
      EntityEntity.class.getSimpleName();
  private final String entityAttributeName =
      EntityAttribute.class.getSimpleName();
  private final String askName = Ask.class.getSimpleName();
  private final String validationName =
      Validation.class.getSimpleName();
  private final String dataTypeName = DataType.class.getSimpleName();
  private final String attributeLinkName =
      AttributeLink.class.getSimpleName();

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

  public ModuleDataDomain(String sheetId, String moduleName) {
    this.sheetId = sheetId;
    this.moduleName = moduleName;
    setBaseEntity();
    setAttribute();
    setAttributeLink();
    setQuestionQuestion();
    setValidation();
    setDataType();
    setQuestion();
    setAsk();
    setNotifications();
    setEntityAttribute();
    setEntityEntity();
  }


  public void setBaseEntity() {
    try {
      this.baseEntitys = xlsOnline.getInTableFormatInKey
          .apply(sheetId, baseEntityName, codeKey);
    } catch (Exception e1) {
      baseEntitys = new HashMap<>();
    }
  }

  public void setAttribute() {
    System.out.println(moduleName);
    try {
      this.attribute = xlsOnline.getInTableFormatInKey.apply(sheetId,
          attributeName, codeKey);
    } catch (Exception e1) {
      attribute = new HashMap<>();
    }
  }

  public void setAttributeLink() {
    try {
      this.attributeLink = xlsOnline.getInTableFormatInKey
          .apply(sheetId, attributeLinkName, codeKey);
    } catch (Exception e1) {
      attributeLink = new HashMap<>();
    }
  }

  public void setQuestionQuestion() {
    try {
      this.questionQuestion = xlsOnline.getInTableFormatInKey.apply(
          sheetId, questionQuestionName, targetCodeParentCodeKey);
    } catch (Exception e1) {
      questionQuestion = new HashMap<>();
    }
  }

  public void setValidation() {
    try {
      this.validation = xlsOnline.getInTableFormatInKey.apply(sheetId,
          validationName, codeKey);
    } catch (Exception e1) {
      validation = new HashMap<>();
    }
  }

  public void setDataType() {
    try {
      this.dataType = xlsOnline.getInTableFormatInKey.apply(sheetId,
          dataTypeName, codeKey);
    } catch (Exception e1) {
      dataType = new HashMap<>();
    }
  }

  public void setQuestion() {
    try {
      this.question = xlsOnline.getInTableFormatInKey.apply(sheetId,
          questionName, codeKey);
    } catch (Exception e1) {
      question = new HashMap<>();
    }
  }

  public void setAsk() {
    try {
      this.ask = xlsOnline.getInTableFormatInKey.apply(sheetId,
          askName, questionCodeSourceCodeTargetCode);
    } catch (Exception e1) {
      ask = new HashMap<>();
    }
  }

  public void setNotifications() {
    try {
      this.notifications = xlsOnline.getInTableFormatInKey
          .apply(sheetId, notificationsName, codeKey);
    } catch (Exception e1) {
      notifications = new HashMap<>();
    }
  }

  public void setEntityAttribute() {
    try {
      this.entityAttribute =
          xlsOnline.getInTableFormatInKey.apply(sheetId,
              entityAttributeName, baseEntityCodeattributeCodeKey);
    } catch (Exception e1) {
      entityAttribute = new HashMap<>();
    }
  }

  public void setEntityEntity() {
    try {
      this.entityEntity = xlsOnline.getInTableFormatInKey.apply(
          sheetId, entityEntityName, targetCodeParentCodeLinkCodeKey);
    } catch (Exception e1) {
      entityEntity = new HashMap<>();
    }
  }

}
