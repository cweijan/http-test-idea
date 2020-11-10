package github.cweijan.http.test.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;

/**
 * @author cweijan
 * @since 2020/11/06 13:31
 */
public abstract class MvcUtil {

    private static final String[] SERVICE_QUALIFIED_NAMES = new String[]{"org.springframework.stereotype.Service",
            "org.springframework.stereotype.Component"};
    public static final String[] CONTROLLER_QUALIFIED_NAMES = new String[]{"org.springframework.stereotype.Controller",
            "org.springframework.web.bind.annotation.RestController"};

    /**
     * 根据名称返回mvc相应的注解全限定名
     */
    public static String getAnnotationByName(String name) {
        String prefix = "org.springframework.web.bind.annotation.";
        String request = "PostMapping";
        String suffix = String.format("(\"/%s\")", name);

        if (name.matches("^(list|get|query|select|find|count|statistic).+")) {
            request = "GetMapping";
        } else if (name.matches("^(update|change|put).+")) {
            request = "PutMapping";
        } else if (name.matches("^(delete|remove).+")) {
            request = "DeleteMapping";
        }
        return prefix + request + suffix;
    }

    /**
     * 判断方法是否是mvc的请求方法
     */
    public static boolean isRequest(PsiMember psiMember) {
        return ArrayUtil.findOne(psiMember.getAnnotations(), psiAnnotation ->
                psiAnnotation.getQualifiedName().startsWith("org.springframework.web.bind.annotation")
        ) != null;
    }

    /**
     * 判断类是否为mvc的controller类
     */
    public static boolean isController(PsiClass psiClass) {
        return isSimpleClass(psiClass) && PsiUtils.hasAnnotation(psiClass, CONTROLLER_QUALIFIED_NAMES);
    }

    /**
     * 判断类是否为mvc的service类
     */
    public static boolean isService(PsiClass psiClass) {
        return isSimpleClass(psiClass) &&  PsiUtils.hasAnnotation(psiClass, SERVICE_QUALIFIED_NAMES);
    }

    /**
     * 判断类是否为可操作的类
     */
    public static boolean isSimpleClass(PsiClass psiClass){
        return psiClass!=null && !psiClass.isInterface() && !psiClass.isEnum() && !psiClass.isAnnotationType() && !psiClass.isRecord();
    }

}
