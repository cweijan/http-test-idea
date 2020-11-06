package github.cweijan.http.test.util;

import java.util.function.Predicate;

/**
 * @author cweijan
 * @since 2020/11/06 10:14
 */
public abstract class ListUtil {

    public static <T> T findOne(T[] list, Predicate<T> match){
        for (T t : list) {
            if(match.test(t)){
                return t;
            }
        }
        return null;
    }

}
