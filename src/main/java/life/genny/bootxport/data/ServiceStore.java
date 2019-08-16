package life.genny.bootxport.data;

import life.genny.bootxport.xport.DataDao;
import life.genny.bootxport.xport.GennyData;

public class ServiceStore {

  
  public GennyData data = new GennyData();
  private DataDao dataDao = new DataDao();

  
  private static ServiceStore ss;
  
  public void initializeData() {
    data.setBaseEntitys(dataDao.findAllBaseEntitys());
    data.setAttributess(dataDao.findAllAttributess());
    data.setValidations(dataDao.findAllValidations());
    data.setQuestionQuestions(dataDao.findAllQuestionQuestions());
    data.setQuestions(dataDao.findAllQuestions());
    data.setMessages(dataDao.findAllMessages());
    data.setQuestions(dataDao.findAllQuestions());
    data.setEntityEntities(dataDao.findAllEntityEntities());
    data.setAsks(dataDao.findAllAsks());
    data.setEntityAttributes(dataDao.findAllEntityAttributes());
  }
  
  private ServiceStore() {
  }
  
  
  public static ServiceStore getServiceStore() {
    if(ss != null) {
      return ss;
    }
    else {
      ss = new ServiceStore();
      ss.initializeData();
      return ss; 
    }
  }
}
