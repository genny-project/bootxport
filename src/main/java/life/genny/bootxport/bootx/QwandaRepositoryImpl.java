package life.genny.bootxport.bootx;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import life.genny.bootxport.xlsimport.BatchLoading;
import life.genny.qwanda.Ask;
import life.genny.qwanda.CodedEntity;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.message.QEventLinkChangeMessage;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.utils.VertxUtils;
import life.genny.qwandautils.BeanNotNullFields;
import org.mortbay.log.Log;


public class QwandaRepositoryImpl implements QwandaRepository {
    protected static final Logger log = LogManager.getLogger(
            MethodHandles.lookup().lookupClass().getCanonicalName());
    private static final int BATCHSIZE = 500;

    EntityManager em;

    public static final String REALM_HIDDEN = "hidden";
    Map<String, String> ddtCacheMock = new ConcurrentHashMap<>();

    ValidatorFactory factory = javax.validation.Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();


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
        EntityTransaction transaction = em.getTransaction();
        if (!transaction.isActive()) transaction.begin();
        return em;
    }


    @Override
    public Validation upsert(Validation validation) {
        try {
            String code = validation.getCode();
            Validation val = null;

            val = findValidationByCode(code);
            if (val != null) {
                BeanNotNullFields copyFields = new BeanNotNullFields();
                copyFields.copyProperties(val, validation);

                val.setRealm(getRealm());

                val = getEntityManager().merge(val);
            } else {
                throw new NoResultException();
            }
            return val;
        } catch (NoResultException | IllegalAccessException | InvocationTargetException e) {
            try {

                validation.setRealm(getRealm());
                if (BatchLoading.isSynchronise()) {
                    Validation val = findValidationByCode(validation.getCode(), REALM_HIDDEN);
                    if (val != null) {
                        val.setRealm(getRealm());
                        updateRealm(val);
                        return val;
                    }
                }

                getEntityManager().persist(validation);
            } catch (javax.validation.ConstraintViolationException ce) {
                log.error("Error in saving attribute due to constraint issue:" + validation + " :"
                        + ce.getLocalizedMessage());

                log.info("Trying to update realm from hidden to genny");
                validation.setRealm("genny");
                updateRealm(validation);

            } catch (javax.persistence.PersistenceException pe) {
                log.error("Error in saving validation :" + validation + " :" + pe.getLocalizedMessage());
            }

            Validation id = validation;
            return id;
        }
    }


    public Validation findValidationByCode(@NotNull final String code, @NotNull final String realm)
            throws NoResultException {
        Validation result = null;
        try {
            result = (Validation) getEntityManager()
                    .createQuery("SELECT a FROM Validation a where a.code=:code and a.realm=:realmStr")
                    .setParameter("realmStr", realm).setParameter("code", code).getSingleResult();
        } catch (Exception e) {
            return null;
        }

        return result;
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

    public Attribute findAttributeByCode(@NotNull final String code, @NotNull final String realm)
            throws NoResultException {
        Attribute result = null;
//		List<Attribute> results = null;
        String cleanCode = code.trim().toUpperCase();
        try {
            result = (Attribute) getEntityManager()
                    .createQuery("SELECT a FROM Attribute a where a.code=:code and a.realm=:realmStr")
                    .setParameter("realmStr", realm).setParameter("code", cleanCode).getSingleResult();

        } catch (Exception e) {
            // throw new NoResultException("Attribute Code :"+code+" not found in db");
        }
//		if (results == null || results.isEmpty()) {
//			return null;
//		} else {
//			return results.get(0); // return first one for now TODO
//		}
        return result;
    }

    @Override
    public Attribute upsert(Attribute attr) {
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
            return val;
        } catch (NoResultException | IllegalAccessException | InvocationTargetException e) {
            try {

                attr.setRealm(getRealm());
                if (BatchLoading.isSynchronise()) {
                    Attribute val = findAttributeByCode(attr.getCode(), REALM_HIDDEN);
                    if (val != null) {
                        val.setRealm(getRealm());
                        updateRealm(val);
                        return val;
                    }
                }

                getEntityManager().persist(attr);
            } catch (javax.validation.ConstraintViolationException ce) {
                log.error(
                        "Error in saving attribute due to constraint issue:" + attr + " :" + ce.getLocalizedMessage());
            } catch (javax.persistence.PersistenceException pe) {
                log.error("Error in saving attribute :" + attr + " :" + pe.getLocalizedMessage());
            }
            Long id = attr.getId();
            return attr;
        }
    }

    @Override
    public BaseEntity upsert(BaseEntity be) {
        String realm = getRealm();
        try {
            String code = be.getCode();
            BaseEntity val = findBaseEntityByCode(code);

//			Session session = getEntityManager().unwrap(org.hibernate.Session.class);
//			Criteria criteria = session.createCriteria(BaseEntity.class);
//			BaseEntity val = (BaseEntity)criteria
//					.add(Restrictions.eq("code", code))
//					.add(Restrictions.eq("realm", realm))
//			                             .uniqueResult();

            if (val == null) {
                throw new NoResultException();
            }
            BeanNotNullFields copyFields = new BeanNotNullFields();
            copyFields.copyProperties(val, be);
            // val.setRealm(realm);
            // log.debug("***********" + val);
            val.merge(be);

            val = getEntityManager().merge(val);

            return val;
        } catch (NoResultException | NullPointerException | IllegalAccessException | InvocationTargetException e) {

            if (BatchLoading.isSynchronise()) {
                BaseEntity val = findBaseEntityByCode(be.getCode(), REALM_HIDDEN);
                if (val != null) {
                    val.setRealm(getRealm());
                    updateRealm(val);
                    return val;
                }
            }
            Long id = insert(be);

            return be;
        }
    }

    public Long insert(BaseEntity entity) {

        // always check if baseentity exists through check for unique code
        try {
            if (StringUtils.isBlank(entity.getName())) {
                entity.setName(entity.getCode());
            }
            entity.setRealm(getRealm());

            getEntityManager().persist(entity);
            String json = JsonUtils.toJson(entity);
            writeToDDT(entity.getCode(), json);
        } catch (javax.validation.ConstraintViolationException e) {
            log.error("Cannot save BaseEntity with code " + entity.getCode() + "! " + e.getLocalizedMessage());
            return -1L;
        } catch (final ConstraintViolationException e) {
            log.error("Entity Already exists - cannot insert" + entity.getCode());
            return entity.getId();
        } catch (final PersistenceException e) {
            return entity.getId();
        } catch (final IllegalStateException e) {
            return entity.getId();
        }
        return entity.getId();
    }

    public BaseEntity findBaseEntityByCode(@NotNull final String baseEntityCode, @NotNull final String realm)
            throws NoResultException {

        BaseEntity result = null;

        try {

            result = (BaseEntity) getEntityManager()
                    .createQuery("SELECT be FROM BaseEntity be where be.code=:baseEntityCode  and be.realm=:realmStr")
                    .setParameter("baseEntityCode", baseEntityCode.toUpperCase()).setParameter("realmStr", realm)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }

        return result;
    }

    @Override
    public Question upsert(Question q, HashMap<String, Question> mapping) {
        try {
            String code = q.getCode();
            Question val = mapping.get(code);
            BeanNotNullFields copyFields = new BeanNotNullFields();
            if (val == null) {
                throw new NoResultException();
            }
            copyFields.copyProperties(val, q);

            val.setRealm(getRealm());
            Set<ConstraintViolation<Question>> constraints = validator.validate(val);
            for (ConstraintViolation<Question> constraint : constraints) {
                log.error(constraint.getPropertyPath() + " " + constraint.getMessage());
            }
            if (constraints.isEmpty()) {
                val = getEntityManager().merge(val);
                return val;
            } else {
                log.error("Error in Hibernate Validation for quesiton "+q.getCode()+" with attribute code :"+q.getAttributeCode());
            }
            return null; // TODO throw an error

        } catch (NoResultException | IllegalAccessException | InvocationTargetException e) {
            try {

                q.setRealm(getRealm());
                if (BatchLoading.isSynchronise()) {
                    Question val = findQuestionByCode(q.getCode(), REALM_HIDDEN);
                    if (val != null) {
                        val.setRealm(getRealm());
                        updateRealm(val);
                        return val;
                    }
                }

                getEntityManager().persist(q);
            } catch (javax.validation.ConstraintViolationException ce) {
                log.error("Error in saving question due to constraint issue:" + q + " :" + ce.getLocalizedMessage());
            } catch (javax.persistence.PersistenceException pe) {
                log.error("Error in saving question :" + q + " :" + pe.getLocalizedMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Long id = q.getId();
            return q;
        }
    }

    @Override
    public Question upsert(Question q) {
        try {
            String code = q.getCode();
            Question val = findQuestionByCode(code);
            BeanNotNullFields copyFields = new BeanNotNullFields();
            if (val == null) {
                throw new NoResultException();
            }
            copyFields.copyProperties(val, q);

            val.setRealm(getRealm());
            Set<ConstraintViolation<Question>> constraints = validator.validate(val);
            for (ConstraintViolation<Question> constraint : constraints) {
                log.error(constraint.getPropertyPath() + " " + constraint.getMessage());
            }
            if (constraints.isEmpty()) {
                val = getEntityManager().merge(val);
                return val;
            } else {
                log.error("Error in Hibernate Validation for quesiton "+q.getCode()+" with attribute code :"+q.getAttributeCode());
            }
            return null; // TODO throw an error

        } catch (NoResultException | IllegalAccessException | InvocationTargetException e) {
            try {

                q.setRealm(getRealm());
                if (BatchLoading.isSynchronise()) {
                    Question val = findQuestionByCode(q.getCode(), REALM_HIDDEN);
                    if (val != null) {
                        val.setRealm(getRealm());
                        updateRealm(val);
                        return val;
                    }
                }

                getEntityManager().persist(q);
            } catch (javax.validation.ConstraintViolationException ce) {
                log.error("Error in saving question due to constraint issue:" + q + " :" + ce.getLocalizedMessage());
            } catch (javax.persistence.PersistenceException pe) {
                log.error("Error in saving question :" + q + " :" + pe.getLocalizedMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Long id = q.getId();
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

    public BaseEntity findBaseEntityByCode(@NotNull final String baseEntityCode, boolean includeEntityAttributes)
            throws NoResultException {
        String realm = getRealm();
        BaseEntity result = null;
        String userRealmStr = getRealm();

        // log.info("FIND BASEENTITY BY CODE ["+baseEntityCode+"]in realm
        // "+userRealmStr);
//		if (includeEntityAttributes) {
        String privacySQL = "";

//			String sql = "SELECT be FROM BaseEntity be LEFT JOIN be.baseEntityAttributes ea where be.code=:baseEntityCode and be.realm in (\"genny\",\"" + userRealmStr + "\")  "
//					+ privacySQL;
//			String sql = "SELECT be FROM BaseEntity be LEFT JOIN be.baseEntityAttributes ea where be.code=:baseEntityCode and be.realm=:realmStr  "
//					+ privacySQL;
        // log.info("FIND BASEENTITY BY CODE :"+sql);
        try {

            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<BaseEntity> query = cb.createQuery(BaseEntity.class);
            Root<BaseEntity> root = query.from(BaseEntity.class);

            query = query.select(root).where(cb.equal(root.get("code"), baseEntityCode.toUpperCase()),
                    cb.equal(root.get("realm"), realm));
            result = getEntityManager().createQuery(query).getSingleResult();

//
//
//			Session session = getEntityManager().unwrap(org.hibernate.Session.class);
//			Criteria criteria = session.createCriteria(BaseEntity.class);
//			result = (BaseEntity) criteria.add(Restrictions.eq("code", baseEntityCode.toUpperCase()))
//					.add(Restrictions.eq("realm", realm)).uniqueResult();

        } catch (Exception e) {

            try {
                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
                CriteriaQuery<BaseEntity> query = cb.createQuery(BaseEntity.class);
                Root<BaseEntity> root = query.from(BaseEntity.class);

                query = query.select(root).where(cb.equal(root.get("code"), baseEntityCode.toUpperCase()),
                        cb.equal(root.get("realm"), realm));
                List<BaseEntity> results = getEntityManager().createQuery(query).getResultList();
                if (results.isEmpty()) {
                    throw new NoResultException("Cannot find " + baseEntityCode + " in db! with realm " + realm);
                }
                result = results.get(0);
            } catch (NoResultException ee) {

                throw new NoResultException("Cannot find " + baseEntityCode + " in db! with realm " + realm);

            }
        }

        // else {
//			try {
//
//				result = (BaseEntity) getEntityManager()
//						.createQuery(
//								"SELECT be FROM BaseEntity be where be.code=:baseEntityCode  and  be.realm=:realmStr ")
//
//						.setParameter("baseEntityCode", baseEntityCode.toUpperCase()).setParameter("realmStr", realm)
//						.getSingleResult();
//
//			} catch (Exception e) {
////				if ("GRP_ALL_CONTACTS".equalsIgnoreCase(baseEntityCode)) {
////					log.info("GRP_ADMIN_JOBS");
////				}
//
//				throw new NoResultException("Cannot find " + baseEntityCode + " in db ");
//			}
//
//		}

        return result;

    }

    @Override
    public Long insert(final Ask ask) {
        // Fetch the associated BaseEntitys and Question

        // always check if question exists through check for unique code
        try {
            Question question = null;
            // check that these bes exist
            BaseEntity beSource = findBaseEntityByCode(ask.getSourceCode());
            BaseEntity beTarget = findBaseEntityByCode(ask.getTargetCode());
            Attribute attribute = findAttributeByCode(ask.getAttributeCode());
            Ask newAsk = null;
            if (ask.getQuestionCode() != null) {
                question = findQuestionByCode(ask.getQuestionCode());
                newAsk = new Ask(question, beSource.getCode(), beTarget.getCode());
            } else {
                newAsk = new Ask(attribute.getCode(), beSource.getCode(), beTarget.getCode(), attribute.getName());
            }

            newAsk.setRealm(getRealm());

            Log.info("Creating new Ask " + beSource.getCode() + ":" + beTarget.getCode() + ":" + attribute.getCode()
                    + ":" + (question == null ? "No Question" : question.getCode()));
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
            log.error("Cannot save ask with id=[" + ask.getId() + " , already in system " + e.getLocalizedMessage());
            List<Ask> existingList = findAsksByRawAsk(ask);
            if (existingList.isEmpty()) { // TODO
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
    public Validation findValidationByCode(@NotNull final String code) throws NoResultException {

        return findValidationByCode(code, getRealm());

    }

    @Override
    public Attribute findAttributeByCode(@NotNull final String code) throws NoResultException {

        return findAttributeByCode(code, getRealm());
    }

    @Override
    public BaseEntity findBaseEntityByCode(@NotNull final String baseEntityCode) throws NoResultException {

        return findBaseEntityByCode(baseEntityCode, true);

    }

    @Override
    public Long updateWithAttributes(BaseEntity entity) {
        entity.setRealm(getRealm());

        try {
            // merge in entityAttributes
            entity = getEntityManager().merge(entity);
        } catch (final Exception e) {
            // so persist otherwise
            if (entity.getName()== null) { entity.setName(entity.getCode());}
            getEntityManager().persist(entity);
        }
        String json = JsonUtils.toJson(entity);
        writeToDDT(entity.getCode(), json);
        return entity.getId();
    }

    @Override
    public void bulkUpdateWithAttributes(List<BaseEntity> entities) {
        for (BaseEntity entity : entities) {
            updateWithAttributes(entity);
        }
    }

    @Override
    public EntityEntity findEntityEntity(final String sourceCode, final String targetCode, final String linkCode)
            throws NoResultException {

        // find the BaseEntity
        BaseEntity source = this.findBaseEntityByCode(sourceCode);

        // now loop through this baseentity to find the actual ee (avoid the direct look
        // up loop)
        for (EntityEntity ee : source.getLinks()) {
            if (ee.getLink().getAttributeCode().equals(linkCode) && ee.getLink().getTargetCode().equals(targetCode)) {
                return ee;
            }
        }

        throw new NoResultException("EntityEntity " + sourceCode + ":" + targetCode + ":" + linkCode + " not found");
    }

    @Override
    public Integer updateEntityEntity(final EntityEntity ee) {
        Integer result = 0;

        try {
            String sql = "update EntityEntity ee set ee.weight=:weight, ee.valueString=:valueString, ee.link.weight=:weight, ee.link.linkValue=:valueString where ee.pk.targetCode=:targetCode and ee.link.attributeCode=:linkAttributeCode and ee.link.sourceCode=:sourceCode";
            result = getEntityManager().createQuery(sql).setParameter("sourceCode", ee.getPk().getSource().getCode())
                    .setParameter("linkAttributeCode", ee.getLink().getAttributeCode())
                    .setParameter("targetCode", ee.getPk().getTargetCode()).setParameter("weight", ee.getWeight())
                    .setParameter("valueString", ee.getValueString()).executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public EntityEntity insertEntityEntity(final EntityEntity ee) {

        try {
            getEntityManager().persist(ee);
            QEventLinkChangeMessage msg = new QEventLinkChangeMessage(ee.getLink(), null, getCurrentToken());

            sendQEventLinkChangeMessage(msg);
            log.debug("Sent Event Link Change Msg " + msg);

        } catch (Exception e) {
            // rollback
        }
        return ee;
    }

    @Override
    public QuestionQuestion findQuestionQuestionByCode(final String sourceCode, final String targetCode)
            throws NoResultException {
        QuestionQuestion result = null;
        try {
            result = (QuestionQuestion) getEntityManager().createQuery(

                    "SELECT qq FROM QuestionQuestion qq where qq.pk.sourceCode=:sourceCode and qq.pk.targetCode=:targetCode and qq.pk.source.realm=:realmStr")

                    .setParameter("realmStr", getRealm()).setParameter("sourceCode", sourceCode)
                    .setParameter("targetCode", targetCode).getSingleResult();

        } catch (Exception e) {
            throw new NoResultException("Cannot find QQ " + sourceCode + ":" + targetCode);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Question findQuestionByCode(@NotNull final String code) throws NoResultException {
        List<Question> result = null;
        final String userRealmStr = getRealm();
        try {

            result = getEntityManager().createQuery("SELECT a FROM Question a where a.code=:code and a.realm=:realmStr")

                    .setParameter("realmStr", getRealm()).setParameter("code", code.toUpperCase()).getResultList();

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public QuestionQuestion upsert(QuestionQuestion qq) {
        try {
            QuestionQuestion existing = findQuestionQuestionByCode(qq.getPk().getSource().getCode(),
                    qq.getPk().getTargetCode());
            existing.setMandatory(qq.getMandatory());
            existing.setWeight(qq.getWeight());
            existing.setReadonly(qq.getReadonly());
            existing = getEntityManager().merge(existing);
            return existing;
        } catch (NoResultException e) {
            log.debug("------- QUESTION 00 ------------");
            getEntityManager().persist(qq);
            QuestionQuestion id = qq;
            return id;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Question findQuestionByCode(@NotNull final String code, @NotNull final String realm)
            throws NoResultException {
        List<Question> result = null;
        try {
            result = getEntityManager().createQuery("SELECT a FROM Question a where a.code=:code and a.realm=:realmStr")
                    .setParameter("realmStr", realm).setParameter("code", code.toUpperCase()).getResultList();

        } catch (Exception e) {
            return null;
        }
        return result.get(0);
    }

    @Override
    public Long updateRealm(Question que) {
        Long result = 0L;

        try {
            result = (long) getEntityManager()
                    .createQuery("update Question que set que.realm =:realm where que.code=:code")
                    .setParameter("code", que.getCode()).setParameter("realm", que.getRealm()).executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Long insert(final Question question) {
        // always check if question exists through check for unique code
        try {

            question.setRealm(getRealm());

            getEntityManager().persist(question);
            log.debug("Loaded " + question.getCode());
        } catch (final ConstraintViolationException e) {
            Question existing = findQuestionByCode(question.getCode());

            existing.setRealm(getRealm());

            existing = getEntityManager().merge(existing);
            return existing.getId();
        } catch (final PersistenceException e) {
            Question existing = findQuestionByCode(question.getCode());

            existing.setRealm(getRealm());

            existing = getEntityManager().merge(existing);
            return existing.getId();
        } catch (final IllegalStateException e) {
            Question existing = findQuestionByCode(question.getCode());

            existing.setRealm(getRealm());

            existing = getEntityManager().merge(existing);
            return existing.getId();
        }
        return question.getId();
    }

    @Override
    public QBaseMSGMessageTemplate findTemplateByCode(@NotNull final String templateCode) throws NoResultException {

        QBaseMSGMessageTemplate result = null;

        result = (QBaseMSGMessageTemplate) getEntityManager().createQuery(
                "SELECT temp FROM QBaseMSGMessageTemplate temp where temp.code=:templateCode and temp.realm=:realmStr")
                .setParameter("realmStr", getRealm()).setParameter("templateCode", templateCode.toUpperCase())
                .getSingleResult();

        return result;

    }

    @Override
    public QBaseMSGMessageTemplate findTemplateByCode(@NotNull final String templateCode, @NotNull final String realm)
            throws NoResultException {
        QBaseMSGMessageTemplate result = null;
        try {
            result = (QBaseMSGMessageTemplate) getEntityManager().createQuery(
                    "SELECT temp FROM QBaseMSGMessageTemplate temp where temp.code=:templateCode and temp.realm=:realmStr")
                    .setParameter("realmStr", realm).setParameter("templateCode", templateCode.toUpperCase())
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }

        return result;
    }

    @Override
    public Long updateRealm(QBaseMSGMessageTemplate msg) {
        Long result = 0L;

        try {
            result = (long) getEntityManager()
                    .createQuery("update QBaseMSGMessageTemplate msg set msg.realm =:realm where msg.code=:code")
                    .setParameter("code", msg.getCode()).setParameter("realm", msg.getRealm()).executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public Long insert(final QBaseMSGMessageTemplate template) {

        template.setRealm(getRealm());

        try {
            getEntityManager().persist(template);

        } catch (final EntityExistsException e) {
            e.printStackTrace();
        }
        return template.getId();
    }

    @Override
    public Long update(final QBaseMSGMessageTemplate template) {

        template.setRealm(getRealm());

        QBaseMSGMessageTemplate temp = getEntityManager().merge(template);
        log.debug("klnsnfklsdjfjsdfjklsfsdf " + temp);
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

        EntityManager em = getEntityManager();
        int index = 1;

        for (CodedEntity t : objectList) {
            em.persist(t);
            if (index % BATCHSIZE == 0) {
                //flush a batch of inserts and release memory:
                log.debug("BaseEntity Batch is full, flush to database.");
                em.flush();
            }
            index += 1;
        }
        em.flush();
    }


    @Override
    public void bulkUpdate(ArrayList<CodedEntity> objectList, HashMap<String, CodedEntity> mapping) {
        if (objectList.isEmpty()) return;

        BeanNotNullFields copyFields = new BeanNotNullFields();
        for (CodedEntity t : objectList) {
            if (t instanceof QBaseMSGMessageTemplate) {
                QBaseMSGMessageTemplate msg = (QBaseMSGMessageTemplate) mapping.get(t.getCode());
                msg.setName(t.getName());
                msg.setDescription(((QBaseMSGMessageTemplate) t).getDescription());
                msg.setEmail_templateId(((QBaseMSGMessageTemplate) t).getEmail_templateId());
                msg.setSms_template(((QBaseMSGMessageTemplate) t).getSms_template());
                msg.setSubject(((QBaseMSGMessageTemplate) t).getSubject());
                msg.setToast_template(((QBaseMSGMessageTemplate) t).getToast_template());
                getEntityManager().merge(msg);
            } else {
                CodedEntity val = mapping.get(t.getCode());
                if (val == null) {
                    // Should never raise this exception
                    throw new NoResultException(String.format("Can't find %s from database.", t.getCode()));
                }
                try {
                    copyFields.copyProperties(val, t);
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

    @Override
    public void bulkInsertAsk(ArrayList<Ask> objectList) {

    }

    @Override
    public void bulkUpdateAsk(ArrayList<Ask> objectList, HashMap<String, Ask> mapping) {

    }

    @Override
    public void bulkInsertQuestionQuestion(ArrayList<QuestionQuestion> objectList) {
        if (objectList.isEmpty()) return;

        EntityManager entityManager = getEntityManager();
        int index = 1;

        for (QuestionQuestion t : objectList) {
            entityManager.persist(t);
            if (index % BATCHSIZE == 0) {
                //flush a batch of inserts and release memory:
                log.debug("BaseEntity Batch is full, flush to database.");
                entityManager.flush();
            }
            index += 1;
        }
        entityManager.flush();
    }

    @Override
    public void bulkUpdateQuestionQuestion(ArrayList<QuestionQuestion> objectList, HashMap<String, QuestionQuestion> mapping) {
        for (QuestionQuestion qq : objectList) {
            String uniqCode = qq.getSourceCode() + "-" + qq.getTarketCode();
            QuestionQuestion existing = mapping.get(uniqCode.toUpperCase());
            existing.setMandatory(qq.getMandatory());
            existing.setWeight(qq.getWeight());
            existing.setReadonly(qq.getReadonly());
            existing.setDependency(qq.getDependency());
            existing.setIcon(qq.getIcon());
            getEntityManager().merge(existing);
        }
    }

    @Override
    public void cleanAsk(String realm) {
        String qlString= String.format("delete from ask where realm = '%s'", realm);
//        EntityManager em = getEntityManager();
        Query query = em.createNativeQuery(qlString);
        int number = query.executeUpdate();
        em.flush();
        log.info(String.format("Clean up ask, realm:%s, %d ask deleted", realm, number));
    }

    @Override
    public void cleanFrameFromBaseentityAttribute(String realm) {
        String qlString= "delete from baseentity_attribute " +
                "where baseEntityCode like \'RUL_FRM%_GRP\' " +
                "and attributeCode = \'PRI_ASKS\' " +
                "and realm = \'" + realm + "\'";
//        EntityManager em = getEntityManager();
        Query query = em.createNativeQuery(qlString);
        int number = query.executeUpdate();
        em.flush();
        log.info(String.format("Clean up BaseentityAttribute, realm:%s, %d BaseentityAttribute deleted", realm, number));
    }
}
