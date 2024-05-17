package com.example.dbms.Pojo;

import java.util.*;

public class SharedDataStorage {
    private static final SharedDataStorage instance = new SharedDataStorage();

    private String username=null;
    private Map<String,List<Map<String,String>>> views=new HashMap<>();


    private SharedDataStorage() {
        // 私有构造函数，防止外部实例化
    }
    public void insertViews(String viewPath,List<Map<String,String>> view){

    }

    public static SharedDataStorage getInstance() {
        return instance;
    }

    public void setThreadLocalData(String data) {
        this.username=data;
    }

    public String getThreadLocalData() {
        return username;
    }

    public void clearThreadLocalData() {
       this.username=null;
    }
}

