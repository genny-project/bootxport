package life.genny.bootxport.importation;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import life.genny.bootxport.data.QwandaRepository;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;
import life.genny.qwandautils.GennySettings;

public class BatchLoading {
  private QwandaRepository service;

  private String mainRealm = GennySettings.mainrealm;
  private static final String VALIDATIONS = "validations";
  private static boolean isSynchronise;

  protected static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(
          MethodHandles.lookup().lookupClass().getCanonicalName());

  public void validations(Map<String, Map<String,String>> project) {
    Optional<Object> ifValidationsNull =
        Optional.ofNullable(project.get(VALIDATIONS));

    if (!ifValidationsNull.isPresent()) {
      return;
    }

    ValidatorFactory factory =
        javax.validation.Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    project.entrySet()
        .stream().forEach(data -> {
          Map<String, String> validations = data.getValue();
          String regex = null;

          regex = (String) validations.get("regex");
          if (regex != null) {
            regex = regex.replaceAll("^\"|\"$", "");
          }
          String code = ((String) validations.get("code"))
              .replaceAll("^\"|\"$", "");;
          if ("VLD_AU_DRIVER_LICENCE_NO".equalsIgnoreCase(code)) {
            log.info("detected VLD_AU_DRIVER_LICENCE_NO");
          }
          String name = ((String) validations.get("name"))
              .replaceAll("^\"|\"$", "");;
          String recursiveStr = (String) validations.get("recursive");
          String multiAllowedStr =
              (String) validations.get("multi_allowed");
          String groupCodesStr =
              (String) validations.get("group_codes");
          Boolean recursive = getBooleanFromString(recursiveStr);
          Boolean multiAllowed =
              getBooleanFromString(multiAllowedStr);

          Validation val = null;

          if (code.startsWith(
              Validation.getDefaultCodePrefix() + "SELECT_")) {
            val = new Validation(code, name, groupCodesStr, recursive,
                multiAllowed);
          } else {
            val = new Validation(code, name, regex);

          }


          // val.setRealm(this.mainRealm);
          val.setRealm((String) validations.get("realm"));
          log.info(validations.get("realm") + "code " + code
              + ",name:" + name + ",val:" + val + ", grp="


              + (groupCodesStr != null ? groupCodesStr : "X"));

          Set<ConstraintViolation<Validation>> constraints =
              validator.validate(val);
          for (ConstraintViolation<Validation> constraint : constraints) {
            log.error(constraint.getPropertyPath() + " "
                + constraint.getMessage());
          }
          if (constraints.isEmpty()) {
            service.upsert(val);
          }
        });
  }

  private Boolean getBooleanFromString(final String booleanString) {
    if (booleanString == null) {
      return false;
    }

    if ("TRUE".equalsIgnoreCase(booleanString.toUpperCase())
        || "YES".equalsIgnoreCase(booleanString.toUpperCase())
        || "T".equalsIgnoreCase(booleanString.toUpperCase())
        || "Y".equalsIgnoreCase(booleanString.toUpperCase())
        || "1".equalsIgnoreCase(booleanString)) {
      return true;
    }
    return false;

  }

