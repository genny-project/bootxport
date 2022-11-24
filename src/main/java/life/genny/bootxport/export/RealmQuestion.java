package life.genny.bootxport.export;

import io.vavr.collection.Seq;
// import life.genny.qwanda.Question;

public class RealmQuestion {

  public static String question = "Question";

  public static String[] questionH =
      new String[] {"code", "name", "attributeCode",};

  public Seq<Realm<Question>> getQuestionRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllQuestions());
  }
}
