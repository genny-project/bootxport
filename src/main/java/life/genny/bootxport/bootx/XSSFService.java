package life.genny.bootxport.bootx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XSSFService {

  public List<List<Object>> offlineService(String sheetId,
      String sheetName) {
    Workbook workbook = null;
    if (sheetName.equals("DataType"))
      System.out.println();
    List<List<Object>> values = null;
    try {
      FileInputStream excelFile =
          new FileInputStream(new File(sheetId));

      workbook = new XSSFWorkbook(excelFile);
      Sheet datatypeSheet = workbook.getSheet(sheetName);

      Stream<Row> targetStream =
          fromIteratorToStream(datatypeSheet.iterator());

      if (sheetName.equals("EntityAttribute")) {
        System.out.println();
      }

      int count = (int) fromIteratorToStream(datatypeSheet.iterator())
          .limit(1).map(r -> r.getLastCellNum()).findFirst().get();

      System.out.println(count);
      values = targetStream.filter(a -> a.cellIterator().hasNext()).map(currentRow -> {

        Stream<Cell> targetStream2 =
            fromIteratorToStream(currentRow.iterator());


        List<Object> rowAsList = targetStream2.map(currentCell -> {
          System.out.println(currentCell);
          List<Object> arrayList1 = new ArrayList<>(
              Collections.nCopies(count , new String("")));

          CellType cellType = currentCell.getCellType();
          int columnIndex = currentCell.getColumnIndex();
          String value = "";

          switch (cellType) {
            case NUMERIC:
              value =
                  Double.toString(currentCell.getNumericCellValue());
              break;
            case BOOLEAN:
              value =
                  Boolean.toString(currentCell.getBooleanCellValue());
              break;
            case FORMULA:
              value =
                  Boolean.toString(currentCell.getBooleanCellValue());
              break;
            case BLANK:
              value = " ";
              break;
            default:
              value = currentCell.getStringCellValue();
          }
          if (sheetName.equals("DataType")) {
            System.out.println();
            System.out.println(value + " " + columnIndex);
            System.out.println("the count is "+count);
            System.out.println("The array size is "+ arrayList1.size());
            System.out.println();
          }

          
          // List<Object> arrayList = new ArrayList<>();
          arrayList1.set(columnIndex, value);
          System.out.println(arrayList1);
          return arrayList1;
          // return value;
        }).reduce((acc, first) -> {
          System.out.println(first);
          List<Object> collect = first.stream()
              .filter(a -> !a.toString().isEmpty()).flatMap(s -> {
                acc.set(first.indexOf(s), s);
                return acc.stream();
              }).collect(Collectors.toList());
//          System.out.println("hee"+collect);
          // acc.set(first.indexOf(o),)
          return collect;
        }).get();
        // .collect(Collectors.toList());

          System.out.println("haaaee"+rowAsList);
          
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


//  public static void main(String... args) {
//    List<Object> arrayList1 =
//        new ArrayList<>(Collections.nCopies(10, new String("")));
//    List<Object> arrayList2 =
//        new ArrayList<>(Collections.nCopies(10, new String("")));
//    arrayList1.add(3, "hello");
//    arrayList2.add(0, "bye");
//
//    List<List<Object>> list = new ArrayList<>();
//    list.add(arrayList1);
//    list.add(arrayList2);
//
//
//    List<Object> collect2 = list.stream().flatMap(add -> add.stream())
//        .collect(Collectors.toList());
//
//    // List<Object> collect = arrayList1.stream().filter(a -> {
//    // return !a.toString().isEmpty();
//    // }).flatMap(a -> {
//    // System.out.println(a);
//    // arrayList2.set(arrayList1.indexOf(a), a);
//    // System.out.println(arrayList2);
//    // return arrayList2.stream();
//    // }).collect(Collectors.toList());
//
//    System.out.println(collect2);
//  }

}
