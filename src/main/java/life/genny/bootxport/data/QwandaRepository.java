package life.genny.bootxport.data;

import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public interface QwandaRepository {


  public void setRealm(String realm);

  public Validation upsert(Validation validation);

  public Attribute upsert(Attribute attribute);

  public BaseEntity upsert(BaseEntity baseEntity);

  public Question upsert(Question q);

  public Long insert(final Ask ask);

  public Validation findValidationByCode(@NotNull final String code)
      throws NoResultException;

  public Attribute findAttributeByCode(@NotNull final String code)
      throws NoResultException;

  public BaseEntity findBaseEntityByCode(
      @NotNull final String baseEntityCode) throws NoResultException;

  public Long updateWithAttributes(BaseEntity entity);

  public EntityEntity findEntityEntity(final String sourceCode,
      final String targetCode, final String linkCode);

  public Integer updateEntityEntity(final EntityEntity ee);

  public EntityEntity insertEntityEntity(final EntityEntity ee);

  public QuestionQuestion findQuestionQuestionByCode(
      final String sourceCode, final String targetCode);

  public Question findQuestionByCode(@NotNull final String code)
      throws NoResultException;

  public QuestionQuestion upsert(QuestionQuestion qq);

  public Question findQuestionByCode(@NotNull final String code,
      @NotNull final String realm) throws NoResultException;

  public Long updateRealm(Question que);

  public Long insert(final Question question);

  public QBaseMSGMessageTemplate findTemplateByCode(
      @NotNull final String templateCode) throws NoResultException;

  public QBaseMSGMessageTemplate findTemplateByCode(
      @NotNull final String templateCode,
      @NotNull final String realm);

  public Long updateRealm(QBaseMSGMessageTemplate msg);

  public Long insert(final QBaseMSGMessageTemplate template);

  public Long update(final QBaseMSGMessageTemplate template);
}
