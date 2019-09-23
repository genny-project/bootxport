package life.genny.bootxport.bootx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportService {

  private XlsxImport xlsxImport;

  public ImportService(XlsxImport xlsxImport) {
    this.xlsxImport = xlsxImport;
  }

  public List<RealmUnit> fetchRealmUnit(String sheetURI) {
    return xlsxImport
        .mappingRawToHeaderAndValuesFmt(sheetURI, "Projects")
        .stream()
        .filter(rawData -> !rawData.isEmpty())
        .map(rawData -> {
          RealmUnit realmUnit = new RealmUnit(xlsxImport,rawData.get("name"), rawData);
          return realmUnit;
         })
        .collect(Collectors.toList());
  }

  public List<ModuleUnit> fetchModuleUnit(String sheetURI) {
    return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "Modules")
        .stream()
        .filter(rawData -> !rawData.isEmpty())
        .map(d1 -> {
          ModuleUnit moduleUnit = new ModuleUnit(xlsxImport, d1.get("sheetID".toLowerCase()));
          moduleUnit.setName(d1.get("name"));
          return moduleUnit;
          })
        .collect(Collectors.toList());
  }

  public Map<String,Map<String,String>> fetchBaseEntity(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "BaseEntity", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchAttribute(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI,"Attribute", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchAttributeLink(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "AttributeLink", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchQuestionQuestion(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "QuestionQuestion",DataKeyColumn.CODE_TARGET_PARENT);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchValidation(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "Validation", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchDataType(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "DataType", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchQuestion(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI,"Question", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchAsk(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "Ask", DataKeyColumn.CODE_QUESTION_SOURCE_TARGET);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchNotifications(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "Notifications", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchMessages(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "Messages", DataKeyColumn.CODE);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchEntityAttribute(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI,"EntityAttribute", DataKeyColumn.CODE_BA);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }

  public Map<String,Map<String,String>> fetchEntityEntity(String sheetURI) {
    try {
      return xlsxImport.mappingRawToHeaderAndValuesFmt(sheetURI, "EntityEntity", DataKeyColumn.CODE_TARGET_PARENT_LINK);
    } catch (Exception e1) {
      return new HashMap<>();
    }
  }
}
