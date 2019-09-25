package life.genny.bootxport.bootx;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.bootxport.utils.HibernateUtil;
import life.genny.bootxport.xlsimport.BatchLoading;

public class ImportTest {

  public static void mains(String... args) {
    GoogleImportService gs = GoogleImportService.getInstance();
    XlsxImport xlsImport = new XlsxImportOnline(gs.getService());
    Realm realm = new Realm(xlsImport,
        "1zzz6bYXuryASR09Tsyok4_qiJI9n81DBsxD4oFBk5mw");
    realm.getDataUnits().stream()
        .forEach(data -> System.out.println(data.questions.size()));
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

  public static void main(String... args) {

    String FILE_NAME =
      "/Users/helios/.genny/multitenancy/multitenancy.xlsx";

    XSSFService xssService = new XSSFService();
    XlsxImport xlsImport = new XlsxImportOffline(xssService);
    
    Realm realm = new Realm(xlsImport,
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
