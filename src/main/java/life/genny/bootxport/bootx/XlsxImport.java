package life.genny.bootxport.bootx;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface XlsxImport {

   public List<Map<String, String>> transformHeaderToValuesFormat(String sheetURI, String sheetName);
   public List<Map<String, String>> transformHeaderToValuesFormat(String sheetURI, String sheetName, Set<String> keys);

}
