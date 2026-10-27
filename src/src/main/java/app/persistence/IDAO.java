package app.persistence;

import java.util.List;

public interface  IDAO<T, I> {

    T create(T obj);

    T read(I id);

    List<T> readAll();

    T update(T object);

    void delete(I id);
}
