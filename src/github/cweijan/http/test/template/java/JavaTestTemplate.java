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

    private JavaTestTemplate() {
    }


    @Override
    public FileTemplate loadTestClassTemplate() {
//        if (classFileTemplate == null)
            classFileTemplate = loadTemplate(CLASS_PREFIX, "java/TestClass");
        return classFileTemplate;
    }

    @Override
    public String loadTestMethodTemplate() {
        return null;
    }

    @Override
    public String loadBeforeMethodTemplate() {
        return null;
    }

    @Override
    public String extension() {
        return "java";
    }

}
