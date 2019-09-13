package life.genny.bootxport.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.codehaus.jackson.map.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.message.QBaseMSGMessageTemplate;
import life.genny.qwanda.validation.Validation;

public class PreExport {

  public static <T, W> List<W> convertToContext(List<T> obj,
      Function<T, W> oo) {
    return obj.stream().map(oo::apply).collect(Collectors.toList());
  }
  public PreExport(Realm realm) {
    List<Attribute> attributes = realm.getAttributes();
    ObjectMapper oMapper = new ObjectMapper();
    /*
     * table names or work sheet names on the spreadsheet
     */
    Function<Attribute, Tuple2<Map<String, String>, Map<String, String>>> interpolateDataType =
        type -> {
          /*
           * Convert Objects or POJOS to Map
           */
          @SuppressWarnings("unchecked")
          Map<String, String> attributeMap =
              oMapper.convertValue(type, Map.class);
          @SuppressWarnings("unchecked")
          Map<String, String> dataTypeMap =
              oMapper.convertValue(type.dataType, Map.class);
          String dataTypeCode = UUID.randomUUID().toString();

          /*
           * Convert Objects or POJOS to Map
           */
          Optional<Validation> validation = type.getDataType()
              .getValidationList().stream().findFirst();

          attributeMap.put("datatype", dataTypeCode);

          if (validation.isPresent()) {
            dataTypeMap.put("validationList",
                validation.get().getCode());
          }

          dataTypeMap.put("code", dataTypeCode);
          dataTypeMap.put("name", dataTypeCode);
          return Tuple.of(attributeMap, dataTypeMap);
        };


    List<Tuple2<Map<String, String>, Map<String, String>>> attributesDatatypeOneToOne =
        convertToContext(attributes, interpolateDataType);

    List<Map<String, String>> attributesMap =
        attributesDatatypeOneToOne.stream()
            .map(attributeAndDataType -> attributeAndDataType._1)
            .collect(Collectors.toList());

    List<Map<String, String>> dataTypesMap =
        attributesDatatypeOneToOne.stream()
            .map(attributeAndDataType -> attributeAndDataType._2)
            .collect(Collectors.toList());

    List<Map<String, String>> attributesLinkMap = attributesMap
        .stream().filter(a -> a.get("code").startsWith("LNK"))
        .collect(Collectors.toList());
    ModuleUnit<Ask> askUnit = new ModuleUnit<Ask>();
    askUnit.setData(realm.getAsks());
    askUnit.setName("Ask");
    askUnit.setHeader(Arrays.asList(DataHeader.askH));

    ModuleUnit<Map<String, String>> attributeLinkUnit =
        new ModuleUnit<Map<String, String>>();
    attributeLinkUnit.setData(attributesLinkMap);
    attributeLinkUnit.setName ("AttributeLink");
    attributeLinkUnit.setHeader( Arrays.asList(DataHeader.attributeH));

    ModuleUnit<Map<String, String>> attributeUnit =
        new ModuleUnit<Map<String, String>>();
    attributeUnit.setData( attributesMap);
    attributeUnit.setName ("Attribute");
    attributeUnit.setHeader( Arrays.asList(DataHeader.attributeH));

    ModuleUnit<BaseEntity> baseEntityUnit =
        new ModuleUnit<BaseEntity>();
    baseEntityUnit.setData( realm.getBaseEntitys());
    baseEntityUnit.setName( "BaseEntity");
    baseEntityUnit.setHeader( Arrays.asList(DataHeader.baseEntityH));

    ModuleUnit<EntityEntity> entityEntityUnit =
        new ModuleUnit<EntityEntity>();
    entityEntityUnit.setData( realm.getEntityEntitys());
    entityEntityUnit.setName ("EntityEntity");
    entityEntityUnit.setHeader( Arrays.asList(DataHeader.entityEntityH));

    ModuleUnit<EntityAttribute> entityAttributeUnit =
        new ModuleUnit<EntityAttribute>();
    entityAttributeUnit.setData( realm.getEntityAttributes());
    entityAttributeUnit.setName ("EntityAttribute");
    entityAttributeUnit.setHeader(
        Arrays.asList(DataHeader.entityAttributeH));

    ModuleUnit<Map<String, String>> dataTypeUnit =
        new ModuleUnit<Map<String, String>>();
    dataTypeUnit.setData( dataTypesMap);
    dataTypeUnit.setName ("DataType");
    dataTypeUnit.setHeader( Arrays.asList(DataHeader.dataTypeH));

    ModuleUnit<Validation> validationUnit =
        new ModuleUnit<Validation>();
    validationUnit.setData( realm.getValidations());
    validationUnit.setName ("Validation");
    validationUnit.setHeader( Arrays.asList(DataHeader.validationH));

    ModuleUnit<QuestionQuestion> questionQuestionUnit =
        new ModuleUnit<QuestionQuestion>();
    questionQuestionUnit.setData( realm.getQuestionQuestions());
    questionQuestionUnit.setName ("QuestionQuestion");
    questionQuestionUnit.setHeader(
        Arrays.asList(DataHeader.questionQuestionH));

    ModuleUnit<Question> questionUnit = new ModuleUnit<Question>();
    questionUnit.setData( realm.getQuestions());
    questionUnit.setName ("Question");
    questionUnit.setHeader( Arrays.asList(DataHeader.questionH));

    ModuleUnit<QBaseMSGMessageTemplate> messagesUnit =
        new ModuleUnit<QBaseMSGMessageTemplate>();
    messagesUnit.setData( realm.getMessages());
    messagesUnit.setName( "Messages");
    messagesUnit.setHeader(Arrays.asList(DataHeader.messagesH));
    
    
    moduleUnits.add(askUnit);
    moduleUnits.add(attributeLinkUnit);
    moduleUnits.add(attributeUnit);
    moduleUnits.add(baseEntityUnit);
    moduleUnits.add(entityEntityUnit);
    moduleUnits.add(entityAttributeUnit);
    moduleUnits.add(dataTypeUnit);
    moduleUnits.add(validationUnit);
    moduleUnits.add(questionQuestionUnit);
    moduleUnits.add(questionUnit);
    moduleUnits.add(messagesUnit);
  }
  
  private List<ModuleUnit<?>> moduleUnits = new ArrayList<>();
  
  public List<ModuleUnit<?>> getModuleUnits(){
    return moduleUnits;
    
  }
}
