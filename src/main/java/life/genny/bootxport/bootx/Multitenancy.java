package life.genny.bootxport.bootx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import life.genny.bootxport.xlsimport.Realm;

public class Multitenancy {
  
  private List<Realm> realms =  new ArrayList<>();
  
  public final String sheetURI;

  private XlsxImport xlsxImport;
  
  public Multitenancy(XlsxImport xlsxImport, String sheetURI) {
   this.xlsxImport = xlsxImport;
   this.sheetURI = sheetURI;
   setRealms();
  }

  public void setRealms() {
    realms = xlsxImport.transformHeaderToValuesFormat(sheetURI, "Projects")
        .stream()
        .map(rawData -> new Realm(rawData.get("name"), rawData))
        .collect(Collectors.toList());
  }

  public List<Realm> getRealms() {
    return realms ; 
  }
  
  
}
