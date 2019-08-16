package life.genny.bootxport.data;

import java.util.List;
//import io.vavr.collection.List;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class Realm {

  
  private String name; 
  private List<BaseEntity> baseEntitys;
  private List<Attribute> attributes;
  private List<Validation>  validations;
  private List<QBaseMSGMessageTemplate> messages;
  private List<QuestionQuestion> questionQuestions;
  private List<Question> questions;

  public List<Question> getQuestions() {
    return questions;
  }
  public void setQuestions(List<Question> questions) {
    this.questions = questions;
  }

  private List<Ask> asks ;
  private List<EntityEntity> entityEntitys;
  private List<EntityAttribute> entityAttributes;

    
  public List<EntityAttribute> getEntityAttributes() {
    return entityAttributes;
  }
  public void setEntityAttributes(
      List<EntityAttribute> entityAttributes) {
    this.entityAttributes = entityAttributes;
  }
  public List<Attribute> getAttributes() {
    return attributes;
  }
  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }
  public List<Validation> getValidations() {
    return validations;
  }
  public void setValidations(List<Validation> validations) {
    this.validations = validations;
  }
  public List<QBaseMSGMessageTemplate> getMessages() {
    return messages;
  }
  public void setMessages(List<QBaseMSGMessageTemplate> messages) {
    this.messages = messages;
  }
  public List<QuestionQuestion> getQuestionQuestions() {
    return questionQuestions;
  }
  public void setQuestionQuestions(
      List<QuestionQuestion> questionQuestions) {
    this.questionQuestions = questionQuestions;
  }
  public List<Ask> getAsks() {
    return asks;
  }
  public void setAsks(List<Ask> asks) {
    this.asks = asks;
  }
  public List<EntityEntity> getEntityEntitys() {
    return entityEntitys;
  }
  public void setEntityEntitys(List<EntityEntity> entityEntitys) {
    this.entityEntitys = entityEntitys;
  }
  public Realm(String name) {
    this.name = name;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public List<BaseEntity> getBaseEntitys() {
    return baseEntitys;
  }
  public void setBaseEntitys(List<BaseEntity> baseEntitys) {
    this.baseEntitys = baseEntitys;
  }
  
  @Override
  public String toString() {
    return "Realm [name=" + name + ", baseEntitys=" + baseEntitys
        + ", attributes=" + attributes + ", validations="
        + validations + ", messages=" + messages
        + ", questionQuestions=" + questionQuestions + ", asks="
        + asks + ", entityEntitys=" + entityEntitys + "]";
  }
}
