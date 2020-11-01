/**
 * 拦截请求, 可在请求之前做一些操作
 */
@org.junit.jupiter.api.BeforeAll
public static void beforeRequest(){
    addRequestInterceptor(request->{
        request.header("cookie","user=http_test");
    });
}