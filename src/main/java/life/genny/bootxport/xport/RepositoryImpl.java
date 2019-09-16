package life.genny.bootxport.xport;

import java.util.List;
import life.genny.bootxport.utils.StorageUtils;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class RepositoryImpl implements IRepository {


  public List<BaseEntity> findAllBaseEntitys() {
    List<BaseEntity> bes =
        StorageUtils.<BaseEntity>fetchAll(queryFetchAllBaseEntitys);
    return bes;
  }

  public List<Attribute> findAllAttributess() {
    List<Attribute> attrs =
        StorageUtils.<Attribute>fetchAll(queryFetchAllAttributes);
    return attrs;
  }

  public List<Question> findAllQuestions() {
    List<Question> que =
        StorageUtils.<Question>fetchAll(queryFetchAllQuesstion);
    return que;
  }

  public List<QuestionQuestion> findAllQuestionQuestions() {
    List<QuestionQuestion> queque = StorageUtils
        .<QuestionQuestion>fetchAll(queryFetchAllQuestionQuestion);
    return queque;
  }

  public List<QBaseMSGMessageTemplate> findAllMessages() {
    List<QBaseMSGMessageTemplate> temp = StorageUtils
        .<QBaseMSGMessageTemplate>fetchAll(queryFetchAllTemplates);
    return temp;
  }

  public List<Validation> findAllValidations() {
    List<Validation> val =
        StorageUtils.<Validation>fetchAll(queryFetchAllValidation);
    return val;
  }

  public List<EntityEntity> findAllEntityEntities() {
    List<EntityEntity> entEnt = StorageUtils
        .<EntityEntity>fetchAll(queryFetchAllEntityEntitys);
    return entEnt;
  }

  public List<EntityAttribute> findAllEntityAttributes() {
    List<EntityAttribute> entAttr = StorageUtils
        .<EntityAttribute>fetchAll(queryFetchAllEntityAttributes);
    return entAttr;
  }

  public List<Ask> findAllAsks() {
    List<Ask> asks = StorageUtils.<Ask>fetchAll(queryFetchAllAsks);
    return asks;
  }

}
