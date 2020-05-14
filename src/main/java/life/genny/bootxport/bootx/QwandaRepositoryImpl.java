package life.genny.bootxport.bootx;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import life.genny.qwanda.CodedEntity;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import life.genny.bootxport.xlsimport.BatchLoading;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.message.QEventLinkChangeMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.JsonUtils;

public class QwandaRepositoryImpl implements QwandaRepository {
    protected static final Logger log = LogManager.getLogger(
            MethodHandles.lookup().lookupClass().getCanonicalName());
    private static final int BATCHSIZE = 500;

    EntityManager em;

    public static final String REALM_HIDDEN = "hidden";
    Map<String, String> ddtCacheMock = new ConcurrentHashMap<>();

    public void writeToDDT(final String key, final String value) {
        ddtCacheMock.put(key, value);
    }

    private String realm;

    protected String getRealm() {

        return realm;
    }

    public void setRealm(String realm) {

        this.realm = realm;
    }

    protected EntityManager getEntityManager() {
        return em;
    }


    @Override
    public Validation upsert(Validation validation) {
        String validationRealm = validation.getRealm();
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        try {
            String code = validation.getCode();
            Validation val = null;
            val = findValidationByCode(code);
            if (val != null) {
                BeanNotNullFields copyFields = new BeanNotNullFields();
                copyFields.copyProperties(val, validation);
                val.setRealm(validationRealm);
                val = getEntityManager().merge(val);
            } else {
                throw new NoResultException();
            }
            return val;
        } catch (NoResultException | IllegalAccessException
                | InvocationTargetException e) {
            try {
                validation.setRealm(validationRealm);
                if (BatchLoading.isSynchronise()) {
                    Validation val = findValidationByCode(validation.getCode(),
                            REALM_HIDDEN);
                    if (val != null) {
                        val.setRealm(getRealm());
                        updateRealm(val);
                        return val;
                    }
                }
                em.persist(validation);
            } catch (javax.validation.ConstraintViolationException ce) {
                log.error(String.format("Error in saving attribute due to constraint issue:%s, Error:%s.", validation, ce.getLocalizedMessage()));
                log.info("Trying to update realm from hidden to genny");
                validation.setRealm("genny");
                updateRealm(validation);
            } catch (javax.persistence.PersistenceException pe) {
                log.error(String.format("Error in saving validation:%s, Error:%s.", validation, pe.getLocalizedMessage()));
            }
            Validation id = validation;
            transaction.commit();

            return id;
        }
    }


    public Validation findValidationByCode(@NotNull final String code, @NotNull final String realm) {
        ArrayList<Validation> valList = (ArrayList<Validation>) getEntityManager().createQuery(
                "SELECT a FROM Validation a where a.code=:code and a.realm=:realmStr")
                .setParameter("realmStr", realm).setParameter("code", code).getResultList();
        if (valList.isEmpty()) {
            return null;
        } else {
            return valList.get(0);
        }
    }

    public Long update(BaseEntity entity) {
        getEntityManager().createQuery(
                "update BaseEntity be set be.name =:name where be.code=:sourceCode and be.realm=:realmStr")
                .setParameter("sourceCode", entity.getCode())
                .setParameter("name", entity.getName())
                .setParameter("realmStr", getRealm()).executeUpdate();

        BaseEntity updated = this.findBaseEntityByCode(entity.getCode());
        String json = JsonUtils.toJson(updated);
        writeToDDT(entity.getCode(), json);
        return entity.getId();
    }

    public long updateRealm(BaseEntity entity) {
        return getEntityManager().createQuery(
                "update BaseEntity be set be.realm =:realm where be.code=:sourceCode")
                .setParameter("sourceCode", entity.getCode())
                .setParameter("realm", entity.getRealm()).executeUpdate();
    }

