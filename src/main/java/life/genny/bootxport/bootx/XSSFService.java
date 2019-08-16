package life.genny.bootxport.bootx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    if(sheetName.equals("Ask"))
      System.out.println();
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
          CellType cellType = currentCell.getCellType();
          String value = "";

          switch(cellType) {
            case NUMERIC:
              value = Double.toString(currentCell.getNumericCellValue());
              break;
            case BOOLEAN:
              value = Boolean.toString(currentCell.getBooleanCellValue());
              break;
            case FORMULA:
              value = Boolean.toString(currentCell.getBooleanCellValue());
              break;
            default:
              value = currentCell.getStringCellValue();
          }

          return value;
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
}
