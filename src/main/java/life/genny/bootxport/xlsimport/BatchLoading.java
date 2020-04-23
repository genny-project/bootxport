package life.genny.bootxport.xlsimport;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import life.genny.bootxport.bootx.QwandaRepository;
import life.genny.bootxport.bootx.QwandaRepositoryImpl;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.NotFoundException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

class Options {
    public String optionCode = null;
    public String optionLabel = null;
}

public class BatchLoading {
    private final QwandaRepositoryImpl service;

    private String mainRealm = GennySettings.mainrealm;
    private static final String VALIDATIONS = "validations";
    private static final boolean isSynchronise = false;

    protected static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    public BatchLoading(QwandaRepository repo) {
        this.service = (QwandaRepositoryImpl)repo;
    }

    private Validation buildValidation(Map<String, String> validations, String realmName, String code) {
        boolean needCheckOptions = false;
        boolean hasValidOptions = false;
        Gson gsonObject = new Gson();
        String optionString = validations.get("options");
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

        String regex = validations.get("regex");
        if (regex != null) {
            regex = regex.replaceAll("^\"|\"$", "");
        }
        if ("VLD_AU_DRIVER_LICENCE_NO".equalsIgnoreCase(code)) {
            log.info("detected VLD_AU_DRIVER_LICENCE_NO");
        }
        String name = validations.get("name").replaceAll("^\"|\"$", "");
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
        log.info("realm:" + realmName + ",code:" + code + ",name:" + name + ",val:" + val + ", grp="
                + (groupCodesStr != null ? groupCodesStr : "X"));
        return val;
    }

