package corsex.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.logging.Logger;


@Controller
public class MainController {

    private Logger logger = Logger.getLogger(MainController.class.getName());

    /**
     * /test 엔드 포인트를 요청하는 main.html 을 정의한다.
     */
    @GetMapping("/")
    public String main(){
        return "main";
    }

    /**
     * 자바 스크립트에서 호출하는 API 를 가정한다.
     */
    @PostMapping("/test")
    @ResponseBody
    @CrossOrigin("http://localhost:8080") // 교차 출처 요청을 허용한다.
    public String test(){
        logger.info("Test API method Called");
        return "Hello";
    }















}
