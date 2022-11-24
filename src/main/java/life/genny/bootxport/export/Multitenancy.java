package life.genny.bootxport.export;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import io.vavr.collection.Seq;
// import life.genny.qwanda.Ask;
// import life.genny.qwanda.Question;
// import life.genny.qwanda.QuestionQuestion;
// import life.genny.qwanda.attribute.EntityAttribute;
// import life.genny.qwanda.entity.BaseEntity;
// import life.genny.qwanda.entity.EntityEntity;
// import life.genny.qwanda.message.QBaseMSGMessageTemplate;
// import life.genny.qwanda.validation.Validation;

public class Multitenancy {
  
  public RealmBaseEntity be = new RealmBaseEntity();
  public Val val = new Val();
  public RealmAttribute attr = new RealmAttribute();
  public RealmEntityEntity entEnt = new RealmEntityEntity();
  public RealmAsk ask = new RealmAsk();
  public RealmEntityAttribute entAttr = new RealmEntityAttribute();
  public RealmQuestion que = new RealmQuestion();
  public RealmQuestionQuestion queQue = new RealmQuestionQuestion();
  public RealmMessage mess = new RealmMessage();

  public Seq<Realm<BaseEntity>> beRealm ;
  public Seq<Realm<Validation>> valRealm ;
  public Seq<Realm<Map<String, String>>> attrRealm;
  public Seq<Realm<Map<String, String>>> dataTypeRealm;
  public Seq<Realm<EntityEntity>> entEntRealm ;
  public Seq<Realm<Ask>> askRealm;
  public Seq<Realm<EntityAttribute>> entAttrRealm ;
  public Seq<Realm<Question>> questionRealm;
  public Seq<Realm<QuestionQuestion>> queQueRealm;
  public Seq<Realm<QBaseMSGMessageTemplate>> messageRealm;
  public Set<String> collect;

  public void all() {


    beRealm = be.getBERealm();
   
    valRealm = val.getValRealm();
    attrRealm = attr.getAttrRealm();
    dataTypeRealm = attr.getDataTypeRealm();
    entEntRealm = entEnt.getEntEntRealm();
    askRealm = ask.getAskRealm();
    entAttrRealm = entAttr.getEntAttrRealm();
    questionRealm = que.getQuestionRealm();
    queQueRealm = queQue.getQueQueRealm();
    messageRealm = mess.getMessageRealm();
    
    collect = beRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect2 = valRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect3 = attrRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect4 = dataTypeRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect5 = entEntRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect6 = askRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect7 = entAttrRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect8 = questionRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect9 = queQueRealm.map(d -> d.name).collect(Collectors.toSet());
    Set<String> collect10 = messageRealm.map(d -> d.name).collect(Collectors.toSet());
    
    collect.addAll(collect2);
    collect.addAll(collect3);
    collect.addAll(collect4);
    collect.addAll(collect5);
    collect.addAll(collect6);
    collect.addAll(collect7);
    collect.addAll(collect8);
    collect.addAll(collect9);
    collect.addAll(collect10);
  }
  
}
