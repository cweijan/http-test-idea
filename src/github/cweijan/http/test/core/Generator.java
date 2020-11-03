package github.cweijan.http.test.core;

import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.testIntegration.createTest.CreateTestAction;
import com.intellij.testIntegration.createTest.JavaTestGenerator;
import github.cweijan.http.test.template.TestTemplate;
import github.cweijan.http.test.util.PsiUtils;
import github.cweijan.http.test.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;

/**
 * @author cweijan
 * @since 2020/10/30 23:35
 */
public class Generator {

    public static final String ANNOTATION_NAME = "io.github.cweijan.mock.jupiter.HttpTest(host = \"127.0.0.1\")";

    public static PsiClass getOrCreateTestClass(GenerateContext generateContext) {

        @NotNull Project project = generateContext.project;
        @NotNull PsiClass originClass = generateContext.sourceClass;
        String testClassName = originClass.getName() + "Test";

        final PsiPackage srcPackage = JavaDirectoryService.getInstance().getPackage(originClass.getContainingFile().getContainingDirectory());
        PsiDirectory psiDirectory = getTestDirectory(project, originClass, srcPackage);

        PsiClass existsTestClass = findExistsTestClass(testClassName, psiDirectory);
        if (existsTestClass != null) {
            return existsTestClass;
        }

        Properties properties = new Properties();
        properties.setProperty("NAME", testClassName);
        properties.setProperty("CLASS_NAME", originClass.getQualifiedName());
        PsiClass testClass = ReflectUtil.invoke(FileTemplateUtil.class, "createFromTemplate",
                TestTemplate.getInstance().loadTestClassTemplate(), testClassName, properties, psiDirectory);
        generateContext.testClass = testClass;

        PsiUtils.doWrite(project, () -> {

            if (generateContext.superClassName != null && !generateContext.superClassName.equals("")) {
                ReflectUtil.invoke(JavaTestGenerator.class, "addSuperClass", testClass, project, generateContext.superClassName);
            }

            if (generateContext.createBefore) {
                createBeforeMethod(generateContext);
            }

            checkAndAddAnnotation(project, testClass);

            return testClass;
        });

        return testClass;

    }

    private static void createBeforeMethod(GenerateContext generateContext) {
        String fullMethod = TestTemplate.getInstance().loadBeforeMethodTemplate();
        PsiMethod beforeMethod = JVMElementFactories.getFactory(generateContext.testClass.getLanguage(), generateContext.project).createMethodFromText(fullMethod, generateContext.testClass);
        generateContext.testClass.add(beforeMethod);
        JavaCodeStyleManager.getInstance(generateContext.project).shortenClassReferences(generateContext.testClass);
    }

    private static void checkAndAddAnnotation(Project project, PsiClass testClass) {
        PsiClass superClass = testClass.getSuperClass();
        List<PsiAnnotation> allAnnotations = Arrays.asList(testClass.getAnnotations());
        while (superClass != null) {
            allAnnotations.addAll(Arrays.asList(superClass.getAnnotations()));
            superClass = superClass.getSuperClass();
        }

        for (PsiAnnotation annotation : allAnnotations) {
            if (annotation.getQualifiedName().equals(ANNOTATION_NAME)) {
                return;
            }
        }

        PsiModifierList classModifierList = testClass.getModifierList();
        PsiAnnotation addAnnotation = classModifierList.addAnnotation(ANNOTATION_NAME);
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(addAnnotation);

    }

    @Nullable
    private static PsiDirectory getTestDirectory(Project project, @NotNull PsiClass psiClass, PsiPackage srcPackage) {
        final Module srcModule = ModuleUtilCore.findModuleForPsiElement(psiClass);
        Module testModule = CreateTestAction.suggestModuleForTests(project, srcModule);
        PackageCreator packageCreator = new PackageCreator(project, testModule);
        return packageCreator.createPackage(srcPackage.getQualifiedName());
    }

