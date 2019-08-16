package life.genny.bootxport.bootx;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import com.google.common.collect.Maps;

public class RealmUnit extends DataUnit{

  private String code;
  private String name;
  private Module module ;
  private String urlList ;
  private String clientSecret ;
  private String keycloakUrl;
  private Boolean disable;
  private Boolean skipGoogleDoc;
  private String securityKey;
  private String servicePassword;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrlList() {
    return urlList;
  }

  public void setUrlList(String urlList) {
    this.urlList = urlList;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getKeycloakUrl() {
    return keycloakUrl;
  }

  public void setKeycloakUrl(String keycloakUrl) {
    this.keycloakUrl = keycloakUrl;
  }

  public Boolean getDisable() {
    return disable;
  }

  public void setDisable(Boolean disable) {
    this.disable = disable;
  }

  public Boolean getSkipGoogleDoc() {
    return skipGoogleDoc;
  }

  public void setSkipGoogleDoc(Boolean skipGoogleDoc) {
    this.skipGoogleDoc = skipGoogleDoc;
  }

  public String getSecurityKey() {
    return securityKey;
  }

  public void setSecurityKey(String securityKey) {
    this.securityKey = securityKey;
  }

  public String getServicePassword() {
    return servicePassword;
  }

  public void setServicePassword(String servicePassword) {
    this.servicePassword = servicePassword;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String name) {
    this.code = name;
  }


  public Module getModule() {
    return module;
  }

  public void setModule(Module module) {
    this.module = module;
  }

  private BinaryOperator<HashMap<String, Map<String, String>>> overrideByPrecedence
  = (weakModule, strongModule) -> {
    strongModule.entrySet().forEach(data ->{
      if(weakModule.containsKey(data.getKey())) {
        System.out.println("For Module Name: " + code);
        System.out.println(data.getKey() + " This will be overrided ");
      }
    });
    weakModule.putAll(strongModule);
    return weakModule;
  };
  

//  BiFunction<Map<String,String>,String,String> cleanKeys = (map,key) -> map.get(key.toLowerCase().replaceAll("^\"|\"$|_|-", "")); 

  public RealmUnit(XlsxImport xlsxImport,String name, Map<String, String> realm) {
    Optional<String> disabelStr = Optional.ofNullable(realm.get("disable"));
    Boolean disable = disabelStr.map(Boolean::valueOf).orElse(false);
    Optional<String> skipGoogleDocStr = Optional.ofNullable(realm.get("skipGoogleDoc".toLowerCase().replaceAll("^\"|\"$|_|-", "")));
    Boolean skipGoogleDoc = skipGoogleDocStr.map(Boolean::valueOf).orElse(false);

    setCode(realm.get("code"));
    setName(realm.get("name"));
    setUrlList(realm.get("urlList".toLowerCase().replaceAll("^\"|\"$|_|-", "")));
    setDisable(disable);
    setSkipGoogleDoc(skipGoogleDoc);
    setSecurityKey(realm.get("ENV_SECURITY_KEY".toLowerCase().replaceAll("^\"|\"$|_|-", "")));
    setSecurityKey(realm.get("ENV_SERVICE_PASSWORD".toLowerCase().replaceAll("^\"|\"$|_|-", "")));

    if(skipGoogleDoc) {
      System.out.println("Skipping google doc for realm " + this.name);
    }
    else {
        module = new Module(xlsxImport,realm.get("sheetID".toLowerCase()));
      

      super.baseEntitys = module.getDataUnits().stream()
          .map(moduleUnit -> Maps.newHashMap(moduleUnit.baseEntitys))
          .reduce(overrideByPrecedence)
          .get();
      super.attributes = module.getDataUnits().stream()
          .map(moduleUnit -> Maps.newHashMap(moduleUnit.attributes))
          .reduce(overrideByPrecedence)
          .get();
      super.attributeLinks = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.attributeLinks))
          .reduce(overrideByPrecedence)
          .get();
      super.notifications = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.notifications))
          .reduce(overrideByPrecedence)
          .get();
      super.entityEntitys = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.entityEntitys))
          .reduce(overrideByPrecedence)
          .get();
      super.questions = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.questions))
          .reduce(overrideByPrecedence)
          .get();
      super.entityAttributes = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.entityAttributes))
          .reduce(overrideByPrecedence)
          .get();
      super.asks = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.asks))
          .reduce(overrideByPrecedence)
          .get();
      super.questionQuestions = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.questionQuestions))
          .reduce(overrideByPrecedence)
          .get();
      super.validations = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.validations))
          .reduce(overrideByPrecedence)
          .get();
      super.dataTypes = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.dataTypes))
          .reduce(overrideByPrecedence)
          .get();
      super.messages = module.getDataUnits().stream()
          .map(mm -> Maps.newHashMap(mm.messages))
          .reduce(overrideByPrecedence)
          .get();
    }
  }
  
}
