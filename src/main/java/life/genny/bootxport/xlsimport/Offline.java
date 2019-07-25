package life.genny.bootxport.xlsimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import io.vavr.Function2;
import io.vavr.Function3;
import life.genny.qwanda.entity.BaseEntity;

public class Offline {

  private final String RANGE = "!A1:Z";

  private static final String FILE_NAME =
      "/Users/helios/.genny/internmatch.xlsx";

  public List<Map<String, String>> toTableFormat(
      final List<List<Object>> values) {

    final List<String> keys = new ArrayList<String>();
    final List<Map<String, String>> k = new ArrayList<>();
    for (final Object key : values.get(0)) {
      keys.add(((String) key).toLowerCase());
    }
    values.remove(0);
    for (final List row : values) {
      final Map<String, String> mapper =
          new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(keys.get(counter), row.get(counter).toString());
      }
      k.add(mapper);
    }
    return k;
  }

  public Map<String, Map<String, String>> toTableFormatInKey(
      final List<List<Object>> values, Set<String> keyColumns) {

    final List<String> keys = new ArrayList<String>();
    final Map<String, Map<String, String>> k = new HashMap<>();
    for (final Object key : values.get(0)) {
      keys.add(((String) key).toLowerCase());
    }
    values.remove(0);
    for (final List row : values) {
      final Map<String, String> mapper =
          new HashMap<String, String>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(keys.get(counter), row.get(counter).toString());
      }
      mapper.entrySet().stream().forEach(System.out::println);
      String join = mapper.keySet().stream()
          .filter(keyColumns::contains).collect(Collectors.joining());


      k.put(mapper.get(join), mapper);
    }
    return k;
  }

  private List<Map<String, String>> table(final String sheetId,
      final String sheetName) {

    List<List<Object>> data = null;
    try {
      data = offlineService(sheetId, sheetName);
    } catch (NullPointerException e) {
    }
    return toTableFormat(data);
  };

  private Map<String, Map<String, String>> tableInKey(
      final String sheetId, final String sheetName,
      Set<String> keyColumns) {

    List<List<Object>> data = null;
    try {
      data = offlineService(sheetId, sheetName);
    } catch (NullPointerException e) {
    }
    return toTableFormatInKey(data, keyColumns);
  };

  public Function2<String, String, List<Map<String, String>>> getInTableFormat =
      Function2.of(this::table).memoized();

  public Function3<String, String, Set<String>, Map<String, Map<String, String>>> getInTableFormatInKey =
      Function3.of(this::tableInKey).memoized();


  public List<List<Object>> offlineService(String sheetId,
      String sheetName) {

    Workbook workbook = null;
    List<List<Object>> values = null;
    try {
      FileInputStream excelFile =
          new FileInputStream(new File(sheetId));

      workbook = new XSSFWorkbook(excelFile);
      Sheet datatypeSheet = workbook.getSheet(sheetName);

      Stream<Row> targetStream =
          fromIteratorToStream(datatypeSheet.iterator());

      values = targetStream.map(currentRow -> {

        Stream<Cell> targetStream2 =
            fromIteratorToStream(currentRow.iterator());

        List<Object> rowAsList = targetStream2.map(currentCell -> {

          return currentCell.getStringCellValue();
        }).collect(Collectors.toList());

        return rowAsList;

      }).collect(Collectors.toList());

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return values;
  }

  public <T> Stream<T> fromIteratorToStream(Iterator<T> iterator) {

    Iterable<T> iterable = () -> iterator;

    Stream<T> targetStream =
        StreamSupport.stream(iterable.spliterator(), false);

    return targetStream;
  }

  private static Set<String> codeKey = new HashSet<>();
  private static Set<String> baseEntityCodeattributeCodeKey =
      new HashSet<>();
  private static Set<String> targetCodeParentCodeLinkCodeKey =
      new HashSet<>();
  private static Set<String> targetCodeParentCodeKey = new HashSet<>();
  private static Set<String> questionCodeSourceCodeTargetCode =
      new HashSet<>();


  static {
    codeKey.add("code");

    baseEntityCodeattributeCodeKey.add("baseEntityCode");
    baseEntityCodeattributeCodeKey.add("attributeCode");

    targetCodeParentCodeLinkCodeKey.add("targetCode");
    targetCodeParentCodeLinkCodeKey.add("parentCode");
    targetCodeParentCodeLinkCodeKey.add("linkCode");

    targetCodeParentCodeKey.add("targetCode");
    targetCodeParentCodeKey.add("parentCode");
    targetCodeParentCodeKey.add("linkCode");

    questionCodeSourceCodeTargetCode.add("question_code");
    questionCodeSourceCodeTargetCode.add("sourceCode");
    questionCodeSourceCodeTargetCode.add("targetCode");
  }

  public static void main(String... str) {
    Offline o = new Offline();


    Map<String, Map<String, String>> apply = o.getInTableFormatInKey.apply(FILE_NAME,
        BaseEntity.class.getSimpleName(),codeKey);

    apply.entrySet().stream().forEach(System.out::println);


  }


}
