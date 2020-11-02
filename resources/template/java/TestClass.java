import static io.github.cweijan.mock.Mocker.*;
import static io.github.cweijan.mock.request.Generator.*;
import io.github.cweijan.mock.Asserter;
import org.junit.jupiter.api.Test;
import javax.annotation.Resource;

#set($name = ${CLASS_NAME.replaceAll(".+\.(\w+)$","$1")})
#set($name = $name.substring(0,1).toLowerCase() + $name.substring(1))
#parse("File Header.java")
class ${NAME} {

    @Resource
    private ${CLASS_NAME} ${name};
    ${BODY}

}