    public void validations(Map<String, Map<String, String>> project, String realmName) {
        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        List<Validation> validationsFromDB = service.queryValidation(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (Validation vld : validationsFromDB) {
            codeSet.add(vld.getCode());
        }
        ArrayList<Validation> validationList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map<String, String> validations : project.values()) {
            total += 1;
            String code = validations.get("code").replaceAll("^\"|\"$", "");

            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("Validation:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            Validation val = buildValidation(validations, realmName, code);

            Set<ConstraintViolation<Validation>> constraints = validator.validate(val);
            for (ConstraintViolation<Validation> constraint : constraints) {
                log.error(constraint.getPropertyPath() + " " + constraint.getMessage());
            }

            if (constraints.isEmpty()) {
                validationList.add(val);
            } else {
                invalid += 1;
            }
        }
        service.insertValidations(validationList);
        log.debug("Validation: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    private Boolean getBooleanFromString(final String booleanString) {
        if (booleanString == null) {
            return false;
        }

        return "TRUE".equalsIgnoreCase(booleanString.toUpperCase()) || "YES".equalsIgnoreCase(booleanString.toUpperCase())
                || "T".equalsIgnoreCase(booleanString.toUpperCase())
                || "Y".equalsIgnoreCase(booleanString.toUpperCase()) || "1".equalsIgnoreCase(booleanString);

    }

    private Attribute buildAttrribute(Map<String, String> attributes, Map<String, DataType> dataTypeMap,
                                      String realmName, String code) {
        String dataType = null;
        if (!attributes.containsKey("datatype")) {
            log.error("DataType for " + code + " cannot be null");
            throw new NotFoundException("Bad DataType given for code " + code);
        }

        dataType = attributes.get("datatype").trim().replaceAll("^\"|\"$", "");
//        log.info("This is the datatype object code: " + dataType);
        String name = attributes.get("name").replaceAll("^\"|\"$", "");
        DataType dataTypeRecord = dataTypeMap.get(dataType);
//        log.info("This is the datatype map: " + dataTypeRecord);

        String privacyStr = attributes.get("privacy");
        if (privacyStr != null) {
            privacyStr = privacyStr.toUpperCase();
        }

        Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);
        if (privacy) {
            log.info("Realm:" + realmName + ", Attribute " + code + " has default privacy");
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
        // attr.setRealm(mainRealm);
        return attr;
    }

    public void attributes(Map<String, Map<String, String>> project, Map<String, DataType> dataTypeMap, String realmName) {
        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        List<Attribute> attributesFromDB = service.queryAttributes(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (Attribute att : attributesFromDB) {
            codeSet.add(att.getCode());
        }

        ArrayList<Attribute> attributeList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> data : project.entrySet()) {
            total += 1;
            Map<String, String> attributes = data.getValue();
            String code = attributes.get("code").replaceAll("^\"|\"$", "");
            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("Attributes:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            Attribute attr = buildAttrribute(attributes, dataTypeMap, realmName, code);

            Set<ConstraintViolation<Attribute>> constraints = validator.validate(attr);
            for (ConstraintViolation<Attribute> constraint : constraints) {
                log.info(constraint.getPropertyPath() + " " + constraint.getMessage());
            }

            if (constraints.isEmpty()) {
                attributeList.add(attr);
            } else {
                invalid += 1;
            }
        }
        service.insertAttributes(attributeList);
        log.debug("Attribute: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    public Map<String, DataType> dataType(Map<String, Map<String, String>> project) {
        final Map<String, DataType> dataTypeMap = new HashMap<>();
        project.entrySet().stream().filter(d -> !d.getKey().matches("\\s*")).forEach(data -> {
            Map<String, String> dataType = data.getValue();
            String validations = dataType.get("validations");
            String code = dataType.get("code").trim().replaceAll("^\"|\"$", "");
            String name = dataType.get("name").replaceAll("^\"|\"$", "");
            String inputmask = dataType.get("inputmask");
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
                final DataType dataTypeRecord = new DataType(name, validationList, name, inputmask);
                dataTypeRecord.setDttCode(code);
                dataTypeMap.put(code, dataTypeRecord);
            }
        });
        return dataTypeMap;
    }

    public void baseEntitys(Map<String, Map<String, String>> project, String realmName) {
        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        List<BaseEntity> baseEntityFromDB = service.queryBaseEntitys(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (BaseEntity be : baseEntityFromDB) {
            codeSet.add(be.getCode());
        }

        ArrayList<BaseEntity> baseEntityList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            String key = entry.getKey();
            Map<String, String> baseEntitys = entry.getValue();
            String code = baseEntitys.get("code").replaceAll("^\"|\"$", "");

            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("BaseEntity:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            String name = getNameFromMap(baseEntitys, "name", code);
            BaseEntity be = new BaseEntity(code, name);
            be.setRealm(realmName);

            Set<ConstraintViolation<BaseEntity>> constraints = validator.validate(be);
            for (ConstraintViolation<BaseEntity> constraint : constraints) {
                log.info(constraint.getPropertyPath() + " " + constraint.getMessage());
            }

            if (constraints.isEmpty()) {
                baseEntityList.add(be);
            } else {
                invalid += 1;
            }
        }
        service.insertBaseEntitys(baseEntityList);
        log.debug("BaseEntity: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    private String getNameFromMap(Map<String, String> baseEntitys, String key, String defaultString) {
        String ret = defaultString;
        if (baseEntitys.containsKey(key)) {
            if (baseEntitys.get("name") != null) {
                ret = baseEntitys.get("name").replaceAll("^\"|\"$", "");
            }
        }
        return ret;
    }

    private EntityAttribute buildEntityAttribute(Map<String, String> baseEntityAttr,
                                                 String realmName,
                                                 Attribute attribute,
                                                 BaseEntity baseEntity) {
        String weight = baseEntityAttr.get("weight");
        String privacyStr = baseEntityAttr.get("privacy");
        Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);
        double weightField = 0.0;
        if (isDouble(weight)) {
            weightField = Double.parseDouble(weight);
        }

        List<String> asList = Collections.singletonList("valuestring");
        Optional<String> valueString = asList.stream().map(baseEntityAttr::get).findFirst();
        Integer valueInt = null;
        Optional<String> ofNullable = Optional.ofNullable(baseEntityAttr.get("valueinteger"));
        if (ofNullable.isPresent() && !baseEntityAttr.get("valueinteger").matches("\\s*")) {
            System.out.println(baseEntityAttr.get("valueinteger"));
            BigDecimal big = new BigDecimal(baseEntityAttr.get("valueinteger"));
            Optional<String[]> nullableVal = Optional.of(big.toPlainString().split("[.]"));
            valueInt = nullableVal.filter(d -> d.length > 0).map(d -> Integer.valueOf(d[0])).get();
        }
        String valueStr = null;
        valueStr = valueString.get().replaceAll("^\"|\"$", "");

        EntityAttribute ea = null;
        if (valueInt != null) {
            ea = new EntityAttribute(baseEntity, attribute, weightField, valueInt);
        } else {
            ea = new EntityAttribute(baseEntity, attribute, weightField, valueStr);
        }
        if (privacy || attribute.getDefaultPrivacyFlag()) {
            ea.setPrivacyFlag(true);
        }
        ea.setRealm(realmName);
        return ea;
    }

    public void baseEntityAttributes(Map<String, Map<String, String>> project, String realmName) {
        List<BaseEntity> baseEntityFromDB = service.queryBaseEntitys(realmName);
//        HashSet<String> beCodeSet = new HashSet<>();
        HashMap<String, BaseEntity> beHashMap = new HashMap<>();
        for (BaseEntity be : baseEntityFromDB) {
//            beCodeSet.add(be.getCode());
            beHashMap.put(be.getCode(), be);
        }

        List<Attribute> attributeFromDB = service.queryAttributes(realmName);
//        HashSet<String> attrCodeSet = new HashSet<>();
        HashMap<String, Attribute> attrHashMap = new HashMap<>();
        for (Attribute attribute : attributeFromDB) {
//            attrCodeSet.add(attribute.getCode());
            attrHashMap.put(attribute.getCode(), attribute);
        }

        List<EntityAttribute> entityAttributeFromDB = service.queryEntityAttribute(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (EntityAttribute entityAttribute : entityAttributeFromDB) {
            String beCode = entityAttribute.getBaseEntityCode();
            String attrCode = entityAttribute.getAttributeCode();
            String uniqueCode = beCode + "-" + attrCode;
            codeSet.add(uniqueCode);
        }

        ArrayList<EntityAttribute> entityAttributeList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total++;
            String key = entry.getKey();
            Map<String, String> baseEntityAttr = entry.getValue();

            String attributeCode = null;
            String searchKey = "attributeCode".toLowerCase().replaceAll("^\"|\"$|_|-", "");
            if (baseEntityAttr.containsKey(searchKey)) {
                attributeCode = baseEntityAttr.get(searchKey).replaceAll("^\"|\"$", "");
            } else {
                invalid++;
                log.error("AttributeCode not found [" + baseEntityAttr + "]");
                continue;
            }

            String baseEntityCode = null;
            searchKey = "baseEntityCode".toLowerCase().replaceAll("^\"|\"$|_|-", "");
            if (baseEntityAttr.containsKey(searchKey)) {
                baseEntityCode = baseEntityAttr.get(searchKey).replaceAll("^\"|\"$", "");
            } else {
                invalid++;
                log.error("BaseEntityCode not found [" + baseEntityAttr + "]");
                continue;
            }

            String code = baseEntityCode + "-" + attributeCode;
            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("EntityAttribute:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            Attribute attribute = attrHashMap.get(attributeCode.toUpperCase());
            if (attribute == null) {
                log.error("EntityAttribute Attribute Code:" + attributeCode + " doesn't exit in database, skip.");
                invalid++;
                continue;
            }


            BaseEntity baseEntity = beHashMap.get(baseEntityCode.toUpperCase());
            if (baseEntity == null) {
                log.error("EntityAttribute BaseEntity Code:" + baseEntityCode + " doesn't exit in database, skip.");
                invalid++;
                continue;
            }
            EntityAttribute entityAttribute = buildEntityAttribute(baseEntityAttr, realmName, attribute, baseEntity);
            entityAttributeList.add(entityAttribute);
        }
        service.insertEntityAttribute(entityAttributeList);
        log.debug("EntityAttribute: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }


    private EntityEntity buildEntityEntity(Map<String, String> entEnts,
                                           String realmName,
                                           Attribute linkAttribute,
                                           BaseEntity sbe, BaseEntity tbe) {
        String weightStr = entEnts.get("weight");
        String valueString = entEnts.get("valueString".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
        Optional<String> weightStrOpt = Optional.ofNullable(weightStr);
        final Double weight = weightStrOpt.filter(d -> !d.equals(" ")).map(Double::valueOf).orElse(0.0);
        EntityEntity ee = new EntityEntity(sbe, tbe, linkAttribute, weight);
        ee.setValueString(valueString);
        ee.setRealm(realmName);
        return ee;
    }

    public void entityEntitys(Map<String, Map<String, String>> project, String realmName) {
        List<BaseEntity> baseEntityFromDB = service.queryBaseEntitys(realmName);
//        HashSet<String> beCodeSet = new HashSet<>();
        HashMap<String, BaseEntity> beHashMap = new HashMap<>();
        for (BaseEntity be : baseEntityFromDB) {
//            beCodeSet.add(be.getCode());
            beHashMap.put(be.getCode(), be);
        }

        List<Attribute> attributeFromDB = service.queryAttributes(realmName);
//        HashSet<String> attrCodeSet = new HashSet<>();
        HashMap<String, Attribute> attrHashMap = new HashMap<>();
        for (Attribute attribute : attributeFromDB) {
//            attrCodeSet.add(attribute.getCode());
            attrHashMap.put(attribute.getCode(), attribute);
        }

        List<EntityEntity> entityEntityFromDB = service.queryEntityEntity(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (EntityEntity entityEntity : entityEntityFromDB) {
            String beCode = entityEntity.getPk().getSource().getCode();
            String attrCode = entityEntity.getPk().getAttribute().getCode();
            String targetCode = entityEntity.getPk().getTargetCode();
            String uniqueCode = beCode + "-" + attrCode + "-" + targetCode;
            codeSet.add(uniqueCode);
        }

        ArrayList<EntityEntity> entityEntityList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total++;
            String key = entry.getKey();
            Map<String, String> entEnts = entry.getValue();

            String linkCode = entEnts.get("linkCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (linkCode == null)
                linkCode = entEnts.get("code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String parentCode = entEnts.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (parentCode == null)
                parentCode = entEnts.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String targetCode = entEnts.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String code = parentCode + "-" + linkCode + "-" + targetCode;
            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("EntityEntity:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            Attribute linkAttribute = attrHashMap.get(linkCode.toUpperCase());
            BaseEntity sbe = beHashMap.get(parentCode.toUpperCase());
            BaseEntity tbe = beHashMap.get(targetCode.toUpperCase());

            if (linkAttribute == null) {
                log.error("EntityEntity Link code:" + linkCode + " doesn't exist in Attribute table.");
                invalid++;
                continue;
            } else if (sbe == null) {
                log.error("EntityEntity parent code:" + parentCode + " doesn't exist in Baseentity table.");
                invalid++;
                continue;
            } else if (tbe == null) {
                log.error("EntityEntity target Code:" + targetCode + " doesn't exist in Baseentity table.");
                invalid++;
                continue;
            }

            EntityEntity entityEntity = buildEntityEntity(entEnts, realmName, linkAttribute, sbe, tbe);
            entityEntityList.add(entityEntity);
        }
        service.insertEntityEntitys(entityEntityList);
        log.debug("EntityEntity: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    private boolean isDouble(String doubleStr) {
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex =
                ("[\\x00-\\x20]*" +  // Optional leading "whitespace"
                        "[+-]?(" + // Optional sign character
                        "NaN|" +           // "NaN" string
                        "Infinity|" +      // "Infinity" string

                        // A decimal floating-point string representing a finite positive
                        // number without a leading sign has at most five basic pieces:
                        // Digits . Digits ExponentPart FloatTypeSuffix
                        //
                        // Since this method allows integer-only strings as input
                        // in addition to strings of floating-point literals, the
                        // two sub-patterns below are simplifications of the grammar
                        // productions from section 3.10.2 of
                        // The Java Language Specification.

                        // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                        "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                        // . Digits ExponentPart_opt FloatTypeSuffix_opt
                        "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                        // Hexadecimal strings
                        "((" +
                        // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "(\\.)?)|" +

                        // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                        "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                        ")[pP][+-]?" + Digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");// Optional trailing "whitespace"

        return Pattern.matches(fpRegex, doubleStr);
    }

    private QuestionQuestion buildQuestionQuestion(Map<String, String> queQues,
                                                   String realmName,
                                                   Question sbe, Question tbe) throws BadDataException {
        String weightStr = queQues.get("weight");
        String mandatoryStr = queQues.get("mandatory");
        String readonlyStr = queQues.get("readonly");
        Boolean readonly = "TRUE".equalsIgnoreCase(readonlyStr);
        Boolean formTrigger = queQues.get("formtrigger") != null && "TRUE".equalsIgnoreCase(queQues.get("formtrigger"));
        Boolean createOnTrigger = queQues.get("createontrigger") != null && "TRUE".equalsIgnoreCase(queQues.get("createontrigger"));
        double weight = 0.0;
        if (isDouble(weightStr)) {
            weight = Double.parseDouble(weightStr);
        }

        Boolean mandatory = "TRUE".equalsIgnoreCase(mandatoryStr);
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

        // qq.setRealm(mainRealm);
        qq.setRealm(realmName);
        return qq;
    }

    public void questionQuestions(Map<String, Map<String, String>> project, String realmName) {
        List<QuestionQuestion> questionQuestionFromDB = service.queryQuestionQuestion(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (QuestionQuestion qq : questionQuestionFromDB) {
            String sourceCode = qq.getSourceCode();
            String targetCode = qq.getTarketCode();
            String uniqCode = sourceCode + "-" + targetCode;
            codeSet.add(uniqCode);
        }

        List<Question> questionFromDB = service.queryQuestion(realmName);
//        HashSet<String> questionCodeSet = new HashSet<>();
        HashMap<String, Question> questionHashMap = new HashMap<>();

        for (Question question : questionFromDB) {
//            questionCodeSet.add(question.getCode());
            questionHashMap.put(question.getCode(), question);
        }

        ArrayList<QuestionQuestion> questionQuestionList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            String key = entry.getKey();
            Map<String, String> queQues = entry.getValue();

            String parentCode = queQues.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (parentCode == null) {
                parentCode = queQues.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            }

            String targetCode = queQues.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String code = parentCode + "-" + targetCode;
            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("QuestionQuestion:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            Question sbe = questionHashMap.get(parentCode.toUpperCase());
            Question tbe = questionHashMap.get(targetCode.toUpperCase());
            if (sbe == null) {
                log.error("QuestionQuesiton parent code:" + parentCode + " doesn't exist in Question table.");
                invalid++;
                continue;
            } else if (tbe == null) {
                log.error("QuestionQuesiton target Code:" + targetCode + " doesn't exist in Question table.");
                invalid++;
                continue;
            }

            try {
                QuestionQuestion qq = buildQuestionQuestion(queQues, realmName, sbe, tbe);
                questionQuestionList.add(qq);
            } catch (BadDataException bde) {
                invalid += 1;
                log.error("Bad Exception occurred when build question, parentCode:" + parentCode + ", TargetCode:" + targetCode);
            }
        }
        service.insertQuestionQuestions(questionQuestionList);
        log.debug("QuestionQuestion: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    private AttributeLink buildAttributeLink(Map<String, String> attributeLink, Map<String, DataType> dataTypeMap, String realmName, String code) {
        String name = attributeLink.get("name").replaceAll("^\"|\"$", "");
        String privacyStr = attributeLink.get("privacy");
        Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);

        AttributeLink linkAttribute = new AttributeLink(code, name);
        linkAttribute.setDefaultPrivacyFlag(privacy);
        linkAttribute.setRealm(realmName);

        String dataTypeStr = "dataType".toLowerCase();
        if (attributeLink.containsKey(dataTypeStr)) {
            String dataType = attributeLink.get("dataType".toLowerCase().trim().replaceAll("^\"|\"$|_|-", ""))
                    .replaceAll("^\"|\"$", "");
            DataType dataTypeRecord = dataTypeMap.get(dataType);
            linkAttribute.setDataType(dataTypeRecord);
        }
//         else {
//            linkAttribute.setRealm(attributeLink.get("realm"));
//            linkAttribute.setRealm(attributeLink.get("realm"));
//        }
        return linkAttribute;
    }

    public void attributeLinks(Map<String, Map<String, String>> project, Map<String, DataType> dataTypeMap, String realmName) {
        List<Attribute> attributeLinksFromDB = service.queryAttributes(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (Attribute attributeLink : attributeLinksFromDB) {
            codeSet.add(attributeLink.getCode());
        }

        ArrayList<AttributeLink> attributeLinkList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            String key = entry.getKey();
            Map<String, String> attributeLink = entry.getValue();
            String code = attributeLink.get("code").replaceAll("^\"|\"$", "");

            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("AttributeLink:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }
            AttributeLink linkAttribute = buildAttributeLink(attributeLink, dataTypeMap, realmName, code);
            attributeLinkList.add(linkAttribute);
        }
        service.insertAttributeLinks(attributeLinkList);
        log.debug("AttributeLink: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    private Question bulidQuestion(Map<String, String> questions, String code, Attribute attr, String realmName) {
        Question q = null;
        String name = questions.get("name");
        String placeholder = questions.get("placeholder");
        String html = questions.get("html");
        String oneshotStr = questions.get("oneshot");
        String readonlyStr = questions.get("readonly");
        // String hiddenStr = (String) questions.get("hidden");
        String mandatoryStr = questions.get("mandatory");
        Boolean oneshot = getBooleanFromString(oneshotStr);
        Boolean readonly = getBooleanFromString(readonlyStr);
        Boolean mandatory = getBooleanFromString(mandatoryStr);

        if (placeholder != null) {
            q = new Question(code, name, attr, placeholder);
        } else {
            q = new Question(code, name, attr);
        }
        q.setOneshot(oneshot);
        q.setHtml(html);
        q.setReadonly(readonly);
        q.setMandatory(mandatory);
        q.setRealm(realmName);
        return q;
    }

    public void questions(Map<String, Map<String, String>> project, String realmName) {
        // Get all questions from database
        List<Question> questionsFromDB = service.queryQuestion(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (Question q : questionsFromDB) {
            codeSet.add(q.getCode());
        }

        // Get all Attributes from database
        List<Attribute> attributesFromDB = service.queryAttributes(realmName);
        HashSet<String> attrCodeSet = new HashSet<>();
        HashMap<String, Attribute> attributeHashMap = new HashMap<>();

        for (Attribute attribute : attributesFromDB) {
            attrCodeSet.add(attribute.getCode());
            attributeHashMap.put(attribute.getCode(), attribute);
        }

        ArrayList<Question> questionList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> rawData : project.entrySet()) {
            total += 1;
            if (rawData.getKey().isEmpty()) {
                skipped += 1;
                continue;
            }

            Map<String, String> questions = rawData.getValue();
            String code = questions.get("code");

            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("Question:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            String attrCode = questions.get("attribute_code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            // Check if AttributeCode exist in database
            if (!attrCodeSet.contains(attrCode.toUpperCase())) {
                log.error("Attribute:" + attrCode + " not in database!, skip.");
                skipped += 1;
                continue;
            }

            Attribute attr = attributeHashMap.get(attrCode.toUpperCase());
            Question question = bulidQuestion(questions, code, attr, realmName);
            questionList.add(question);
        }
        service.insertQuestions(questionList);
        log.debug("Question: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    private Ask buildAsk(Map<String, String> asks, String realmName,
                         HashMap<String, Question> questionHashMap) {
        String attributeCode = asks.get("attributeCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
        String sourceCode = asks.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
        String expired = asks.get("expired");
        String refused = asks.get("refused");
        String targetCode = asks.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
        String qCode = asks.get("question_code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
        String name = asks.get("name");
        String expectedId = asks.get("expectedId".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
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

        Question question = questionHashMap.get(qCode.toUpperCase());

        Ask ask = new Ask(question, sourceCode, targetCode, mandatory, weight);
        ask.setName(name);
        ask.setHidden(hidden);
        ask.setReadonly(readonly);
        ask.setRealm(realmName);
        return ask;

    }

    public void asks(Map<String, Map<String, String>> project, String realmName) {
        // Get all asks
        List<Ask> askFromDB = service.queryAsk(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (Ask ask : askFromDB) {
            String targetCode = ask.getTargetCode();
            String sourceCode = ask.getSourceCode();
            String attributeCode = ask.getAttributeCode();
            String questionCode = ask.getQuestionCode();
            String uniqueCode = questionCode + "-" + sourceCode + "-" + targetCode + "-" + attributeCode;
            codeSet.add(uniqueCode);
        }

        // Get  all BaseEntity from database
//        List<BaseEntity> baseEntityFromDB = service.queryBaseEntitys(realmName);
//        HashMap<String, BaseEntity> beHashMap = new HashMap<>();
//        for (BaseEntity be : baseEntityFromDB) {
//            beHashMap.put(be.getCode(), be);
//        }

        // Get all Attribute from database
//        List<Attribute> attributeFromDB = service.queryAttributes(realmName);
//        HashMap<String, Attribute> attrHashMap = new HashMap<>();
//        for (Attribute be : attributeFromDB) {
//            attrHashMap.put(be.getCode(), be);
//        }

        List<Question> questionFromDB = service.queryQuestion(realmName);
        HashMap<String, Question> questionHashMap = new HashMap<>();
        for (Question q : questionFromDB) {
            questionHashMap.put(q.getCode(), q);
        }

        ArrayList<Ask> askList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            String key = entry.getKey();
            Map<String, String> asks = entry.getValue();
            String qCode = asks.get("question_code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String attributeCode = asks.get("attributeCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String sourceCode = asks.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String targetCode = asks.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String uniqueCode = qCode + "-" + sourceCode + "-" + targetCode + "-" + attributeCode;

            if (codeSet.contains(uniqueCode.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("Ask:" + uniqueCode + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            if (!questionHashMap.containsKey(qCode)) {
                log.error("Question code" + qCode + ", Realm:" + realmName + "doesn't exists in db, skip process Ask:" + uniqueCode);
                invalid += 1;
                continue;
            }
            Ask ask = buildAsk(asks, realmName, questionHashMap);
            askList.add(ask);
        }
        service.insertAsks(askList);
        log.debug("Ask: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
    }

    public static boolean isSynchronise() {
        return isSynchronise;
    }

    private QBaseMSGMessageTemplate buildQBaseMSGMessageTemplate(Map<String, String> template, String code, String realmName) {
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

        QBaseMSGMessageTemplate templateObj = new QBaseMSGMessageTemplate();
        templateObj.setCode(code);
        templateObj.setName(name);
        templateObj.setCreated(LocalDateTime.now());
        templateObj.setDescription(description);
        templateObj.setEmail_templateId(emailTemplateDocId);
        templateObj.setSms_template(smsTemplate);
        templateObj.setSubject(subject);
        templateObj.setToast_template(toastTemplate);
        templateObj.setRealm(realmName);
        return templateObj;
    }

    public void messageTemplates(Map<String, Map<String, String>> project, String realmName) {
        List<QBaseMSGMessageTemplate> qBaseMSGMessageTemplateFromDB = service.queryMessage(realmName);
        HashSet<String> codeSet = new HashSet<>();
        for (QBaseMSGMessageTemplate message : qBaseMSGMessageTemplateFromDB) {
            codeSet.add(message.getCode());
        }
        ArrayList<QBaseMSGMessageTemplate> messageList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;

        for (Map.Entry<String, Map<String, String>> data : project.entrySet()) {
            total += 1;
//            log.trace("messages, data ::" + data);
            Map<String, String> template = data.getValue();
            String code = template.get("code");
            String name = template.get("name");
            if (codeSet.contains(code.toUpperCase())) {
                // TODO merger and update if needed
//                log.trace("Templates:" + code + ", Realm:" + realmName + " exists in db, skip.");
                skipped += 1;
                continue;
            }

            if (StringUtils.isBlank(name)) {
                log.error("Templates:" + code + "has EMPTY name.");
                invalid += 1;
                continue;
            }
            QBaseMSGMessageTemplate msg = buildQBaseMSGMessageTemplate(template, code, realmName);
            messageList.add(msg);
        }
        service.inserTemplate(messageList);
        log.debug("Templates: Total:" + total + ", invalid:" + invalid + ", skipped:" + skipped);
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
                log.info("[" + this.mainRealm + "] " + constraint.getPropertyPath() + " " + constraint.getMessage());
            }
            service.upsert(attr);
        }
        try {
            EntityAttribute ea = be.addAttribute(attr, 0.0, keycloakJson);
        } catch (BadDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        if (attr == null) {
            DataType dataType = new DataType("DTT_TEXT");
            dataType.setDttCode("DTT_TEXT");
            attr = new Attribute("ENV_URL_LIST", "Url List", dataType);
            Set<ConstraintViolation<Attribute>> constraints = validator.validate(attr);
            for (ConstraintViolation<Attribute> constraint : constraints) {
                log.info("[" + this.mainRealm + "]" + constraint.getPropertyPath() + " " + constraint.getMessage());
            }
            service.upsert(attr);
        }
        try {
            EntityAttribute ea = be.addAttribute(attr, 0.0, urlList);
        } catch (BadDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        service.updateWithAttributes(be);
    }

    public String constructKeycloakJson(final RealmUnit realm) {
//    final String PROJECT_CODE = "PRJ_" + this.mainRealm.toUpperCase();
        this.mainRealm = realm.getCode();
        String keycloakUrl = null;
        String keycloakSecret = null;
        String keycloakJson = null;

        if (realm != null) {
            keycloakUrl = realm.getKeycloakUrl();
            keycloakSecret = realm.getClientSecret();
        }

        keycloakJson = "{\n" + "  \"realm\": \"" + this.mainRealm + "\",\n" + "  \"auth-server-url\": \"" + keycloakUrl
                + "/auth\",\n" + "  \"ssl-required\": \"external\",\n" + "  \"resource\": \"" + this.mainRealm + "\",\n"
                + "  \"credentials\": {\n" + "    \"secret\": \"" + keycloakSecret + "\" \n" + "  },\n"
                + "  \"policy-enforcer\": {}\n" + "}";

        log.info("[" + this.mainRealm + "] Loaded keycloak.json... " + keycloakJson);
        return keycloakJson;

    }

    public void persistProject(life.genny.bootxport.bootx.RealmUnit rx) {
        String code = rx.getCode();
        boolean skipGoogleDoc = rx.getSkipGoogleDoc();
        boolean disabled = rx.getDisable();
        if (disabled) {
            log.info("Realm:" + code + " is disabled, skip");
            return;
        }

        if (skipGoogleDoc) {
            log.info("Realm:" + code + " disabled google sheet loading, skip");
            return;
        }

        service.setRealm(code);
        validations(rx.getValidations(), code);
        log.info("Realm:" + code + ", Persisted Validation.");

        Map<String, DataType> dataTypes = dataType(rx.getDataTypes());
        attributes(rx.getAttributes(), dataTypes, code);
        log.info("Realm:" + code + ", Persisted Attributes.");

        baseEntitys(rx.getBaseEntitys(), code);
        log.info("Realm:" + code + ", Persisted BaseEntitys.");

        attributeLinks(rx.getAttributeLinks(), dataTypes, code);
        log.info("Realm:" + code + ", Persisted AttributeLinks.");

        baseEntityAttributes(rx.getEntityAttributes(), code);
        log.info("Realm:" + code + ", Persisted EntityAttributes.");

        entityEntitys(rx.getEntityEntitys(), code);
        log.info("Realm:" + code + ", Persisted EntityEntitys.");

        questions(rx.getQuestions(), code);
        log.info("Realm:" + code + ", Persisted Questions.");

        questionQuestions(rx.getQuestionQuestions(), code);
        log.info("Realm:" + code + ", Persisted QuestionQuestions.");

        asks(rx.getAsks(), code);
        log.info("Realm:" + code + ", Persisted Asks.");

        messageTemplates(rx.getNotifications(), code);
        log.info("Realm:" + code + ", Persisted Notifications.");

        messageTemplates(rx.getMessages(), code);
        log.info("Realm:" + code + ", Persisted Messages.");
    }

    public void deleteFromProject(life.genny.bootxport.bootx.RealmUnit rx) {
        service.setRealm(rx.getCode());
        deleteAttributes(rx.getAttributes(), rx.getCode());
        deleteBaseEntitys(rx.getBaseEntitys(), rx.getCode());
        deleteAttributeLinks(rx.getAttributeLinks(), rx.getCode());
        deleteEntityEntitys(rx.getEntityEntitys());
        deleteQuestions(rx.getQuestions(), rx.getCode());
        deleteQuestionQuestions(rx.getQuestionQuestions(), rx.getCode());
        deleteMessageTemplates(rx.getNotifications(), rx.getCode());
        deleteMessageTemplates(rx.getMessages(), rx.getCode());
    }

    public void deleteAttributes(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().forEach(d -> {
            Attribute attribute = service.findAttributeByCode(d.getKey());
            service.delete(attribute);
        });
    }

    public void deleteBaseEntitys(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().forEach(d -> {
            BaseEntity baseEntity = service.findBaseEntityByCode(d.getKey());
            service.delete(baseEntity);
        });

    }

    public void deleteAttributeLinks(Map<String, Map<String, String>> project, String realmName) {
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

    public void deleteQuestions(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().forEach(d -> {
            Question question = service.findQuestionByCode(d.getKey());
            service.delete(question);
        });
    }

    public void deleteQuestionQuestions(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().forEach(d -> {
            Map<String, String> queQues = d.getValue();
            String parentCode = queQues.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            String targetCode = queQues.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            QuestionQuestion questionQuestion = service.findQuestionQuestionByCode(parentCode, targetCode);
            service.delete(questionQuestion);
        });

    }

    public void deleteMessageTemplates(Map<String, Map<String, String>> project, String realmName) {
        project.entrySet().stream().forEach(d -> {
            QBaseMSGMessageTemplate template = service.findTemplateByCode(d.getKey());
            service.delete(template);
        });

    }

}
