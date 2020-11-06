package github.cweijan.http.test.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;

/**
 * @author cweijan
 * @since 2020/11/06 13:31
 */
public abstract class MvcUtil {

    /**
     * 根据名称返回mvc相应的注解全限定名
     */
    public static String getAnnotationByName(String name) {
        String prefix = "org.springframework.web.bind.annotation.";
        String request = "PostMapping";
        String suffix = String.format("(\"/%s\")", name);

        if (name.matches("^(list|get|query|select|find)")) {
            request = "GetMapping";
        } else if (name.matches("^(update|change)")) {
            request = "PutMapping";
        } else if (name.matches("^(delete|remove)")) {
            request = "DeleteMapping";
        }
        return prefix + request + suffix;
    }

    /**
     * 判断方法是否是mvc的请求方法
     */
    public static boolean isRequest(PsiMember psiMember) {
        return ListUtil.findOne(psiMember.getAnnotations(), psiAnnotation ->
                psiAnnotation.getQualifiedName().startsWith("org.springframework.web.bind.annotation")
        ) != null;
    }

    /**
     * 判断类是否为mvc的controller类
     */
    public static boolean isController(PsiClass psiClass) {
        String[] qualifiedNames = new String[]{"org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation.RestController"};
        return PsiUtils.hasAnnotation(psiClass, qualifiedNames);
    }

    /**
     * 判断类是否为可操作的类
     */
    public static boolean isSimpleClass(PsiClass psiClass){
        return !psiClass.isInterface() && !psiClass.isEnum() && !psiClass.isAnnotationType() && !psiClass.isRecord();
    }

}
