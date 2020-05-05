package life.genny.bootxport.xlsimport;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.ws.rs.NotFoundException;

public class GoogleSheetBuilder {
    private static final Log log = LogFactory.getLog(GoogleSheetBuilder.class);
    private static final String WEIGHT = "weight";
    private static final String REGEX_1 = "^\"|\"$";
    private static final String REGEX_2 = "^\"|\"$|_|-";
    private static final String PRIVACY = "privacy";
    private static final String VALUEINTEGER = "valueinteger";
    private static final String MANDATORY = "mandatory";
    private static final String READONLY = "readonly";

    private GoogleSheetBuilder() {
    }

    private static boolean isDouble(String doubleStr) {
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

    private static boolean getBooleanFromString(final String booleanString) {
        if (booleanString == null) {
            return false;
        }
        return "TRUE".equalsIgnoreCase(booleanString.toUpperCase()) || "YES".equalsIgnoreCase(booleanString.toUpperCase())
                || "T".equalsIgnoreCase(booleanString.toUpperCase())
                || "Y".equalsIgnoreCase(booleanString.toUpperCase()) || "1".equalsIgnoreCase(booleanString);

    }

    public static Validation buildValidation(Map<String, String> validations, String realmName, String code) {
        boolean hasValidOptions = false;
        Gson gsonObject = new Gson();
        String optionString = validations.get("options");
        if (optionString != null && (!optionString.equals(" "))) {
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
            regex = regex.replaceAll(REGEX_1, "");
        }
        if ("VLD_AU_DRIVER_LICENCE_NO".equalsIgnoreCase(code)) {
            log.info("detected VLD_AU_DRIVER_LICENCE_NO");
        }
        String name = validations.get("name").replaceAll(REGEX_1, "");
        String recursiveStr = validations.get("recursive");
        String multiAllowedStr = validations.get("multi_allowed".toLowerCase().replaceAll(REGEX_2, ""));
        String groupCodesStr = validations.get("group_codes".toLowerCase().replaceAll(REGEX_2, ""));
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

    public static Attribute buildAttrribute(Map<String, String> attributes, Map<String, DataType> dataTypeMap,
                                            String realmName, String code) {
        String dataType = null;
        if (!attributes.containsKey("datatype")) {
            log.error("DataType for " + code + " cannot be null");
            throw new NotFoundException("Bad DataType given for code " + code);
        }

        dataType = attributes.get("datatype").trim().replaceAll(REGEX_1, "");
        String name = attributes.get("name").replaceAll(REGEX_1, "");
        DataType dataTypeRecord = dataTypeMap.get(dataType);

        String privacyStr = attributes.get(PRIVACY);
        if (privacyStr != null) {
            privacyStr = privacyStr.toUpperCase();
        }

        boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);
        if (privacy) {
            log.info("Realm:" + realmName + ", Attribute " + code + " has default privacy");
        }
        String descriptionStr = attributes.get("description");
        String helpStr = attributes.get("help");
        String placeholderStr = attributes.get("placeholder");
        String defaultValueStr = attributes.get("defaultValue".toLowerCase().replaceAll(REGEX_2, ""));
        Attribute attr = new Attribute(code, name, dataTypeRecord);
        attr.setDefaultPrivacyFlag(privacy);
        attr.setDescription(descriptionStr);
        attr.setHelp(helpStr);
        attr.setPlaceholder(placeholderStr);
        attr.setDefaultValue(defaultValueStr);
        attr.setRealm(realmName);
        return attr;
    }

    public static AttributeLink buildAttributeLink(Map<String, String> attributeLink, Map<String, DataType> dataTypeMap, String realmName, String code) {
        String name = attributeLink.get("name").replaceAll(REGEX_1, "");
        String privacyStr = attributeLink.get(PRIVACY);
        Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);

        AttributeLink linkAttribute = new AttributeLink(code, name);
        linkAttribute.setDefaultPrivacyFlag(privacy);
        linkAttribute.setRealm(realmName);

        String dataTypeStr = "dataType".toLowerCase();
        if (attributeLink.containsKey(dataTypeStr)) {
            String dataType = attributeLink.get("dataType".toLowerCase().trim().replaceAll(REGEX_2, ""))
                    .replaceAll(REGEX_1, "");
            DataType dataTypeRecord = dataTypeMap.get(dataType);
            linkAttribute.setDataType(dataTypeRecord);
        }
        return linkAttribute;
    }

