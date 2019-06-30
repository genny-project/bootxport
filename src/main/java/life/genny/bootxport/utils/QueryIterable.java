package life.genny.bootxport.utils;

import java.util.Iterator;

import org.hibernate.Query;
import org.hibernate.engine.HibernateIterator;

public class QueryIterable<T> implements Iterable<T> {

    private Query query;

    public QueryIterable(Query query) {
        this.query = query;
    }

    @Override
    public Iterator<T> iterator() {
        return new CloseableIterator<T>((HibernateIterator) query.iterate());
    }
}