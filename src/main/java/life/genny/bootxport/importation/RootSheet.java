package life.genny.bootxport.importation;

import java.io.IOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

public class RootSheet implements Traversable{

  
  private String name;

  private String id;

  public RootSheet(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void traverse() throws IOException {
    try {
      XlsImportOnline.getFactoryMain(id).table("Projects")
          .forEach(r -> System.out.println(r.get("sheetID")));;
    } catch (GoogleJsonResponseException e) {
      System.out.println("null");
    }
  }
  
}
