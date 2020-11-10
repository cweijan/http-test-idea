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
    public static final String VERSION = "0.0.9";
    /**
     * 上下文菜单文字
     */
    public static final String TEST_TEXT = "创建Http测试用例";
    public static final String PARAM_CREATE_TEXT = "创建请求参数";
    /**
     * 依赖检测类
     */
    public static final String DEPENDENCY_ANNOTATION = "io.github.cweijan.mock.jupiter.HttpTest";
    /**
     * lombok依赖检测类
     */
    public static final String LOMBOK_DEPENDENCY_ANNOTATION = "lombok.Data";
    /**
     * 依赖
     */
    public static final ExternalLibraryDescriptor TESTNG_DESCRIPTOR =
            new ExternalLibraryDescriptor("io.github.cweijan", "http-test", VERSION, VERSION, VERSION);

}
