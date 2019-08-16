package life.genny.bootxport.bootx;

import java.util.HashMap;
import java.util.Map;

public abstract class DataUnit {

  protected Map<String, Map<String, String>> attributes =  new HashMap<>();
  protected Map<String, Map<String, String>> attributeLinks =  new HashMap<>();
  protected Map<String, Map<String, String>> questionQuestions =  new HashMap<>();
  protected Map<String, Map<String, String>> validations =  new HashMap<>();
  protected Map<String, Map<String, String>> dataTypes =  new HashMap<>();
  protected Map<String, Map<String, String>> questions =  new HashMap<>();
  protected Map<String, Map<String, String>> asks =  new HashMap<>();
  protected Map<String, Map<String, String>> notifications =  new HashMap<>();
  protected Map<String, Map<String, String>> entityAttributes =  new HashMap<>();
  protected Map<String, Map<String, String>> entityEntitys =  new HashMap<>();
  protected Map<String, Map<String, String>> baseEntitys =  new HashMap<>();
  protected Map<String, Map<String, String>> messages =  new HashMap<>();


  public Map<String, Map<String, String>> getAttributes() {
    return attributes;
  }

  public Map<String, Map<String, String>> getAttributeLinks() {
    return attributeLinks;
  }

  public Map<String, Map<String, String>> getQuestionQuestions() {
    return questionQuestions;
  }

  public Map<String, Map<String, String>> getValidations() {
    return validations;
  }

  public Map<String, Map<String, String>> getDataTypes() {
    return dataTypes;
  }

  public Map<String, Map<String, String>> getQuestions() {
    return questions;
  }

  public Map<String, Map<String, String>> getAsks() {
    return asks;
  }
  public Map<String, Map<String, String>> getNotifications() {
    return notifications;
  }

  public Map<String, Map<String, String>> getEntityAttributes() {
    return entityAttributes;
  }

  public Map<String, Map<String, String>> getEntityEntitys() {
    return entityEntitys;
  }

  public Map<String, Map<String, String>> getBaseEntitys() {
    return baseEntitys;
  }
  
  public Map<String, Map<String, String>> getMessages() {
    return messages;
  }
  
}
