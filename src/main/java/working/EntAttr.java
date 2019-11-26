package working;

import io.vavr.collection.Seq;
import life.genny.qwanda.attribute.EntityAttribute;

public class EntAttr {

  public static String entAttr = "EntityAttribute";

  public static String[] entityAttributeH = new String[] {
      "baseEntityCode", "attributeCode", "inferred", "privacyFlag",
      "readonly", "valueBoolean", "valueDate", "valueDateRange",
      "valueDateTime", "valueDouble", "valueInteger", "valueLong",
      "valueMoney", "valueString", "valueTime", "weight",};

  public Seq<R<EntityAttribute>> getEntAttrRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllEntityAttributes());
  }

}
