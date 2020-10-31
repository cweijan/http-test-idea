package github.cweijan.http.test.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author cweijan
 * @since 2020/10/31 16:51
 */
public abstract class ReflectUtil {

    public static <T> T invoke(@NotNull Class<?> clazz, @NotNull String method, Object... args) {
        return invoke(clazz,null,method,args);
    }

    public static <T> T invoke(@NotNull Object instance, @NotNull String method, Object... args) {
        return invoke(instance.getClass(),instance,method,args);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(@NotNull Class<?> clazz, Object instance,@NotNull String method, Object... args) {

        Method targetMethod=null;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method1 : methods) {
            if(method1.getName().equals(method) && method1.getParameterCount()==args.length){
                targetMethod=method1;
                break;
            }
        }

        if(targetMethod==null){
            throw new IllegalArgumentException("对应的方法未找到!");
        }

        try {
            targetMethod.setAccessible(true);
            return (T) targetMethod.invoke(instance,args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
