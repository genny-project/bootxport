package life.genny.bootxport.bootx;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Lists;
//import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Tuple2;

public class XlsxImportOnline extends XlsxImport {

    private final Sheets service;

    private Function2<String, String, List<Map<String, String>>> mappingAndCacheHeaderToValues =
            (sheetURI, sheetName) -> {
                System.out.println("Function2 not memoized for SheetURI:" + sheetURI + ", SheetName:" + sheetName);
                List<List<Object>> data = Lists.newArrayList(fetchSpreadSheet(sheetURI, sheetName));
                if (data.isEmpty()) {
                    System.out.println("Function 2 SheetURI:" + sheetURI + ", SheetName:" + sheetName + " is empty");
                    return new ArrayList<>();
                } else
                    return mappingHeaderToValues(data);
            };

    private Function3<String, String, Set<String>, Map<String, Map<String, String>>> mappingAndCacheKeyHeaderToHeaderValues =
            (sheetURI, sheetName, keys) -> {
                System.out.println("Function3 not memoized for SheetURI:" + sheetURI + ", SheetName:" + sheetName);
                List<List<Object>> data = Lists.newArrayList(fetchSpreadSheet(sheetURI, sheetName));
                if (data.isEmpty()) {
                    System.out.println("Function 3 SheetURI:" + sheetURI + ", SheetName:" + sheetName + " is empty");
                    return new HashMap<>();
                } else {
                    return mappingKeyHeaderToHeaderValues(data, keys);
                }
            };

    public XlsxImportOnline(Sheets service) {
        this.service = service;
        memoized();
    }

    @Override
    public List<Map<String, String>> mappingRawToHeaderAndValuesFmt(String sheetURI, String sheetName) {
        return mappingAndCacheHeaderToValues.apply(sheetURI, sheetName);
    }

    @Override
    public Map<String, Map<String, String>> mappingRawToHeaderAndValuesFmt(
            String sheetURI, String sheetName, Set<String> keys) {
        return mappingAndCacheKeyHeaderToHeaderValues.apply(sheetURI, sheetName, keys);
    }

    public List<Map<String, String>> mappingHeaderToValues(
            final List<List<Object>> values) {
        final List<Map<String, String>> k = new ArrayList<>();
        Tuple2<List<String>, List<List<Object>>> headerAndValues = sliceDataToHeaderAndValues(values);
        for (final List<Object> row : headerAndValues._2) {
            final Map<String, String> mapper = new HashMap<>();
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
            final Map<String, String> mapper = new HashMap<>();
            for (int counter = 0; counter < row.size(); counter++) {
                mapper.put(headerAndValues._1.get(counter), row.get(counter).toString());
            }
            String join = mapper.keySet().stream()
                    .filter(keyColumns::contains).map(mapper::get).collect(Collectors.joining());
            k.put(join, mapper);
        }
        return k;
    }

    public List<List<Object>> fetchSpreadSheet(String sheetId, String sheetName) {
        String RANGE = "!A1:Z";
        final String absoluteRange = sheetName + RANGE;
        try {
            ValueRange response = service.spreadsheets().values().get(sheetId, absoluteRange).execute();
            return response.getValues();
        } catch (IOException ioe) {
            log.error("Fail to get sheet content, SheetID:" + sheetId + ", SheetName:" + sheetName + ", May be doesn't exist.");
        }
        return Collections.emptyList();
    }


//    Map<String, List<List<Object>>> responseState = new HashMap<>();

    public void memoized() {
        mappingAndCacheHeaderToValues = mappingAndCacheHeaderToValues.memoized();
        mappingAndCacheKeyHeaderToHeaderValues = mappingAndCacheKeyHeaderToHeaderValues.memoized();
    }
//  public static void main(String...args) {
//
//  Function1<String, String> mappingAndCacheKeyHeaderToHeaderValuess = 
//      (sheetName) -> {
//        System.out.println("it has been called");
//        if(sheetName.equals("hello"))
//          return null;
//        else if(sheetName.equals("bye"))
//          return null;
//      return sheetName;
//    };
//    mappingAndCacheKeyHeaderToHeaderValuess = mappingAndCacheKeyHeaderToHeaderValuess.memoized();
//    mappingAndCacheKeyHeaderToHeaderValuess.apply("hello");
//    mappingAndCacheKeyHeaderToHeaderValuess.apply("hello");
//    mappingAndCacheKeyHeaderToHeaderValuess.apply("bye");
//    mappingAndCacheKeyHeaderToHeaderValuess.apply("bye");
//  }
}
