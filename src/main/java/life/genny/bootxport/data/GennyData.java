package life.genny.bootxport.data;

import java.util.ArrayList;
import java.util.List;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class GennyData {

  private List<BaseEntity> baseEntitys = new ArrayList<BaseEntity>();
  private List<Attribute> attributess = new ArrayList<Attribute>();
  private List<Question> questions = new ArrayList<Question>();
  private List<QuestionQuestion> questionQuestions = new ArrayList<QuestionQuestion>();
  private List<EntityEntity> entityEntities = new ArrayList<EntityEntity>();
  private List<EntityAttribute> entityAttributes = new ArrayList<EntityAttribute>();
  private List<Ask> asks = new ArrayList<Ask>();
  private List<QBaseMSGMessageTemplate> messages = new ArrayList<QBaseMSGMessageTemplate>();
  private List<Validation> validations = new ArrayList<Validation>();

  public List<BaseEntity> getBaseEntitys() {
    return baseEntitys;
  }
  public void setBaseEntitys(List<BaseEntity> baseEntitys) {
    this.baseEntitys = baseEntitys;
  }
  public List<Attribute> getAttributess() {
    return attributess;
  }
  public void setAttributess(List<Attribute> attributess) {
    this.attributess = attributess;
  }
  public List<Question> getQuestions() {
    return questions;
  }
  public void setQuestions(List<Question> questions) {
    this.questions = questions;
  }
  public List<QuestionQuestion> getQuestionQuestions() {
    return questionQuestions;
  }
  public void setQuestionQuestions(
      List<QuestionQuestion> questionQuestions) {
    this.questionQuestions = questionQuestions;
  }
  public List<EntityEntity> getEntityEntities() {
    return entityEntities;
  }
  public void setEntityEntities(List<EntityEntity> entityEntities) {
    this.entityEntities = entityEntities;
  }
  public List<EntityAttribute> getEntityAttributes() {
    return entityAttributes;
  }
  public void setEntityAttributes(
      List<EntityAttribute> entityAttributes) {
    this.entityAttributes = entityAttributes;
  }
  public List<Ask> getAsks() {
    return asks;
  }
  public void setAsks(List<Ask> asks) {
    this.asks = asks;
  }
  public List<QBaseMSGMessageTemplate> getMessages() {
    return messages;
  }
  public void setMessages(List<QBaseMSGMessageTemplate> messages) {
    this.messages = messages;
  }
  public List<Validation> getValidations() {
    return validations;
  }
  public void setValidations(List<Validation> validations) {
    this.validations = validations;
  }

}
