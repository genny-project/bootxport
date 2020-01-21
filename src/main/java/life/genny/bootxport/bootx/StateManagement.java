package life.genny.bootxport.bootx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.gson.Gson;

public class StateManagement {

  private static Realm realm;

  public static void initStateManagement(Realm realm) {
    StateManagement.realm = realm;
    savePreviousRealmUnits();
    syncWithLatest();
  }
  
  public static void setStateModel(StateModel model) {
    SheetState.setUpdateState(model.getSheetIDWorksheetConcatenated());
  }

  public static void syncWithLatest() {
    realm.init();
  }

  public static void updateRealmUnits() {
    SheetState.setRealmUnitState();
  }
  public static void savePreviousRealmUnits() {
    SheetState.setPreviousRealm(realm);
    SheetState.setRealmUnitState();
  }

  public static List<RealmUnit> getUpdatedRealmUnits() {
    List<RealmUnit> updatedRealms = findEnabledRealm().stream()
       .map(SheetState::getUpdatedRealms)
       .collect(Collectors.toList());
    syncWithLatest();
    SheetState.setRealmUnitState();
   return updatedRealms;
  }

  public static List<String> findEnabledRealm() {
    return realm.getDataUnits().stream()
        .filter(d-> !d.getDisable())
        .map(d-> d.getCode())
        .collect(Collectors.toList());
  }

  public static StateModel partOneStateManagement() {
    List<String> realms = new ArrayList<>();
    realms.add("internmatch");

    StateModel model = new StateModel();

    model.setRealms(realms);
    
    String key1 = "17_13b2xCzhiahg9bl5DbYTaneeZnjQkPLYhFdsjJyS0" +"BaseEntity";
    String key2 = "1n60kJeBGY4v084JnhZtAxW-V1dnK9yNzjAs5qnDpd2k" +"Attribute";
    Set<String> upd=new HashSet<>();

    upd.add(key1);
    upd.add(key2);
    
    model.setSheetIDWorksheetConcatenated(upd);
    
    Gson gson = new Gson();
    String json = gson.toJson(model);
    System.out.println(json);
    StateModel fromJson = gson.fromJson(json, StateModel.class);
    System.out.println(fromJson);

    return fromJson;
  }
  
  public static void main(String...str) {
    
//    partOneStateManagement();
  }
}