  public void attributes(Map<String, Map<String,String>> project,
      Map<String, DataType> dataTypeMap) {
    if (project.get("attributes") == null) {
      return;
    }
    ValidatorFactory factory =
        javax.validation.Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    project.entrySet()
        .stream().forEach(data -> {
          try {
            Map<String, String> attributes = data.getValue();
            String code = ((String) attributes.get("code"))
                .replaceAll("^\"|\"$", "");;
            String dataType = null;
            try {
              dataType = ((String) attributes.get("dataType"))
                  .replaceAll("^\"|\"$", "");;
            } catch (NullPointerException npe) {
              log.error("DataType for " + code + " cannot be null");
              throw new Exception(
                  "Bad DataType given for code " + code);
            }
            String name = ((String) attributes.get("name"))
                .replaceAll("^\"|\"$", "");;
            DataType dataTypeRecord = dataTypeMap.get(dataType);
            String privacyStr = (String) attributes.get("privacy");
            if (privacyStr != null) {
              privacyStr = privacyStr.toUpperCase();
            }
            Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);
            if (privacy) {
              log.info(attributes.get("realm") + "Attribute " + code
                  + " has default privacy");
            }
            String descriptionStr =
                (String) attributes.get("description");
            String helpStr = (String) attributes.get("help");
            String placeholderStr =
                (String) attributes.get("placeholder");
            String defaultValueStr =
                (String) attributes.get("defaultValue");
            Attribute attr =
                new Attribute(code, name, dataTypeRecord);
            attr.setDefaultPrivacyFlag(privacy);
            attr.setDescription(descriptionStr);
            attr.setHelp(helpStr);
            attr.setPlaceholder(placeholderStr);
            attr.setDefaultValue(defaultValueStr);
            attr.setRealm((String) attributes.get("realm"));
            // attr.setRealm(mainRealm);
            Set<ConstraintViolation<Attribute>> constraints =
                validator.validate(attr);
            for (ConstraintViolation<Attribute> constraint : constraints) {
              log.info(constraint.getPropertyPath() + " "
                  + constraint.getMessage());
            }
            if (constraints.isEmpty()) {
              service.upsert(attr);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

  public Map<String, DataType> dataType(Map<String, Map<String,String>> project) {
    if (project.get("dataType") == null) {
      return null;
    }
    final Map<String, DataType> dataTypeMap = new HashMap<>();
    project.entrySet()
        .stream().forEach(data -> {
          Map<String, String> dataType = data.getValue();
          String validations = (String) dataType.get("validations");
          String code = ((String) dataType.get("code"))
              .replaceAll("^\"|\"$", "");;
          String name = ((String) dataType.get("name"))
              .replaceAll("^\"|\"$", "");;
          String inputmask = (String) dataType.get("inputmask");
          final ValidationList validationList = new ValidationList();
          validationList
              .setValidationList(new ArrayList<Validation>());
          if (validations != null) {
            final String[] validationListStr = validations.split(",");
            for (final String validationCode : validationListStr) {
              try {
                Validation validation =
                    service.findValidationByCode(validationCode);
                validationList.getValidationList().add(validation);
              } catch (NoResultException e) {
                log.error(
                    "Could not load Validation " + validationCode);
              }
            }
          }
          if (!dataTypeMap.containsKey(code)) {
            final DataType dataTypeRecord =
                new DataType(name, validationList, name, inputmask);
            dataTypeMap.put(code, dataTypeRecord);
          }
        });
    return dataTypeMap;
  }

  public void baseEntitys(Map<String, Map<String,String>> project) {
    if (project.get("baseEntitys") == null) {
      return;
    }
    ValidatorFactory factory =
        javax.validation.Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    project.entrySet()
        .stream().forEach(data -> {
          Map<String, String> baseEntitys = data.getValue();
          String code = ((String) baseEntitys.get("code"))
              .replaceAll("^\"|\"$", "");;
          String name = getNameFromMap(baseEntitys, "name", code);
          BaseEntity be = new BaseEntity(code, name);


          // be.setRealm(mainRealm);
          be.setRealm((String) baseEntitys.get("realm"));


          Set<ConstraintViolation<BaseEntity>> constraints =
              validator.validate(be);
          for (ConstraintViolation<BaseEntity> constraint : constraints) {
            log.info(constraint.getPropertyPath() + " "
                + constraint.getMessage());
          }
          if ("SEL_OCCUPATION_SALES".equals(code)) {
            log.info("SEL_OCCUPATION_SALES");
          }
          if (constraints.isEmpty()) {
            service.upsert(be);
          }
        });
  }

  private String getNameFromMap(Map<String, String> baseEntitys,
      String key, String defaultString) {
    String ret = defaultString;
    if (baseEntitys.containsKey(key)) {
      if (baseEntitys.get("name") != null) {
        ret = ((String) baseEntitys.get("name")).replaceAll("^\"|\"$",
            "");;
      }
    }
    return ret;
  }

  public void baseEntityAttributes(Map<String, Map<String,String>> project) {
    if (project.get("attibutesEntity") == null) {
      return;
    }
    project
        .entrySet().stream().forEach(data -> {
          Map<String, String> baseEntityAttr = data.getValue();
          String attributeCode = null;
          try {
            attributeCode =
                ((String) baseEntityAttr.get("attributeCode"))
                    .replaceAll("^\"|\"$", "");
          } catch (Exception e2) {
            log.error(
                "AttributeCode not found [" + baseEntityAttr + "]");
          }
          String valueString =
              (String) baseEntityAttr.get("valueString");
          if (valueString != null) {
            valueString = valueString.replaceAll("^\"|\"$", "");
          }
          String baseEntityCode = null;

          try {
            baseEntityCode =
                ((String) baseEntityAttr.get("baseEntityCode"))
                    .replaceAll("^\"|\"$", "");
            String weight = (String) baseEntityAttr.get("weight");
            String privacyStr =
                (String) baseEntityAttr.get("privacy");
            Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);
            Attribute attribute = null;
            BaseEntity be = null;
            try {
              attribute = service.findAttributeByCode(attributeCode);
              if (attribute == null) {
                log.error("BASE ENTITY CODE: " + baseEntityCode);
                log.error(attributeCode
                    + " is not in the Attribute Table!!!");
              } else {
                be = service.findBaseEntityByCode(baseEntityCode);
                Double weightField = null;
                try {
                  weightField = Double.valueOf(weight);
                } catch (java.lang.NumberFormatException ee) {
                  weightField = 0.0;
                }
                try {
                  EntityAttribute ea = be.addAttribute(attribute,
                      weightField, valueString);
                  if (privacy || attribute.getDefaultPrivacyFlag()) {
                    ea.setPrivacyFlag(true);
                  }
                } catch (final BadDataException e) {
                  e.printStackTrace();
                }
                // be.setRealm(mainRealm);
                be.setRealm((String) baseEntityAttr.get("realm"));
                service.updateWithAttributes(be);
              }
            } catch (final NoResultException e) {
            }

          } catch (Exception e1) {
            String beCode = "BAD BE CODE";
            if (baseEntityAttr != null) {
              beCode = (String) baseEntityAttr.get("baseEntityCode");
            }
            log.error(
                "Error in getting baseEntityAttr  for AttributeCode "
                    + attributeCode + " and beCode=" + beCode);
          }

        });
  }

  public void entityEntitys(Map<String, Map<String,String>> project) {
    if (project.get("basebase") == null) {
      return;
    }
    project.entrySet()
        .stream().forEach(data -> {
          Map<String, String> entEnts = data.getValue();
          String linkCode = (String) entEnts.get("linkCode");
          String parentCode = (String) entEnts.get("parentCode");
          String targetCode = (String) entEnts.get("targetCode");
          String weightStr = (String) entEnts.get("weight");
          String valueString = (String) entEnts.get("valueString");
          final Double weight = Double.valueOf(weightStr);
          BaseEntity sbe = null;
          BaseEntity tbe = null;
          Attribute linkAttribute =
              service.findAttributeByCode(linkCode);
          try {
            sbe = service.findBaseEntityByCode(parentCode);
            tbe = service.findBaseEntityByCode(targetCode);
            if (isSynchronise) {
              try {
                EntityEntity ee = service.findEntityEntity(parentCode,
                    targetCode, linkCode);
                ee.setWeight(weight);
                ee.setValueString(valueString);
                service.updateEntityEntity(ee);
              } catch (final NoResultException e) {
                EntityEntity ee =
                    new EntityEntity(sbe, tbe, linkAttribute, weight);
                ee.setValueString(valueString);
                service.insertEntityEntity(ee);
              }
              return;
            }
            sbe.addTarget(tbe, linkAttribute, weight, valueString);
            service.updateWithAttributes(sbe);
          } catch (final NoResultException e) {
            log.warn("CODE NOT PRESENT IN LINKING: " + parentCode
                + " : " + targetCode + " : " + linkAttribute);
          } catch (final BadDataException e) {
            e.printStackTrace();
          } catch (final NullPointerException e) {
            e.printStackTrace();
          }
        });
  }

  public void questionQuestions(Map<String, Map<String,String>> project) {
    if (project.get("questionQuestions") == null) {
      return;
    }
    project
        .entrySet().stream().forEach(data -> {
          Map<String, String> queQues = data.getValue();
          String parentCode = (String) queQues.get("parentCode");
          String targetCode = (String) queQues.get("targetCode");
          String weightStr = (String) queQues.get("weight");
          String mandatoryStr = (String) queQues.get("mandatory");
          String readonlyStr = (String) queQues.get("readonly");
          Boolean readonly = readonlyStr == null ? false
              : "TRUE".equalsIgnoreCase(readonlyStr);

          Double weight = 0.0;
          try {
            weight = Double.valueOf(weightStr);
          } catch (NumberFormatException e1) {
            weight = 0.0;
          }
          Boolean mandatory = "TRUE".equalsIgnoreCase(mandatoryStr);

          Question sbe = null;
          Question tbe = null;

          try {
            sbe = service.findQuestionByCode(parentCode);
            tbe = service.findQuestionByCode(targetCode);
            try {
              String oneshotStr = (String) queQues.get("oneshot");
              Boolean oneshot = false;
              if (oneshotStr == null) {
                // Set the oneshot to be that of the targetquestion
                oneshot = tbe.getOneshot();
              } else {
                oneshot = "TRUE".equalsIgnoreCase(oneshotStr);
              }


              QuestionQuestion qq = sbe
                  .addChildQuestion(tbe.getCode(), weight, mandatory);
              qq.setOneshot(oneshot);
              qq.setReadonly(readonly);

              // qq.setRealm(mainRealm);
              qq.setRealm((String) queQues.get("realm"));

              QuestionQuestion existing = null;
              try {
                existing = service.findQuestionQuestionByCode(
                    parentCode, targetCode);
                if (existing == null) {
                  qq = service.upsert(qq);
                } else {
                  service.upsert(qq);
                }
              } catch (NoResultException e1) {
                qq = service.upsert(qq);
              } catch (Exception e) {
                existing.setMandatory(qq.getMandatory());
                existing.setOneshot(qq.getOneshot());
                existing.setWeight(qq.getWeight());
                existing.setReadonly(qq.getReadonly());

                // existing.setRealm(mainRealm);
                existing.setRealm((String) queQues.get("realm"));

                qq = service.upsert(existing);
              }

            } catch (NullPointerException e) {
              log.error("Cannot find QuestionQuestion targetCode:"
                  + targetCode + ":parentCode:" + parentCode);



            }
          } catch (final BadDataException e) {
            e.printStackTrace();
          }
        });
  }

  public void attributeLinks(Map<String, Map<String,String>> project,
      Map<String, DataType> dataTypeMap) {
    project
        .entrySet().stream().forEach(data -> {
          Map<String, String> attributeLink = data.getValue();

          String code = ((String) attributeLink.get("code"))
              .replaceAll("^\"|\"$", "");;
          String dataType = null;
          AttributeLink linkAttribute = null;

          try {
            dataType = ((String) attributeLink.get("dataType"))
                .replaceAll("^\"|\"$", "");;
            String name = ((String) attributeLink.get("name"))
                .replaceAll("^\"|\"$", "");;
            DataType dataTypeRecord = dataTypeMap.get(dataType);
            String privacyStr = (String) attributeLink.get("privacy");
            Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);

            linkAttribute = new AttributeLink(code, name);
            linkAttribute.setDefaultPrivacyFlag(privacy);
            linkAttribute.setDataType(dataTypeRecord);
            // linkAttribute.setRealm(mainRealm);
            linkAttribute
                .setRealm((String) attributeLink.get("realm"));
            service.upsert(linkAttribute);
          } catch (Exception e) {
            String name = ((String) attributeLink.get("name"))
                .replaceAll("^\"|\"$", "");;
            String privacyStr = (String) attributeLink.get("privacy");
            Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);

            linkAttribute = new AttributeLink(code, name);
            linkAttribute.setDefaultPrivacyFlag(privacy);
            // linkAttribute.setRealm(mainRealm);
            linkAttribute
                .setRealm((String) attributeLink.get("realm"));
          }

          service.upsert(linkAttribute);


        });
  }

  public void questions(Map<String, Map<String,String>> project) {
    project.entrySet()
        .stream().forEach(data -> {
          Map<String, String> questions = data.getValue();
          String code = (String) questions.get("code");
          String name = (String) questions.get("name");
          String attrCode = (String) questions.get("attribute_code");
          String html = (String) questions.get("html");
          String oneshotStr = (String) questions.get("oneshot");
          String readonlyStr = (String) questions.get("readonly");
          String hiddenStr = (String) questions.get("hidden");
          String mandatoryStr = (String) questions.get("mandatory");

          Boolean oneshot = getBooleanFromString(oneshotStr);
          Boolean readonly = getBooleanFromString(readonlyStr);
          Boolean mandatory = getBooleanFromString(mandatoryStr);
          Attribute attr;
          attr = service.findAttributeByCode(attrCode);
          Question q = new Question(code, name, attr);
          q.setOneshot(oneshot);
          q.setHtml(html);
          q.setReadonly(readonly);
          q.setMandatory(mandatory);


          // q.setRealm(mainRealm);
          q.setRealm((String) questions.get("realm"));


          Question existing = service.findQuestionByCode(code);
          if (existing == null) {
            if (isSynchronise()) {
              Question val =
                  service.findQuestionByCode(q.getCode(), mainRealm);
              if (val != null) {


                // val.setRealm(mainRealm);
                val.setRealm((String) questions.get("realm"));


                service.updateRealm(val);
                return;
              }
            }
            service.insert(q);
          } else {
            existing.setName(name);
            existing.setHtml(html);
            existing.setOneshot(oneshot);
            existing.setReadonly(readonly);
            existing.setMandatory(mandatory);
            service.upsert(existing);
          }
        });
  }

  public void asks(Map<String,Map<String,String>> project) {
    project.entrySet()
        .stream().forEach(data -> {
          Map<String, String> asks = data.getValue();
          String attributeCode = (String) asks.get("attributeCode");
          String sourceCode = (String) asks.get("sourceCode");
          String expired = (String) asks.get("expired");
          String refused = (String) asks.get("refused");
          String targetCode = (String) asks.get("targetCode");
          String qCode = (String) asks.get("question_code");
          String name = (String) asks.get("name");
          String expectedId = (String) asks.get("expectedId");
          String weightStr = (String) asks.get("weight");
          String mandatoryStr = (String) asks.get("mandatory");
          String readonlyStr = (String) asks.get("readonly");
          String hiddenStr = (String) asks.get("hidden");
          final Double weight = Double.valueOf(weightStr);
          if ("QUE_USER_SELECT_ROLE".equals(targetCode)) {
            log.info("dummy");
          }
          Boolean mandatory = "TRUE".equalsIgnoreCase(mandatoryStr);
          Boolean readonly = "TRUE".equalsIgnoreCase(readonlyStr);
          Boolean hidden = "TRUE".equalsIgnoreCase(hiddenStr);
          Question question = service.findQuestionByCode(qCode);
          final Ask ask = new Ask(question, sourceCode, targetCode,
              mandatory, weight);
          ask.setName(name);
          ask.setHidden(hidden);
          ask.setReadonly(readonly);

          // ask.setRealm(mainRealm);
          ask.setRealm((String) asks.get("realm"));


          service.insert(ask);
        });
  }

  public static boolean isSynchronise() {
    return isSynchronise;
  }

  public void persistProject(boolean isSynchronise, String table,
      boolean isDelete, Realm rx) {

    validations(rx.getValidation());
    Map<String, DataType> dataTypes = dataType(rx.getDataType());
    attributes(rx.getAttribute(), dataTypes);
    baseEntitys(rx.getBaseEntity());
    baseEntityAttributes(rx.getEntityAttribute());
    attributeLinks(rx.getAttributeLink(), dataTypes);
    entityEntitys(rx.getEntityEntity());
    questions(rx.getQuestion());
    questionQuestions(rx.getQuestionQuestion());
    messageTemplates(rx.getNotifications());

  }

  public void messageTemplates(Map<String, Map<String,String>> project) {

    if (project.get("messages") == null) {
      log.info("project.get(messages) is null");
      return;
    }

    project.entrySet()
        .stream().forEach(data -> {

          log.info("messages, data ::" + data);
          Map<String, String> template = data.getValue();
          String code = (String) template.get("code");
          String name = (String) template.get("name");
          String description = (String) template.get("description");
          String subject = (String) template.get("subject");
          String emailTemplateDocId = (String) template.get("email");
          String smsTemplate = (String) template.get("sms");
          String toastTemplate = (String) template.get("toast");

          final QBaseMSGMessageTemplate templateObj =
              new QBaseMSGMessageTemplate();
          templateObj.setCode(code);
          templateObj.setName(name);
          templateObj.setCreated(LocalDateTime.now());
          templateObj.setDescription(description);
          templateObj.setEmail_templateId(emailTemplateDocId);
          templateObj.setSms_template(smsTemplate);
          templateObj.setSubject(subject);
          templateObj.setToast_template(toastTemplate);

          if (StringUtils.isBlank(name)) {
            log.error("Empty Name");
          } else {
            try {
              QBaseMSGMessageTemplate msg =
                  service.findTemplateByCode(code);
              try {
                if (msg != null) {
                  msg.setName(name);
                  msg.setDescription(description);
                  msg.setEmail_templateId(emailTemplateDocId);
                  msg.setSms_template(smsTemplate);
                  msg.setSubject(subject);
                  msg.setToast_template(toastTemplate);
                  Long id = service.update(msg);
                  log.info("updated message id ::" + id);
                } else {
                  Long id = service.insert(templateObj);
                  log.info("message id ::" + id);
                }

              } catch (Exception e) {
                log.error("Cannot update QDataMSGMessage " + code);
              }
            } catch (NoResultException e1) {
              try {
                if (isSynchronise()) {
                  QBaseMSGMessageTemplate val =
                      service.findTemplateByCode(
                          templateObj.getCode(), "hidden");
                  if (val != null) {
                    val.setRealm("genny");
                    service.updateRealm(val);
                    return;
                  }
                }
                Long id = service.insert(templateObj);
                log.info("message id ::" + id);
              } catch (javax.validation.ConstraintViolationException ce) {
                log.error(
                    "Error in saving message due to constraint issue:"
                        + templateObj + " :"
                        + ce.getLocalizedMessage());
                log.info(
                    "Trying to update realm from hidden to genny");
                templateObj.setRealm("genny");
                service.updateRealm(templateObj);
              }

            } catch (Exception e) {
              log.error("Cannot add MessageTemplate");

            }
          }
        });
  }
}
