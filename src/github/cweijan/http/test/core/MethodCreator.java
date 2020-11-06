package github.cweijan.http.test.core;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import github.cweijan.http.test.util.ArrayUtil;

/**
 * @author cweijan
 * @since 2020/11/01 3:02
 */
public class MethodCreator {

    public static void createMethod(Project project, PsiClass sourceClass, PsiClass testClass, PsiMethod method) {

        PsiMethod existsMethod = ArrayUtil.findOne(testClass.getMethods(), (existsTestMethod) ->
                method.getName().equals(existsTestMethod.getName())
        );

        PsiJavaFile psiJavaFile = (PsiJavaFile) testClass.getContainingFile();
        Generator.importClass(psiJavaFile, method);
        PsiMethod testMethod = Generator.generateMethodContent(project, psiJavaFile, sourceClass, testClass, method);

        if (existsMethod != null) {
            existsMethod.replace(testMethod);
        } else {
            testClass.add(testMethod);
        }
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(testMethod);

    }

}
