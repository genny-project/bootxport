package working;

import java.util.List;
import io.vavr.collection.Seq;
import life.genny.qwanda.entity.BaseEntity;

public class BE {
  
  public final String be = "BaseEntity";
  
  public final String[] baseEntityH = new String[] {
      "name", 
      "code",
      };

  public Seq<R<BaseEntity>> getBERealm(){
    List<BaseEntity> allBaseEntitys = QwandaTables.findAllBaseEntitys();
    Seq<R<BaseEntity>> convertToQwandaWrapper = QwandaTables.convertToQwandaWrapper(allBaseEntitys);
    return convertToQwandaWrapper;
  }
  
}
