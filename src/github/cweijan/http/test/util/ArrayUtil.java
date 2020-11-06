package github.cweijan.http.test.util;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author cweijan
 * @since 2020/11/06 10:14
 */
public abstract class ArrayUtil {

    public static <T> T findOne(T[] list, Predicate<T> match) {
        for (T t : list) {
            if (match.test(t)) {
                return t;
            }
        }
        return null;
    }


    public static <O, T> boolean compare(O[] array1, T[] array2, BiPredicate<O,T> predicate) {
        if(array1.length!=array2.length){
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            O element1 = array1[i];
            T element2 = array2[i];
            if(predicate.test(element1,element2)){
                return false;
            }
        }

        return true;
    }

    /**
     * 语法糖函数, 将集合转为目标类型List
     *
     * @param originArray 对象数组
     * @param converter   转换回调
     * @param <O>         原始类型
     * @param <T>         目标类型
     * @return 目标List
     */
    public static <O, T> List<T> mapArray(O[] originArray, Function<O, T> converter) {
        return Stream.of(originArray).map(converter).collect(Collectors.toList());
    }

}
