package github.cweijan.http.test.core;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.refactoring.util.classMembers.MemberInfo;

import java.util.Collection;

/**
 * @author cweijan
 * @since 2020/11/01 0:12
 */
public class GenerateContext {

    public Project project;

    public boolean createBefore;

    public PsiDirectory targetDirector;

    public String superClassName;

    public Collection<MemberInfo> methods;

    public PsiClass sourceClass;

    public PsiClass testClass;

}
