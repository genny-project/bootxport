package life.genny.bootxport.xlsimport;

import life.genny.bootxport.bootx.QwandaRepository;
import life.genny.qwanda.*;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.KeycloakUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.*;

public class Optimization {
    private static final Logger log = LoggerFactory.getLogger(Optimization.class);

    private QwandaRepository service;


    public Optimization(QwandaRepository repo) {
        this.service = repo;
    }

    private void printSummary(String tableName, int total, int invalid, int skipped, int updated, int newItem) {
        log.info(String.format("Table:%s: Total:%d, invalid:%d, skipped:%d, updated:%d, new item:%d.",
                tableName, total, invalid, skipped, updated, newItem));
    }

    private boolean isValid(CodedEntity t) {
        if (t == null) return false;

        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<CodedEntity>> constraints = validator.validate(t);
        for (ConstraintViolation<CodedEntity> constraint : constraints) {
            log.error(String.format("Validates constraints failure, Code:%s, PropertyPath:%s,Error:%s.",
                    t.getCode(), constraint.getPropertyPath(), constraint.getMessage()));
        }
        return constraints.isEmpty();
    }

    // Check if sheet data changed
    //TODO
    private <T> boolean isChanged(T orgItem, T newItem) {
        return true;
    }

    public void asksOptimization(Map<String, Map<String, String>> project, String realmName) {
        // Get all asks
        String tableName = "Ask";
        List<Ask> askFromDB = service.queryTableByRealm(tableName, realmName);

        HashMap<String, Ask> codeAskMapping = new HashMap<>();
        for (Ask ask : askFromDB) {
            String targetCode = ask.getTargetCode();
            String sourceCode = ask.getSourceCode();
            String attributeCode = ask.getAttributeCode();
            String questionCode = ask.getQuestionCode();
            String uniqueCode = questionCode + "-" + sourceCode + "-" + targetCode + "-" + attributeCode;
            codeAskMapping.put(uniqueCode, ask);
        }

        tableName = "Question";
        List<Question> questionsFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, Question> questionHashMap = new HashMap<>();

        for (Question q : questionsFromDB) {
            questionHashMap.put(q.getCode(), q);
        }

        ArrayList<Ask> askInsertList = new ArrayList<>();
        ArrayList<Ask> askUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            Map<String, String> asks = entry.getValue();
            Ask ask = GoogleSheetBuilder.buildAsk(asks, realmName, questionHashMap);
            if (ask == null) {
                invalid++;
                continue;
            }
            service.insert(ask);
            newItem++;
        }
        printSummary("Ask", total, invalid, skipped, updated, newItem);


//            String qCode = asks.get("question_code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
//            String attributeCode = asks.get("attributeCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
//            String sourceCode = asks.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
//            String targetCode = asks.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
//            String uniqueCode = qCode + "-" + sourceCode + "-" + targetCode + "-" + attributeCode;
//            if (codeAskMapping.containsKey(uniqueCode.toUpperCase())) {
//                if (isChanged(ask, codeAskMapping.get(uniqueCode.toUpperCase()))) {
//                    askUpdateList.add(ask);
//                    updated++;
//                }
//                skipped++;
//            } else {
//                 insert new item
//                askInsertList.add(ask);
//                newItem++;
//            }
//        }
//        service.bulkInsertAsk(askInsertList);
//        service.bulkUpdateAsk(askUpdateList, codeAskMapping);
//        printSummary(tableName, total, invalid, skipped, updated, newItem);
    }

    public void attributeLinksOptimization
            (Map<String, Map<String, String>> project, Map<String, DataType> dataTypeMap, String realmName) {
        String tableName = "Attribute";
        List<Attribute> attributeLinksFromDB = service.queryTableByRealm(tableName, realmName);

        HashSet<String> codeSet = new HashSet<>();
        HashMap<String, CodedEntity> codeAttributeMapping = new HashMap<>();

        for (Attribute attr : attributeLinksFromDB) {
            codeSet.add(attr.getCode());
            codeAttributeMapping.put(attr.getCode(), attr);
        }

        ArrayList<CodedEntity> attributeLinkInsertList = new ArrayList<>();
        ArrayList<CodedEntity> attributeLinkUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            Map<String, String> attributeLink = entry.getValue();
            String code = attributeLink.get("code").replaceAll("^\"|\"$", "");
            AttributeLink attrlink = GoogleSheetBuilder.buildAttributeLink(attributeLink, dataTypeMap, realmName, code);
            // validation check
            if (isValid(attrlink)) {
                if (codeSet.contains(code.toUpperCase())) {
                    if (isChanged(attrlink, codeAttributeMapping.get(code.toUpperCase()))) {
                        attributeLinkUpdateList.add(attrlink);
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    // insert new item
                    attributeLinkInsertList.add(attrlink);
                    newItem++;
                }
            } else {
                invalid++;
            }
        }

        service.bulkInsert(attributeLinkInsertList);
        service.bulkUpdate(attributeLinkUpdateList, codeAttributeMapping);
        printSummary("AttributeLink", total, invalid, skipped, updated, newItem);
    }

    public void attributesOptimization(Map<String, Map<String, String>> project,
                                       Map<String, DataType> dataTypeMap, String realmName) {
        String tableName = "Attribute";
        List<Attribute> attributesFromDB = service.queryTableByRealm(tableName, realmName);

        HashMap<String, CodedEntity> codeAttributeMapping = new HashMap<>();

        for (Attribute attr : attributesFromDB) {
            codeAttributeMapping.put(attr.getCode(), attr);
        }

        ArrayList<CodedEntity> attributeInsertList = new ArrayList<>();
        ArrayList<CodedEntity> attributeUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> data : project.entrySet()) {
            total += 1;
            Map<String, String> attributes = data.getValue();
            String code = attributes.get("code").replaceAll("^\"|\"$", "");

            Attribute attr = GoogleSheetBuilder.buildAttrribute(attributes, dataTypeMap, realmName, code);

            // validation check
            if (isValid(attr)) {
                if (codeAttributeMapping.containsKey(code.toUpperCase())) {
                    if (isChanged(attr, codeAttributeMapping.get(code.toUpperCase()))) {
                        attributeUpdateList.add(attr);
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    // insert new item
                    attributeInsertList.add(attr);
                    newItem++;
                }
            } else {
                invalid++;
            }
        }

        service.bulkInsert(attributeInsertList);
        service.bulkUpdate(attributeUpdateList, codeAttributeMapping);
        printSummary(tableName, total, invalid, skipped, updated, newItem);
    }

    public void baseEntityAttributesOptimization(Map<String, Map<String, String>> project, String realmName,
                                                 HashMap<String, String> userCodeUUIDMapping) {
        // Get all BaseEntity
        String tableName = "BaseEntity";
        List<BaseEntity> baseEntityFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, BaseEntity> beHashMap = new HashMap<>();
        for (BaseEntity be : baseEntityFromDB) {
            beHashMap.put(be.getCode(), be);
        }

        // Get all Attribute
        tableName = "Attribute";
        List<Attribute> attributeFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, Attribute> attrHashMap = new HashMap<>();
        for (Attribute attribute : attributeFromDB) {
            attrHashMap.put(attribute.getCode(), attribute);
        }

        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total++;
            Map<String, String> baseEntityAttr = entry.getValue();

            String baseEntityCode = GoogleSheetBuilder.getBaseEntityCodeFromBaseEntityAttribute(baseEntityAttr,
                    userCodeUUIDMapping);
            if (baseEntityCode == null) {
                invalid++;
                continue;
            }
            String attributeCode = GoogleSheetBuilder.getAttributeCodeFromBaseEntityAttribute(baseEntityAttr);
            if (attributeCode == null) {
                invalid++;
                continue;
            }

            BaseEntity be = GoogleSheetBuilder.buildEntityAttribute(baseEntityAttr, realmName, attrHashMap, beHashMap,
                    userCodeUUIDMapping);
            if (be != null) {
                service.updateWithAttributes(be);
                newItem++;
            } else {
                invalid++;
            }
        }
        printSummary("BaseEntityAttributes", total, invalid, skipped, updated, newItem);
    }

    public void baseEntitysOptimization(Map<String, Map<String, String>> project, String realmName,
                                        HashMap<String, String> userCodeUUIDMapping) {
        String tableName = "BaseEntity";
        List<BaseEntity> baseEntityFromDB = service.queryTableByRealm(tableName, realmName);

        HashMap<String, CodedEntity> codeBaseEntityMapping = new HashMap<>();

        for (BaseEntity be : baseEntityFromDB) {
            codeBaseEntityMapping.put(be.getCode(), be);
        }

        ArrayList<CodedEntity> baseEntityInsertList = new ArrayList<>();
        ArrayList<CodedEntity> baseEntityUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            Map<String, String> baseEntitys = entry.getValue();
            String code = baseEntitys.get("code").replaceAll("^\"|\"$", "");
            BaseEntity baseEntity = GoogleSheetBuilder.buildBaseEntity(baseEntitys, realmName);
            // validation check
            if (isValid(baseEntity)) {
                // get keycloak uuid from keycloak, replace code and beasentity
                if (baseEntity.getCode().startsWith("PER_")) {
                    String keycloakUUID = KeycloakUtils.getKeycloakUUIDByUserCode(baseEntity.getCode(), userCodeUUIDMapping);
                    baseEntity.setCode(keycloakUUID);
                }

                if (codeBaseEntityMapping.containsKey(baseEntity.getCode())) {
                    if (isChanged(baseEntity, codeBaseEntityMapping.get(baseEntity.getCode()))) {
                        baseEntityUpdateList.add(baseEntity);
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    // insert new item
                    baseEntityInsertList.add(baseEntity);
                    newItem++;
                }
            } else {
                invalid++;
            }
        }
        service.bulkInsert(baseEntityInsertList);
        service.bulkUpdate(baseEntityUpdateList, codeBaseEntityMapping);
        printSummary(tableName, total, invalid, skipped, updated, newItem);
    }

    public void entityEntitysOptimization(Map<String, Map<String, String>> project, String realmName,
                                          boolean isSynchronise, HashMap<String, String> userCodeUUIDMapping) {
        // Get all BaseEntity
        String tableName = "BaseEntity";
        List<BaseEntity> baseEntityFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, BaseEntity> beHashMap = new HashMap<>();
        for (BaseEntity be : baseEntityFromDB) {
            beHashMap.put(be.getCode(), be);
        }

        // Get all Attribute
        tableName = "Attribute";
        List<Attribute> attributeFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, Attribute> attrHashMap = new HashMap<>();
        for (Attribute attribute : attributeFromDB) {
            attrHashMap.put(attribute.getCode(), attribute);
        }

        tableName = "EntityEntity";
        List<EntityEntity> entityEntityFromDB = service.queryTableByRealm(tableName, realmName);

        HashMap<String, EntityEntity> codeBaseEntityEntityMapping = new HashMap<>();
        for (EntityEntity entityEntity : entityEntityFromDB) {
            String beCode = entityEntity.getPk().getSource().getCode();
            String attrCode = entityEntity.getPk().getAttribute().getCode();
            String targetCode = entityEntity.getPk().getTargetCode();
            if (targetCode.toUpperCase().startsWith("PER_")) {
               targetCode = KeycloakUtils.getKeycloakUUIDByUserCode(targetCode.toUpperCase(), userCodeUUIDMapping);
            }
            String uniqueCode = beCode + "-" + attrCode + "-" + targetCode;
            codeBaseEntityEntityMapping.put(uniqueCode, entityEntity);
        }

        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total++;
            Map<String, String> entEnts = entry.getValue();
            String linkCode = entEnts.get("linkCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            if (linkCode == null)
                linkCode = entEnts.get("code".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String parentCode = entEnts.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (parentCode == null)
                parentCode = entEnts.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String targetCode = entEnts.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (targetCode.toUpperCase().startsWith("PER_")) {
                targetCode = KeycloakUtils.getKeycloakUUIDByUserCode(targetCode.toUpperCase(), userCodeUUIDMapping);
            }

            String weightStr = entEnts.get("weight");
            String valueString = entEnts.get("valueString".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            Optional<String> weightStrOpt = Optional.ofNullable(weightStr);
            final Double weight = weightStrOpt.filter(d -> !d.equals(" ")).map(Double::valueOf).orElse(0.0);

            Attribute linkAttribute = attrHashMap.get(linkCode.toUpperCase());
            BaseEntity sbe = beHashMap.get(parentCode.toUpperCase());
            BaseEntity tbe = beHashMap.get(targetCode.toUpperCase());
            if (linkAttribute == null) {
                log.error("EntityEntity Link code:" + linkCode + " doesn't exist in Attribute table.");
                invalid++;
                continue;
            } else if (sbe == null) {
                log.error("EntityEntity parent code:" + parentCode + " doesn't exist in BaseEntity table.");
                invalid++;
                continue;
            } else if (tbe == null) {
                log.error("EntityEntity target Code:" + targetCode + " doesn't exist in BaseEntity table.");
                invalid++;
                continue;
            }

            String code = parentCode + "-" + linkCode + "-" + targetCode;
            if (isSynchronise) {
                if (codeBaseEntityEntityMapping.containsKey(code.toUpperCase())) {
                    EntityEntity ee = codeBaseEntityEntityMapping.get(code.toUpperCase());
                    ee.setWeight(weight);
                    ee.setValueString(valueString);
                    service.updateEntityEntity(ee);
                    updated++;
                } else {
                    EntityEntity ee = new EntityEntity(sbe, tbe, linkAttribute, weight);
                    ee.setValueString(valueString);
                    service.insertEntityEntity(ee);
                    newItem++;
                }
            } else {
                try {
                    sbe.addTarget(tbe, linkAttribute, weight, valueString);
                    service.updateWithAttributes(sbe);
                    newItem++;
                } catch (BadDataException be) {
                    log.error(String.format("Should never reach here!, BaseEntity:%s, Attribute:%s ", tbe.getCode(), linkAttribute.getCode()));
                }
            }
        }
        printSummary("EntityEntity", total, invalid, skipped, updated, newItem);
    }

    public void messageTemplatesOptimization(Map<String, Map<String, String>> project, String realmName) {
        String tableName = "QBaseMSGMessageTemplate";
        List<QBaseMSGMessageTemplate> qBaseMSGMessageTemplateFromDB = service.queryTableByRealm(tableName, realmName);

        HashMap<String, CodedEntity> codeMsgMapping = new HashMap<>();
        for (QBaseMSGMessageTemplate message : qBaseMSGMessageTemplateFromDB) {
            codeMsgMapping.put(message.getCode(), message);
        }

        ArrayList<CodedEntity> messageInsertList = new ArrayList<>();
        ArrayList<CodedEntity> messageUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> data : project.entrySet()) {
            total += 1;
            Map<String, String> template = data.getValue();
            String code = template.get("code");
            String name = template.get("name");
            if (StringUtils.isBlank(name)) {
                log.error("Templates:" + code + "has EMPTY name.");
                invalid += 1;
                continue;
            }

            QBaseMSGMessageTemplate msg = GoogleSheetBuilder.buildQBaseMSGMessageTemplate(template, realmName);
            if (codeMsgMapping.containsKey(code.toUpperCase())) {
                if (isChanged(msg, codeMsgMapping.get(code.toUpperCase()))) {
                    messageUpdateList.add(msg);
                    updated++;
                } else {
                    skipped++;
                }
            } else {
                // insert new item
                messageInsertList.add(msg);
                newItem++;
            }
        }
        service.bulkInsert(messageInsertList);
        service.bulkUpdate(messageUpdateList, codeMsgMapping);
        printSummary(tableName, total, invalid, skipped, updated, newItem);
    }

    public void questionQuestionsOptimization(Map<String, Map<String, String>> project, String realmName) {
        String tableName = "Question";
        List<Question> questionFromDB = service.queryTableByRealm(tableName, realmName);
        HashSet<String> questionCodeSet = new HashSet<>();
        HashMap<String, Question> questionHashMap = new HashMap<>();

        for (Question question : questionFromDB) {
            questionCodeSet.add(question.getCode());
            questionHashMap.put(question.getCode(), question);
        }

        tableName = "QuestionQuestion";
        List<QuestionQuestion> questionQuestionFromDB = service.queryTableByRealm(tableName, realmName);

        HashMap<String, QuestionQuestion> codeQuestionMapping = new HashMap<>();

        for (QuestionQuestion qq : questionQuestionFromDB) {
            String sourceCode = qq.getSourceCode();
            String targetCode = qq.getTarketCode();
            String uniqCode = sourceCode + "-" + targetCode;
            codeQuestionMapping.put(uniqCode, qq);
        }

        ArrayList<QuestionQuestion> questionQuestionInsertList = new ArrayList<>();
        ArrayList<QuestionQuestion> questionQuestionUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            Map<String, String> queQues = entry.getValue();

            QuestionQuestion qq = GoogleSheetBuilder.buildQuestionQuestion(queQues, realmName, questionHashMap);
            if (qq == null) {
                invalid++;
                continue;
            }

            String parentCode = queQues.get("parentCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            if (parentCode == null) {
                parentCode = queQues.get("sourceCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));
            }

            String targetCode = queQues.get("targetCode".toLowerCase().replaceAll("^\"|\"$|_|-", ""));

            String uniqueCode = parentCode + "-" + targetCode;
            if (codeQuestionMapping.containsKey(uniqueCode.toUpperCase())) {
                if (isChanged(qq, codeQuestionMapping.get(uniqueCode.toUpperCase()))) {
                    questionQuestionUpdateList.add(qq);
                    updated++;
                } else {
                    skipped++;
                }
            } else {
                // insert new item
                questionQuestionInsertList.add(qq);
                newItem++;
            }
        }
        service.bulkInsertQuestionQuestion(questionQuestionInsertList);
        service.bulkUpdateQuestionQuestion(questionQuestionUpdateList, codeQuestionMapping);
        printSummary("QuestionQuestion", total, invalid, skipped, updated, newItem);
    }

    public void questionsOptimization(Map<String, Map<String, String>> project, String realmName, boolean isSynchronise) {
        // Get all questions from database
        String tableName = "Question";
        String mainRealm = GennySettings.mainrealm;
        List<Question> questionsFromDBMainRealm = new ArrayList<>();
        HashMap<String, Question> codeQuestionMappingMainRealm = new HashMap<>();

        if (!realmName.equals(mainRealm)) {
            questionsFromDBMainRealm = service.queryTableByRealm(tableName, mainRealm);
            for (Question q : questionsFromDBMainRealm) {
                codeQuestionMappingMainRealm.put(q.getCode(), q);
            }
        }

        List<Question> questionsFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, Question> codeQuestionMapping = new HashMap<>();

        for (Question q : questionsFromDB) {
            codeQuestionMapping.put(q.getCode(), q);
        }

        // Get all Attributes from database
        tableName = "Attribute";
        List<Attribute> attributesFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, Attribute> attributeHashMap = new HashMap<>();

        for (Attribute attribute : attributesFromDB) {
            attributeHashMap.put(attribute.getCode(), attribute);
        }

        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> rawData : project.entrySet()) {
            total += 1;
            if (rawData.getKey().isEmpty()) {
                skipped += 1;
                continue;
            }

            Map<String, String> questions = rawData.getValue();
            String code = questions.get("code");

            Question question = GoogleSheetBuilder.buildQuestion(questions, attributeHashMap, realmName);
            if (question == null) {
                invalid++;
                continue;
            }

            Question existing = codeQuestionMapping.get(code.toUpperCase());
            if (existing == null) {
                if (isSynchronise) {
                    Question val = codeQuestionMappingMainRealm.get(code.toUpperCase());
                    if (val != null) {
                        val.setRealm(realmName);
                        service.updateRealm(val);
                        updated++;
                        continue;
                    }
                }
                service.insert(question);
                newItem++;
            } else {
                String name = questions.get("name");
                String html = questions.get("html");
                String directions = questions.get("directions");
                String helper = questions.get("helper");
                String icon = question.getIcon();
                existing.setName(name);
                existing.setHtml(html);
                existing.setDirections(directions);
                existing.setHelper(helper);
                existing.setIcon(icon);

                String oneshotStr = questions.get("oneshot");
                String readonlyStr = questions.get(GoogleSheetBuilder.READONLY);
                String mandatoryStr = questions.get(GoogleSheetBuilder.MANDATORY);
                boolean oneshot = GoogleSheetBuilder.getBooleanFromString(oneshotStr);
                boolean readonly = GoogleSheetBuilder.getBooleanFromString(readonlyStr);
                boolean mandatory = GoogleSheetBuilder.getBooleanFromString(mandatoryStr);
                existing.setOneshot(oneshot);
                existing.setReadonly(readonly);
                existing.setMandatory(mandatory);
                service.upsert(existing, codeQuestionMapping);
                updated++;
            }
        }
        printSummary("Question", total, invalid, skipped, updated, newItem);
    }

    public void validationsOptimization(Map<String, Map<String, String>> project, String realmName) {
        String tableName = "Validation";
        // Get existing validation by realm from database
        List<Validation> validationsFromDB = service.queryTableByRealm(tableName, realmName);

        // Unique code set
        HashSet<String> codeSet = new HashSet<>();
        // Code to validation object mapping
        HashMap<String, CodedEntity> codeValidationMapping = new HashMap<>();

        for (Validation vld : validationsFromDB) {
            codeSet.add(vld.getCode());
            codeValidationMapping.put(vld.getCode(), vld);
        }

        ArrayList<CodedEntity> validationInsertList = new ArrayList<>();
        ArrayList<CodedEntity> validationUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map<String, String> validations : project.values()) {
            total += 1;
            String code = validations.get("code").replaceAll("^\"|\"$", "");
            Validation val = GoogleSheetBuilder.buildValidation(validations, realmName, code);

            // validation check
            if (isValid(val)) {
                if (codeSet.contains(code.toUpperCase())) {
                    if (isChanged(val, codeValidationMapping.get(code.toUpperCase()))) {
                        validationUpdateList.add(val);
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    validationInsertList.add(val);
                    newItem++;
                }
            } else {
                invalid += 1;
            }
        }
        service.bulkInsert(validationInsertList);
        service.bulkUpdate(validationUpdateList, codeValidationMapping);
        printSummary(tableName, total, invalid, skipped, updated, newItem);
    }

    public void def_baseEntityAttributesOptimization(Map<String, Map<String, String>> project, String realmName,
                                                 HashMap<String, String> userCodeUUIDMapping) {
        // Get all BaseEntity
        String tableName = "BaseEntity";
        List<BaseEntity> baseEntityFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, BaseEntity> beHashMap = new HashMap<>();
        for (BaseEntity be : baseEntityFromDB) {
            beHashMap.put(be.getCode(), be);
        }

        // Get all Attribute
        tableName = "Attribute";
        List<Attribute> attributeFromDB = service.queryTableByRealm(tableName, realmName);
        HashMap<String, Attribute> attrHashMap = new HashMap<>();
        for (Attribute attribute : attributeFromDB) {
            attrHashMap.put(attribute.getCode(), attribute);
        }

        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;


        List<BaseEntity> baseEntities = new ArrayList<>();
        // Attribute code start with ATT_
        ArrayList<CodedEntity> virtualDefAttribute = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total++;
            Map<String, String> baseEntityAttr = entry.getValue();

            String baseEntityCode = GoogleSheetBuilder.getBaseEntityCodeFromBaseEntityAttribute(baseEntityAttr,
                    userCodeUUIDMapping);
            if (baseEntityCode == null) {
                invalid++;
                continue;
            }

            String attributeCode = GoogleSheetBuilder.getAttributeCodeFromBaseEntityAttribute(baseEntityAttr);
            if (attributeCode == null) {
                invalid++;
                continue;
            } else if(attributeCode.startsWith("ATT")) {
                String trimedAttrCode = attributeCode.replaceFirst("ATT_", "");
                // check if real attribute exist
                if(attrHashMap.get(trimedAttrCode.toUpperCase()) == null) {
                    log.error("Found DEF attribute:" + attributeCode + ", but real attribute code:" +  trimedAttrCode + " does not exist");
                    invalid++;
                    continue;
                } else {
                    // ATT_ doesn't exist in database, create and persist
                    if (!attrHashMap.containsKey(attributeCode)) {
                        Attribute virtualAttr = new Attribute(attributeCode, attributeCode, new DataType(String.class));
                        virtualDefAttribute.add(virtualAttr);
                        attrHashMap.put(attributeCode, virtualAttr);
                        log.debug("Create new virtual Attribute:" + attributeCode);
                    }
                }
            }

            BaseEntity be = GoogleSheetBuilder.buildEntityAttribute(baseEntityAttr, realmName, attrHashMap, beHashMap,
                    userCodeUUIDMapping);
            if (be != null) {
                baseEntities.add(be);
                newItem++;
            } else {
                invalid++;
            }
        }
        service.bulkInsert(virtualDefAttribute);
        service.bulkUpdateWithAttributes(baseEntities);
        printSummary("BaseEntityAttributes", total, invalid, skipped, updated, newItem);
    }

    public void def_baseEntitysOptimization(Map<String, Map<String, String>> project, String realmName,
                                        HashMap<String, String> userCodeUUIDMapping) {

        log.info("Processing DEF_Baseentity data");
        String tableName = "BaseEntity";
        List<BaseEntity> baseEntityFromDB = service.queryTableByRealm(tableName, realmName);

        HashMap<String, CodedEntity> codeBaseEntityMapping = new HashMap<>();

        for (BaseEntity be : baseEntityFromDB) {
            codeBaseEntityMapping.put(be.getCode(), be);
        }

        ArrayList<CodedEntity> baseEntityInsertList = new ArrayList<>();
        ArrayList<CodedEntity> baseEntityUpdateList = new ArrayList<>();
        int invalid = 0;
        int total = 0;
        int skipped = 0;
        int newItem = 0;
        int updated = 0;

        for (Map.Entry<String, Map<String, String>> entry : project.entrySet()) {
            total += 1;
            Map<String, String> baseEntitys = entry.getValue();
            BaseEntity baseEntity = GoogleSheetBuilder.buildBaseEntity(baseEntitys, realmName);
            // validation check
            if (isValid(baseEntity)) {
                // get keycloak uuid from keycloak, replace code and beasentity
                if (baseEntity.getCode().startsWith("PER_")) {
                    String keycloakUUID = KeycloakUtils.getKeycloakUUIDByUserCode(baseEntity.getCode(), userCodeUUIDMapping);
                    baseEntity.setCode(keycloakUUID);
                }

                if (codeBaseEntityMapping.containsKey(baseEntity.getCode())) {
                    if (isChanged(baseEntity, codeBaseEntityMapping.get(baseEntity.getCode()))) {
                        baseEntityUpdateList.add(baseEntity);
                        updated++;
                    } else {
                        skipped++;
                    }
                } else {
                    // insert new item
                    baseEntityInsertList.add(baseEntity);
                    newItem++;
                }
            } else {
                invalid++;
            }
        }
        service.bulkInsert(baseEntityInsertList);
        service.bulkUpdate(baseEntityUpdateList, codeBaseEntityMapping);
        printSummary(tableName, total, invalid, skipped, updated, newItem);
    }
}
