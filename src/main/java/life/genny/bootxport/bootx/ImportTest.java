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

//    private void info(Realm realm) {
//        realm.getDataUnits().stream().filter(d -> d.getCode().equals("internmatch")).map(d -> d.)
//                .forEach(data -> System.out.println(data.baseEntitys.size()));
//        realm.getDataUnits().forEach(d -> System.out.println(d.getQuestions()));
//        realm.getDataUnits().forEach(data ->
//                data.getModule()
//                        .getDataUnits()
//                        .forEach(module -> {
//                            System.out.println(module.getName() + " " + module.questions.size());
//                            System.out.println(module.getName() + " " + module.baseEntitys.size());
//                        })
//        );
//        realm.getDataUnits().forEach(data ->
//                System.out.println(data.getQuestions().size())
//        );
//        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
//        Session openSession = sessionFactory.openSession(); //    EntityManager createEntityManager =
//        openSession.getEntityManagerFactory().createEntityManager();
//        QwandaRepository repo =
//                new QwandaRepositoryImpl(createEntityManager);
//        BatchLoading bl = new BatchLoading(repo);
//        realm.getDataUnits().forEach(bl::persistProject);
//    }

    public static void main(String... args) {
        String sheetURI = "17CbqWLICh882xKVTU5J5mqqvGVl2F0Z7mdTgiAHAXx8";
        Realm realm = new Realm(BatchLoadMode.ONLINE, sheetURI);

        StateManagement.initStateManagement(realm);

        List<Tuple2<RealmUnit, BatchLoading>> collect = realm.getDataUnits().stream().map(d -> {
                    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                    Session openSession = sessionFactory.openSession();
                    EntityManager createEntityManager = openSession.getEntityManagerFactory().createEntityManager();
                    QwandaRepository repo = new QwandaRepositoryImpl(createEntityManager);
                    BatchLoading bl = new BatchLoading(repo);
                    return Tuple.of(d, bl);
                }
        ).collect(Collectors.toList());
//        collect.parallelStream().forEach(d -> d._2.persistProject(d._1));
        collect.stream().forEach(d -> d._2.persistProject(d._1));
    }
}
