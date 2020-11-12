package github.cweijan.http.test.config;

import com.intellij.openapi.roots.ExternalLibraryDescriptor;

/**
 * @author cweijan
 * @since 2020/11/01 0:10
 */
public class Constant {
    /**
     * 上下文菜单字体
     */
    public static final String FONT_FAMILY = "Microsoft yaHei";
    public static final String HTTP_TEST_VERSION = "0.0.9";
    public static final String SPRING_BOOT_VERSION = "2.3.0.RELEASE";
    /**
     * 上下文菜单文字
     */
    public static final String TEST_TEXT = "创建Http测试用例";
    public static final String SPRING_TEST_TEXT = "创建Spring测试用例";
    public static final String PARAM_CREATE_TEXT = "创建请求参数";
    /**
     * 依赖检测类
     */
    public static final String DEPENDENCY_ANNOTATION = "io.github.cweijan.mock.jupiter.HttpTest";
    public static final String SPRING_DEPENDENCY_ANNOTATION = "org.springframework.boot.test.context.SpringBootTest";
    /**
     * lombok依赖检测类
     */
    public static final String LOMBOK_DEPENDENCY_ANNOTATION = "lombok.Data";
    /**
     * 依赖
     */
    public static final ExternalLibraryDescriptor TESTNG_DESCRIPTOR =
            new ExternalLibraryDescriptor("io.github.cweijan", "http-test", HTTP_TEST_VERSION, HTTP_TEST_VERSION, HTTP_TEST_VERSION);
    public static final ExternalLibraryDescriptor SPRING_TESTNG_DESCRIPTOR =
            new ExternalLibraryDescriptor("org.springframework.boot", "spring-boot-starter-test", SPRING_BOOT_VERSION, SPRING_BOOT_VERSION, SPRING_BOOT_VERSION);

}
