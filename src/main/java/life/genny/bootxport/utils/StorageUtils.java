package life.genny.bootxport.utils;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public interface StorageUtils {


  static SessionFactory sessionFactory =
      HibernateUtil.getSessionFactory();
  static Session session = sessionFactory.openSession();

  // static T monthByMonth(final double[] values) {
  // return (time) -> values[time - 1];
  // }
  // public static <T> Iterable<T> fetchAll(String query) {
  //
  // return new QueryIterable<T>(session.createQuery(query));
  // }

  public static <T> List<T> fetchAll(String query) {

    return session.createQuery(query).getResultList();
  }

}
