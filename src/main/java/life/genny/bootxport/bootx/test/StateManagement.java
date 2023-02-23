package life.genny.bootxport.bootx.test;

import java.util.List;
import java.util.stream.Collectors;

import life.genny.bootxport.bootx.Realm;
import life.genny.bootxport.bootx.RealmUnit;
import life.genny.bootxport.bootx.SheetState;
import life.genny.bootxport.bootx.StateModel;

public class StateManagement {
    private StateManagement() {
    }

    static Realm realm;

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

    public static List<RealmUnit> getDeletedRowsFromRealmUnits() {
        List<RealmUnit> updatedRealms = findEnabledRealm().stream()
                .map(SheetState::getDeletedRowsFromRealms)
                .collect(Collectors.toList());
        syncWithLatest();
        SheetState.setRealmUnitState();
        return updatedRealms;
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
                .filter(d -> !d.getDisable())
                .map(RealmUnit::getCode)
                .collect(Collectors.toList());
    }
}
