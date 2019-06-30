package life.genny.bootxport.importation;

import java.io.IOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

public class Module implements Traversable {

  private String id;

  public Module(String id) {
    this.id = id;

  }

  @Override
  public void traverse() throws IOException {
    try {
      XlsImportOnline.getFactoryMain(id).table("Modules")
          .forEach(System.out::println);;
    } catch (GoogleJsonResponseException e) {
      System.out.println("null");
    }
  }

}
