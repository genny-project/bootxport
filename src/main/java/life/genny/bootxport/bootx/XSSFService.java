package life.genny.bootxport.bootx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XSSFService {
    private final Log log = LogFactory.getLog(XSSFService.class);

    public List<List<Object>> offlineService(String sheetId,
                                             String sheetName) {
        Workbook workbook = null;
        List<List<Object>> values = null;
        FileInputStream excelFile = null;
        try {
            excelFile = new FileInputStream(Paths.get(System.getProperty("user.home"), sheetId).toFile());
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage());
            return Collections.emptyList();
        }

        try {
            workbook = new XSSFWorkbook(excelFile);
        } catch (IOException ex) {
            log.error(ex.getMessage());
            return Collections.emptyList();
        }
        Sheet datatypeSheet = workbook.getSheet(sheetName);

        Stream<Row> targetStream =
                fromIteratorToStream(datatypeSheet.iterator());


        int count = (int) fromIteratorToStream(datatypeSheet.iterator()).limit(1).map(r -> r.getLastCellNum()).findFirst().get();

        values = targetStream.filter(a -> a.cellIterator().hasNext()).map(currentRow -> {

            Stream<Cell> targetStream2 =
                    fromIteratorToStream(currentRow.iterator());


            return targetStream2.map(currentCell -> {
                List<Object> arrayList1 = new ArrayList<>(
                        Collections.nCopies(count, new String("")));

                CellType cellType = currentCell.getCellType();
                int columnIndex = currentCell.getColumnIndex();
                String value = " ";

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
                    case _NONE:
                        value = " ";
                        break;
                    case ERROR:
                        value = " ";
                        break;
                    default:
                        value = currentCell.getStringCellValue().equals("") ? " " : currentCell.getStringCellValue();
                }


                arrayList1.set(columnIndex, value);
                return arrayList1;
            }).reduce((acc, first) -> {
                return first.stream()
                        .filter(a -> !a.toString().isEmpty()).flatMap(s -> {
                            acc.set(first.indexOf(s), s);
                            return acc.stream();
                        }).collect(Collectors.toList());
            }).get();

        }).collect(Collectors.toList());
        return values;
    }

    public <T> Stream<T> fromIteratorToStream(Iterator<T> iterator) {
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
