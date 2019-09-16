package life.genny.bootxport.bootx;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import io.vavr.Tuple;
import io.vavr.Tuple2;

public abstract class XlsxImport {

  public abstract List<Map<String, String>> mappingRawToHeaderAndValuesFmt(String sheetURI, String sheetName);
  public abstract Map<String,Map<String, String>> mappingRawToHeaderAndValuesFmt(String sheetURI, String sheetName, Set<String> keys);

  public Tuple2<List<String>,List<List<Object>>> sliceDataToHeaderAndValues(List<List<Object>> data){
    List<String> header = data.get(0).stream()
        .map(d -> d.toString().toLowerCase().replaceAll("^\"|\"$|_|-", ""))
        .peek(System.out::println)
        .collect(Collectors.toList());
    data.remove(0);
    List<List<Object>> values = data;
    Tuple2<List<String>, List<List<Object>>> headerAndValues = Tuple.of(header, values);
    return headerAndValues;
  }
}