    public Long updateRealm(Attribute attr) {
        Long result = 0L;

        try {
            result = (long) getEntityManager().createQuery(
                    "update Attribute attr set attr.realm =:realm where attr.code=:code")
                    .setParameter("code", attr.getCode())
                    .setParameter("realm", attr.getRealm()).executeUpdate();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Long updateRealm(QuestionQuestion qq) {
        Long result = 0L;

        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        try {
            result = (long) getEntityManager().createQuery(
                    "delete QuestionQuestion qq where qq.pk.sourceCode=:sourceCode and qq.pk.targetCode=:targetCode")
                    .setParameter("sourceCode",
                            qq.getPk().getSource().getCode())
                    .setParameter("targetCode", qq.getPk().getTargetCode())
                    .executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        transaction.commit();
        return result;
    }

    public Long updateRealm(Validation val) {
        Long result = 0L;

        try {
            result = (long) getEntityManager().createQuery(
                    "update Validation val set val.realm =:realm where val.code=:code")
                    .setParameter("code", val.getCode())
                    .setParameter("realm", val.getRealm()).executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Attribute findAttributeByCode(@NotNull final String code,
                                         @NotNull final String realm) throws NoResultException {
        Attribute result = null;
        try {
            result = (Attribute) getEntityManager().createQuery(
                    "SELECT a FROM Attribute a where a.code=:code and a.realm=:realmStr")
                    .setParameter("realmStr", realm)
                    .setParameter("code", code.toUpperCase()).getSingleResult();

        } catch (Exception e) {
            return null;

        }
        return result;
    }

    @Override
    public Attribute upsert(Attribute attr) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        try {
            String code = attr.getCode();
            Attribute val = findAttributeByCode(code);
            if (val == null) {
                throw new NoResultException();
            }
            BeanNotNullFields copyFields = new BeanNotNullFields();
            copyFields.copyProperties(val, attr);

            val.setRealm(getRealm());

            val = getEntityManager().merge(val);
            transaction.commit();
            return val;
        } catch (NoResultException | IllegalAccessException | InvocationTargetException e) {
            attr.setRealm(getRealm());
            if (BatchLoading.isSynchronise()) {
                Attribute val =
                        findAttributeByCode(attr.getCode(), REALM_HIDDEN);
                if (val != null) {
                    val.setRealm(getRealm());
                    updateRealm(val);
                    return val;
                }
            }
            getEntityManager().persist(attr);
            transaction.commit();
            return attr;
        }
    }

    @Override
    public BaseEntity upsert(BaseEntity be) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        String s = getRealm();
        try {
            String code = be.getCode();
            BaseEntity val = findBaseEntityByCode(code);

            if (val == null) {
                throw new NoResultException();
            }
            BeanNotNullFields copyFields = new BeanNotNullFields();
            copyFields.copyProperties(val, be);
            val.setRealm(s);
            val = getEntityManager().merge(val);
            transaction.commit();
            return val;
        } catch (NoResultException | IllegalAccessException
                | InvocationTargetException | NullPointerException e) {

            if (BatchLoading.isSynchronise()) {
                BaseEntity val =
                        findBaseEntityByCode(be.getCode(), REALM_HIDDEN);
                if (val != null) {
                    val.setRealm(getRealm());
                    updateRealm(val);
                    transaction.commit();
                    return val;
                }
            }
            insert(be);

            transaction.commit();
            return be;
        }
    }

    public Long insert(BaseEntity entity) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        try {
            entity.setRealm(getRealm());
            getEntityManager().persist(entity);
            String json = JsonUtils.toJson(entity);
            writeToDDT(entity.getCode(), json);
        } catch (javax.validation.ConstraintViolationException e) {
            log.error(String.format("Cannot save BaseEntity with code:%s, Error:%s.", entity.getCode(), e.getLocalizedMessage()));
            return -1L;
        } catch (final ConstraintViolationException e) {
            log.error(String.format("Entity:%s already exists - cannot insert.", entity.getCode()));
            return entity.getId();
        } catch (final PersistenceException e) {
            return entity.getId();
        } catch (final IllegalStateException e) {
            return entity.getId();
        }
        return entity.getId();
    }

    public BaseEntity findBaseEntityByCode(
            @NotNull final String baseEntityCode,
            @NotNull final String realm) throws NoResultException {
        BaseEntity result = null;
        try {

            result = (BaseEntity) getEntityManager().createQuery(
                    "SELECT be FROM BaseEntity be where be.code=:baseEntityCode  and be.realm=:realmStr")
                    .setParameter("baseEntityCode",
                            baseEntityCode.toUpperCase())
                    .setParameter("realmStr", realm).getSingleResult();
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    @Override
    public Question upsert(Question q) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            String code = q.getCode();
            Question val = findQuestionByCode(code);
            BeanNotNullFields copyFields = new BeanNotNullFields();
            if (val == null) {
                throw new NoResultException();
            }
            copyFields.copyProperties(val, q);

            val.setRealm(getRealm());

            val = getEntityManager().merge(val);
            transaction.commit();
            return val;
        } catch (NoResultException | IllegalAccessException | InvocationTargetException e) {
            q.setRealm(getRealm());
            if (BatchLoading.isSynchronise()) {
                Question val =
                        findQuestionByCode(q.getCode(), REALM_HIDDEN);
                if (val != null) {
                    val.setRealm(getRealm());
                    updateRealm(val);
                    return val;
                }
            }
            getEntityManager().persist(q);
            transaction.commit();
            return q;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Ask> findAsksByRawAsk(final Ask ask) {
        final List<Ask> results = getEntityManager().createQuery(
                "SELECT ea FROM Ask ea where ea.targetCode=:targetCode and  ea.sourceCode=:sourceCode and ea.attributeCode=:attributeCode  and ea.realm=:realmStr")
                .setParameter("targetCode", ask.getTargetCode())
                .setParameter("sourceCode", ask.getSourceCode())
                .setParameter("attributeCode", ask.getAttributeCode())
                .setParameter("realmStr", getRealm()).getResultList();
        return results;
    }

    public Ask findAskById(final Long id) {
        Ask ret = null;

        try {
            ret = getEntityManager().find(Ask.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void sendQEventLinkChangeMessage(final QEventLinkChangeMessage event) {
        log.info("Send Link Change:" + event);
    }

    public String getToken() {
        return "DUMMY";
    }

    protected String getCurrentToken() {
        return "DUMMY_TOKEN";
    }

    public BaseEntity findBaseEntityByCode(
            @NotNull final String baseEntityCode,
            boolean includeEntityAttributes) throws NoResultException {
        String realm = getRealm();
        BaseEntity result = null;
        String userRealmStr = getRealm();
        String privacySQL = "";
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<BaseEntity> query =
                    cb.createQuery(BaseEntity.class);
            Root<BaseEntity> root = query.from(BaseEntity.class);
            query = query.select(root).where(
                    cb.equal(root.get("code"), baseEntityCode.toUpperCase()),
                    cb.equal(root.get("realm"), realm));
            result =
                    getEntityManager().createQuery(query).getSingleResult();
        } catch (Exception e) {
            try {
                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
                CriteriaQuery<BaseEntity> query =
                        cb.createQuery(BaseEntity.class);
                Root<BaseEntity> root = query.from(BaseEntity.class);

                query = query.select(root).where(
                        cb.equal(root.get("code"), baseEntityCode.toUpperCase()),
                        cb.equal(root.get("realm"), realm));
                List<BaseEntity> results =
                        getEntityManager().createQuery(query).getResultList();
                if (results.isEmpty()) {
                    throw new NoResultException("Cannot find " + baseEntityCode
                            + " in db! with realm " + realm);
                }
                result = results.get(0);
            } catch (NoResultException ee) {

                throw new NoResultException("Cannot find " + baseEntityCode
                        + " in db! with realm " + realm);

            }
        }
        return result;
    }

    @Override
    public Long insert(Ask ask) {
        try {
            Question question = null;
            BaseEntity beSource = findBaseEntityByCode(ask.getSourceCode());
            BaseEntity beTarget = findBaseEntityByCode(ask.getTargetCode());
            Attribute attribute =
                    findAttributeByCode(ask.getAttributeCode());
            Ask newAsk = null;
            if (ask.getQuestionCode() != null) {
                question = findQuestionByCode(ask.getQuestionCode());
                newAsk =
                        new Ask(question, beSource.getCode(), beTarget.getCode());
            } else {
                newAsk = new Ask(attribute.getCode(), beSource.getCode(),
                        beTarget.getCode(), attribute.getName());
            }
            newAsk.setRealm(getRealm());
            log.info("Creating new Ask " + beSource.getCode() + ":"
                    + beTarget.getCode() + ":" + attribute.getCode() + ":"
                    + (question == null ? "No Question" : question.getCode()));
            List<Ask> existingList = findAsksByRawAsk(newAsk);
            if (existingList.isEmpty()) {
                getEntityManager().persist(newAsk);
                ask.setId(newAsk.getId());
            } else {
                ask.setId(existingList.get(0).getId());
            }
        } catch (final ConstraintViolationException e) {
            Ask existing = findAskById(ask.getId());
            existing.setRealm(getRealm());
            existing = getEntityManager().merge(existing);
            return existing.getId();
        } catch (final PersistenceException e) {
            log.error("Cannot save ask with id=[" + ask.getId()
                    + " , already in system " + e.getLocalizedMessage());
            List<Ask> existingList = findAsksByRawAsk(ask);
            if (existingList.isEmpty()) {
                return 0L;
            }
            Ask existing = existingList.get(0);
            return existing.getId();
        } catch (final IllegalStateException e) {
            Ask existing = findAskById(ask.getId());
            existing.setRealm(getRealm());
            existing = getEntityManager().merge(existing);
            return existing.getId();
        }
        return ask.getId();
    }

    @Override
    public Validation findValidationByCode(@NotNull final String code)
            throws NoResultException {
        return findValidationByCode(code, getRealm());

    }

    @Override
    public Attribute findAttributeByCode(@NotNull final String code)
            throws NoResultException {
        return findAttributeByCode(code, getRealm());
    }

    @Override
    public BaseEntity findBaseEntityByCode(
            @NotNull final String baseEntityCode) throws NoResultException {
        return findBaseEntityByCode(baseEntityCode, true);

    }

    @Override
    public Long updateWithAttributes(BaseEntity entity) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        entity.setRealm(getRealm());
        try {
            entity = getEntityManager().merge(entity);
        } catch (final Exception e) {
            getEntityManager().persist(entity);
        }
        String json = JsonUtils.toJson(entity);
        writeToDDT(entity.getCode(), json);
        transaction.commit();
        return entity.getId();
    }

    @Override
    public EntityEntity findEntityEntity(final String sourceCode,
                                         final String targetCode, final String linkCode)
            throws NoResultException {
        BaseEntity source = this.findBaseEntityByCode(sourceCode);
        for (EntityEntity ee : source.getLinks()) {
            if (ee.getLink().getAttributeCode().equals(linkCode)
                    && ee.getLink().getTargetCode().equals(targetCode)) {
                return ee;
            }
        }
        throw new NoResultException("EntityEntity " + sourceCode + ":"
                + targetCode + ":" + linkCode + " not found");
    }

    @Override
    public Integer updateEntityEntity(final EntityEntity ee) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        Integer result = 0;
        try {
            String sql =
                    "update EntityEntity ee set ee.weight=:weight, ee.valueString=:valueString, ee.link.weight=:weight, ee.link.linkValue=:valueString where ee.pk.targetCode=:targetCode and ee.link.attributeCode=:linkAttributeCode and ee.link.sourceCode=:sourceCode";
            result = getEntityManager().createQuery(sql)
                    .setParameter("sourceCode",
                            ee.getPk().getSource().getCode())
                    .setParameter("linkAttributeCode",
                            ee.getLink().getAttributeCode())
                    .setParameter("targetCode", ee.getPk().getTargetCode())
                    .setParameter("weight", ee.getWeight())
                    .setParameter("valueString", ee.getValueString())
                    .executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        transaction.commit();
        return result;
    }

    @Override
    public EntityEntity insertEntityEntity(final EntityEntity ee) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        getEntityManager().persist(ee);
        QEventLinkChangeMessage msg = new QEventLinkChangeMessage(ee.getLink(), null, getCurrentToken());
        sendQEventLinkChangeMessage(msg);
        transaction.commit();
        log.info(String.format("Sent Event Link Change Msg:%s.", msg));
        return ee;
    }

    @Override
    public QuestionQuestion findQuestionQuestionByCode(final String sourceCode, final String targetCode) {
        QuestionQuestion result = null;
        try {
            result = (QuestionQuestion) getEntityManager().createQuery(
                    "SELECT qq FROM QuestionQuestion qq where qq.pk.sourceCode=:sourceCode and qq.pk.targetCode=:targetCode and qq.pk.source.realm=:realmStr")
                    .setParameter("realmStr", getRealm())
                    .setParameter("sourceCode", sourceCode)
                    .setParameter("targetCode", targetCode).getSingleResult();
        } catch (Exception e) {
            throw new NoResultException("Cannot find QQ " + sourceCode + ":" + targetCode);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Question findQuestionByCode(@NotNull final String code) {
        List<Question> result = null;
        result = getEntityManager().createQuery(
                "SELECT a FROM Question a where a.code=:code and a.realm=:realmStr")
                .setParameter("realmStr", getRealm())
                .setParameter("code", code.toUpperCase()).getResultList();

        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public QuestionQuestion upsert(QuestionQuestion qq) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        try {
            QuestionQuestion existing =
                    findQuestionQuestionByCode(qq.getPk().getSource().getCode(),
                            qq.getPk().getTargetCode());
            existing.setMandatory(qq.getMandatory());
            existing.setWeight(qq.getWeight());
            existing.setReadonly(qq.getReadonly());
            existing.setCreateOnTrigger(qq.getCreateOnTrigger());
            existing.setFormTrigger(qq.getFormTrigger());
            existing.setDisabled(qq.getDisabled());
            existing.setHidden(qq.getHidden());
            existing.setOneshot(qq.getOneshot());
            existing = getEntityManager().merge(existing);
            return existing;
        } catch (NoResultException e) {
            log.debug("------- QUESTION 00 ------------");
            getEntityManager().persist(qq);
            QuestionQuestion id = qq;
            transaction.commit();
            return id;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Question findQuestionByCode(@NotNull final String code, @NotNull final String realm) {
        List<Question> result = getEntityManager().createQuery(
                "SELECT a FROM Question a where a.code=:code and a.realm=:realmStr")
                .setParameter("realmStr", realm)
                .setParameter("code", code.toUpperCase()).getResultList();
        return result.get(0);
    }

    @Override
    public Long updateRealm(Question que) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        long result = getEntityManager().createQuery(
                "update Question que set que.realm =:realm where que.code=:code")
                .setParameter("code", que.getCode())
                .setParameter("realm", que.getRealm()).executeUpdate();
        transaction.commit();
        return result;
    }

    @Override
    public Long insert(final Question question) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        try {
            question.setRealm(getRealm());
            getEntityManager().persist(question);
            log.info(String.format("Saved question:%s. ", question.getCode()));
            transaction.commit();
        } catch (PersistenceException | IllegalStateException e) {
            Question existing = findQuestionByCode(question.getCode());
            existing.setRealm(getRealm());
            existing = getEntityManager().merge(existing);
            transaction.commit();
            return existing.getId();
        }
        return question.getId();
    }

    @Override
    public QBaseMSGMessageTemplate findTemplateByCode(@NotNull final String templateCode) {
        return (QBaseMSGMessageTemplate) getEntityManager().createQuery(
                "SELECT temp FROM QBaseMSGMessageTemplate temp where temp.code=:templateCode and temp.realm=:realmStr")
                .setParameter("realmStr", getRealm())
                .setParameter("templateCode", templateCode.toUpperCase())
                .getSingleResult();
    }

    @Override
    public QBaseMSGMessageTemplate findTemplateByCode(@NotNull final String templateCode, @NotNull final String realm) {
        return (QBaseMSGMessageTemplate) getEntityManager()
                .createQuery("SELECT temp FROM QBaseMSGMessageTemplate temp where temp.code=:templateCode and temp.realm=:realmStr")
                .setParameter("realmStr", realm)
                .setParameter("templateCode", templateCode.toUpperCase())
                .getSingleResult();
    }

    @Override
    public Long updateRealm(QBaseMSGMessageTemplate msg) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        long result = getEntityManager().createQuery(
                "update QBaseMSGMessageTemplate msg set msg.realm =:realm where msg.code=:code")
                .setParameter("code", msg.getCode())
                .setParameter("realm", msg.getRealm()).executeUpdate();
        transaction.commit();
        return result;
    }

    @Override
    public Long insert(final QBaseMSGMessageTemplate template) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        template.setRealm(getRealm());
        getEntityManager().persist(template);
        transaction.commit();
        return template.getId();
    }

    @Override
    public Long update(final QBaseMSGMessageTemplate template) {
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        template.setRealm(getRealm());
        getEntityManager().merge(template);
        transaction.commit();
        return template.getId();
    }

    public QwandaRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public <T> void delete(T entity) {
        em.remove(entity);
    }


    @Override
    public <T> List<T> queryTableByRealm(String tableName, String realm) {
        List<T> result = Collections.emptyList();
        try {
            Query query = getEntityManager().createQuery(String.format("SELECT temp FROM %s temp where temp.realm=:realmStr", tableName));
            query.setParameter("realmStr", realm);
            result = query.getResultList();
        } catch (Exception e) {
            log.error(String.format("Query table %s Error:%s".format(realm, e.getMessage())));
        }
        return result;
    }

    @Override
    public void bulkInsert(ArrayList<CodedEntity> objectList) {
        if (objectList.isEmpty()) return;

        int index = 1;
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();

        for (CodedEntity codedEntity : objectList) {
            em.persist(codedEntity);
            if (index % BATCHSIZE == 0) {
                //flush a batch of inserts and release memory:
                log.debug("BaseEntity Batch is full, flush to database.");
                em.flush();
            }
//            saveToDDT(baseEntity);
            index += 1;
        }
        transaction.commit();

    }

    @Override
    public void bulkInsertAsk(ArrayList<Ask> objectList) {

    }

    @Override
    public void bulkUpdateAsk(ArrayList<Ask> objectList, HashMap<String, Ask> mapping) {

    }

    @Override
    public void bulkUpdate(ArrayList<CodedEntity> objectList, HashMap<String, CodedEntity> mapping) {
        if (objectList.isEmpty()) return;
        BeanNotNullFields copyFields = new BeanNotNullFields();
        for (CodedEntity codedEntity : objectList) {
            if (codedEntity instanceof QuestionQuestion) {
                QuestionQuestion qq = (QuestionQuestion) codedEntity;
                String uniqCode = qq.getSourceCode() + "-" + qq.getTarketCode();
                QuestionQuestion existing = (QuestionQuestion) mapping.get(uniqCode.toUpperCase());
                existing.setMandatory(qq.getMandatory());
                existing.setWeight(qq.getWeight());
                existing.setReadonly(qq.getReadonly());
                getEntityManager().merge(existing);
            } else if (codedEntity instanceof QBaseMSGMessageTemplate) {
                codedEntity.setRealm(getRealm());
                getEntityManager().merge((QBaseMSGMessageTemplate) codedEntity);
            } else {
                CodedEntity val = mapping.get(codedEntity.getCode());
                if (val == null) {
                    // Should never raise this exception
                    throw new NoResultException(String.format("Can't find %s from database.", codedEntity.getCode()));
                }
                try {
                    copyFields.copyProperties(val, codedEntity);
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    log.error(String.format("Failed to copy Properties for %s", val.getCode()));
                }

                val.setRealm(getRealm());
                getEntityManager().merge(val);
            }
        }
    }

    private void saveToDDT(BaseEntity baseEntity) {
        String realmStr = getRealm();
        assert (realmStr.equals(baseEntity.getRealm()));
        String code = baseEntity.getCode();
        baseEntity.setRealm(realmStr);
        try {
            String json = JsonUtils.toJson(baseEntity);
            writeToDDT(baseEntity.getCode(), json);
        } catch (javax.validation.ConstraintViolationException e) {
            log.error(String.format("Cannot save BaseEntity with code:%s, Error:%s.", code, e.getLocalizedMessage()));
        } catch (final ConstraintViolationException e) {
            log.error(String.format("Entity Already exists - cannot insert:%s.", code));
        }
    }
}
