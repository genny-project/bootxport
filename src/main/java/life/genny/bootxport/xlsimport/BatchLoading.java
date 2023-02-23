package life.genny.bootxport.xlsimport;

import life.genny.bootxport.bootx.QwandaRepository;
import life.genny.bootxport.bootx.RealmUnit;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
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
import java.time.Duration;
import java.time.Instant;
import java.util.*;

class Options {
    public String optionCode = null;
    public String optionLabel = null;
}

public class BatchLoading {
    private QwandaRepository service;

    private String mainRealm = GennySettings.mainrealm;
    private static boolean isSynchronise;

    private final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    public BatchLoading(QwandaRepository repo) {
        this.service = repo;
    }

    public Map<String, DataType> dataType(Map<String, Map<String, String>> project) {
        final Map<String, DataType> dataTypeMap = new HashMap<>();

        project.entrySet().stream().filter(d -> !d.getKey().matches("\\s*")).forEach(data -> {

            Map<String, String> dataType = data.getValue();
            String validations = dataType.get("validations");
            String code = dataType.get("code");

            // should never reach. Data safe though
            if(code == null) {
                log.error("DataType Code is null for row with key: " + data.getKey());
                return;
            }
            code = code.trim().replaceAll("^\"|\"$", "");

            log.info("Processing DataType: " + code);

            String className = (dataType.get("classname")).replaceAll("^\"|\"$", "");
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
                        log.error("Could not load Validation " + validationCode + " when loading datatype: " + code);
                    }
                }
            }
            if (!dataTypeMap.containsKey(code)) {
                DataType dataTypeRecord;
                if (component == null) {
                    log.warn("No frontend \"component\" set for DataType: " + code);
                    dataTypeRecord = new DataType(className, validationList, name, inputmask);
                } else {
                    dataTypeRecord = new DataType(className, validationList, name, inputmask, component);
                }
                dataTypeRecord.setDttCode(code);
                dataTypeMap.put(code, dataTypeRecord);
            } else {
                log.error("Found duplicate DataType: " + code);
            }
        });


        return dataTypeMap;
    }

    public static boolean isSynchronise() {
        return isSynchronise;
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

    private Attribute getUrlListAttr() {
        String attrName = "Url List";
        String attrCode = "ENV_URL_LIST";
        String dttCode = "DTT_TEXT";

        ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Attribute attr = service.findAttributeByCode(attrCode);
        if (attr == null) {
            DataType dataType = new DataType(dttCode);
            dataType.setDttCode(dttCode);
            attr = new Attribute(attrCode, attrName, dataType);
        } else {
            attr.setName(attrName);
        }
        attr.setRealm(mainRealm);
        Set<ConstraintViolation<Attribute>> constraints = validator.validate(attr);
        for (ConstraintViolation<Attribute> constraint : constraints) {
            log.info(String.format("[\" %s\"] %s, %s.", this.mainRealm, constraint.getPropertyPath(), constraint.getMessage()));
        }
        return attr;
    }

    public void upsertProjectUrls(String urlList) {

        final String PROJECT_CODE = "PRJ_" + this.mainRealm.toUpperCase();
        BaseEntity be = service.findBaseEntityByCode(PROJECT_CODE);

        Attribute attr = getUrlListAttr();
        service.upsert(attr);

        try {
            be.addAttribute(attr, 0.0, urlList);
        } catch (BadDataException e) {
            log.error(String.format("BadDataException:%s", e.getMessage()));
        }
        service.updateWithAttributes(be);
    }

    private String fetchEnvOr(String env, String defaultValue) {
        String value = System.getenv(env);
        if(value == null) {
            log.error("Missing Environment Variable: " + env + ". Please set it. Defaulting to: " + defaultValue);
            return defaultValue;
        }
        return value;
    }

    public String constructKeycloakJson(final RealmUnit realm) {
        this.mainRealm = realm.getCode();
        String clientId = this.mainRealm;

        String masterRealm = fetchEnvOr("GENNY_KEYCLOAK_REALM", "internamtch");
        String keycloakUrl = null;
        String keycloakSecret = null;
        String keycloakJson = null;

        keycloakUrl = realm.getKeycloakUrl();
        keycloakSecret = realm.getClientSecret();

        // TODO: Need a better way to figure out if a clientId is private. This is bad
        if ("internmatch".equals(clientId)) {
            keycloakJson = "{\n" + "  \"realm\": \"" + masterRealm + "\",\n" + "  \"auth-server-url\": \"" + keycloakUrl
                + "/auth\",\n" + "  \"ssl-required\": \"external\",\n" + "  \"resource\": \"" + this.mainRealm + "\",\n"
                + "  \"credentials\": {\n" + "    \"secret\": \"" + keycloakSecret + "\" \n" + "  },\n"
                + "  \"policy-enforcer\": {}\n" + "}";

        } else {
            keycloakJson = "{\n" + "  \"realm\": \"" + masterRealm + "\",\n" + "  \"auth-server-url\": \"" + keycloakUrl
                + "/auth\",\n" + "  \"ssl-required\": \"external\",\n" + "  \"resource\": \"" + this.mainRealm + "\",\n"
                + "     \"public-client\": true,\n"
                + "  \"confidential-port\": 0\n" + "}";
        }

        log.info("[" + this.mainRealm + "] Loaded keycloak.json:" + keycloakJson);
        return keycloakJson;

    }

    public void persistProject(life.genny.bootxport.bootx.RealmUnit rx) {
        persistProjectOptimization(rx);
    }

    private String decodePassword(String realm, String securityKey, String servicePass) {
        // TODO: Fix the hardcoding below:
        String initVector = "PRJ_INTERNMATCH"; // "PRJ_" + realm.toUpperCase();
        initVector = StringUtils.rightPad(initVector, 16, '*');
        String decrypt = SecurityUtils.decrypt(securityKey, initVector, servicePass);
        return decrypt;
    }


    public void persistProjectOptimization(life.genny.bootxport.bootx.RealmUnit rx) {
        service.setRealm(rx.getCode());

        // String decrypt = decodePassword(rx.getCode(), rx.getSecurityKey(), rx.getServicePassword());

        String debugStr = "Time profile";
        Instant start = Instant.now();
        HashMap<String, String> userCodeUUIDMapping = new HashMap<>();

        // if (StringUtils.isEmpty(GennySettings.keycloakUserEmails)) {
        //     userCodeUUIDMapping = KeycloakUtils.getUsersByRealm(rx.getKeycloakUrl(), rx.getCode(), decrypt);
        // } else {
        //     userCodeUUIDMapping  = KeycloakUtils.getSpecificUsersByRealm(rx.getKeycloakUrl(), rx.getCode(), decrypt,
        //                            GennySettings.keycloakUserEmails);
        // }

        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished get user from keycloak, cost:" + timeElapsed.toMillis() + " millSeconds.");

        Optimization optimization = new Optimization(service);

        // clean up       
        if (GennySettings.CleanupTaskAndBeAttrForm) {
            System.out.println("Clean Task and BeAttrForm");
            service.cleanAsk(rx.getCode());
            service.cleanFrameFromBaseentityAttribute(rx.getCode());
        }

        start = Instant.now();
        optimization.validationsOptimization(rx.getValidations(), rx.getCode());
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished validations, cost:" + timeElapsed.toMillis() + " millSeconds.");

        start = Instant.now();
        log.info("Start DataType " + Duration.between(end, start) + "ms");
        Map<String, DataType> dataTypes = dataType(rx.getDataTypes());
        end = Instant.now();
        log.info("End DataType. Time elapsed/cost: " + Duration.between(start, end) + "ms");

        start = Instant.now();
        optimization.attributesOptimization(rx.getAttributes(), dataTypes, rx.getCode());
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished attribute, cost:" + timeElapsed.toMillis() + " millSeconds.");

        start = Instant.now();
        optimization.def_baseEntitysOptimization(rx.getDef_baseEntitys(), rx.getCode(), userCodeUUIDMapping);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished def_baseentity, cost:" + timeElapsed.toMillis() + " millSeconds.");

        start = Instant.now();
        optimization.def_baseEntityAttributesOptimization(rx.getDef_entityAttributes(), rx.getCode(), userCodeUUIDMapping, dataTypes);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished def_baseentity_attribute, cost:" + timeElapsed.toMillis() + " millSeconds.");

        start = Instant.now();
        optimization.baseEntitysOptimization(rx.getBaseEntitys(), rx.getCode(), userCodeUUIDMapping);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished baseentity, cost:" + timeElapsed.toMillis() + " millSeconds.");

        log.info("SKIPPING ATTRIBUTE LINK");
        // optimization.attributeLinksOptimization(rx.getAttributeLinks(), dataTypes, rx.getCode());

        start = Instant.now();
        optimization.baseEntityAttributesOptimization(rx.getEntityAttributes(), rx.getCode(), userCodeUUIDMapping);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished baseentity_attribute, cost:" + timeElapsed.toMillis() + " millSeconds.");

        start = Instant.now();
        optimization.entityEntitysOptimization(rx.getEntityEntitys(), rx.getCode(), isSynchronise, userCodeUUIDMapping);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished entity_entity, cost:" + timeElapsed.toMillis() + " millSeconds.");

        start = Instant.now();
        optimization.questionsOptimization(rx.getQuestions(), rx.getCode(), isSynchronise);
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished question, cost:" + timeElapsed.toMillis() + " millSeconds.");

        start = Instant.now();
        optimization.questionQuestionsOptimization(rx.getQuestionQuestions(), rx.getCode());
        end = Instant.now();
        timeElapsed = Duration.between(start, end);
        log.info(debugStr + " Finished question_question, cost:" + timeElapsed.toMillis() + " millSeconds.");

        optimization.asksOptimization(rx.getAsks(), rx.getCode());

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
