package github.cweijan.http.test.core;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author cweijan
 * @since 2020/11/01 15:53
 */
public class HttpBundle extends DynamicBundle {
    public static final String BUNDLE = "HTTPTestMessage";
    private static final HttpBundle INSTANCE = new HttpBundle();
    private HttpBundle() {
        super(BUNDLE);
    }

    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

}
