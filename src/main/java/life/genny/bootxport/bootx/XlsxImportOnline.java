package life.genny.bootxport.bootx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Lists;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Tuple2;

public class XlsxImportOnline extends XlsxImport {

  private final String RANGE = "!A1:Z";

  private Sheets service;

  private Function2<String, String, List<Map<String, String>>> mappingAndCacheHeaderToValues = 
     (sheetName,sheetId) -> {
      System.out.println(sheetName + " " + sheetId);
      List<List<Object>> data = Lists.newArrayList(fetchSpreadSheet(sheetName, sheetId));
      if(data.isEmpty()) 
        System.out.println("It is empty");
      return mappingHeaderToValues(data);
    };
  private Function3<String, String, Set<String>, Map<String, Map<String, String>>> mappingAndCacheKeyHeaderToHeaderValues = 
      (sheetName,sheetId,keys) -> {
      List<List<Object>> data = Lists.newArrayList(fetchSpreadSheet(sheetName, sheetId));
      return mappingKeyHeaderToHeaderValues(data,keys);
    };

  public XlsxImportOnline(Sheets service) {
     this.service = service;
     memoized();
  }

  @Override
  public List<Map<String, String>> mappingRawToHeaderAndValuesFmt(String sheetURI, String sheetName) {
    long timeBefore, timeAfter= 0;
    timeBefore = System.currentTimeMillis();
    List<Map<String, String>> result = mappingAndCacheHeaderToValues.apply(sheetURI, sheetName);
    timeAfter = System.currentTimeMillis();
    System.out.println("In sheet: " + sheetURI + " " + sheetName);
    return result;
  }

  @Override
  public Map<String, Map<String, String>> mappingRawToHeaderAndValuesFmt(
      String sheetURI, String sheetName, Set<String> keys) {
    Map<String, Map<String, String>> result = new HashMap<>();
    long timeBefore, timeAfter = 0;
    timeBefore = System.currentTimeMillis();
    result = mappingAndCacheKeyHeaderToHeaderValues.apply(sheetURI, sheetName, keys);
    timeAfter = System.currentTimeMillis();
    System.out.println("In sheet: " + sheetURI + " " + sheetName );
    return result;
  }

  public List<Map<String, String>> mappingHeaderToValues(
      final List<List<Object>> values) {
    final List<Map<String, String>> k = new ArrayList<>();
    Tuple2<List<String>, List<List<Object>>> headerAndValues = sliceDataToHeaderAndValues(values);
    for (final List<Object> row : headerAndValues._2) {
      final Map<String, String> mapper = new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(headerAndValues._1.get(counter), row.get(counter).toString());
      }
      k.add(mapper);
    }
    return k;
  }

  public Map<String, Map<String, String>> mappingKeyHeaderToHeaderValues(
      final List<List<Object>> values, Set<String> keyColumns) {
    final Map<String, Map<String, String>> k = new HashMap<>();
    Tuple2<List<String>, List<List<Object>>> headerAndValues = sliceDataToHeaderAndValues(values);
    for (final List<Object> row : headerAndValues._2) {
      final Map<String, String> mapper = new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(headerAndValues._1.get(counter), row.get(counter).toString());
      }
      String join = mapper.keySet().stream()
          .filter(keyColumns::contains).map(mapper::get).collect(Collectors.joining());
      k.put(join, mapper);
    }
    return k;
  }
  
  public List<List<Object>> fetchSpreadSheet(String sheetId,String sheetName) {
    final String absoluteRange = sheetName + RANGE;
    ValueRange response = null;
    List<List<Object>> data = new ArrayList<>();
    try {
      response = service.spreadsheets().values().get(sheetId, absoluteRange).execute();
      data = response.getValues();
    } catch (IOException e) {
      System.out.println("Does not exist");
    }
    return data;
  }
  


  Map<String,List<List<Object>>> responseState = new HashMap<>();

  public void memoized() {
    mappingAndCacheHeaderToValues =  mappingAndCacheHeaderToValues.memoized();
    mappingAndCacheKeyHeaderToHeaderValues = mappingAndCacheKeyHeaderToHeaderValues.memoized();
  }
}
