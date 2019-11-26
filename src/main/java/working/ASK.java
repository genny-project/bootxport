package working;

import io.vavr.collection.Seq;
import life.genny.qwanda.Ask;

public class ASK {

  public static String ask = "Ask";

  public static String[] askH = new String[] {"questionCode", "name",
      "sourceCode", "targetCode", "attributeCode", "weight",
      "readonly", "oneshot", "mandatory", "hidden", "disabled",};
  public Seq<R<Ask>> getAskRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllAsks());
  }

}
