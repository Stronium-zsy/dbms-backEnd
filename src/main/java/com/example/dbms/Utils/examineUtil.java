package com.example.dbms.Utils;

import com.example.dbms.Pojo.SharedDataStorage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Types;
public class examineUtil {
    public examineUtil(){};
    private XMLUtil xmlUtil;
    private String username;
    private List<Map<String ,String>> limits;
    public examineUtil(XMLUtil xmlUtil) throws Exception{

        this.xmlUtil = xmlUtil;
    }

    public examineUtil(String username) throws Exception{
        this.username=username;

    }
    public String examineInsert(String[] columnNames, List<String[]> allValues) throws Exception {
        Map<String,String> tableInfo=xmlUtil.getTableInfo();



        for(String[] values:allValues){
            if(columnNames.length!=values.length){
                return "The number of columns does not match the number of values.";
            }
           for(int i=0;i<values.length;i++){
               if(!tableInfo.containsKey(columnNames[i])){
                   return "Column "+columnNames[i]+" does not exist.";
               }
               if(!examineValue(values[i],tableInfo.get(columnNames[i]))){
                   return "Invalid value for column "+columnNames[i]+".";
               }
           }
        }
        return "Examine Insert Passed";

    }
    public String examineDelete(String columnName) throws Exception {
        if(columnName==null)return "Examine Delete Passed";
        Map<String,String> tableInfo=xmlUtil.getTableInfo();
        if(!tableInfo.containsKey(columnName)){
            return "Column "+columnName+" does not exist.";
        }
        return "Examine Delete Passed";
    }

    public String examinePermission(String databasename,String action) throws Exception {
        if(SharedDataStorage.getInstance().getThreadLocalData().equals("root")&&this.username==null){
            return "Examine Permission Passed";
        }
        List<String> columnName=new ArrayList<>();
        Map<String,String> conditions=new HashMap<>();
        columnName.add(databasename);
        String tempname=SharedDataStorage.getInstance().getThreadLocalData();
        if(this.username!=null){
            tempname=this.username;
        }
        this.xmlUtil=new XMLUtil("resources/static/users/"+tempname);

        List<Map<String,String>> result=this.xmlUtil.selectData("resources/static/users/"+SharedDataStorage.getInstance().getThreadLocalData()+".xml",columnName,conditions);
        for(Map<String,String> map:result){
            if(map.get(databasename).equals(action)){
                return "Examine Permission Passed";
            }
        }

        return "Examine Permission Rejected";
    }
    public String examineAlter(String[] columnName,String action)throws  Exception{
        Map<String,String> tableInfo=xmlUtil.getTableInfo();
        if(action.equals("add column")){
            for(String name:columnName){
                if(tableInfo.containsKey(name)){
                    return "Column "+name+" already exist.";
                }
            }
        }else if(action.equals("drop column")){
            for (String name:columnName){
                if(!tableInfo.containsKey(name)){
                    return "Column "+name+" does not exist.";
                }
            }
        }else if(action.equals("modify column")){
            for (String name:columnName){
                if(!tableInfo.containsKey(name)){
                    return "Column "+name+" does not exist.";
                }
            }
        }

        return "Examine Alter Passed";
    }
    public String examineSelect(List<String> columnName) throws Exception {
        Map<String,String> tableInfo=xmlUtil.getTableInfo();
        for(String name:columnName){
            if(!tableInfo.containsKey(name)){
                return "Column "+name+" does not exist.";
            }
        }
        return "Examine Select Passed";
    }
    public String examineType(String[] types){
        for(String type:types){
            switch (type.toUpperCase()) {
                case "TINYINT":
                case "SMALLINT":
                case "MEDIUMINT":
                case "INT":
                case "INTEGER":
                case "BIGINT":
                case "FLOAT":
                case "REAL":
                case "DOUBLE":
                case "BOOLEAN":
                case "DATE":
                   continue;
                default:
                    // 检查 VARCHAR 类型的长度
                    if (type.matches("(?i)^VARCHAR\\s*\\(\\s*(\\d+)\\s*\\)$")) {
                        continue;
                    } else {
                        // 对于未知的类型，认为校验不通过
                        return type + "not a correct type";
                    }
            }
        }
        return "Examine type passed";
    }


    public boolean examineValue(String value, String type) {
        if (value == null || value.isEmpty()) {
            return true; // 如果值为空，则认为校验通过
        }

        switch (type.toUpperCase()) {
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "INTEGER":
            case "BIGINT":
                try {
                    long longValue = Long.parseLong(value);
                    if (type.toUpperCase().equals("TINYINT") && (longValue < Byte.MIN_VALUE || longValue > Byte.MAX_VALUE)) {
                        return false; // TINYINT 类型值超出范围
                    } else if (type.toUpperCase().equals("SMALLINT") && (longValue < Short.MIN_VALUE || longValue > Short.MAX_VALUE)) {
                        return false; // SMALLINT 类型值超出范围
                    } else if (type.toUpperCase().equals("MEDIUMINT") && (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE)) {
                        return false; // MEDIUMINT 类型值超出范围
                    }
                    // 整数类型校验通过
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "FLOAT":
            case "REAL":
            case "DOUBLE":
                try {
                    double doubleValue = Double.parseDouble(value);
                    // 浮点数类型校验通过
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "BOOLEAN":
                // 布尔类型校验通过
                return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
            case "DATE":
                try {
                    // 尝试将值解析为日期
                    LocalDate.parse(value);
                    // 日期类型校验通过
                    return true;
                } catch (DateTimeParseException e) {
                    return false;
                }
            default:
                // 检查 VARCHAR 类型的长度
                if (type.matches("(?i)^VARCHAR\\s*\\(\\s*(\\d+)\\s*\\)$")) {
                    // 匹配 VARCHAR(长度) 的模式
                    int maxLength = Integer.parseInt(type.replaceAll("\\D", ""));
                    return value.length() <= maxLength;
                } else {
                    // 对于未知的类型，认为校验不通过
                    return false;
                }
        }
    }




}
