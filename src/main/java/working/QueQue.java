package working;

import io.vavr.collection.Seq;
import life.genny.qwanda.QuestionQuestion;

public class QueQue {

  public static String questionQuestion = "QuestionQuestion";

  public static String[] questionQuestionH = new String[] {
      "pk.sourceCode", "pk.targetCode", "weight", "mandatory",};

  public Seq<R<QuestionQuestion>> getQueQueRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllQuestionQuestions());
  }

}
