package com.example.dbms.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileUtil {
    public FileUtil(){

    }

    public static void createFolder(String databaseFolderPath) {
        File databaseFolder = new File(databaseFolderPath);
        if (!databaseFolder.exists()) {
            if (!databaseFolder.mkdirs()) {
                System.out.println("Failed to create database folder");
            }
        }
    }
    public static boolean findTable(String dbName, String tableName){
        File folder=new File("./resources/static/databases", dbName);
        // 检查文件夹是否存在
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if(file.getName().equals(tableName+".xml")){
                            return true;
                        }
                    }
                }
            }
        } else {
            System.out.println("数据库文件夹不存在或不是一个文件夹。");
        }
        return false;
    }


    public static List<Map<String,String>> showTables(String dbName) {
        File folder = new File("./resources/static/databases", dbName);
        List<Map<String,String>> ret= new java.util.ArrayList<>();
        // 检查文件夹是否存在
        // 检查文件夹是否存在
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        Map<String, String> tableInfo = new HashMap<>();
                        tableInfo.put(dbName, file.getName()); // 将文件名作为表名
                        ret.add(tableInfo);
                    }
                }
            }
        } else {
            System.out.println("数据库文件夹不存在或不是一个文件夹。");
        }

        return ret;
    }
    public static boolean findDatabases(String dbName){
        File folder=new File("./resources/static/databases");
        // 检查文件夹是否存在
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        if(file.getName().equals(dbName)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean findUser(String UserName){
        File folder=new File("./resources/static/users");
        // 检查文件夹是否存在
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if(file.getName().equals(UserName+".xml")){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static List<Map<String,String>> showDatabases(){
        File folder=new File("./resources/static/databases");
        List<Map<String,String>> ret= new java.util.ArrayList<>();
        // 检查文件夹是否存在
        if (folder.exists() && folder.isDirectory()) {
            // 获取文件夹下的所有文件
            File[] files = folder.listFiles();

            // 遍历文件数组并输出文件名
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        Map<String,String> map = new HashMap<>();
                        map.put("name",file.getName());
                        ret.add(map);
                    }
                }
            } else {
                System.out.println("文件夹为空或无法访问。");
            }

        } else {
            System.out.println("指定的路径不是一个有效的文件夹。");
        }
        return ret;
    }

    public static void deleteDatabaseFolder(String dbName) {
        Path path = Paths.get("./resources/static/databases", dbName);
        try {
            Files.walk(path)
                    .sorted((p1, p2) -> -p1.compareTo(p2))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void writeToLogFile(String logMessage) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true))) {
            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void deleteTable(String dataBaseName,String tableName){

        Path path = Paths.get("./resources/static/databases",dataBaseName, tableName+".xml");
        try {
            Files.walk(path)
                    .sorted((p1, p2) -> -p1.compareTo(p2))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createDatabaseFolder(String dbName) throws Exception {
        File databaseFolder = new File("./resources/static/databases", dbName);
        File viewFile = new File(databaseFolder, "views.xml");
        if (!databaseFolder.exists()) {
            if (!databaseFolder.mkdirs()) {
                throw new Exception("Failed to create database folder");
            }
        } else {
            throw new Exception("Database folder already exists");
        }
        if (!viewFile.exists()) {
            if (!viewFile.createNewFile()) {
                throw new Exception("Failed to create view.xml file");
            }
            new XMLUtil(viewFile.getAbsolutePath()).createTable("views", new String[]{}, new String[]{});
            // 如果需要在 view.xml 文件中写入一些内容，可以在此处进行操作
        } else {
            throw new Exception("view.xml file already exists");
        }
    }




}
