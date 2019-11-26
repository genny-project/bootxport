package working;

import java.util.List;
import java.util.Map;

public class R<T>{
    String name;
    List<T> entity;

    public void setName(String name) {
      this.name = name;
    }
    public String getName() {
      return this.name ;
    }
    public List<T> getData() {
      return entity;
    }
    R(String s, List<T> entity){
      name = s;
      this.entity = entity;
    }

    @Override
    public String toString() {
      return "R [name=" + name + ", entity=" + entity + "]";
    } 
  }
