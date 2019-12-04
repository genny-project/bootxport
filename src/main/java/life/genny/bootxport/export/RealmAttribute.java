package life.genny.bootxport.export;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.codehaus.jackson.map.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.validation.Validation;

public class RealmAttribute {

  public final String attr = "Attribute";
  public final String dtype = "DataType";

  public String[] attributeH =
      new String[] {"code", "name", "datatype",};

  public String[] dataTypeH = new String[] {"validationList",
      "typeName", "className", "inputmask", "code", "name",};
 
  ObjectMapper oMapper = new ObjectMapper();

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
        dataTypeMap.put("name", type.dataType.getClassName());
        dataTypeMap.put("realm", attributeMap.get("realm"));
        return Tuple.of(attributeMap, dataTypeMap);
      };

  List<Tuple2<Map<String, String>, Map<String, String>>> attributesDatatypeOneToOne =
        convertToContext(QwandaTables.findAllAttributess(), interpolateDataType);

  public Seq<Realm<Map<String,String>>> getAttrRealm(){
    List<Map<String, String>> attributesMap =
        attributesDatatypeOneToOne.stream()
            .map(attributeAndDataType -> attributeAndDataType._1)
            .collect(Collectors.toList());
    Seq<Realm<Map<String, String>>> convertToQwandaWrapper = QwandaTables.convertToQwandaWrapper(attributesMap);
    return convertToQwandaWrapper;
  }

  public Seq<Realm<Map<String, String>>> getDataTypeRealm(){
    List<Map<String, String>> dataTypesMap =
        attributesDatatypeOneToOne.stream()
            .map(attributeAndDataType -> attributeAndDataType._2)
            .collect(Collectors.toList());
    return QwandaTables.convertToQwandaWrapper(dataTypesMap);
  }

  public static <T, W> List<W> convertToContext(List<T> obj,
      Function<T, W> oo) {
    return obj.stream().map(oo::apply).collect(Collectors.toList());
  }

}
