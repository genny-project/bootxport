package life.genny.bootxport.bootx;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class StateModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Set<String> sheetIDWorksheetConcatenated;
    private List<String> realms;

    public Set<String> getSheetIDWorksheetConcatenated() {
        return sheetIDWorksheetConcatenated;
    }

    public List<String> getRealms() {
        return realms;
    }

    public void setRealms(List<String> realms) {
        this.realms = realms;
    }

    public void setSheetIDWorksheetConcatenated(Set<String> sheetIDWorksheetConcatenated) {
        this.sheetIDWorksheetConcatenated = sheetIDWorksheetConcatenated;
    }

    @Override
    public String toString() {
        return "StateModel [sheetIDWorksheetConcatenated=" + sheetIDWorksheetConcatenated + ", realms=" + realms + "]";
    }
}
