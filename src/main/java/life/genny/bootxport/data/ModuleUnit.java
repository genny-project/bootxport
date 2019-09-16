package life.genny.bootxport.data;

import java.util.List;

public class ModuleUnit<T> {

  private String name;

  private List<T> data;

  private List<String> header;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }

  public List<String> getHeader() {
    return header;
  }

  public void setHeader(List<String> header) {
    this.header = header;
  }
}
