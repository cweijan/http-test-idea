package github.cweijan.http.test.template.java;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import github.cweijan.http.test.template.TestTemplate;

/**
 * @author cweijan
 * @since 2020/10/31 17:50
 */
public class JavaTestTemplate implements TestTemplate {

    public static final JavaTestTemplate instance = new JavaTestTemplate();

    private CustomFileTemplate classFileTemplate;
    private CustomFileTemplate emptyClassFileTemplate;
    private String beforeMethod;
    private String testMethod;

    private JavaTestTemplate() {
    }


    @Override
    public FileTemplate loadTestClassTemplate() {
        if (classFileTemplate == null)
            classFileTemplate = loadTemplate("java/TestClass");
        return classFileTemplate;
    }

    @Override
    public FileTemplate loadEmptyClassTemplate() {
        if (emptyClassFileTemplate == null)
            emptyClassFileTemplate = loadTemplate("java/EmptyClass");
        return emptyClassFileTemplate;
    }

    @Override
    public String loadControllerMethodTemplate() {
        return loadTemplateStr("java/ControllerMethod");
    }

    @Override
    public String loadTestMethodTemplate() {
        if (testMethod == null)
            testMethod = loadTemplateStr("java/TestMethod");
        return testMethod;
    }

    @Override
    public String loadBeforeMethodTemplate() {
//        if (beforeMethod == null)
        beforeMethod = loadTemplateStr("java/BeforeMethod");
        return beforeMethod;
    }

    @Override
    public String extension() {
        return "java";
    }

}
