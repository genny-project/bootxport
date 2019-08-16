package life.genny.bootxport.xport;

import java.util.List;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public interface Store {

  
  public List<BaseEntity> findAllBaseEntitys();

  public List<Attribute> findAllAttributess();
  public List<Question> findAllQuestions();
  public List<QuestionQuestion> findAllQuestionQuestions();
  public List<EntityEntity> findAllEntityEntities();
  public List<EntityAttribute> findAllEntityAttributes();
  public List<Ask> findAllAsks();
  public List<QBaseMSGMessageTemplate> findAllMessages();
  public List<Validation> findAllValidations();

}
