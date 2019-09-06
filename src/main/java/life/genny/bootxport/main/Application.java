package life.genny.bootxport.main;

import java.util.List;
import java.util.stream.Collectors;
//import io.vavr.collection.List;
////import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import life.genny.bootxport.data.Realm;
import life.genny.bootxport.data.ServiceStore;
import life.genny.bootxport.xport.GennyData;
import life.genny.bootxport.xport.Multitenancy;
import life.genny.qwanda.Ask;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class Application {

//  public static void main(String... args) {
//    GennyData data = ServiceStore.getServiceStore().data;
//
//
//
//    Map<String, List<BaseEntity>> baseEntitysGroupByRealm =
//        Stream.ofAll(data.getBaseEntitys())
//           .groupBy(be -> be.getRealm())
//           .bimap(k -> k, v -> v.toJavaList());
//    
//    Set<Realm> realms = baseEntitysGroupByRealm
//        .keySet()
//        .map(name -> new Realm(name));
//
//    Map<String, List<Attribute>> attributestGroupByRealm = Stream.ofAll(data.getAttributess())
//       .groupBy(attr -> attr.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<Validation>>  validationsGroupByRealm = Stream.ofAll(data.getValidations())
//       .groupBy(vals -> vals.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<QBaseMSGMessageTemplate>> messagesGroupByRealm = Stream.ofAll(data.getMessages())
//       .groupBy(msgs -> msgs.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<QuestionQuestion>> questionQuestionGroupByRealm = Stream.ofAll(data.getQuestionQuestions())
//       .groupBy(queQues -> queQues.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<Ask>> askGroupByRealm = Stream.ofAll(data.getAsks())
//       .groupBy(asks -> asks.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    Map<String, List<EntityEntity>> entityEntityGroupByRealm = Stream.ofAll(data.getEntityEntities())
//       .groupBy(entEnts -> entEnts.getRealm())
//       .bimap(k -> k, v -> v.toJavaList());
//
//    
//    Processor pro = new Processor();
//    pro.setAskGroupByRealm(askGroupByRealm);
//    pro.setAttributestGroupByRealm(attributestGroupByRealm);
//    pro.setValidationsGroupByRealm(validationsGroupByRealm);
//    pro.setMessagesGroupByRealm(messagesGroupByRealm);
//    pro.setQuestionQuestionGroupByRealm(questionQuestionGroupByRealm);
//    pro.setBaseEntitysGroupByRealm(baseEntitysGroupByRealm);
//    pro.setEntityEntityGroupByRealm(entityEntityGroupByRealm);
//    
//
//    List<Realm> list = pro.getRealms().toStream().map(realm ->{
//     realm.setBaseEntitys(pro.getBaseEntitysGroupByRealm().get(realm.getName()).get());
//     realm.setAttributes(pro.getAttributestGroupByRealm().get(realm.getName()).get());
//     realm.setAsks(pro.getAskGroupByRealm().get(realm.getName()).get());
//     realm.setEntityEntitys(pro.getEntityEntityGroupByRealm().get(realm.getName()).get());
//     realm.setMessages(pro.getMessagesGroupByRealm().get(realm.getName()).get());
//     realm.setQuestionQuestions(pro.getQuestionQuestionGroupByRealm().get(realm.getName()).get());
//     realm.setValidations(pro.getValidationsGroupByRealm().get(realm.getName()).get());
//     return realm;
//    }).collect(Collectors.toList());
//
//    
//    list.forEach(System.out::println);
//     
//  }
  
}
