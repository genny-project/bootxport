package life.genny.bootxport.bootx;

import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;

import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

import java.util.ArrayList;
import java.util.List;

public interface QwandaRepository {


    void setRealm(String realm);

    <T> void delete(T entity);

    Validation upsert(Validation validation);

    Attribute upsert(Attribute attribute);

    BaseEntity upsert(BaseEntity baseEntity);

    Question upsert(Question q);

    Long insert(final Ask ask);

    Validation findValidationByCode(@NotNull final String code)
            throws NoResultException;

    Attribute findAttributeByCode(@NotNull final String code)
            throws NoResultException;

    BaseEntity findBaseEntityByCode(
            @NotNull final String baseEntityCode) throws NoResultException;

    Long updateWithAttributes(BaseEntity entity);

    EntityEntity findEntityEntity(final String sourceCode,
                                  final String targetCode, final String linkCode);

    Integer updateEntityEntity(final EntityEntity ee);

    EntityEntity insertEntityEntity(final EntityEntity ee);

    QuestionQuestion findQuestionQuestionByCode(
            final String sourceCode, final String targetCode);

    Question findQuestionByCode(@NotNull final String code)
            throws NoResultException;

    QuestionQuestion upsert(QuestionQuestion qq);

    Question findQuestionByCode(@NotNull final String code,
                                @NotNull final String realm) throws NoResultException;

    Long updateRealm(Question que);

    Long insert(final Question question);

    QBaseMSGMessageTemplate findTemplateByCode(
            @NotNull final String templateCode) throws NoResultException;

    QBaseMSGMessageTemplate findTemplateByCode(
            @NotNull final String templateCode,
            @NotNull final String realm);

    Long updateRealm(QBaseMSGMessageTemplate msg);

    Long insert(final QBaseMSGMessageTemplate template);

    Long update(final QBaseMSGMessageTemplate template);

    List<Validation> queryValidation(@NotNull final String realm);

    List<Attribute> queryAttributes(@NotNull final String realm);

    List<BaseEntity> queryBaseEntitys(@NotNull final String realm);

    List<EntityAttribute> queryEntityAttribute(@NotNull final String realm);

    List<EntityEntity> queryEntityEntity(@NotNull final String realm);

    List<Question> queryQuestion(@NotNull final String realm);

    List<QuestionQuestion> queryQuestionQuestion(@NotNull final String realm);

    List<Ask> queryAsk(@NotNull final String realm);

    List<QBaseMSGMessageTemplate> queryNotification(@NotNull final String realm);

    List<QBaseMSGMessageTemplate> queryMessage(@NotNull final String realm);

    void insertValidations(ArrayList<Validation> validationList);

    void insertAttributes(ArrayList<Attribute> attributeList);
    void insertBaseEntitys(ArrayList<BaseEntity> baseEntityList);
    void insertAttributeLinks(ArrayList<AttributeLink> attributeLinkList);

    Long insert(BaseEntity baseEntity);
}
