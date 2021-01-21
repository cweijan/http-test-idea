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

    public static final String HTTP_TEST_ANNOTATION_NAME = "io.github.cweijan.mock.jupiter.HttpTest";
    public static final String SPRING_TEST_ANNOTATION_NAME = "org.springframework.boot.test.context.SpringBootTest";

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

        if (generateContext.superClassName != null && !generateContext.superClassName.equals("")) {
            ReflectUtil.invoke(JavaTestGenerator.class, "addSuperClass", testClass, project, generateContext.superClassName);
        }

        if (generateContext.createBefore) {
            createBeforeMethod(generateContext);
        }

        if (generateContext.isSpring) {
            checkAndAddAnnotation(project, testClass, SPRING_TEST_ANNOTATION_NAME);
        } else {
            checkAndAddAnnotation(project, testClass, HTTP_TEST_ANNOTATION_NAME);
        }

        return testClass;
    }

    private static void createBeforeMethod(GenerateContext generateContext) {
        String fullMethod = TestTemplate.getInstance().loadBeforeMethodTemplate();
        PsiMethod beforeMethod = JVMElementFactories.getFactory(generateContext.testClass.getLanguage(), generateContext.project).createMethodFromText(fullMethod, generateContext.testClass);
        generateContext.testClass.add(beforeMethod);
        JavaCodeStyleManager.getInstance(generateContext.project).shortenClassReferences(generateContext.testClass);
    }

    private static void checkAndAddAnnotation(Project project, PsiClass testClass, @NotNull String annotationName) {
        PsiClass superClass = testClass.getSuperClass();
        List<PsiAnnotation> allAnnotations = new ArrayList<>(Arrays.asList(testClass.getAnnotations()));
        while (superClass != null) {
            allAnnotations.addAll(Arrays.asList(superClass.getAnnotations()));
            superClass = superClass.getSuperClass();
        }

        for (PsiAnnotation annotation : allAnnotations) {
            if (annotation.getQualifiedName().equals(annotationName)) {
                return;
            }
        }

        PsiModifierList classModifierList = testClass.getModifierList();
        PsiAnnotation addAnnotation = classModifierList.addAnnotation(annotationName);
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

        FieldCode fieldCode = new FieldCode(psiJavaFile, parameter.getType());

        PsiClass parameterClass = PsiTypesUtil.getPsiClass(parameter.getType());
        StringBuilder stringBuilder = new StringBuilder(fieldCode.getNewStatement() + "\n");
        for (PsiMethod setMethod : PsiUtils.extractSetMethods(parameterClass)) {
            PsiParameter psiParameter = setMethod.getParameterList().getParameters()[0];
            PsiType parameterType = psiParameter.getType();
            importType(psiJavaFile, parameterType);
            stringBuilder.append("    ")
                    .append(format("%s %s=%s;", psiParameter.getType().getCanonicalText(), psiParameter.getName(),  buildMock(parameterType)))
                    .append("\n");
            stringBuilder.append("    ")
                    .append(format("%s.%s(%s)", fieldCode.getName(), setMethod.getName(),  psiParameter.getName()))
                .append(";\n");
        }
        fieldCode.setSetCode(stringBuilder.toString());

        return fieldCode;
    }

    private static String buildMock(PsiType type) {

        PsiClass typeClass = PsiTypesUtil.getPsiClass(type);
        PsiType psiType = PsiUtil.extractIterableTypeParameter(type, false);
        if (typeClass != null && psiType != null) {
            String qualifiedName = typeClass.getQualifiedName();
            if (qualifiedName.equals("java.util.List")) {
                return "java.util.Arrays.asList(mock(" + ((PsiClassReferenceType) psiType).getClassName() + ".class))";
            }
        }
        if (type instanceof PsiClassReferenceType) {
            return "mock(" + ((PsiClassReferenceType) type).getClassName() + ".class)";
        } else if (type instanceof PsiPrimitiveType) {
            return "mock(" + ((PsiPrimitiveType) type).getBoxedTypeName() + ".class)";
        }

        return "mock(" + type.getCanonicalText() + ".class)";
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
            FieldCode fieldCode = generateSetter(psiJavaFile, parameter);
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
