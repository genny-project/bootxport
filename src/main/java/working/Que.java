package working;

import io.vavr.collection.Seq;
import life.genny.qwanda.Question;

public class Que {

  public static String question = "Question";

  public static String[] questionH =
      new String[] {"code", "name", "attributeCode",};

  public Seq<R<Question>> getQuestionRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllQuestions());
  }
}
