package life.genny.bootxport.bootx;

public class Realm extends SheetReferralType<RealmUnit>{
  
  
  public Realm(XlsxImport xlsxImport, String sheetURI) {
    super(xlsxImport,sheetURI);
  }

  @Override
  public void init() {
     setDataUnits(getService().fetchRealmUnit(sheetURI));
  }

}
