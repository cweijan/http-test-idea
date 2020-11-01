package github.cweijan.http.test.template;


import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import github.cweijan.http.test.template.java.JavaTestTemplate;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author cweijan
 * @since 2020/10/31 17:52
 */
public interface TestTemplate {

    String CLASS_PREFIX = "HTTP_TEST_CLASS_";
    String METHOD_PREFIX = "HTTP_TEST_METHOD_";

    String BEFORE_PREFIX = "HTTP_TEST_BEFORE_";

    FileTemplate loadTestClassTemplate();

    String loadTestMethodTemplate();

    String loadBeforeMethodTemplate();

    String extension();

    static TestTemplate getInstance() {
        return getInstance(TemplateType.java);
    }

    static TestTemplate getInstance(@NotNull TemplateType templateType) {
        switch (templateType) {
            case java:
            default:
                return JavaTestTemplate.instance;
        }
    }

    default CustomFileTemplate loadTemplate(String prefix, String filePath) {
        CustomFileTemplate fileTemplate = new CustomFileTemplate(prefix + this.extension(), this.extension());
        fileTemplate.setText(loadTemplateStr(filePath));
        return fileTemplate;
    }

    default String loadTemplateStr(String file) {
        InputStream in = TestTemplate.class.getResourceAsStream("/template/" + file + "." + this.extension());
        try {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
