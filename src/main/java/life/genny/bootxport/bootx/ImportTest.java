package life.genny.bootxport.bootx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.bootxport.utils.HibernateUtil;
import life.genny.bootxport.xlsimport.BatchLoading;


public class ImportTest {

  public static void main(String... args) {

    Realm realm = new Realm(BatchLoadMode.ONLINE,
        "1BhLyxJr7HglCOH1NwZeay0Pzje2VMJsiAVWEeHoSmnk");

    
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    StateManagement.initStateManagement(realm);
    StateModel model = StateManagement.partOneStateManagement();
    StateManagement.setStateModel(model);


    List<RealmUnit> collect  = StateManagement.getUpdatedRealmUnits();
    

    collect.stream().forEach(d -> {
      System.out.println(d.getCode());
      System.out.println(d.asks.size());
      System.out.println(d.baseEntitys.size());
      System.out.println(d.entityAttributes.size());
      System.out.println(d.attributeLinks.size());
      System.out.println(d.attributes.size());
      System.out.println(d.dataTypes.size());
      System.out.println(d.messages.size());
      System.out.println(d.notifications.size());
      System.out.println(d.questionQuestions.size());
      System.out.println(d.questions.size());
      System.out.println(d.validations.size());
    });
    StateManagement.savePreviousRealmUnits();
    
    collect  = StateManagement.getUpdatedRealmUnits();
    collect.stream().forEach(d -> {
      System.out.println(d.getCode());
      System.out.println(d.asks.size());
      System.out.println(d.baseEntitys.size());
      System.out.println(d.entityAttributes.size());
      System.out.println(d.attributeLinks.size());
      System.out.println(d.attributes.size());
      System.out.println(d.dataTypes.size());
      System.out.println(d.messages.size());
      System.out.println(d.notifications.size());
      System.out.println(d.questionQuestions.size());
      System.out.println(d.questions.size());
      System.out.println(d.validations.size());
    });

//    collect  = mgnt.getUpdatedRealmUnits();
//    collect.stream().forEach(d -> {
//      System.out.println(d.asks.size());
//      System.out.println(d.baseEntitys.size());
//      System.out.println(d.entityAttributes.size());
//      System.out.println(d.attributeLinks.size());
//      System.out.println(d.attributes.size());
//      System.out.println(d.dataTypes.size());
//      System.out.println(d.messages.size());
//      System.out.println(d.notifications.size());
//      System.out.println(d.questionQuestions.size());
//      System.out.println(d.questions.size());
//      System.out.println(d.validations.size());
//    });
//    realm.getDataUnits().stream().filter(d -> d.getCode().equals("internmatch")).map(d ->d.)
//        .forEach(data -> System.out.println(data.baseEntitys.size()));

//    realm.getDataUnits().stream().forEach(d -> System.out.println(d.getQuestions()));
//    realm.getDataUnits().stream().forEach(data-> 
//      data.getModule()
//      .getDataUnits()
//      .forEach(module -> {
//        System.out.println(module.getName() + " " + module.questions.size());
//        System.out.println(module.getName() + " " + module.baseEntitys.size());
//        
//      })
//    );
//    realm.getDataUnits().stream().forEach(data-> 
//      System.out.println(data.getQuestions().size())
//    );
//    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
//    Session openSession = sessionFactory.openSession();
//    EntityManager createEntityManager =
//        openSession.getEntityManagerFactory().createEntityManager();
//    QwandaRepository repo =
//        new QwandaRepositoryImpl(createEntityManager);
//    BatchLoading bl = new BatchLoading(repo);
//    realm.getDataUnits().stream().forEach(bl::persistProject);

//    List<Tuple2<RealmUnit, BatchLoading>> collect = realm.getDataUnits().stream().map(d -> {
//      SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
//      Session openSession = sessionFactory.openSession();
//      EntityManager createEntityManager =
//          openSession.getEntityManagerFactory().createEntityManager();
//      QwandaRepository repo =
//          new QwandaRepositoryImpl(createEntityManager);
//      BatchLoading bl = new BatchLoading(repo);
//      return Tuple.of(d,bl);
//      }
//    ).collect(Collectors.toList());
//    
//    collect.parallelStream().forEach(d -> d._2.persistProject(d._1));
  }

  public static void mains(String... args) {

    String FILE_NAME =
      ".genny/multitenancy/multitenancy.xlsx";

    XSSFService xssService = new XSSFService();
    XlsxImport xlsImport = new XlsxImportOffline(xssService);
    
    Realm realm = new Realm(BatchLoadMode.OFFLINE,
        FILE_NAME);
//    realm.getDataUnits().stream()
//        .forEach(data -> System.out.println(data.questions.size()));

//    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
//    Session openSession = sessionFactory.openSession();
//    EntityManager createEntityManager =
//        openSession.getEntityManagerFactory().createEntityManager();
//    QwandaRepository repo =
//        new QwandaRepositoryImpl(createEntityManager);
//    BatchLoading bl = new BatchLoading(repo);
//    realm.getDataUnits().parallelStream().forEach(bl::persistProject);
    
    List<Tuple2<RealmUnit, BatchLoading>> collect = realm.getDataUnits().stream().map(d -> {
      SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
      Session openSession = sessionFactory.openSession();
      EntityManager createEntityManager =
          openSession.getEntityManagerFactory().createEntityManager();
      QwandaRepository repo =
          new QwandaRepositoryImpl(createEntityManager);
      BatchLoading bl = new BatchLoading(repo);
      return Tuple.of(d,bl);
      }
    ).collect(Collectors.toList());
    
    collect.parallelStream().forEach(d -> d._2.persistProject(d._1));
  }

}
