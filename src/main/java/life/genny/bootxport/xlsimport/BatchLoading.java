package life.genny.bootxport.xlsimport;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import life.genny.bootxport.bootx.QwandaRepository;
import life.genny.bootxport.bootx.RealmUnit;
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
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

class Options {
    public String optionCode = null;
    public String optionLabel = null;
}

public class BatchLoading {
    private QwandaRepository service;

    private String mainRealm = GennySettings.mainrealm;
    private static boolean isSynchronise;

    protected static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    public BatchLoading(QwandaRepository repo) {
        this.service = repo;
    }

    public void validations(Map<String, Map<String, String>> project, String realmName) {
        Gson gsonObject = new Gson();
        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        project.entrySet().stream().forEach(data -> {
            Map<String, String> validations = data.getValue();

            String optionString = validations.get("options");
            boolean needCheckOptions = false;
            boolean hasValidOptions = false;

            if (optionString != null && (!optionString.equals(" "))) {
                needCheckOptions = true;
            }

            if (needCheckOptions) {
                try {
                    gsonObject.fromJson(optionString, Options[].class);
                    log.info("FOUND VALID OPTIONS STRING:" + optionString);
                    hasValidOptions = true;
                } catch (JsonSyntaxException ex) {
                    log.error("FOUND INVALID OPTIONS STRING:" + optionString);
                    throw new JsonSyntaxException(ex.getMessage());
                }
            }

            String regex = null;

            regex = validations.get("regex");

            if (regex != null) {
                regex = regex.replaceAll("^\"|\"$", "");
            }
            String code = (validations.get("code")).replaceAll("^\"|\"$", "");

            if ("VLD_AU_DRIVER_LICENCE_NO".equalsIgnoreCase(code)) {
                log.info("detected VLD_AU_DRIVER_LICENCE_NO");
            }

            String name = (validations.get("name")).replaceAll("^\"|\"$", "");
            String recursiveStr = validations.get("recursive");
            String multiAllowedStr = validations
                    .get("multi_allowed".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String groupCodesStr = validations.get("group_codes".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            Boolean recursive = getBooleanFromString(recursiveStr);
            Boolean multiAllowed = getBooleanFromString(multiAllowedStr);

            Validation val = null;

            if (code.startsWith(Validation.getDefaultCodePrefix() + "SELECT_")) {
                if (hasValidOptions) {
                    log.info("Case 1, build Validation with OPTIONS String");
                    val = new Validation(code, name, groupCodesStr, recursive, multiAllowed, optionString);
                } else {
                    val = new Validation(code, name, groupCodesStr, recursive, multiAllowed);
                }
            } else {
                if (hasValidOptions) {
                    log.info("Case 2, build Validation with OPTIONS String");
                    val = new Validation(code, name, regex, optionString);
                } else {
                    val = new Validation(code, name, regex);
                }
            }

            val.setRealm(realmName);
            log.info(String.format("realm:%s, code:%s, name:%s, val:%s, grp:%s", validations.get("realm"), code, name, val, (groupCodesStr != null ? groupCodesStr : "X")));
            Set<ConstraintViolation<Validation>> constraints = validator.validate(val);
            for (ConstraintViolation<Validation> constraint : constraints) {
                log.error(String.format("%s,%s", constraint.getPropertyPath(), constraint.getMessage()));
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

        return "TRUE".equalsIgnoreCase(booleanString.toUpperCase()) || "YES".equalsIgnoreCase(booleanString.toUpperCase())
                || "T".equalsIgnoreCase(booleanString.toUpperCase())
                || "Y".equalsIgnoreCase(booleanString.toUpperCase()) || "1".equalsIgnoreCase(booleanString);

    }

    public void attributes(Map<String, Map<String, String>> project, Map<String, DataType> dataTypeMap, String realmName) {
        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        project.entrySet().stream().forEach(data -> {
            try {
                Map<String, String> attributes = data.getValue();
                String code = (attributes.get("code")).replaceAll("^\"|\"$", "");
                String dataType = null;
                try {
                    dataType = attributes.get("datatype").trim().replaceAll("^\"|\"$", "");
                } catch (NullPointerException npe) {
                    log.error(String.format("DataType for %s cannot be null.", code));
                    throw new Exception("Bad DataType given for code " + code);
                }
                String name = attributes.get("name").replaceAll("^\"|\"$", "");
                DataType dataTypeRecord = dataTypeMap.get(dataType);
                String privacyStr = attributes.get("privacy");
                if (privacyStr != null) {
                    privacyStr = privacyStr.toUpperCase();
                }

                boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);

                if (privacy) {
                    log.info(String.format("Realm:%s, Attribute:%s has default privacy.", realmName, code));
                }
                String descriptionStr = attributes.get("description");
                String helpStr = attributes.get("help");
                String placeholderStr = attributes.get("placeholder");
                String defaultValueStr = attributes.get("defaultValue".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
                Attribute attr = new Attribute(code, name, dataTypeRecord);
                attr.setDefaultPrivacyFlag(privacy);
                attr.setDescription(descriptionStr);
                attr.setHelp(helpStr);
                attr.setPlaceholder(placeholderStr);
                attr.setDefaultValue(defaultValueStr);
                attr.setRealm(realmName);
                Set<ConstraintViolation<Attribute>> constraints = validator.validate(attr);
                for (ConstraintViolation<Attribute> constraint : constraints) {
                    log.info(String.format("%s, %s.", constraint.getPropertyPath(), constraint.getMessage()));
                }
                if (constraints.isEmpty()) {
                    service.upsert(attr);
                }
            } catch (Exception e) {
                log.error(String.format("Exception:%s", e.getMessage()));
            }
        });
    }


    public Map<String, DataType> dataType(Map<String, Map<String, String>> project) {
        final Map<String, DataType> dataTypeMap = new HashMap<>();
        project.entrySet().stream().filter(d -> !d.getKey().matches("\\s*")).forEach(data -> {
            Map<String, String> dataType = data.getValue();
            String validations = dataType.get("validations");
            String code = (dataType.get("code")).trim().replaceAll("^\"|\"$", "");
//            String className = (dataType.get("classname")).replaceAll("^\"|\"$", "");
            String name = (dataType.get("name")).replaceAll("^\"|\"$", "");
            String inputmask = dataType.get("inputmask");
            String component = dataType.get("component");
            final ValidationList validationList = new ValidationList();
            validationList.setValidationList(new ArrayList<Validation>());
            if (validations != null) {
                final String[] validationListStr = validations.split(",");
                for (final String validationCode : validationListStr) {
                    try {
                        Validation validation = service.findValidationByCode(validationCode);
                        validationList.getValidationList().add(validation);
                    } catch (NoResultException e) {
                        log.error("Could not load Validation " + validationCode);
                    }
                }
            }
            if (!dataTypeMap.containsKey(code)) {
                DataType dataTypeRecord;
                if (component == null) {
                    dataTypeRecord = new DataType(name, validationList, name, inputmask);
                } else {
                    dataTypeRecord = new DataType(name, validationList, name, inputmask, component);
                }
                dataTypeRecord.setDttCode(code);
                dataTypeMap.put(code, dataTypeRecord);
            }
        });
        return dataTypeMap;
    }

    public void baseEntitys(Map<String, Map<String, String>> project, String realmName) {
        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        project.entrySet().stream().forEach(data -> {
            Map<String, String> baseEntitys = data.getValue();
            String code = (baseEntitys.get("code")).replaceAll("^\"|\"$", "");
            String name = getNameFromMap(baseEntitys, "name", code);
            BaseEntity be = new BaseEntity(code, name);

            be.setRealm(realmName);

            Set<ConstraintViolation<BaseEntity>> constraints = validator.validate(be);
            for (ConstraintViolation<BaseEntity> constraint : constraints) {
                log.info(constraint.getPropertyPath() + " " + constraint.getMessage());
            }

            if (constraints.isEmpty()) {
                service.upsert(be);
            }
        });
    }


    private String getNameFromMap(Map<String, String> baseEntitys, String key, String defaultString) {
        String ret = defaultString;
        if (baseEntitys.containsKey(key)) {
            if (baseEntitys.get("name") != null) {
                ret = ((String) baseEntitys.get("name")).replaceAll("^\"|\"$", "");
            }
        }
        return ret;
    }

    public void baseEntityAttributes(Map<String, Map<String, String>> project, String realmName) {

        project.entrySet().stream().forEach(data -> {

            Map<String, String> baseEntityAttr = data.getValue();
            if (baseEntityAttr.get("attributecode").equals("PRI_HASHCODE"))
                System.out.println();
            String attributeCode = null;
            try {
                attributeCode = ((String) baseEntityAttr
                        .get("attributeCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""))).replaceAll("^\"|\"$", "");
            } catch (Exception e2) {
                log.error("AttributeCode not found [" + baseEntityAttr + "]");
            }
            List<String> asList = Arrays.asList("valuestring");
            Optional<String> valueString = asList.stream().map(baseEntityAttr::get).findFirst();
            Integer valueInt = null;
            Optional<String> ofNullable = Optional.ofNullable(baseEntityAttr.get("valueinteger"));
            if (ofNullable.isPresent() && !baseEntityAttr.get("valueinteger").matches("\\s*")) {
                log.info(String.format("valueinteger:%s", baseEntityAttr.get("valueinteger")));
                BigDecimal big = new BigDecimal(baseEntityAttr.get("valueinteger"));
                Optional<String[]> nullableVal = Optional.ofNullable(big.toPlainString().split("[.]"));
                valueInt = nullableVal.filter(d -> d.length > 0).map(d -> Integer.valueOf(d[0])).get();
            }
            String valueStr = null;
            if (valueString.isPresent()) {
                valueStr = valueString.get().replaceAll("^\"|\"$", "");
            }
            String baseEntityCode = null;
            try {
                baseEntityCode = (baseEntityAttr.get("baseEntityCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""))).replaceAll("^\"|\"$", "");
                String weight = baseEntityAttr.get("weight");
                String privacyStr = baseEntityAttr.get("privacy");
                Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);
                Attribute attribute = null;
                BaseEntity be = null;
                try {
                    attribute = service.findAttributeByCode(attributeCode);
                    if (attribute == null) {
                        log.error(String.format("BASE ENTITY CODE:%s, AttributeCode:%s is not in the Attribute Table!!!", baseEntityCode, attributeCode));
                    } else {
                        be = service.findBaseEntityByCode(baseEntityCode);
                        Double weightField = null;
                        try {
                            weightField = Double.valueOf(weight);
                        } catch (java.lang.NumberFormatException ee) {
                            weightField = 0.0;
                        }
                        try {
                            EntityAttribute ea;
                            if (valueInt != null) {
                                ea = be.addAttribute(attribute, weightField, valueInt);
                            } else {
                                ea = be.addAttribute(attribute, weightField, valueStr);
                            }
                            if (privacy || attribute.getDefaultPrivacyFlag()) {
                                ea.setPrivacyFlag(true);
                            }
                        } catch (final BadDataException e) {
                            log.error(String.format("BadDataException:%s", e.getMessage()));
                        }
                        be.setRealm(realmName);
                        service.updateWithAttributes(be);
                    }
                } catch (final NoResultException e) {
                    log.error(String.format("NoResultException:%s", e.getMessage()));
                }

            } catch (Exception e1) {
                String beCode = "BAD BE CODE";
                if (baseEntityAttr != null) {
                    beCode = baseEntityAttr.get("baseEntityCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
                }
                log.error("Error in getting baseEntityAttr  for AttributeCode " + attributeCode + " and beCode="
                        + beCode);
            }

        });
    }


    public void entityEntitys(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(data -> {
            Map<String, String> entEnts = data.getValue();
            String linkCode = entEnts.get("linkCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            if (linkCode == null)
                linkCode = entEnts.get("code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String parentCode = entEnts.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            if (parentCode == null)
                parentCode = entEnts.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String targetCode = entEnts.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String weightStr = entEnts.get("weight");
            String valueString = entEnts.get("valueString".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            Optional<String> weightStrOpt = Optional.ofNullable(weightStr);
            final Double weight = weightStrOpt.filter(d -> !d.equals(" ")).map(Double::valueOf).orElse(0.0);
            BaseEntity sbe = null;
            BaseEntity tbe = null;
            Attribute linkAttribute = service.findAttributeByCode(linkCode);
            try {
                sbe = service.findBaseEntityByCode(parentCode);
                tbe = service.findBaseEntityByCode(targetCode);
                if (isSynchronise) {
                    try {
                        EntityEntity ee = service.findEntityEntity(parentCode, targetCode, linkCode);
                        ee.setWeight(weight);
                        ee.setValueString(valueString);
                        service.updateEntityEntity(ee);
                    } catch (final NoResultException e) {
                        EntityEntity ee = new EntityEntity(sbe, tbe, linkAttribute, weight);
                        ee.setValueString(valueString);
                        service.insertEntityEntity(ee);
                    }
                    return;
                }
                sbe.addTarget(tbe, linkAttribute, weight, valueString);
                service.updateWithAttributes(sbe);
            } catch (final NoResultException e) {
                log.warn(String.format("CODE NOT PRESENT IN LINKING:ParentCode:%s, TargetCode:%s, LinkAttribute:%s", parentCode, targetCode, linkAttribute));
            } catch (final BadDataException e) {
                log.error(String.format("BadDataException:%s", e.getMessage()));
            } catch (final NullPointerException e) {
                log.error(String.format("NullPointerException:%s", e.getMessage()));
            }
        });
    }


    public void questionQuestions(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().forEach(data -> {
            Map<String, String> queQues = data.getValue();
            String parentCode = queQues.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (parentCode == null) {
                parentCode = queQues.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            }
            if (queQues.get("parentcode") != null) {
                if ((queQues.get("parentcode")).startsWith("QUE_NEW_USER_PROFILE")) {
                    log.info("Got to here...");
                }
            }

            String targetCode = queQues.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String weightStr = queQues.get("weight");
            String mandatoryStr = queQues.get("mandatory");
            String readonlyStr = queQues.get("readonly");
            Boolean readonly = readonlyStr != null && "TRUE".equalsIgnoreCase(readonlyStr);
            Boolean formTrigger = (queQues.get("formtrigger")) != null && "TRUE".equalsIgnoreCase(queQues.get("formtrigger"));
            Boolean createOnTrigger = queQues.get("createontrigger") != null && "TRUE".equalsIgnoreCase(queQues.get("createontrigger"));
            double weight = 0.0;

            try {
                weight = Double.parseDouble(weightStr);
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
                    String oneshotStr = queQues.get("oneshot");
                    Boolean oneshot = false;
                    if (oneshotStr == null) {
                        // Set the oneshot to be that of the targetquestion
                        oneshot = tbe.getOneshot();
                    } else {
                        oneshot = "TRUE".equalsIgnoreCase(oneshotStr);
                    }

                    QuestionQuestion qq = sbe.addChildQuestion(tbe.getCode(), weight, mandatory);
                    qq.setOneshot(oneshot);
                    qq.setReadonly(readonly);
                    qq.setCreateOnTrigger(createOnTrigger);
                    qq.setFormTrigger(formTrigger);
                    qq.setRealm(realmName);

                    QuestionQuestion existing = null;
                    try {
                        existing = service.findQuestionQuestionByCode(parentCode, targetCode);
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
                        existing.setCreateOnTrigger(qq.getCreateOnTrigger());
                        existing.setFormTrigger(qq.getFormTrigger());
                        // existing.setRealm(mainRealm);
                        existing.setRealm(queQues.get("realm"));

                        qq = service.upsert(existing);
                    }

                } catch (NullPointerException e) {
                    if (sbe == null) {
                        log.error("Cannot find parentCode:" + parentCode + " from Question.");
                    } else if (tbe == null) {
                        log.error("Cannot find targetCode:" + targetCode + " from Question.");
                    } else {
                        log.error("Exception:" + e.toString());
                    }
                }
            } catch (final BadDataException e) {
                log.error(String.format("BadDataException:%s", e.getMessage()));
            }
        });
    }


    public void attributeLinks(Map<String, Map<String, String>> project, Map<String, DataType> dataTypeMap, String realmName) {
        project.entrySet().stream().forEach(data -> {
            Map<String, String> attributeLink = data.getValue();

            String code = attributeLink.get("code").replaceAll("^\"|\"$", "");
            String dataType = null;
            AttributeLink linkAttribute = null;

            try {
                dataType = attributeLink.get("dataType".toLowerCase().trim().replaceAll("^\"|\"$|_|-", "")).replaceAll("^\"|\"$", "");
                String name = attributeLink.get("name").replaceAll("^\"|\"$", "");
                DataType dataTypeRecord = dataTypeMap.get(dataType);
                String privacyStr = attributeLink.get("privacy");
                Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);

                linkAttribute = new AttributeLink(code, name);
                linkAttribute.setDefaultPrivacyFlag(privacy);
                linkAttribute.setDataType(dataTypeRecord);
                linkAttribute.setRealm(realmName);
                service.upsert(linkAttribute);
            } catch (Exception e) {
                String name = attributeLink.get("name").replaceAll("^\"|\"$", "");
                String privacyStr = attributeLink.get("privacy");
                Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);

                linkAttribute = new AttributeLink(code, name);
                linkAttribute.setDefaultPrivacyFlag(privacy);
                linkAttribute.setRealm(attributeLink.get("realm"));
            }
            service.upsert(linkAttribute);

        });
    }


    public void questions(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().filter(rawData -> !rawData.getKey().isEmpty()).forEach(data -> {
            Map<String, String> questions = data.getValue();
            String code = questions.get("code");
            String name = questions.get("name");
            String placeholder = questions.get("placeholder");
            String attrCode = questions.get("attribute_code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String html = questions.get("html");
            String oneshotStr = questions.get("oneshot");
            String readonlyStr = questions.get("readonly");
            String mandatoryStr = questions.get("mandatory");
            String helper = questions.get("helper");

            Boolean oneshot = getBooleanFromString(oneshotStr);
            Boolean readonly = getBooleanFromString(readonlyStr);
            Boolean mandatory = getBooleanFromString(mandatoryStr);
            Attribute attr;
            attr = service.findAttributeByCode(attrCode);
            if (attr == null) {
                log.error(String.format("%s HAS NO ATTRIBUTE IN DATABASE", attrCode));
            } else {
                Question q = null;
                if (placeholder != null) {
                    q = new Question(code, name, attr, placeholder);
                } else {
                    q = new Question(code, name, attr);
                }
                q.setOneshot(oneshot);
                q.setHtml(html);
                q.setHelper(helper);
                q.setReadonly(readonly);
                q.setMandatory(mandatory);
                q.setRealm(realmName);

                Question existing = service.findQuestionByCode(code);
                if (existing == null) {
                    if (isSynchronise()) {
                        Question val = service.findQuestionByCode(q.getCode(), mainRealm);
                        if (val != null) {
                            val.setRealm(realmName);
                            service.updateRealm(val);
                            return;
                        }
                    }
                    service.insert(q);
                } else {
                    existing.setName(name);
                    existing.setHtml(html);
                    existing.setHelper(helper);
                    existing.setOneshot(oneshot);
                    existing.setReadonly(readonly);
                    existing.setMandatory(mandatory);
                    service.upsert(existing);
                }
            }

        });
    }


    public void asks(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().forEach(data -> {
            Map<String, String> asks = data.getValue();
            String sourceCode = asks.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String targetCode = asks.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String qCode = asks.get("question_code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String name = asks.get("name");
            String weightStr = asks.get("weight");
            String mandatoryStr = asks.get("mandatory");
            String readonlyStr = asks.get("readonly");
            String hiddenStr = asks.get("hidden");
            final Double weight = Double.valueOf(weightStr);
            if ("QUE_USER_SELECT_ROLE".equals(targetCode)) {
                log.info("dummy");
            }
            Boolean mandatory = "TRUE".equalsIgnoreCase(mandatoryStr);
            Boolean readonly = "TRUE".equalsIgnoreCase(readonlyStr);
            Boolean hidden = "TRUE".equalsIgnoreCase(hiddenStr);
            Question question = service.findQuestionByCode(qCode);
            final Ask ask = new Ask(question, sourceCode, targetCode, mandatory, weight);
            ask.setName(name);
            ask.setHidden(hidden);
            ask.setReadonly(readonly);
            ask.setRealm(realmName);

            service.insert(ask);
        });
    }

    public static boolean isSynchronise() {
        return isSynchronise;
    }

    public void messageTemplates(Map<String, Map<String, String>> project, String realmName) {

        project.entrySet().stream().forEach(data -> {

            log.info("messages, data ::" + data);
            Map<String, String> template = data.getValue();
            String code = template.get("code");
            String name = template.get("name");
            String description = template.get("description");
            String subject = template.get("subject");
            String emailTemplateDocId = template.get("email");
            if (emailTemplateDocId == null)
                emailTemplateDocId = template.get("emailtemplateid");
            String smsTemplate = template.get("sms");
            if (smsTemplate == null)
                smsTemplate = template.get("smstemplate");
            String toastTemplate = template.get("toast");
            if (toastTemplate == null)
                toastTemplate = template.get("toasttemplate");

            final QBaseMSGMessageTemplate templateObj = new QBaseMSGMessageTemplate();
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
                    QBaseMSGMessageTemplate msg = service.findTemplateByCode(code);
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
                            QBaseMSGMessageTemplate val = service.findTemplateByCode(templateObj.getCode(), "hidden");
                            if (val != null) {
                                val.setRealm("genny");
                                service.updateRealm(val);
                                return;
                            }
                        }
                        Long id = service.insert(templateObj);
                        log.info("message id ::" + id);
                    } catch (javax.validation.ConstraintViolationException ce) {
                        log.error("Error in saving message due to constraint issue:" + templateObj + " :" + ce.getLocalizedMessage());
                        log.info("Trying to update realm from hidden to genny");
                        templateObj.setRealm("genny");
                        service.updateRealm(templateObj);
                    }

                } catch (Exception e) {
                    log.error("Cannot add MessageTemplate");

                }
            }
        });
    }


    public void upsertKeycloakJson(String keycloakJson) {
        final String PROJECT_CODE = "PRJ_" + this.mainRealm.toUpperCase();
        BaseEntity be = service.findBaseEntityByCode(PROJECT_CODE);

        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Attribute attr = service.findAttributeByCode("ENV_KEYCLOAK_JSON");
        if (attr == null) {
            DataType dataType = new DataType("DTT_TEXT");
            dataType.setDttCode("DTT_TEXT");
            attr = new Attribute("ENV_KEYCLOAK_JSON", "Keycloak Json", dataType);
            attr.setRealm(mainRealm);
            Set<ConstraintViolation<Attribute>> constraints = validator.validate(attr);
            for (ConstraintViolation<Attribute> constraint : constraints) {
                log.info(String.format("[\"%s\"], %s, %s.", this.mainRealm,
                        constraint.getPropertyPath(), constraint.getMessage()));
            }
            service.upsert(attr);
        }
        try {
            be.addAttribute(attr, 0.0, keycloakJson);
        } catch (BadDataException e) {
            log.error(String.format("BadDataException:%s", e.getMessage()));
        }

        service.updateWithAttributes(be);

    }

    public void upsertProjectUrls(String urlList) {

        final String PROJECT_CODE = "PRJ_" + this.mainRealm.toUpperCase();
        BaseEntity be = service.findBaseEntityByCode(PROJECT_CODE);

        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Attribute attr = service.findAttributeByCode("ENV_URL_LIST");
        attr.setRealm(mainRealm);
        DataType dataType = new DataType("DTT_TEXT");
        dataType.setDttCode("DTT_TEXT");
        attr = new Attribute("ENV_URL_LIST", "Url List", dataType);
        Set<ConstraintViolation<Attribute>> constraints = validator.validate(attr);
        for (ConstraintViolation<Attribute> constraint : constraints) {
            log.info(String.format("[\" %s\"] %s, %s.", this.mainRealm, constraint.getPropertyPath(), constraint.getMessage()));
        }
        service.upsert(attr);
        try {
            be.addAttribute(attr, 0.0, urlList);
        } catch (BadDataException e) {
            log.error(String.format("BadDataException:%s", e.getMessage()));
        }
        service.updateWithAttributes(be);
    }

    public String constructKeycloakJson(final RealmUnit realm) {
        this.mainRealm = realm.getCode();
        String keycloakUrl = null;
        String keycloakSecret = null;
        String keycloakJson = null;

        keycloakUrl = realm.getKeycloakUrl();
        keycloakSecret = realm.getClientSecret();

        keycloakJson = "{\n" + "  \"realm\": \"" + this.mainRealm + "\",\n" + "  \"auth-server-url\": \"" + keycloakUrl
                + "/auth\",\n" + "  \"ssl-required\": \"external\",\n" + "  \"resource\": \"" + this.mainRealm + "\",\n"
                + "  \"credentials\": {\n" + "    \"secret\": \"" + keycloakSecret + "\" \n" + "  },\n"
                + "  \"policy-enforcer\": {}\n" + "}";

        log.info(String.format("[%s] Loaded keycloak.json:%s ", this.mainRealm, keycloakJson));
        return keycloakJson;

    }

    public void persistProject(life.genny.bootxport.bootx.RealmUnit rx) {
        boolean useOptimization = true;
        if (useOptimization) {
            persistProjectOptimization(rx);
        } else {
            service.setRealm(rx.getCode());
            validations(rx.getValidations(), rx.getCode());
            Map<String, DataType> dataTypes = dataType(rx.getDataTypes());
            attributes(rx.getAttributes(), dataTypes, rx.getCode());
            baseEntitys(rx.getBaseEntitys(), rx.getCode());
            attributeLinks(rx.getAttributeLinks(), dataTypes, rx.getCode());
            baseEntityAttributes(rx.getEntityAttributes(), rx.getCode());
            entityEntitys(rx.getEntityEntitys());
            questions(rx.getQuestions(), rx.getCode());
            questionQuestions(rx.getQuestionQuestions(), rx.getCode());
            asks(rx.getAsks(), rx.getCode());
            messageTemplates(rx.getNotifications(), rx.getCode());
            messageTemplates(rx.getMessages(), rx.getCode());
        }
    }

    private String decodePassword(String realm, String securityKey, String servicePass) {
        String initVector = "PRJ_" + realm.toUpperCase();
        initVector = StringUtils.rightPad(initVector, 16, '*');
        String decrypt = SecurityUtils.decrypt(securityKey, initVector, servicePass);
        return decrypt;
    }


    public void persistProjectOptimization(life.genny.bootxport.bootx.RealmUnit rx) {
        service.setRealm(rx.getCode());

        String decrypt = decodePassword(rx.getCode(), rx.getSecurityKey(), rx.getServicePassword());
        HashMap<String, String> userCodeUUIDMapping = KeycloakUtils.getUsersByRealm(rx.getKeycloakUrl(), rx.getCode(), decrypt);
        Optimization optimization = new Optimization(service);

        // clean up
        service.cleanAsk(rx.getCode());
        service.cleanFrameFromBaseentityAttribute(rx.getCode());

        optimization.validationsOptimization(rx.getValidations(), rx.getCode());

        Map<String, DataType> dataTypes = dataType(rx.getDataTypes());
        optimization.attributesOptimization(rx.getAttributes(), dataTypes, rx.getCode());

        optimization.baseEntitysOptimization(rx.getBaseEntitys(), rx.getCode(), userCodeUUIDMapping);

        optimization.attributeLinksOptimization(rx.getAttributeLinks(), dataTypes, rx.getCode());

        optimization.baseEntityAttributesOptimization(rx.getEntityAttributes(), rx.getCode(), userCodeUUIDMapping);

        optimization.entityEntitysOptimization(rx.getEntityEntitys(), rx.getCode(), isSynchronise, userCodeUUIDMapping);

        optimization.questionsOptimization(rx.getQuestions(), rx.getCode(), isSynchronise);

        optimization.questionQuestionsOptimization(rx.getQuestionQuestions(), rx.getCode());

        optimization.asksOptimization(rx.getAsks(), rx.getCode());

        optimization.messageTemplatesOptimization(rx.getNotifications(), rx.getCode());
        optimization.messageTemplatesOptimization(rx.getMessages(), rx.getCode());
    }

    public void deleteFromProject(life.genny.bootxport.bootx.RealmUnit rx) {
        service.setRealm(rx.getCode());
        deleteAttributes(rx.getAttributes());
        deleteBaseEntitys(rx.getBaseEntitys());
        deleteAttributeLinks(rx.getAttributeLinks());
        deleteEntityEntitys(rx.getEntityEntitys());
        deleteQuestions(rx.getQuestions());
        deleteQuestionQuestions(rx.getQuestionQuestions());
        deleteMessageTemplates(rx.getNotifications());
        deleteMessageTemplates(rx.getMessages());
    }

    public void deleteAttributes(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(d -> {
            Attribute attribute = service.findAttributeByCode(d.getKey());
            service.delete(attribute);
        });
    }

    public void deleteBaseEntitys(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(d -> {
            BaseEntity baseEntity = service.findBaseEntityByCode(d.getKey());
            service.delete(baseEntity);
        });

    }

    public void deleteAttributeLinks(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(d -> {
            Attribute attribute = service.findAttributeByCode(d.getKey());
            service.delete(attribute);
        });

    }

    public void deleteEntityEntitys(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(d -> {
            Map<String, String> entEnts = d.getValue();
            String parentCode = entEnts.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String linkCode = entEnts.get("linkCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (parentCode == null)
                parentCode = entEnts.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String targetCode = entEnts.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            EntityEntity entityEntity = service.findEntityEntity(parentCode, targetCode, linkCode);
            service.delete(entityEntity);

        });
    }

    public void deleteQuestions(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(d -> {
            Question question = service.findQuestionByCode(d.getKey());
            service.delete(question);
        });
    }

    public void deleteQuestionQuestions(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(d -> {
            Map<String, String> queQues = d.getValue();
            String parentCode = queQues.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String targetCode = queQues.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            QuestionQuestion questionQuestion = service.findQuestionQuestionByCode(parentCode, targetCode);
            service.delete(questionQuestion);
        });

    }

    public void deleteMessageTemplates(Map<String, Map<String, String>> project) {
        project.entrySet().stream().forEach(d -> {
            QBaseMSGMessageTemplate template = service.findTemplateByCode(d.getKey());
            service.delete(template);
        });
    }

}
