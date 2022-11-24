package life.genny.bootxport.export;

import io.vavr.collection.Seq;
// import life.genny.qwanda.QuestionQuestion;

public class RealmQuestionQuestion {

  public static String questionQuestion = "QuestionQuestion";

  public static String[] questionQuestionH = new String[] {
      "pk.sourceCode", "pk.targetCode", "weight", "mandatory",};

  public Seq<Realm<QuestionQuestion>> getQueQueRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllQuestionQuestions());
  }

}
