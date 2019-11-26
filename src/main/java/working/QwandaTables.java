package working;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
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

public class QwandaTables {

  final static String queryFetchAllAttributes = "from Attribute";
  final static String queryFetchAllBaseEntitys = "from BaseEntity";
  final static String queryFetchAllEntityEntitys = "from EntityEntity";
  final static String queryFetchAllEntityAttributes = "from EntityAttribute";
  final static String queryFetchAllAsks = "from Ask";
  final static String queryFetchAllQuesstion = "from Question";
  final static String queryFetchAllValidation = "from Validation";
  final static String queryFetchAllQuestionQuestion = "from QuestionQuestion";
  final static String queryFetchAllTemplates = "from QBaseMSGMessageTemplate";

  public static List<BaseEntity> findAllBaseEntitys() {
    List<BaseEntity> bes =
        StorageUtils.<BaseEntity>fetchAll(queryFetchAllBaseEntitys);
    return bes;
  }

  public static List<Attribute> findAllAttributess() {
    List<Attribute> attrs =
        StorageUtils.<Attribute>fetchAll(queryFetchAllAttributes);
    return attrs;
  }

  public static List<Question> findAllQuestions() {
    List<Question> que =
        StorageUtils.<Question>fetchAll(queryFetchAllQuesstion);
    return que;
  }

  public static List<QuestionQuestion> findAllQuestionQuestions() {
    List<QuestionQuestion> queque = StorageUtils
        .<QuestionQuestion>fetchAll(queryFetchAllQuestionQuestion);
    return queque;
  }

  public static List<QBaseMSGMessageTemplate> findAllMessages() {
    List<QBaseMSGMessageTemplate> temp = StorageUtils
        .<QBaseMSGMessageTemplate>fetchAll(queryFetchAllTemplates);
    return temp;
  }

  public static List<Validation> findAllValidations() {
    List<Validation> val =
        StorageUtils.<Validation>fetchAll(queryFetchAllValidation);
    return val;
  }

  public static  List<EntityEntity> findAllEntityEntities() {
    List<EntityEntity> entEnt = StorageUtils
        .<EntityEntity>fetchAll(queryFetchAllEntityEntitys);
    return entEnt;
  }

  public static List<EntityAttribute> findAllEntityAttributes() {
    List<EntityAttribute> entAttr = StorageUtils
        .<EntityAttribute>fetchAll(queryFetchAllEntityAttributes);
    return entAttr;
  }

  public static List<Ask> findAllAsks() {
    List<Ask> asks = StorageUtils.<Ask>fetchAll(queryFetchAllAsks);
    return asks;
  }
  
  public static <T> Seq<R<T>> convertToQwandaWrapper(List<T> list) {
    List<L<T>> collect = list.stream().map(L::new).collect(Collectors.toList());
    io.vavr.collection.Map<String, List<T>> bimap = Stream.ofAll(collect)
           .groupBy(be -> be.getRealm())
           .bimap(k -> k, v -> v.map(d-> d.object).collect(Collectors.toList()));
    Seq<R<T>> map = bimap.map(d -> d.apply(R::new)); 
    return map;
  }

}
