package com.example.dbms.Controller;
import com.example.dbms.Pojo.Result;
import com.example.dbms.Pojo.SharedDataStorage;
import com.example.dbms.Utils.FileUtil;
import com.unfbx.chatgpt.ChatGPTClient;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
//@RestController
//@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    //@PostMapping("/execute")
    public Result executePythonScript(@RequestBody Map<String,String> requestBody) {
        Process proc;

        String databaseInfo=readAllXMLFiles("./resources/static");



        String input = requestBody.get("userMessage");
        System.out.println(input);
        System.out.println(databaseInfo);
        try {
            // 指定输入文本，并将其发送给Python脚本
            ProcessBuilder builder = new ProcessBuilder("python", "E:\\IdeaProjects\\DBMS\\src\\main\\resources\\scripts\\main.py",databaseInfo, input);
            builder.redirectErrorStream(true);
            proc = builder.start();


            // 读取Python脚本的输出（指定UTF-8编码）
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),"GBK"));
            StringBuilder ret = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                
                ret.append(line);
            }
            in.close();

            // 等待Python脚本执行完毕
            int exitCode = proc.waitFor();
            if (exitCode != 0) {
                // 如果脚本执行失败，返回错误信息
                return Result.error("Python脚本执行失败，退出码：" + exitCode);
            }

            Result result=Result.success(ret.toString());
            String username = SharedDataStorage.getInstance().getThreadLocalData();
            String logMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +" - User: "+username+
                    " - SQL: " + input + " - Message: " + ret;

            // 将记录写入文件
            FileUtil.writeToLogFile(logMessage);

            return result;
        } catch (IOException | InterruptedException e) {
            return Result.error(e.getMessage());
        }
    }
    @PostMapping("/aihelper")
    public Result aihelper(@RequestBody Map<String,String> requestBody) {
        Process proc;

        String databaseInfo=readAllXMLFiles("./resources/static");



        String input = requestBody.get("userMessage");
        System.out.println(input);
        System.out.println(databaseInfo);
        try {
            // 指定输入文本，并将其发送给Python脚本
            ProcessBuilder builder = new ProcessBuilder("python", "E:\\IdeaProjects\\DBMS\\src\\main\\resources\\scripts\\aihelper.py",databaseInfo, input);
            builder.redirectErrorStream(true);
            proc = builder.start();


            // 读取Python脚本的输出（指定UTF-8编码）
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(),"GBK"));
            StringBuilder ret = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {

                ret.append(line);
            }
            in.close();

            // 等待Python脚本执行完毕
            int exitCode = proc.waitFor();
            if (exitCode != 0) {
                // 如果脚本执行失败，返回错误信息
                return Result.error("Python脚本执行失败，退出码：" + exitCode);
            }

            Result result=Result.success(ret.toString());
            String username = SharedDataStorage.getInstance().getThreadLocalData();
            String logMessage = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +" - User: "+username+
                    " - SQL: " + input + " - Message: " + ret;

            // 将记录写入文件
            FileUtil.writeToLogFile(logMessage);

            return result;
        } catch (IOException | InterruptedException e) {
            return Result.error(e.getMessage());
        }
    }



    public String readAllXMLFiles(String path) {
        File folder=new File(path);
        String ret="";
        // 检查文件夹是否存在
        if (folder.exists() && folder.isDirectory()) {
            // 获取文件夹下的所有文件
            File[] files = folder.listFiles();

            // 遍历文件数组并输出文件名
            for(File file:files){
                if(file.isFile()&&file.getName().endsWith(".xml")){
                    ret+=readFileContents(file);
                }else if(file.isDirectory()){
                    ret+=file.getName();
                    ret+=readAllXMLFiles(file.getPath());

                }
            }

        } else {
            System.out.println("指定的路径不是一个有效的文件夹。");
        }
        return ret;
    }

    private String readFileContents(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }


    // 调用示例


}
