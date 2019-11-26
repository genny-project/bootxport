package life.genny.bootxport.xport;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
//import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import life.genny.bootxport.data.Realm;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class Multitenancy {
//
//
//  public Multitenancy() {
//    realms = init();
//  }
//  
//  public static Multitenancy p = null;
//  
//  public List<Realm> realms;
//  
//  public List<Realm> getRealms(){
//    return this.realms;
//  }
//
//
//  private List<Realm> init() {
//    RepositoryImpl data = new RepositoryImpl();
//
//    Map<String, List<BaseEntity>> baseEntitysGroupByRealm =
//        Stream.ofAll(data.findAllBaseEntitys())
//           .groupBy(be -> be.getRealm())
//           .bimap(k -> k, v -> v.toJavaList());
//    
//    Set<Realm> realms = baseEntitysGroupByRealm
//        .keySet()
//        .map(name -> new Realm(name));
//
//    Map<String, List<Attribute>> attributestGroupByRealm = Stream.ofAll(data.findAllAttributess())
//       .groupBy(attr -> attr.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<Validation>>  validationsGroupByRealm = Stream.ofAll(data.findAllValidations())
//       .groupBy(vals -> vals.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<QBaseMSGMessageTemplate>> messagesGroupByRealm = Stream.ofAll(data.findAllMessages())
//       .groupBy(msgs -> msgs.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<QuestionQuestion>> questionQuestionGroupByRealm = Stream.ofAll(data.findAllQuestionQuestions())
//       .groupBy(queQues -> queQues.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<Ask>> askGroupByRealm = Stream.ofAll(data.findAllAsks())
//       .groupBy(asks -> asks.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<EntityEntity>> entityEntityGroupByRealm = Stream.ofAll(data.findAllEntityEntities())
//       .groupBy(entEnts -> entEnts.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<EntityAttribute>> entityAttributeGroupByRealm = Stream.ofAll(data.findAllEntityAttributes())
//       .groupBy(entAttrs -> entAttrs.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//    
//    Map<String, List<Question>> questionsGroupByRealm = Stream.ofAll(data.findAllQuestions())
//       .groupBy(ques -> ques.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//
//    List<Realm> r = realms.toStream().map(realm ->{
//     realm.setBaseEntitys(baseEntitysGroupByRealm.get(realm.getName()).get());
//     realm.setAttributes(attributestGroupByRealm.get(realm.getName()).get());
//     realm.setEntityEntitys(entityEntityGroupByRealm.get(realm.getName()).get());
//     try {
//        realm.setAsks(askGroupByRealm.get(realm.getName()).get());
//     }catch(NoSuchElementException e) {
//       //System.out.println("no exist");
//     }
//     try {
//        realm.setMessages(messagesGroupByRealm.get(realm.getName()).get());
//     }catch(NoSuchElementException e) {
//       //System.out.println("no exist");
//     }
//     try {
//        realm.setQuestionQuestions(questionQuestionGroupByRealm.get(realm.getName()).get());
//     }catch(NoSuchElementException e) {
//       //System.out.println("no exist");
//     }
//     realm.setValidations(validationsGroupByRealm.get(realm.getName()).get());
//     realm.setEntityAttributes(entityAttributeGroupByRealm.get(realm.getName()).get());
//     realm.setQuestions(questionsGroupByRealm.get(realm.getName()).get());
//
//     return realm;
//    }).collect(Collectors.toList());
//      
//    return r;
//  }
//
//
}
