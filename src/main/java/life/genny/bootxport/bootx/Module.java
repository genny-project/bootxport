package life.genny.bootxport.bootx;

public class Module extends SheetReferralType<ModuleUnit>{


  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Module(XlsxImport xlsxImport,String sheetURI) {
    super(xlsxImport,sheetURI);
  }

  @Override
  public void init() {
    setDataUnits(getService().fetchModuleUnit(sheetURI));
  }

}
