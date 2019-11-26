package life.genny.bootxport.export;

import io.vavr.collection.Seq;
import life.genny.qwanda.attribute.EntityAttribute;

public class RealmEntityAttribute {

  public static String entAttr = "EntityAttribute";

  public static String[] entityAttributeH = new String[] {
      "baseEntityCode", "attributeCode", "inferred", "privacyFlag",
      "readonly", "valueBoolean", "valueDate", "valueDateRange",
      "valueDateTime", "valueDouble", "valueInteger", "valueLong",
      "valueMoney", "valueString", "valueTime", "weight",};

  public Seq<Realm<EntityAttribute>> getEntAttrRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllEntityAttributes());
  }

}
