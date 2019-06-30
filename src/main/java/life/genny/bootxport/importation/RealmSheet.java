package life.genny.bootxport.importation;

public class RealmSheet {
  
  private String id;

  private String name;

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
  public String toString() {
    return "Realm [id=" + id + ", name=" + name + "]";
  }
}
