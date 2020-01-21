package life.genny.bootxport.bootx;

public class ModuleUnit extends DataUnit{
  
  private ImportService service;
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ModuleUnit(BatchLoadMode mode,XlsxImport xlsxImport, String sheetURI) { 
    
    this.service = new ImportService(mode, SheetState.getState());
    this.baseEntitys = service.fetchBaseEntity(sheetURI);
    this.attributes = service.fetchAttribute(sheetURI);
    this.attributeLinks = service.fetchAttributeLink(sheetURI);
    this.questionQuestions = service.fetchQuestionQuestion(sheetURI);
    this.validations = service.fetchValidation(sheetURI);
    this.dataTypes = service.fetchDataType(sheetURI);
    this.questions = service.fetchQuestion(sheetURI);
    this.asks = service.fetchAsk(sheetURI);
//    this.notifications = service.fetchNotifications(sheetURI);
    this.messages = service.fetchMessages(sheetURI);
    this.entityAttributes = service.fetchEntityAttribute(sheetURI);
    this.entityEntitys = service.fetchEntityEntity(sheetURI);
    
  }

 

}
