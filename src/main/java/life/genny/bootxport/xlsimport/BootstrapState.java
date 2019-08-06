package life.genny.bootxport.xlsimport;

import java.util.ArrayList;
import java.util.List;

public class BootstrapState {

  private static volatile BootstrapState instance = null;

  public static BootstrapState getInstance() {
    if (instance == null) {
      synchronized (BootstrapState.class) {
        if (instance == null) {
          instance = new BootstrapState();
        }
      }
    }
    return instance;
  }
  
  
  private List<Realm> realms = new ArrayList<>();

  public List<Realm> getRealms() {
    return realms;
  }


  public void setRealms(List<Realm> realms) {
    this.realms = realms;
  }


  public static void setInstance(BootstrapState instance) {
    BootstrapState.instance = instance;
  }
  
  

}
