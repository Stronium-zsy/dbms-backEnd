package com.example.dbms.Controller;
import com.example.dbms.Pojo.Result;
import com.example.dbms.Service.Impl.sqlServiceImpl;
import com.example.dbms.Service.Impl.userServiceImpl;
import com.example.dbms.Service.sqlService;
import com.example.dbms.Utils.FileUtil;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins="http://localhost:5173")
@RestController
public class userController {

    @Autowired
    private userServiceImpl Service;
    public userController(userServiceImpl Service) {
        this.Service = Service;
    }
    @PostMapping("/login")
    public Result Login(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String password = requestBody.get("password");
        Result result=Service.login(username, password);
        String logMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +" - action: login"+" - User: "+username+
                " - PassWord: " + password + " - Message: " + result.getMessage();
        FileUtil.writeToLogFile(logMessage);

        return result;

    }
    @PostMapping("/register")
    public Result Register(@RequestBody Map<String, String> requestBody) {
        String username = requestBody.get("username");
        String password = requestBody.get("password");
        Result result=Service.register(username, password);
        String logMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +" - action: register"+" - User: "+username+
                " - PassWord: " + password + " - Message: " + result.getMessage();
        FileUtil.writeToLogFile(logMessage);
        return result;

    }


}
