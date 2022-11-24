package life.genny.bootxport.export;

import java.util.Map;
// import life.genny.qwanda.CoreEntity;
// import life.genny.qwanda.QuestionQuestion;
// import life.genny.qwanda.attribute.EntityAttribute;
// import life.genny.qwanda.entity.EntityEntity;
// import life.genny.qwanda.message.QBaseMSGMessageTemplate;
// import life.genny.qwanda.validation.Validation;

public class QwandaWrapper<T> {
    
    T object ;

    String realm;

    String getRealm() {
      return realm;
    };

    QwandaWrapper(T l){
      if(l instanceof CoreEntity) {
        realm = ((CoreEntity)l).getRealm();
      }
      else if(l instanceof EntityEntity) {
        realm = ((EntityEntity)l).getRealm();
        if(realm == null)
          realm = "genny";
      }
      else if(l instanceof Map) {
        realm = ((Map<?,?>)l).get("realm") != null ?  ((Map<?,?>)l).get("realm").toString() : "non-realm" ;
      }
      else if(l instanceof EntityAttribute) {
        realm = ((EntityAttribute)l).getRealm();
        if(realm == null)
          realm = "genny";
      }
      else if(l instanceof QuestionQuestion) {
        realm = ((QuestionQuestion)l).getRealm();
        if(realm == null)
          realm = "genny";
      }
      else if(l instanceof QBaseMSGMessageTemplate) {
        realm = ((QBaseMSGMessageTemplate)l).getRealm();
        if(realm == null)
          realm = "genny";
      }
      else if(l instanceof QBaseMSGMessageTemplate) {
        realm = ((QBaseMSGMessageTemplate)l).getRealm();
        if(realm == null)
          realm = "genny";
      }
      else if(l instanceof Validation) {
        realm = ((Validation)l).getRealm();
        if(realm == null)
          realm = "genny";
      }
      object = l;
    }
  }
