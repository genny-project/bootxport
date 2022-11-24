package life.genny.bootxport.bootx;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;

// import life.genny.qwandautils.JsonUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import life.genny.bootxport.utils.HibernateUtil;
import life.genny.bootxport.xlsimport.BatchLoading;


public class ImportTest {

    public static void main(String[] args) throws InterruptedException {

        Realm realm = new Realm(BatchLoadMode.ONLINE,
//               "1W9BRH6cTRNACGPH8cVJtgUlefdZzG9U-WMCj1Qw4L2k"/* "1BhLyxJr7HglCOH1NwZeay0Pzje2VMJsiAVWEeHoSmnk"*/);
                "17CbqWLICh882xKVTU5J5mqqvGVl2F0Z7mdTgiAHAXx8");

        Thread.sleep(10000);
        StateManagement.initStateManagement(realm);

        List<Tuple2<RealmUnit, BatchLoading>> collect = realm.getDataUnits().stream().map(d -> {
                    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                    Session openSession = sessionFactory.openSession();
                    EntityManager createEntityManager =
                            openSession.getEntityManagerFactory().createEntityManager();
                    QwandaRepository repo =
                            new QwandaRepositoryImpl(createEntityManager);
                    BatchLoading bl = new BatchLoading(repo);
                    return Tuple.of(d, bl);
                }
        ).collect(Collectors.toList());

        collect.parallelStream().forEach(d ->
                {
                    if (!d._1.getDisable() && !d._1.getSkipGoogleDoc())
                        d._2.persistProject(d._1);
                    else {
                        System.out.println("Realm:" + d._1.getName()
                                + ", disabled:" + d._1.getDisable()
                                + ", skipGoogleDoc:" + d._1.getSkipGoogleDoc());
                    }
                }
        ) ;
    }

    public static void mains(String[] args) {

        String fileName = ".genny/multitenancy/multitenancy.xlsx";

        Realm realm = new Realm(BatchLoadMode.OFFLINE,
                fileName);

        List<Tuple2<RealmUnit, BatchLoading>> collect = realm.getDataUnits().stream().map(d -> {
                    SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                    Session openSession = sessionFactory.openSession();
                    EntityManager createEntityManager =
                            openSession.getEntityManagerFactory().createEntityManager();
                    QwandaRepository repo =
                            new QwandaRepositoryImpl(createEntityManager);
                    BatchLoading bl = new BatchLoading(repo);
                    return Tuple.of(d, bl);
                }
        ).collect(Collectors.toList());

        collect.parallelStream().forEach(d ->
                {
                    if (!d._1.getDisable() && !d._1.getSkipGoogleDoc())
                        d._2.persistProject(d._1);
                    else {
                        System.out.println("Realm:" + d._1.getName()
                                + ", disabled:" + d._1.getDisable()
                                + ", skipGoogleDoc:" + d._1.getSkipGoogleDoc());
                    }
                }
        ) ;
    }
}
