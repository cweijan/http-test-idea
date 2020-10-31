package github.cweijan.http.test.template.java;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.impl.CustomFileTemplate;
import github.cweijan.http.test.template.TestTemplate;

/**
 * @author cweijan
 * @since 2020/10/31 17:50
 */
public class JavaTestTemplate {

    public static final FileTemplate classTemplate;

    static {
        classTemplate = new CustomFileTemplate(TestTemplate.CLASS_PREFIX + "JAVA", "java");
        initClassTemplate();
    }

    private static void initClassTemplate() {
        classTemplate.setText("import static io.github.cweijan.mock.Mocker.*;\n" +
                "import static io.github.cweijan.mock.Asserter.*;\n" +
                "import org.junit.jupiter.api.Test;\n" +
                "import javax.annotation.Resource;\n" +
                "\n" +
                "#set($name = ${CLASS_NAME.replaceAll(\".+\\.(\\w+)$\",\"$1\")})\n" +
                "#set($name = $name.substring(0,1).toLowerCase() + $name.substring(1))\n" +
                "#parse(\"File Header.java\")\n" +
                "class ${NAME} {\n" +
                "\n" +
                "  @Resource\n" +
                "  private ${CLASS_NAME} ${name};" +
                "  ${BODY}\n" +
                "\n" +
                "}\n");

    }


}
