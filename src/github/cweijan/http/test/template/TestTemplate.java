package github.cweijan.http.test.template;


import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import org.apache.commons.io.IOUtils;

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

    default CustomFileTemplate loadTemplate(String prefix, String filePath) {
        CustomFileTemplate fileTemplate = new CustomFileTemplate(prefix + this.extension(), this.extension());
        InputStream in = TestTemplate.class.getResourceAsStream("/template/" + filePath + "." + this.extension());
        String text;
        try {
            text = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileTemplate.setText(text);
        return fileTemplate;
    }

}
