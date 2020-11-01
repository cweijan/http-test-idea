package github.cweijan.http.test.core;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

/**
 * @author cweijan
 * @since 2020/11/01 3:02
 */
public class MethodCreator {

    public static void createMethod(Project project, PsiClass sourceClass, PsiClass testClass, PsiMethod method) {

        for (PsiMethod existsTestMethod : testClass.getMethods()) {
            if (method.getName().equals(existsTestMethod.getName())) {
                CodeInsightUtil.positionCursor(project, testClass.getContainingFile(), existsTestMethod);
                return;
            }
        }

        PsiJavaFile containingFile = (PsiJavaFile) testClass.getContainingFile();
        Generator.importClass(containingFile, method);
        PsiMethod testMethod = Generator.generateMethodContent(project, sourceClass,testClass, method);

        testClass.add(testMethod);
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(testMethod);

    }

}
