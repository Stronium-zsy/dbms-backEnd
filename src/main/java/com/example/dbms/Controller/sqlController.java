package com.example.dbms.Controller;

import com.example.dbms.Pojo.Result;
import com.example.dbms.Pojo.SharedDataStorage;
import com.example.dbms.Service.Impl.sqlServiceImpl;
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
public class sqlController {
    @Autowired
    private  sqlServiceImpl Service;
    public sqlController(sqlServiceImpl Service) {
        this.Service = Service;
    }

    @PostMapping("/processsql")
    public Result processData(@RequestBody Map<String, String> requestBody) {
        String sql = requestBody.get("sql");
        System.out.println(sql);
        Result result = Service.processSql(sql);
        String username = SharedDataStorage.getInstance().getThreadLocalData();
        System.out.println(username);

        // 记录SQL语句和返回的消息以及时间戳
        String logMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +" - User: "+username+
                " - SQL: " + sql + " - Message: " + result.getMessage();

        // 将记录写入文件
        FileUtil.writeToLogFile(logMessage);

        return result;
    }

}
