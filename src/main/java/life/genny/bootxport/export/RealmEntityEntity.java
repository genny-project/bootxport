package life.genny.bootxport.export;

import io.vavr.collection.Seq;
// import life.genny.qwanda.entity.EntityEntity;

public class RealmEntityEntity {

  public static String entEnt = "EntityEntity";
 
  public static String[] entityEntityH = new String[] {
      "link.targetCode", "link.sourceCode", "pk.attribute.code",
      "link.childColor", "link.linkValue", "link.parentColor",
      "link.rule", "link.weight", "valueBoolean", "valueDate",
      "valueDateTime", "valueDouble", "valueInteger", "valueLong",
      "valueMoney", "valueString", "valueTime", "version", "weight",};

  public Seq<Realm<EntityEntity>> getEntEntRealm(){
    return QwandaTables.convertToQwandaWrapper(QwandaTables.findAllEntityEntities());
  }

}
