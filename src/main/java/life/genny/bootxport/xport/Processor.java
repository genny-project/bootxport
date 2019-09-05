package life.genny.bootxport.xport;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
//import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import life.genny.bootxport.data.Realm;
import life.genny.bootxport.data.ServiceStore;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class Processor {

  private Map<String, List<Attribute>> attributesGroupByRealm;
  private Map<String, List<Validation>> validationsGroupByRealm;
  private Map<String, List<QBaseMSGMessageTemplate>> messagesGroupByRealm;
  private Map<String, List<QuestionQuestion>> questionQuestionsGroupByRealm;
  private Map<String, List<Question>> questionsGroupByRealm;
  public Map<String, List<Question>> getQuestionsGroupByRealm() {
    return questionsGroupByRealm;
  }

  public void setQuestionsGroupByRealm(
      Map<String, List<Question>> questionsGroupByRealm) {
    this.questionsGroupByRealm = questionsGroupByRealm;
  }

  private Map<String, List<Ask>> asksGroupByRealm;
  private Map<String, List<EntityEntity>> entityEntitysGroupByRealm;
  private Map<String, List<EntityAttribute>> entityAttributesGroupByRealm;
  private Set<Realm> realms;
  private Map<String, List<BaseEntity>> baseEntitysGroupByRealm;

  public Map<String, List<BaseEntity>> getBaseEntitysGroupByRealm() {
    return baseEntitysGroupByRealm;
  }

  public void setBaseEntitysGroupByRealm(
      Map<String, List<BaseEntity>> baseEntitysGroupByRealm) {
    this.baseEntitysGroupByRealm = baseEntitysGroupByRealm;
  }

  public Processor() {

  }
  
  public static Processor p = null;
  
  public static List<Realm> multitenancy;
  
  public static Processor getProcessor() {
    if(p != null)
      return p;
    else {
      p = new Processor();
      multitenancy = p.init();
      return p;
    }
    
  }

  public Map<String, List<Attribute>> getAttributestGroupByRealm() {
    return attributesGroupByRealm;
  }

  public void setAttributestGroupByRealm(
      Map<String, List<Attribute>> attributestGroupByRealm) {
    this.attributesGroupByRealm = attributestGroupByRealm;
  }

  public Map<String, List<Validation>> getValidationsGroupByRealm() {
    return validationsGroupByRealm;
  }

  public void setValidationsGroupByRealm(
      Map<String, List<Validation>> validationsGroupByRealm) {
    this.validationsGroupByRealm = validationsGroupByRealm;
  }

  public Map<String, List<QBaseMSGMessageTemplate>> getMessagesGroupByRealm() {
    return messagesGroupByRealm;
  }

  public void setMessagesGroupByRealm(
      Map<String, List<QBaseMSGMessageTemplate>> messagesGroupByRealm) {
    this.messagesGroupByRealm = messagesGroupByRealm;
  }

  public Map<String, List<QuestionQuestion>> getQuestionQuestionGroupByRealm() {
    return questionQuestionsGroupByRealm;
  }

  public void setQuestionQuestionGroupByRealm(
      Map<String, List<QuestionQuestion>> questionQuestionGroupByRealm) {
    this.questionQuestionsGroupByRealm = questionQuestionGroupByRealm;
  }

  public Map<String, List<Ask>> getAskGroupByRealm() {
    return asksGroupByRealm;
  }

  public void setAskGroupByRealm(
      Map<String, List<Ask>> askGroupByRealm) {
    this.asksGroupByRealm = askGroupByRealm;
  }

  public Map<String, List<EntityEntity>> getEntityEntityGroupByRealm() {
    return entityEntitysGroupByRealm;
  }

  public void setEntityEntityGroupByRealm(
      Map<String, List<EntityEntity>> entityEntityGroupByRealm) {
    this.entityEntitysGroupByRealm = entityEntityGroupByRealm;
  }

  public Set<Realm> getRealms() {
    return realms;
  }
  
  public void setRealms(Set<Realm> realms) {
    this.realms = realms;
  }


  private List<Realm> init() {
    GennyData data = ServiceStore.getServiceStore().data;

    Map<String, List<BaseEntity>> baseEntitysGroupByRealm =
        Stream.ofAll(data.getBaseEntitys())
           .groupBy(be -> be.getRealm())
           .bimap(k -> k, v -> v.toJavaList());
    
    Set<Realm> realms = baseEntitysGroupByRealm
        .keySet()
        .map(name -> new Realm(name));

    System.out.println("----------------------------");
    realms.forEach(System.out::println);
    System.out.println("----------------------------");
    Map<String, List<Attribute>> attributestGroupByRealm = Stream.ofAll(data.getAttributess())
       .groupBy(attr -> attr.getRealm())
       .bimap(k -> k, v -> v.toJavaList());

    Map<String, List<Validation>>  validationsGroupByRealm = Stream.ofAll(data.getValidations())
       .groupBy(vals -> vals.getRealm())
       .bimap(k -> k, v -> v.toJavaList());

    Map<String, List<QBaseMSGMessageTemplate>> messagesGroupByRealm = Stream.ofAll(data.getMessages())
       .groupBy(msgs -> msgs.getRealm())
       .bimap(k -> k, v -> v.toJavaList());

    Map<String, List<QuestionQuestion>> questionQuestionGroupByRealm = Stream.ofAll(data.getQuestionQuestions())
       .groupBy(queQues -> queQues.getRealm())
       .bimap(k -> k, v -> v.toJavaList());

    Map<String, List<Ask>> askGroupByRealm = Stream.ofAll(data.getAsks())
       .groupBy(asks -> asks.getRealm())
       .bimap(k -> k, v -> v.toJavaList());

    Map<String, List<EntityEntity>> entityEntityGroupByRealm = Stream.ofAll(data.getEntityEntities())
       .groupBy(entEnts -> entEnts.getRealm())
       .bimap(k -> k, v -> v.toJavaList());

    Map<String, List<EntityAttribute>> entityAttributeGroupByRealm = Stream.ofAll(data.getEntityAttributes())
       .groupBy(entAttrs -> entAttrs.getRealm())
       .bimap(k -> k, v -> v.toJavaList());
    
    Map<String, List<Question>> questionsGroupByRealm = Stream.ofAll(data.getQuestions())
       .groupBy(ques -> ques.getRealm())
       .bimap(k -> k, v -> v.toJavaList());

    setRealms(realms);
    setAskGroupByRealm(askGroupByRealm);
    setAttributestGroupByRealm(attributestGroupByRealm);
    setValidationsGroupByRealm(validationsGroupByRealm);
    setMessagesGroupByRealm(messagesGroupByRealm);
    setQuestionQuestionGroupByRealm(questionQuestionGroupByRealm);
    setBaseEntitysGroupByRealm(baseEntitysGroupByRealm);
    setEntityEntityGroupByRealm(entityEntityGroupByRealm);
    setEntityAttributesGroupByRealm(entityAttributeGroupByRealm);
    setQuestionsGroupByRealm(questionsGroupByRealm);
    

    System.out.println("----------------------------");
    getRealms().forEach(System.out::println);
    System.out.println("----------------------------");
    List<Realm> r = getRealms().toStream().map(realm ->{
     realm.setBaseEntitys(getBaseEntitysGroupByRealm().get(realm.getName()).get());
     realm.setAttributes(getAttributestGroupByRealm().get(realm.getName()).get());
     realm.setEntityEntitys(getEntityEntityGroupByRealm().get(realm.getName()).get());
     try {
        realm.setAsks(getAskGroupByRealm().get(realm.getName()).get());
     }catch(NoSuchElementException e) {
       System.out.println("no exist");
     }
     try {
        realm.setMessages(getMessagesGroupByRealm().get(realm.getName()).get());
     }catch(NoSuchElementException e) {
       System.out.println("no exist");
     }
     try {
        realm.setQuestionQuestions(getQuestionQuestionGroupByRealm().get(realm.getName()).get());
     }catch(NoSuchElementException e) {
       System.out.println("no exist");
     }
     realm.setValidations(getValidationsGroupByRealm().get(realm.getName()).get());
     realm.setEntityAttributes(getEntityAttributesGroupByRealm().get(realm.getName()).get());
     realm.setQuestions(getQuestionsGroupByRealm().get(realm.getName()).get());

//     List<Realm> re = new ArrayList<>();
//     re.add(realm);
//     return re;
     return realm;
    }).collect(Collectors.toList());
//    }).reduce((acc,next)->{
//      System.out.println(next);
//      acc.addAll(next);
//      return acc;
//    });
      
    r.forEach(System.out::println);
    System.out.println(r);
    return r;
  }

  public Map<String, List<EntityAttribute>> getEntityAttributesGroupByRealm() {
    return entityAttributesGroupByRealm;
  }

  public void setEntityAttributesGroupByRealm(
      Map<String, List<EntityAttribute>> entityAttributesGroupByRealm) {
    this.entityAttributesGroupByRealm = entityAttributesGroupByRealm;
  }

}
