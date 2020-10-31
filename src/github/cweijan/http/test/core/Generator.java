package github.cweijan.http.test.core;

import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.testIntegration.createTest.CreateTestAction;
import github.cweijan.http.test.template.java.JavaTestTemplate;
import github.cweijan.http.test.util.PsiClassUtils;
import github.cweijan.http.test.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author cweijan
 * @since 2020/10/30 23:35
 */
public class Generator {

    public static PsiClass generateTestClass(GenerateContext generateContext) {

        @NotNull Project project = generateContext.project;
        @NotNull PsiClass psiClass = generateContext.targetClass;

        final PsiPackage srcPackage = JavaDirectoryService.getInstance().getPackage(psiClass.getContainingFile().getContainingDirectory());
        final Module srcModule = ModuleUtilCore.findModuleForPsiElement(psiClass);
        Module testModule = CreateTestAction.suggestModuleForTests(project, srcModule);

        PackageCreator packageCreator = new PackageCreator(project, testModule);
        PsiDirectory psiDirectory = packageCreator.createPackage(srcPackage.getQualifiedName());

        GlobalSearchScope scope = GlobalSearchScopesCore.directoryScope(psiDirectory, false);
        PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
        String testClassName = psiClass.getName() + "Test";
        PsiClass[] classes = aPackage.findClassByShortName(testClassName, scope);
        if (classes.length > 0) {
            return classes[0];
        }

        Properties properties = new Properties();
        properties.setProperty("NAME", testClassName);
        properties.setProperty("CLASS_NAME", psiClass.getQualifiedName());
        return ReflectUtil.invoke(FileTemplateUtil.class, "createFromTemplate",
                JavaTestTemplate.classTemplate, testClassName, properties, psiDirectory);

    }

    public static void importClass(PsiJavaFile psiJavaFile, PsiMethod psiMethod) {

        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiMethod.getReturnType());
        if (psiClass != null) {
            psiJavaFile.importClass(psiClass);
        }

        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            PsiClass parameterClass = PsiTypesUtil.getPsiClass(parameter.getType());
            if (parameterClass != null) {
                psiJavaFile.importClass(parameterClass);
            }
        }

    }

    public static FieldCode generateSetter(PsiParameter parameter) {

        PsiClass parameterClass = PsiTypesUtil.getPsiClass(parameter.getType());
        FieldCode fieldCode = new FieldCode(parameterClass);

        StringBuilder stringBuilder = new StringBuilder(fieldCode.getNewStatement() + ";\n");
        for (PsiMethod setMethod : PsiClassUtils.extractSetMethods(parameterClass)) {
            stringBuilder.append("    ").append(
                    String.format("%s.%s(any())", fieldCode.getName(), setMethod.getName())
            ).append(";\n");
        }
        fieldCode.setSetCode(stringBuilder.toString());

        return fieldCode;
    }

    public static PsiMethod generateMethodContent(Project project, PsiClass sourceClass, PsiClass testClass, PsiMethod method) {

        PsiField sourceField = findField(testClass,sourceClass);

        ArrayList<String> params = new ArrayList<>();
        StringBuilder methodContent = new StringBuilder();
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            FieldCode fieldCode = generateSetter(parameter);
            methodContent.append(fieldCode.getSetCode());
            params.add(fieldCode.getName());
        }
        String returnTypeStr = method.getReturnTypeElement().getText();
        String methodName = method.getName();
        methodContent.append(
                String.format("%s %s=%s.%s(%s);", returnTypeStr, methodName, sourceField.getName(), methodName, String.join(",", params))
        );
        String fullMethod = "@Test\n" +
                "void " + methodName + "(){\n" +
                "    \n" +
                "    " + methodContent.toString() + "\n" +
                "    \n" +
                "}\n" +
                "\n";

        return JVMElementFactories.getFactory(testClass.getLanguage(), project).createMethodFromText(fullMethod, testClass);
    }

    private static PsiField findField(PsiClass testClass,PsiClass sourceClass) {
        for (PsiField testClassField : testClass.getAllFields()) {
            if(testClassField.getType().getCanonicalText().equals(sourceClass.getQualifiedName())){
                return testClassField;
            }

        }

        // TODO 如果没有找到则创建新的Field
        throw new UnsupportedOperationException("");
    }


}
