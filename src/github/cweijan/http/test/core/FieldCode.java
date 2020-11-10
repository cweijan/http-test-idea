package github.cweijan.http.test.core;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import github.cweijan.http.test.util.PsiUtils;

import java.beans.Introspector;
import java.util.Objects;

import static java.lang.String.format;

/**
 * @author cweijan
 * @since 2020/10/30 23:46
 */
public class FieldCode {
    private String name;
    private String newStatement;
    private String setCode;

    public FieldCode(PsiJavaFile psiJavaFile, PsiType psiType) {
        PsiClass psiClass = Objects.requireNonNull(PsiTypesUtil.getPsiClass(psiType));
        String className = psiClass.getName();
        this.name = Introspector.decapitalize(className);

        String qualifiedName = PsiUtils.getQualifiedName(psiClass);
        if (qualifiedName.equals("java.util.List")) {
            PsiClass listClass = JavaPsiFacade.getInstance(psiClass.getProject()).findClass("java.util.ArrayList", GlobalSearchScope.allScope(psiClass.getProject()));
            if (listClass != null) {
                psiJavaFile.importClass(listClass);
                this.newStatement = format("%s %s=new ArrayList<>();", psiType.getCanonicalText(), name);
            }
        } else if (qualifiedName.startsWith("java.lang")) {
            this.newStatement = format("%s %s=mock(%s.class);", className, name, className);
        } else {
            this.newStatement = format("%s %s=new %s();", className, name, className);
        }

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