    public static QuestionQuestion buildQuestionQuestion(Map<String, String> queQues,
                                                         String realmName,
                                                         Question sbe, Question tbe) throws BadDataException {
        String weightStr = queQues.get(WEIGHT);
        String mandatoryStr = queQues.get(MANDATORY);
        String readonlyStr = queQues.get(READONLY);
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
        qq.setRealm(realmName);
        return qq;
    }

    public static EntityEntity buildEntityEntity(Map<String, String> entEnts,
                                                 String realmName,
                                                 Attribute linkAttribute,
                                                 BaseEntity sbe, BaseEntity tbe) {
        String weightStr = entEnts.get(WEIGHT);
        String valueString = entEnts.get("valueString".toLowerCase().replaceAll(REGEX_2, ""));
        Optional<String> weightStrOpt = Optional.ofNullable(weightStr);
        final Double weight = weightStrOpt.filter(d -> !d.equals(" ")).map(Double::valueOf).orElse(0.0);
        EntityEntity ee = new EntityEntity(sbe, tbe, linkAttribute, weight);
        ee.setValueString(valueString);
        ee.setRealm(realmName);
        return ee;
    }

    public static Ask buildAsk(Map<String, String> asks, String realmName,
                               Map<String, Question> questionHashMap) {
        String sourceCode = asks.get("sourceCode".toLowerCase().replaceAll(REGEX_2, ""));
        String targetCode = asks.get("targetCode".toLowerCase().replaceAll(REGEX_2, ""));
        String qCode = asks.get("question_code".toLowerCase().replaceAll(REGEX_2, ""));
        String name = asks.get("name");
        String weightStr = asks.get(WEIGHT);
        String mandatoryStr = asks.get(MANDATORY);
        String readonlyStr = asks.get(READONLY);
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

    public static QBaseMSGMessageTemplate buildQBaseMSGMessageTemplate(Map<String, String> template, String code, String realmName) {
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

    public static Question bulidQuestion(Map<String, String> questions, String code, Attribute attr, String realmName) {
        Question q = null;
        String name = questions.get("name");
        String placeholder = questions.get("placeholder");
        String html = questions.get("html");
        String oneshotStr = questions.get("oneshot");
        String readonlyStr = questions.get(READONLY);
        String mandatoryStr = questions.get(MANDATORY);
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

    public static EntityAttribute buildEntityAttribute(Map<String, String> baseEntityAttr,
                                                       String realmName,
                                                       Attribute attribute,
                                                       BaseEntity baseEntity) {
        String weight = baseEntityAttr.get(WEIGHT);
        String privacyStr = baseEntityAttr.get(PRIVACY);
        Boolean privacy = "TRUE".equalsIgnoreCase(privacyStr);
        double weightField = 0.0;
        if (isDouble(weight)) {
            weightField = Double.parseDouble(weight);
        }

        List<String> asList = Collections.singletonList("valuestring");
        Optional<String> valueString = asList.stream().map(baseEntityAttr::get).findFirst();
        Integer valueInt = null;
        Optional<String> ofNullable = Optional.ofNullable(baseEntityAttr.get(VALUEINTEGER));
        if (ofNullable.isPresent() && !baseEntityAttr.get(VALUEINTEGER).matches("\\s*")) {
            BigDecimal big = new BigDecimal(baseEntityAttr.get(VALUEINTEGER));
            Optional<String[]> nullableVal = Optional.of(big.toPlainString().split("[.]"));
            valueInt = nullableVal.filter(d -> d.length > 0).map(d -> Integer.valueOf(d[0])).get();
        }
        String valueStr = null;

        valueStr = valueString.get().replaceAll(REGEX_1, "");

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

}
