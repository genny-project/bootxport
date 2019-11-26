package working;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import io.vavr.collection.Seq;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class All {
  
  public BE be = new BE();
  public Val val = new Val();
  public Attr attr = new Attr();
  public EntEnt entEnt = new EntEnt();
  public ASK ask = new ASK();
  public EntAttr entAttr = new EntAttr();
  public Que que = new Que();
  public QueQue queQue = new QueQue();
  public Mess mess = new Mess();

  public Seq<R<BaseEntity>> beRealm ;
  public Seq<R<Validation>> valRealm ;
  public Seq<R<Map<String, String>>> attrRealm;
  public Seq<R<Map<String, String>>> dataTypeRealm;
  public Seq<R<EntityEntity>> entEntRealm ;
  public Seq<R<Ask>> askRealm;
  public Seq<R<EntityAttribute>> entAttrRealm ;
  public Seq<R<Question>> questionRealm;
  public Seq<R<QuestionQuestion>> queQueRealm;
  public Seq<R<QBaseMSGMessageTemplate>> messageRealm;
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
  

  public static void main(String...str) {
    All all = new All();
    all.all();
  }

}
