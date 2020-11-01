package github.cweijan.http.test.core;

import com.intellij.psi.PsiClass;
import github.cweijan.http.test.util.PsiUtils;

import java.beans.Introspector;

/**
 * @author cweijan
 * @since 2020/10/30 23:46
 */
public class FieldCode {
    private String name;
    private String newStatement;
    private String setCode;

    public FieldCode(PsiClass psiClass) {
        String className = psiClass.getName();
        this.name= Introspector.decapitalize(className);

        String qualifiedName = PsiUtils.getQualifiedName(psiClass);
        if (qualifiedName.startsWith("java.util")) {

//            return;
        }

        this.newStatement =String.format("%s %s=new %s()",className, name
                ,className);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getNewStatement() {
        return newStatement;
    }

    public String getSetCode() {
        return setCode;
    }

    public void setSetCode(String setCode) {
        this.setCode = setCode;
    }
}
