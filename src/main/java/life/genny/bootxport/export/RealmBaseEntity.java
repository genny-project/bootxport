package life.genny.bootxport.export;

import java.util.List;
import io.vavr.collection.Seq;
import life.genny.qwanda.entity.BaseEntity;

public class RealmBaseEntity {
  
  public final String be = "BaseEntity";
  
  public final String[] baseEntityH = new String[] {
      "name", 
      "code",
      };

  public Seq<Realm<BaseEntity>> getBERealm(){
    List<BaseEntity> allBaseEntitys = QwandaTables.findAllBaseEntitys();
    Seq<Realm<BaseEntity>> convertToQwandaWrapper = QwandaTables.convertToQwandaWrapper(allBaseEntitys);
    return convertToQwandaWrapper;
  }
  
}
