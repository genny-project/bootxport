package life.genny.bootxport.bootx;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SheetState {

  private static Map<String, XlsxImport> state = new HashMap<>();
  private static Set<String> updateState = new HashSet<>();
  public static Map<String,RealmUnit> previousRealmUnit = new HashMap<>();
  public static Realm previousRealm;

  public static Realm getPreviousRealm() {
    return previousRealm;
  }

  public static void setPreviousRealm(Realm previousRealm) {
    SheetState.previousRealm = previousRealm;
  }

  public static void setRealmUnitState() {
    previousRealm.getDataUnits().stream().peek(System.out::println).forEach(SheetState::setPreviousRealmUnit);
  }
  
  public static RealmUnit getPreviousRealmUnit(String key) {
    return previousRealmUnit.get(key);
  }

  public static void setPreviousRealmUnit(RealmUnit previousRealm) {
    SheetState.previousRealmUnit.put(previousRealm.getCode(), previousRealm);
  }

  public static Map<String, XlsxImport> getState() {
    return state;
  }

  public static Set<String> getUpdateState() {
    return updateState;
  }

  public static void setUpdateState(Set<String> updateState) {
    SheetState.updateState = updateState;
  }
  public static void removeUpdateState(String key) {
    SheetState.updateState.remove(key);
  }
  

  public static RealmUnit getUpdatedRealms(String realmName) {
    Realm realm = getPreviousRealm();
    realm.init();
    RealmUnit updatedRealm = realm.getDataUnits().stream()
        .filter(d -> d.getCode().equals(realmName.toLowerCase()))
        .map(realmUnit -> {
          RealmUnit previousRealm = SheetState.getPreviousRealmUnit(realmUnit.getCode());
          System.out.println(" Base ********" );
          realmUnit.setBaseEntitys(findUpdateRows(realmUnit.baseEntitys,previousRealm.baseEntitys));
          System.out.println(" Ask ********" );
          realmUnit.setAsks(findUpdateRows(realmUnit.asks,previousRealm.asks));
          System.out.println(" AttributeLink ********" );
          realmUnit.setAttributeLinks(findUpdateRows(realmUnit.attributeLinks,previousRealm.attributeLinks));
          System.out.println(" DataType ********" );
          realmUnit.setDataTypes(findUpdateRows(realmUnit.dataTypes,previousRealm.dataTypes));
          System.out.println(" Attribute ********" );
          realmUnit.setAttributes(findUpdateRows(realmUnit.attributes,previousRealm.attributes));
          System.out.println(" EntityAttr ********" );
          realmUnit.setEntityAttributes(findUpdateRows(realmUnit.entityAttributes,previousRealm.entityAttributes));
          System.out.println(" Val ********" );
          realmUnit.setValidations(findUpdateRows(realmUnit.validations,previousRealm.validations));
          System.out.println(" Question ********" );
          realmUnit.setQuestions(findUpdateRows(realmUnit.questions,previousRealm.questions));
          System.out.println(" QuestionQuestion ********" );
          realmUnit.setQuestionQuestions(findUpdateRows(realmUnit.questionQuestions,previousRealm.questionQuestions));
          System.out.println(" Not ********" );
          realmUnit.setNotifications(findUpdateRows(realmUnit.notifications,previousRealm.notifications));
          System.out.println(" Messag ********" );
          realmUnit.setMessages(findUpdateRows(realmUnit.messages,previousRealm.messages));
          return realmUnit;
    }).findFirst().get();
    return updatedRealm;
  }

  public static Map<String, Map<String, String>> findUpdateRows(
      Map<String, Map<String, String>> newRows,
      Map<String, Map<String, String>> oldRows){

      System.out.println("new rows = " + newRows.size());
      System.out.println("old rows = " + oldRows.size());
      Optional<Map<String, Map<String, String>>> reduce = newRows.entrySet().stream()
        .filter(o ->{
//          if(!oldRows.containsKey(o.getKey()))
//            System.out.println("does not contain key " + o.getKey()+  " "+ oldRows.keySet());
//          if(!oldRows.containsValue(o.getValue()))
//            System.out.println("does not contain value "+o.getValue());
          return !oldRows.containsKey(o.getKey())
          ||
          !oldRows.containsValue(o.getValue());}
          )
        .map(data -> {
          Map<String, Map<String, String>> map = new HashMap<>();
          map.put(data.getKey(),data.getValue());
          return map;
        })
        .reduce((acc,n)->{
          acc.putAll(n);
          return acc;
          });
      return reduce.orElseGet(HashMap::new);
  }
}
