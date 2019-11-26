package life.genny.bootxport.export;

import io.vavr.collection.Seq;
import life.genny.qwanda.validation.Validation;

public class Val {

  public static String val = "Validation";

  public static String[] validationH =
      new String[] {"name", "realm", "code", "multiAllowed",
          "recursiveGroup", "regex", "selectionBaseEntityGroupList",};

  public Seq<Realm<Validation>> getValRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllValidations());
  }

}