    @Nullable
    private static PsiClass findExistsTestClass(String testClassName, PsiDirectory psiDirectory) {
        GlobalSearchScope scope = GlobalSearchScopesCore.directoryScope(psiDirectory, false);
        PsiPackage aPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
        PsiClass[] classes = aPackage.findClassByShortName(testClassName, scope);
        if (classes.length > 0) {
            return classes[0];
        }
        return null;
    }

    public static void importClass(PsiJavaFile psiJavaFile, PsiMethod psiMethod) {

        PsiType type = psiMethod.getReturnType();
        importType(psiJavaFile, type);

        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            importType(psiJavaFile, parameter.getType());
        }

    }

    private static void importType(PsiJavaFile psiJavaFile, PsiType type) {
        PsiClass psiClass = PsiTypesUtil.getPsiClass(type);
        if (psiClass != null) {
            psiJavaFile.importClass(psiClass);
            PsiType genericType = PsiUtil.extractIterableTypeParameter(type, false);
            if (genericType != null) {
                PsiClass genericClass = PsiTypesUtil.getPsiClass(genericType);
                if (genericClass != null)
                    psiJavaFile.importClass(genericClass);
            }
        }
    }

    public static FieldCode generateSetter(PsiJavaFile psiJavaFile, PsiParameter parameter) {

        FieldCode fieldCode = new FieldCode(psiJavaFile,parameter.getType());

        PsiClass parameterClass = PsiTypesUtil.getPsiClass(parameter.getType());
        StringBuilder stringBuilder = new StringBuilder(fieldCode.getNewStatement() + "\n");
        for (PsiMethod setMethod : PsiUtils.extractSetMethods(parameterClass)) {
            PsiType parameterType = setMethod.getParameterList().getParameters()[0].getType();
            importType(psiJavaFile, parameterType);
            stringBuilder.append("    ").append(
                    format("%s.%s(request(%s.class))", fieldCode.getName(), setMethod.getName(),
                            ((PsiClassReferenceType) parameterType).getClassName())
            ).append(";\n");
        }
        fieldCode.setSetCode(stringBuilder.toString());

        return fieldCode;
    }

    public static PsiMethod generateMethodContent(Project project, PsiJavaFile psiJavaFile, PsiClass sourceClass, PsiClass testClass, PsiMethod method) {

        PsiField sourceField = findField(testClass, sourceClass);

        ArrayList<String> params = new ArrayList<>();
        StringBuilder methodContent = new StringBuilder();
        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            PsiClass parameterClass = PsiTypesUtil.getPsiClass(parameter.getType());
            if (parameterClass != null) {
                ((PsiJavaFile) testClass.getContainingFile()).importClass(parameterClass);
            }
            FieldCode fieldCode = generateSetter(psiJavaFile,parameter);
            methodContent.append(fieldCode.getSetCode());
            params.add(fieldCode.getName());
        }
        String returnTypeStr = method.getReturnTypeElement().getText();
        String methodName = method.getName();
        if (returnTypeStr.equals("void")) {
            methodContent.append(format("%s.%s(%s);", sourceField.getName(), methodName, String.join(",", params)));
        } else {
            methodContent.append(format("%s %s=%s.%s(%s);", returnTypeStr, methodName, sourceField.getName(), methodName, String.join(",", params)));
            methodContent.append(format("Asserter.assertNotNull(%s);", methodName));
        }

        String fullMethod = TestTemplate.getInstance().loadTestMethodTemplate()
                .replace("${NAME}", methodName)
                .replace("${BODY}", methodContent);

        return JVMElementFactories.getFactory(testClass.getLanguage(), project).createMethodFromText(fullMethod, testClass);
    }

    private static PsiField findField(PsiClass testClass, PsiClass sourceClass) {
        for (PsiField testClassField : testClass.getAllFields()) {
            if (testClassField.getType().getCanonicalText().equals(sourceClass.getQualifiedName())) {
                return testClassField;
            }

        }

        // TODO 如果没有找到则创建新的Field
        throw new UnsupportedOperationException("");
    }


}
