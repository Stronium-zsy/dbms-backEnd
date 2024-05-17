package com.example.dbms.Utils;

import com.example.dbms.Pojo.Result;
import org.apache.ibatis.jdbc.Null;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sqlParserUtil {

    private XMLUtil xmlUtil;
    private examineUtil examineUtil = new examineUtil();
    public String databasename = "";

    public sqlParserUtil(XMLUtil xmlUtil) {
        this.xmlUtil = xmlUtil;
    }

    public sqlParserUtil() {


    }

    public String extractTableName(String sql) {
        // 使用正则表达式匹配查询语句中的表名
        String regex = "(?i)\\bfrom\\s+(\\w+)\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1); // 返回匹配到的表名
        } else {
            return null; // 如果未找到匹配的表名，则返回null
        }
    }
    private static String extractDatabaseName(String sql) {
        String regex = "(?i)^\\s*(?:use|drop)\\s+(\\w+)\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null; // 如果没有匹配到数据库名，返回 null
        }
    }


    public Result processSql(String sql) {

        String lowerSql = sql.toLowerCase(); // 转换为小写
        System.out.println(lowerSql);
        try {
            if (lowerSql.matches("(?s)^\\s*insert\\s+into.*")) {
                if (databasename.equals("")) {
                    throw new Exception("Database Not Select");
                }
                if(!FileUtil.findDatabases(databasename)){
                    throw new Exception("Database "+databasename+" does not exist");
                }
                try {
                    String ret = parseInsert(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
                // 解析 INSERT 语句并执行插入操作
            } else if (lowerSql.matches("(?s)^\\s*update.*")) {

                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    String ret = parseUpdate(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*delete\\s+from.*")) {

                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    String ret = parseDelete(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*select.*")) {

                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }

                    // 解析查询语句，获取表名
                    String tableName = extractTableName(lowerSql);

                    // 检查表名是否在数据库中存在
                    if (FileUtil.findTable(databasename, tableName)) {
                        // 如果表存在，则执行表查询
                        List<Map<String, String>> ret = parseSelect(lowerSql);
                        return Result.success(ret);
                    } else {
                        // 如果表不存在，则执行视图查询
                        List<String> Messages = isView(lowerSql);
                        String viewMessage = Messages.get(Messages.size() - 1);
                        Messages = Messages.subList(0, Messages.size() - 1);
                        if (!viewMessage.equals("View Not Found")) {
                            List<Map<String, String>> temp = parseSelect(viewMessage);
                            List<Map<String, String>> ret = new ArrayList<>();
                            if(Messages.size()==1&&Messages.get(0).equals("*")){
                                return Result.success(temp);
                            }

                            // 遍历视图数据，将列名和数据交替存储在 ret 中
                            for (Map<String, String> map : temp) {
                                Map<String, String> rowData = new HashMap<>();
                                for (String selectName : Messages) {
                                    if (map.containsKey(selectName)) {
                                        // 添加列名和对应的数据到 rowData 中
                                        rowData.put(selectName, map.get(selectName));
                                    } else {
                                        throw new Exception("Column " + selectName + " Not Found");
                                    }
                                }
                                ret.add(rowData); // 将当前行的数据添加到 ret 中
                            }

                            return Result.success(ret);
                        }
                    }

                    // 如果表未找到，或者视图信息解析失败，这里应该进行相应的处理
                    List<Map<String, String>> ret = parseSelect(lowerSql);
                    return Result.success(ret);
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?is)^\\s*create\\s+table\\s+\\w+\\s*\\(.*\\)\\s*;?\\s*$")) {

                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    String ret = parseCreate(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*create database\\s+\\w+\\s*;?\\s*$")) {
                try {
                    String ret = parseCreateDatabase(lowerSql);
                    return Result.success(ret, "");

                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*use\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*;?\\s*$")) {
                try {
                    String db=extractDatabaseName(lowerSql);
                    if(!FileUtil.findDatabases(db)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    String ret = parseUseDatabase(lowerSql);
                    System.out.println(ret);
                    return Result.success(ret, "");

                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*show\\s+tables\\s*;\\s*$")) {

                try {
                    if (this.databasename.equals("")) {
                        return Result.error("no database selected");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    List<Map<String, String>> ret = FileUtil.showTables(this.databasename);
                    for (Map<String, String> m : ret) {
                        //去掉后缀.xml
                        m.put(this.databasename, m.get(this.databasename).substring(0, m.get(this.databasename).length() - 4));
                    }

                    return Result.success(ret);
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?i)^\\s*create\\s+view\\s+\\w+\\s+as\\s+select.*")) {
                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    // 处理视图操作
                    String ret = parseCreateView(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*show\\s+databases\\s*;\\s*$")) {
                try {
                    List<Map<String, String>> ret = FileUtil.showDatabases();
                    return Result.success(ret);
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?s)^\\s*drop\\s+\\w+\\s*;?\\s*$")) {
                try {
                    String db=extractDatabaseName(lowerSql);
                    if(!FileUtil.findDatabases(db)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }

                    String ret = parseDropDatabase(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }

            } else if (lowerSql.matches("(?s)^\\s*drop\\s+table\\s+\\w+\\s*;?\\s*$")) {
                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    String ret = parseDropTable(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?i)^\\s*alter\\s+table\\s+(\\w+)\\s+(add\\s+column|drop\\s+column|modify\\s+column)\\s+(.+)\\s*;?\\s*$")) {
                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    String ret = parseAlterTable(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?i)^\\s*alter\\s+table\\s+(\\w+)\\s+update\\s+column\\s+(\\w+)\\s*;?\\s*$")) {
                try {
                    if (databasename.equals("")) {
                        throw new Exception("Database Not Select");
                    }
                    if(!FileUtil.findDatabases(databasename)){
                        throw new Exception("Database "+databasename+" does not exist");
                    }
                    String ret = parseAlterTable(lowerSql);
                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }


            } else if (lowerSql.matches("(?i)^\\s*revoke\\s+.*$")) {
                try {
                    // 解析并执行 REVOKE 语句的代码
                    // 例如：revoke select on dbname.tablename from 'username'@'hostname';
                    // 根据具体情况编写代码
                    String ret = parseRevoke(lowerSql);


                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            } else if (lowerSql.matches("(?i)^\\s*grant\\s+.*$")) {
                try {
                    // 解析并执行 GRANT 语句的代码
                    // 例如：grant select on dbname.tablename to 'username'@'hostname';
                    // 根据具体情况编写代码
                    String ret = parseGrant(lowerSql);

                    return Result.success(ret, "");
                } catch (Exception e) {
                    return Result.error(e.getMessage());
                }
            }else if(lowerSql.matches("(?i)^\\s*drop\\s+view\\s+(\\w+)\\s*;$")){
                try{
                    String ret = parseDropView(lowerSql);
                    return Result.success(ret, "");
                }catch(Exception e){
                    return Result.error(e.getMessage());
                }

            }
            else {
                return Result.error("Unsupported SQL statement: " + lowerSql);

            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }


    }
    private String updateTable(String sql) throws Exception {
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }


        return"";
    }

    private List<String> isView(String sql) throws Exception {
        if (this.databasename == null) {
            throw new Exception("No Database Selected");
        }
        String regex = "(?i)^\\s*select\\s+((\\*|\\w+)(\\s*,\\s*(\\*|\\w+))*)\\s+from\\s+(\\w+)\\s*;$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        List<String> columnNames = new ArrayList<>();
        if (matcher.find()) {
            String columnName = matcher.group(1);
            String[] tempNames = columnName.split(",");

            for (String s : tempNames) {
                columnNames.add(s);
            }
            String viewName = matcher.group(5);
            String databaseFolderPath = "resources/static/databases/" + databasename;
            String tableFolderPath = databaseFolderPath + "/views.xml";
            this.xmlUtil = new XMLUtil(tableFolderPath);
            List<String> temp = new ArrayList<>();
            temp.add("*");
            List<Map<String, String>> viewInfo = this.xmlUtil.selectData(viewName, temp, new HashMap<>());
            if (viewInfo != null) {
                for (Map<String, String> map : viewInfo) {
                    if (map != null && map.containsKey("name") && map.containsKey("sql")) {
                        String name = map.get("name");
                        String retsql = map.get("sql");
                        if (name.equals(viewName)) {
                            columnNames.add(retsql);
                            return columnNames;
                        }

                    }
                }
            }
// 在这里处理未找到视图或视图信息不完整的情况

        }
        columnNames.add("View Not Found");
        return columnNames;

    }

    private String parseDropView(String sql) throws Exception {
        // 使用正则表达式解析 DROP VIEW 语句
        if (this.databasename == null) {
            throw new Exception("No Database Selected");
        }
        String regex = "(?i)^\\s*drop\\s+view\\s+(\\w+)\\s*;$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String viewName = matcher.group(1); // 视图名
            String databaseFolderPath = "resources/static/databases/" + databasename;
            String tableFolderPath = databaseFolderPath + "/views.xml";
            this.xmlUtil = new XMLUtil(tableFolderPath);
            List<Map<String, String>> viewsInfo = this.xmlUtil.selectData("*", new ArrayList<>(), new HashMap<>());
            if (viewsInfo != null) { // 检查 viewsInfo 是否为 null
                boolean viewExists = false;
                for (Map<String, String> map : viewsInfo) {
                    if (map != null && map.containsKey("name")) { // 检查 map 是否为 null，并且是否包含 "name" 键
                        if (map.get("name").equals(viewName)) {
                            viewExists = true;
                            break;
                        }
                    }
                }
                if (!viewExists) {
                    throw new Exception("View " + viewName + " does not exist");
                }
            } else {
                throw new Exception("No views found");
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"drop");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }

            // 删除视图数据
            this.xmlUtil.deleteData("view", "name", viewName);
            return "Successfully dropped view " + viewName;
            // 处理 DROP VIEW 语句
        }
        return "Invalid DROP VIEW statement: " + sql;

    }
    private String parseCreateView(String sql) throws Exception {
        // 使用正则表达式解析 CREATE VIEW 语句
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        String regex = "(?i)^\\s*create\\s+view\\s+(\\w+)\\s+as\\s+select\\s+(.+)$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String viewName = matcher.group(1); // 视图名
            if(FileUtil.findTable(databasename,viewName)){
                throw new Exception("Table " + viewName + " already exists");
            }
            String selectSql = "select "+matcher.group(2); // 查询语句
            String databaseFolderPath = "resources/static/databases/" + databasename;
            String tableFolderPath = databaseFolderPath + "/views.xml";
            this.xmlUtil = new XMLUtil(tableFolderPath);
            List<Map<String,String>> viewsInfo = this.xmlUtil.selectData("*", new ArrayList<>(), new HashMap<>());
            if (viewsInfo != null) { // 检查 viewsInfo 是否为 null
                for (Map<String,String> map : viewsInfo) {
                    if (map != null && map.containsKey("name")) { // 检查 map 是否为 null，并且是否包含 "name" 键
                        if (map.get("name").equals(viewName)) {
                            throw new Exception("View " + viewName + " already exists");
                        }
                    }
                }
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"create");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }

            this.xmlUtil.insertData("view", new String[]{"name", "sql"}, new String[]{viewName, selectSql});
            return "Successfully Create View " + viewName;
            // 处理 CREATE VIEW 语句
        }
        return "Invalid CREATE VIEW statement: " + sql;
    }

    private String parseRevoke(String sql) throws Exception {
        // 使用正则表达式解析 REVOKE 语句
        String regex = "(?i)^\\s*revoke\\s+(\\w+)\\s+on\\s+(\\w+)\\s+from\\s+'(\\w+)'\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String permission = matcher.group(1); // 权限
            if(!(permission.equals("select")
                    ||permission.equals("insert")
                    ||permission.equals("update")
                    ||permission.equals("delete")
                    ||permission.equals("create")
                    ||permission.equals("drop")
                    ||permission.equals("all")
                    ||permission.equals("alter")
                    ||permission.equals("show"))){
                throw new Exception("Invalid permission: " + permission);
            }
            String database = matcher.group(2); // 数据库名
            String username = matcher.group(3); // 用户名
            if(!FileUtil.findDatabases(database)){
                throw new Exception("Database " + database + " does not exist");
            }
            if(!FileUtil.findUser(username)){
                throw new Exception("User " + username + " does not exist");
            }
            this.xmlUtil=new XMLUtil("./resources/static/users/"+username);

            xmlUtil.deleteData(username,database,permission);

            return "Revoke successful\nPermission: " + permission + "\nDatabase: " + database +
                    "\nUsername: " + username;
        } else {
            throw new Exception("Invalid revoke statement: " + sql);
        }
    }
    private String parseGrant(String sql) throws Exception {
        // 使用正则表达式解析 REVOKE 语句
        String regex = "(?i)^\\s*grant\\s+(\\w+)\\s+on\\s+(\\w+)\\s+from\\s+'(\\w+)'\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String permission = matcher.group(1); // 权限
            if(!(permission.equals("select")
                    ||permission.equals("insert")
                    ||permission.equals("update")
                    ||permission.equals("delete")
                    ||permission.equals("create")
                    ||permission.equals("drop")
                    ||permission.equals("all")
                    ||permission.equals("alter")
                    ||permission.equals("show"))){
                throw new Exception("Invalid permission: " + permission);
            }
            String database = matcher.group(2); // 数据库名
            String username = matcher.group(3); // 用户名
            if(!FileUtil.findDatabases(database)){
                throw new Exception("Database " + database + " does not exist");
            }
            if(!FileUtil.findUser(username)){
                throw new Exception("User " + username + " does not exist");
            }
            this.xmlUtil=new XMLUtil("./resources/static/users/"+username);
            String[] columns=new String[]{database};
            String[] actions=new String[]{permission};
            if(new examineUtil(username).examinePermission(database,permission).equals("Examine Permission Passed")){
                throw new Exception("User " + username + " already has permission " + permission + " on database " + database);
            }
            xmlUtil.insertData(username,columns,actions);

            return "Grant successful\nPermission: " + permission + "\nDatabase: " + database +
                    "\nUsername: " + username;
        } else {
            throw new Exception("Invalid grant statement: " + sql);
        }
    }

    private String parseAlterTable(String sql) throws Exception {
        // 使用正则表达式解析 ALTER TABLE 语句
        String regex = "(?i)^\\s*alter\\s+table\\s+(\\w+)\\s+(add\\s+column|drop\\s+column|modify\\s+column)\\s+(.+)\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);


        if (matcher.find()) {
            String tableName = matcher.group(1);
            String action = matcher.group(2);
            String columnsInfo = matcher.group(3);
            String[] columnDefinitions = columnsInfo.split("\\s*,\\s*");

            // 提取列名和列类型
            String[] columnNames = new String[columnDefinitions.length];
            String[] columnTypes = new String[columnDefinitions.length];
            String databaseFolderPath = "resources/static/databases/" + databasename;

            String tableFolderPath = databaseFolderPath + "/" + tableName+".xml";
            if(!FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " does not exist");
            }


            this.xmlUtil = new XMLUtil(tableFolderPath);
            this.examineUtil=new examineUtil(xmlUtil);

            if(action.equals("add column")){

                for (int i = 0; i < columnDefinitions.length; i++) {
                    String[] parts = columnDefinitions[i].split("\\s+");
                    if (parts.length != 2) {
                        throw new Exception("Invalid column definition: " + columnDefinitions[i]);
                    }
                    columnNames[i] = parts[0];
                    columnTypes[i] = parts[1];

                }
                columnTypes[columnDefinitions.length-1] = columnTypes[columnDefinitions.length-1].substring(0,columnTypes[columnDefinitions.length-1].length()-1);

                String examineMessage=examineUtil.examineAlter(columnNames,"add column");
                if(!examineMessage.equals("Examine Alter Passed")){
                    throw new Exception(examineMessage);
                }
            }
            else if(action.equals("drop column")) {

                for (int i = 0; i < columnDefinitions.length; i++) {
                    String[] parts = columnDefinitions[i].split("\\s+");
                    columnNames[i] = parts[0];
                }

                columnNames[columnNames.length-1] = columnNames[columnNames.length-1].substring(0,columnNames[columnNames.length-1].length()-1);

                String examineMessage=examineUtil.examineAlter(columnNames,"drop column");
                if(!examineMessage.equals("Examine Alter Passed")){
                    throw new Exception(examineMessage);
                }

            }else{
                for (int i = 0; i < columnDefinitions.length; i++) {
                    String[] parts = columnDefinitions[i].split("\\s+");
                    if (parts.length != 2) {
                        throw new Exception("Invalid column definition: " + columnDefinitions[i]);
                    }
                    columnNames[i] = parts[0];
                    columnTypes[i] = parts[1];
                }
                String examineMessage=examineUtil.examineAlter(columnNames,"modify column");
                if(!examineMessage.equals("Examine Alter Passed")){
                    throw new Exception(examineMessage);
                }
            }
            String permissionMessage=this.examineUtil.examinePermission(databasename,"alter");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }


            xmlUtil.alterTable(tableName, columnNames, columnTypes,action);

            return "Successfully execute " + sql;
        } else {
            return "Invalid ALTER TABLE statement";
        }
    }



    private String parseDropTable(String sql) throws Exception{
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }

        // 使用正则表达式解析 DROP TABLE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*drop\\s+table\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            if(!FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " does not exist");
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"drop");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            FileUtil.deleteTable(databasename,tableName);
            return "Success";
        } else {
            throw new Exception("Invalid DROP TABLE statement: " + sql);
        }
    }
    private String parseDropDatabase(String sql) throws Exception{

        // 使用正则表达式解析 DROP DATABASE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*drop\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            if(!FileUtil.findDatabases(dbName)){
                throw new Exception("Database " + dbName + " does not exist");
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"drop");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            FileUtil.deleteDatabaseFolder(dbName);
            return "Success";
        } else {
            throw new Exception("Invalid DROP DATABASE statement: " + sql);
        }
    }
    private String parseCreateDatabase(String sql) throws  Exception{

        // 使用正则表达式解析 CREATE 语句
        FileUtil fileUtil = new FileUtil();

        Pattern pattern = Pattern.compile("(?i)^\\s*create\\s+database\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");

        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            if(FileUtil.findDatabases(dbName)){
                throw new Exception("Database " + dbName + " already exists");
            }
            String permissionMessage=new examineUtil().examinePermission(databasename,"create");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            fileUtil.createDatabaseFolder(dbName);
            return "Success";
        } else {
            throw new Exception("Invalid CREATE DATABASE statement: " + sql);
        }

    }

    private String parseUseDatabase(String sql) throws Exception {
        // 使用正则表达式解析 USE DATABASE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*use\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String dbName = matcher.group(1);
            if(FileUtil.findDatabases(dbName)){
                this.databasename = dbName;
                return "Successfully use database " + dbName;
            }else{
                throw new Exception("No DataBase Found " + sql);
            }
        } else {
            throw new Exception("Invalid USE DATABASE statement: " + sql);
        }
    }


    private String parseCreate(String sql) throws Exception {
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        // 使用正则表达式解析 CREATE 语句
        Pattern pattern = Pattern.compile("(?i)^\\s*create\\s+table\\s+(\\w+)\\s*\\((.*)\\)\\s*;?\\s*$");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            String[] columnDefinitions = matcher.group(2).split("\\s*,\\s*");
            String[] columnNames = new String[columnDefinitions.length];
            String[] columnTypes = new String[columnDefinitions.length];

            for (int i = 0; i < columnDefinitions.length; i++) {
                String[] parts = columnDefinitions[i].trim().split("\\s+");
                columnNames[i] = parts[0];
                columnTypes[i] = parts[1];
            }
            String examineType=new examineUtil().examineType(columnTypes);
            if(!examineType.equals("Examine type passed")){
                return examineType;
            }

            String databaseFolderPath = "resources/static/databases/" + databasename;

            String tableFolderPath = databaseFolderPath + "/" + tableName;
            if(FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " already exists");
            }
            this.xmlUtil = new XMLUtil(tableFolderPath);
            String permissionMessage=new examineUtil().examinePermission(databasename,"create");
            if(!permissionMessage.equals("Examine Permission Passed")){
                throw new Exception(permissionMessage);
            }
            // 调用 XMLUtil 创建表方法
            xmlUtil.createTable(tableName, columnNames, columnTypes);
            return "Successfully execute " + sql;
        } else {
            return "Invalid Create statement";
        }
    }






    private String parseInsert(String sql) throws Exception {
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        String regex = "^\\s*INSERT\\s+INTO\\s+(\\w+)\\s*\\((.*?)\\)\\s*VALUES\\s*(.*?)\\s*;?\\s*$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        // 使用正则表达式解析 INSERT 语句
        Matcher matcher = pattern.matcher(sql);
        try {
            if (matcher.find()) {
                String tableName = matcher.group(1);
                String[] columnNames = matcher.group(2).split("\\s*,\\s*");
                String values = matcher.group(3);

                String databaseFolderPath = "resources/static/databases/" + databasename;

                String tableFolderPath = databaseFolderPath + "/" + tableName;
                if(!FileUtil.findTable(databasename,tableName)){
                    throw new Exception("Table " + tableName + " does not exist");
                }

                this.xmlUtil = new XMLUtil(tableFolderPath);
                this.examineUtil=new examineUtil(xmlUtil);
                // 处理多行值
                List<String[]> allValues = new ArrayList<>();
                Pattern valuesPattern = Pattern.compile("\\((.*?)\\)");
                Matcher valuesMatcher = valuesPattern.matcher(values);

                while (valuesMatcher.find()) {
                    String[] singleValues = valuesMatcher.group(1).split("\\s*,\\s*");

                     //去除每个字符串值的单引号或双引号
                    for (int i = 0; i < singleValues.length; i++) {
                        singleValues[i] = singleValues[i].replaceAll("[\"']", "");
                    }

                    allValues.add(singleValues);
                }
                String message= examineUtil.examineInsert(columnNames,allValues);
                if(!message.equals("Examine Insert Passed")){
                    return message;
                }
                String permissionMessage=new examineUtil().examinePermission(databasename,"insert");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }

                // 调用 XMLUtil 插入数据方法
                xmlUtil.insertMultipleData(tableName, columnNames, allValues);
                return "Successfully execute " + sql;

            } else {
                return "Invalid INSERT statement: " + sql;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private String parseUpdate(String sql) throws Exception {
        if (this.databasename == null) {
            throw new Exception("No Database Selected");
        }

        // 使用正则表达式解析 UPDATE 语句
        Pattern pattern = Pattern.compile("^\\s*UPDATE\\s+(\\w+)\\s+SET\\s+(.*?)\\s*(?:WHERE\\s+(.*?))?\\s*;?\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        try {
            if (matcher.find()) {
                String tableName = matcher.group(1);
                String setClause = matcher.group(2);
                String whereClause = matcher.group(3);

                // 解析 SET 子句，获取列名和对应的值
                String[] setValues = setClause.split("\\s*,\\s*");
                Map<String, String> columnValues = new HashMap<>();
                for (String setValue : setValues) {
                    String[] parts = setValue.split("\\s*=\\s*");
                    String columnName = parts[0];
                    String columnValue = parts[1].replaceAll("[\"']", ""); // 去除单引号或双引号
                    columnValues.put(columnName, columnValue);
                }

                // 解析 WHERE 子句
                Map<String, String> conditions = parseWhereClause(whereClause);

                // 检查权限
                String permissionMessage = new examineUtil().examinePermission(databasename, "update");
                if (!permissionMessage.equals("Examine Permission Passed")) {
                    throw new Exception(permissionMessage);
                }
                String databaseFolderPath = "resources/static/databases/" + databasename;

                String tableFolderPath = databaseFolderPath + "/" + tableName;
                if(!FileUtil.findTable(databasename,tableName)){
                    throw new Exception("Table " + tableName + " does not exist");
                }
                this.xmlUtil= new XMLUtil(tableFolderPath);

                // 调用 XMLUtil 的更新数据方法
                xmlUtil.updateData(tableName, columnValues, conditions);
                return "Successfully execute " + sql;
            } else {
                throw new Exception("Invalid UPDATE statement: " + sql);
            }
        } catch (Exception e) {
            throw e;
        }
    }







    private String parseDelete(String sql) throws  Exception{
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        // 使用正则表达式解析 DELETE 语句
        Pattern pattern = Pattern.compile("^\\s*DELETE\\s+FROM\\s+(\\w+)\\s*(?:WHERE\\s+(\\w+)\\s*=\\s*['\"]?(.*?)['\"]?)?\\s*;?\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columnName = matcher.group(2);
            String value = matcher.group(3);
            String databaseFolderPath = "resources/static/databases/" + databasename;

            String tableFolderPath = databaseFolderPath + "/" + tableName;
            if(!FileUtil.findTable(databasename,tableName)){
                throw new Exception("Table " + tableName + " does not exist");
            }

            this.xmlUtil = new XMLUtil(tableFolderPath);
            this.examineUtil=new examineUtil(xmlUtil);
            String examineMessage= this.examineUtil.examineDelete(columnName);
            if(!examineMessage.equals("Examine Delete Passed")){
                return examineMessage;
            }



            // 检查是否提供了条件
            if (columnName != null && value != null) {
                // 调用 XMLUtil 删除数据方法
                String permissionMessage=new examineUtil().examinePermission(databasename,"delete");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }
                xmlUtil.deleteData(tableName, columnName, value);
                return "Successfully execute "+sql;
            } else {
                // 没有提供条件，则删除全部记录
                String permissionMessage=new examineUtil().examinePermission(databasename,"alter");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }
                xmlUtil.deleteData(tableName, null, null);
                return "Successfully execute "+sql;
            }
        } else {
            return "Invalid Delete statement: " + sql;
        }
    }


    private List<Map<String, String>> parseSelect(String sql) throws  Exception{
        if(this.databasename==null){
            throw new Exception("No Database Selected");
        }
        // 使用正则表达式解析 SELECT 语句
        Pattern pattern = Pattern.compile("^\\s*SELECT\\s+(.*?)\\s+FROM\\s+(\\w+)\\s*(?:WHERE\\s+(.*?))?\\s*;?\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        List<Map<String, String>> ret = new ArrayList<>();
        try {

            if (matcher.find()) {
                String columnsString = matcher.group(1);
                String tableName = matcher.group(2);
                String whereClause = matcher.group(3);

                // 解析列名
                List<String> columnNames;
                if (columnsString.equals("*")) {
                    // 如果选择所有列，则传递一个空列表
                    columnNames = Collections.emptyList();
                } else {
                    // 否则，将逗号分隔的列名字符串拆分为列表
                    columnNames = Arrays.asList(columnsString.split("\\s*,\\s*"));
                }

                // 处理 WHERE 子句中的条件表达式
                Map<String, String> conditions = parseWhereClause(whereClause);

                // 调用 XMLUtil 来处理查询
                String databaseFolderPath = "resources/static/databases/" + databasename;

                String tableFolderPath = databaseFolderPath + "/" + tableName;
                if(!FileUtil.findTable(databasename,tableName)){
                    throw new Exception("Table " + tableName + " does not exist");
                }

                this.xmlUtil = new XMLUtil(tableFolderPath);
                this.examineUtil=new examineUtil(xmlUtil);
                String examineMessage= this.examineUtil.examineSelect(columnNames);
                if(!examineMessage.equals("Examine Select Passed")){
                    throw new Exception(examineMessage);
                }
                String permissionMessage=new examineUtil().examinePermission(databasename,"select");
                if(!permissionMessage.equals("Examine Permission Passed")){
                    throw new Exception(permissionMessage);
                }
                ret = xmlUtil.selectData(tableName, columnNames, conditions);

            } else {
                throw new Exception("Invalid SELECT statement: " + sql);
            }
        }catch(Exception e){
            throw  e;
        }

        return ret;
    }


    // 解析 WHERE 子句中的条件表达式
    // 处理 WHERE 子句中的条件表达式
    private Map<String, String> parseWhereClause(String whereClause) {

        Map<String, String> conditions = new HashMap<>();
        if (whereClause != null && !whereClause.isEmpty()) {
            // 这里可以根据具体的 WHERE 子句格式进行解析
            // 这里假设 WHERE 子句中只有一个条件，并且条件格式为 "columnName = value"
            String[] parts = whereClause.split("\\s*=\\s*");
            if (parts.length == 2) {
                String value = parts[1].replaceAll("[\"']", ""); // 去掉值中的单引号或双引号
                conditions.put(parts[0], value);
            }
        }
        return conditions;
    }


}

