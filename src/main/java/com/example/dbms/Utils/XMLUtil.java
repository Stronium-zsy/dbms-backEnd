package com.example.dbms.Utils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.util.Pair;
import jdk.internal.org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.aliyuncs.utils.XmlUtils.getDocument;

public class XMLUtil {

    private String filename;

    public XMLUtil(String filename) {
        this.filename = filename;
    }
    public String readAllXMLFiles() {
        // 获取resources目录下的所有文件夹
        InputStream rootFolder = getClass().getClassLoader().getResourceAsStream(".");

        StringBuilder result = new StringBuilder();

        if (rootFolder != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(rootFolder, StandardCharsets.UTF_8))) {
                String folder;
                while ((folder = reader.readLine()) != null) {
                    if (folder.endsWith("/")) {
                        // 获取文件夹下所有的XML文件
                        InputStream xmlFolder = getClass().getClassLoader().getResourceAsStream(folder);
                        if (xmlFolder != null) {
                            try (BufferedReader xmlReader = new BufferedReader(new InputStreamReader(xmlFolder, StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = xmlReader.readLine()) != null) {
                                    result.append(line).append("\n");
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }
    public void updateData(String tableName, Map<String, String> columnValues, Map<String, String> conditions) throws Exception {
        Document doc = getDocument();

        // 获取表元素列表
        NodeList tableNodes = doc.getElementsByTagName("column");

        // 遍历表元素列表
        for (int i = 0; i < tableNodes.getLength(); i++) {
            Element tableElement = (Element) tableNodes.item(i);

            // 检查条件匹配
            boolean matchesConditions = true;
            for (Map.Entry<String, String> condition : conditions.entrySet()) {
                String conditionColumnName = condition.getKey();
                String conditionValue = condition.getValue();
                NodeList conditionColumnNodes = tableElement.getElementsByTagName(conditionColumnName);
                if (conditionColumnNodes.getLength() > 0) {
                    String conditionColumnValue = conditionColumnNodes.item(0).getTextContent();
                    if (!conditionColumnValue.equals(conditionValue)) {
                        matchesConditions = false;
                        break;
                    }
                } else {
                    matchesConditions = false;
                    break;
                }
            }

            // 如果条件匹配，则更新列值
            if (matchesConditions) {
                for (Map.Entry<String, String> columnValue : columnValues.entrySet()) {
                    String columnName = columnValue.getKey();
                    String newValue = columnValue.getValue();
                    NodeList columnNodes = tableElement.getElementsByTagName(columnName);
                    if (columnNodes.getLength() > 0) {
                        // 更新列的值
                        Node columnNode = columnNodes.item(0);
                        columnNode.setTextContent(newValue);
                    } else {
                        // 如果列不存在，则抛出异常
                        throw new Exception("Column " + columnName + " does not exist in table " + tableName);
                    }
                }
            }
        }

        // 将更新后的文档写回文件
        saveXML(filename,doc);
    }
    public void joinTables(String table1Name, String table2Name, String joinColumn1, String joinColumn2) {
        try {
            // 获取第一个表的 Document 对象
            DocumentBuilderFactory dbFactory1 = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder1 = dbFactory1.newDocumentBuilder();
            Document table1Doc = dBuilder1.parse(new File(table1Name + ".xml"));
            table1Doc.getDocumentElement().normalize();

            // 获取第二个表的 Document 对象
            DocumentBuilderFactory dbFactory2 = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder2 = dbFactory2.newDocumentBuilder();
            Document table2Doc = dBuilder2.parse(new File(table2Name + ".xml"));
            table2Doc.getDocumentElement().normalize();

            // 创建一个新的 Document 对象，用于存储合并后的数据
            Document mergedDoc = dBuilder1.newDocument();

            // 创建根元素
            Element rootElement = mergedDoc.createElement("mergedData");
            mergedDoc.appendChild(rootElement);

            // 获取第一个表的所有行
            NodeList nodeList1 = table1Doc.getElementsByTagName("row");

            // 遍历第一个表的每一行
            for (int i = 0; i < nodeList1.getLength(); i++) {
                Node node1 = nodeList1.item(i);
                if (node1.getNodeType() == Node.ELEMENT_NODE) {
                    Element row1 = (Element) node1;

                    // 获取第一个表的关联列的值
                    String key1 = row1.getAttribute(joinColumn1);

                    // 获取第二个表中匹配的行
                    NodeList nodeList2 = table2Doc.getElementsByTagName("row");
                    for (int j = 0; j < nodeList2.getLength(); j++) {
                        Node node2 = nodeList2.item(j);
                        if (node2.getNodeType() == Node.ELEMENT_NODE) {
                            Element row2 = (Element) node2;

                            // 获取第二个表的关联列的值
                            String key2 = row2.getAttribute(joinColumn2);

                            // 如果两个表的关联列值相等，则合并数据
                            if (key1.equals(key2)) {
                                // 创建新的行元素，并将其添加到合并后的 Document 对象中
                                Element mergedRow = mergedDoc.createElement("row");
                                rootElement.appendChild(mergedRow);

                                // 复制第一个表的数据到合并后的行
                                Node clonedNode1 = row1.cloneNode(true);
                                mergedRow.appendChild(mergedDoc.importNode(clonedNode1, true));

                                // 复制第二个表的数据到合并后的行
                                Node clonedNode2 = row2.cloneNode(true);
                                mergedRow.appendChild(mergedDoc.importNode(clonedNode2, true));
                            }
                        }
                    }
                }
            }

            // 将合并后的数据保存到 XML 文件中
            saveXML(mergedDoc);
            System.out.println("Tables joined and merged data saved to XML file successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertData(String tableName, String[] columnNames, String[] values) throws Exception{

            Document doc = getDocument();

            Element rootElement = doc.getDocumentElement();
            Element tableElement = doc.createElement("column");
            rootElement.appendChild(tableElement);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            for (int i = 0; i < columnNames.length; i++) {
                Element columnElement = doc.createElement(columnNames[i]);
                columnElement.appendChild(doc.createTextNode(values[i]));
                tableElement.appendChild(columnElement);

                // 添加换行节点
                if (i < columnNames.length - 1) {
                    tableElement.appendChild(doc.createTextNode("\n"));
                }
            }

            // 添加缩进
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-first-line", "yes");

            saveXML(doc, transformer,filename);
            System.out.println("Data inserted successfully.");
    }



    public void deleteData(String tableName, String columnName, String value) throws Exception{
        System.out.println(columnName);
        System.out.println(value);

            Document doc = getDocument();

            // 获取根元素
            Element rootElement = doc.getDocumentElement();

            // 获取表元素列表
            NodeList tableNodes = doc.getElementsByTagName("column");

            if (columnName == null && value == null) {
                for (int i = tableNodes.getLength() - 1; i >= 1; i--) {
                    Element tableElement = (Element) tableNodes.item(i);
                    rootElement.removeChild(tableElement);
                }

                // 保存修改后的 XML 文档
                saveXML(filename,doc);
                System.out.println("All data deleted successfully.");

                return;
            }


            // 遍历表元素列表
            for (int i = 0; i < tableNodes.getLength(); i++) {
                Element tableElement = (Element) tableNodes.item(i);

                // 获取列元素
                NodeList columnNodes = tableElement.getElementsByTagName(columnName);
                if (columnNodes.getLength() > 0) {
                    Element columnElement = (Element) columnNodes.item(0);

                    // 检查列的值是否与给定的值匹配
                    if (columnElement.getTextContent().equals(value)) {
                        // 删除整个表元素
                        rootElement.removeChild(tableElement);
                        saveXML(filename,doc);
                        System.out.println("Data deleted successfully.");
                    }
                }
            }

    }
    public Map<String, String> getTableInfo() throws Exception {
        Map<String, String> result = new HashMap<>();
        Document doc = getDocument();

        // 获取表元素列表
        NodeList tableNodes = doc.getElementsByTagName("column");

        // 遍历表元素列表
        Element tableElement = (Element) tableNodes.item(0);
        NodeList columnNodes = tableElement.getChildNodes();
        for (int i = 0; i < columnNodes.getLength(); i++) {
            Node node = columnNodes.item(i);
            // 仅处理元素节点，忽略其他类型的节点（如文本节点）
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element columnElement = (Element) node;
                String columnName = columnElement.getNodeName();
                String columnLimit = columnElement.getAttribute("type");
                result.put(columnName, columnLimit);
            }
        }
        return result;
    }

    public void insertMultipleData(String tableName, String[] columnNames, List<String[]> allValues) throws Exception{
        System.out.println(tableName);
        for(String[] values:allValues){
            for(String value:values){
                System.out.println(value);
            }
        }

        for(String[] values : allValues) {
            insertData(tableName, columnNames, values);
        }
    }

    private Document getDocument() throws Exception{
        if(this.filename.endsWith(".xml")){
            this.filename = this.filename.substring(0,this.filename.length()-4);
        }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            return dBuilder.parse(new File(this.filename + ".xml"));

    }

    public Document alterTable(String tableName, String[] columnNames, String[] columnTypes,String action) throws Exception{
            // 读取XML文件并解析为Document对象
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(filename + ".xml");

            // 获取根元素
            Element rootElement = doc.getDocumentElement();

            // 创建表元素
            // 获取表元素
            NodeList tableElements = rootElement.getElementsByTagName("column");

// 获取第一个表元素
            Element tableElement = (Element) tableElements.item(0);

            if(action.equals("add column")) {
                // 添加新的列元素
                for (int i = 0; i < columnNames.length; i++) {
                    Element columnElement = doc.createElement(columnNames[i]);
                    columnElement.setAttribute("type", columnTypes[i]);
                    tableElement.appendChild(columnElement);
                    // 添加换行
                    tableElement.appendChild(doc.createTextNode("\n    "));
                }

                // 将新创建的表元素插入到根元素的子节点列表中的第一个位置
                if (rootElement.hasChildNodes()) {
                    rootElement.insertBefore(tableElement, rootElement.getFirstChild());
                } else {
                    rootElement.appendChild(tableElement);
                }
            }else if(action.equals("drop column")){
                for (int j = 0; j < columnNames.length; j++) {
                    String columnName = columnNames[j];
                    // 遍历表元素的子节点列表
                    for (int i = 0; i < tableElements.getLength(); i++) {
                        Element removeElement = (Element) tableElements.item(i);
                        // 获取列元素
                        NodeList columnNodes = removeElement.getElementsByTagName(columnName);
                        // 如果找到具有指定名称的列元素，则移除
                        if (columnNodes.getLength() > 0) {
                            Element columnElement = (Element) columnNodes.item(0);
                            removeElement.removeChild(columnElement);
                        }
                    }
                }


            }else if(action.equals("modify column")){
                for (int j = 0; j < columnNames.length; j++) {
                    String columnName = columnNames[j];
                    // 遍历表元素的子节点列表
                    for (int i = 0; i < tableElements.getLength(); i++) {
                        Element removeElement = (Element) tableElements.item(i);
                        // 获取列元素
                        NodeList columnNodes = removeElement.getElementsByTagName(columnName);
                        // 如果找到具有指定名称的列元素，则移除
                        if (columnNodes.getLength() > 0) {
                            Element columnElement = (Element) columnNodes.item(0);
                            removeElement.removeChild(columnElement);
                        }
                    }
                }
                for (int i = 0; i < columnNames.length; i++) {
                    Element columnElement = doc.createElement(columnNames[i]);
                    columnElement.setAttribute("type", columnTypes[i]);
                    tableElement.appendChild(columnElement);
                    // 添加换行
                    tableElement.appendChild(doc.createTextNode("\n    "));
                }

                // 将新创建的表元素插入到根元素的子节点列表中的第一个位置
                if (rootElement.hasChildNodes()) {
                    rootElement.insertBefore(tableElement, rootElement.getFirstChild());
                } else {
                    rootElement.appendChild(tableElement);
                }
            }

            // 保存修改后的XML文件
            saveXML(filename, doc);
            System.out.println("XML file updated successfully.");
            return doc;

    }
    public Document createUser(String UserName, String PassWord) throws  Exception{

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // 创建根元素，并设置为用户名
            Element rootElement = doc.createElement(UserName);
            doc.appendChild(rootElement);

            // 创建表示密码的子元素，并设置文本内容为密码值
            Element passwordElement = doc.createElement("password");
            passwordElement.appendChild(doc.createTextNode(PassWord));
            rootElement.appendChild(passwordElement);

            // 保存 XML 文件
            saveXML(filename, doc);
            System.out.println("XML file created successfully.");
            return doc;

    }
    public String getPassWord(String UserName) throws  Exception{
        Document doc = getDocument();
        NodeList tableElements = doc.getElementsByTagName(UserName);
        if (tableElements.getLength() > 0) {
            Element tableElement = (Element) tableElements.item(0);
            return tableElement.getElementsByTagName("password").item(0).getTextContent();
        } else {
            return null;
        }
    }

    public Document createTable(String tableName, String[] columnNames, String[] columnTypes) throws Exception{

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // 创建根元素
            Element rootElement = doc.createElement(tableName);
            doc.appendChild(rootElement);

            // 创建表元素
            Element tableElement = doc.createElement("column");
            rootElement.appendChild(tableElement);

            // 添加列元素
            for (int i = 0; i < columnNames.length; i++) {
                Element columnElement = doc.createElement(columnNames[i]);
                columnElement.setAttribute("type", columnTypes[i]);
                tableElement.appendChild(columnElement);
                // 添加换行
                tableElement.appendChild(doc.createTextNode("\n    "));
            }

            saveXML(filename,doc);
            System.out.println("XML file created successfully.");
            return doc;

    }


    // 创建 XML 文件
    public void createXML() throws  Exception{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // 创建根节点
            Element rootElement = doc.createElement(this.filename);
            doc.appendChild(rootElement);

            // 保存 XML 文件
            saveXML(doc);

            System.out.println("XML file created successfully.");
    }
    public void createView(String viewName,String sql){

    }

    public List<Map<String, String>> selectData(String tableName, List<String> columnNames, Map<String, String> conditions) throws  Exception{
        System.out.println(tableName);
        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 在这里对每个键值对进行操作
            System.out.println("Key: " + key + ", Value: " + value);
        }

        List<Map<String, String>> result = new ArrayList<>();
            Document doc = getDocument();

            // 获取表元素列表
            NodeList tableNodes = doc.getElementsByTagName("column");

            // 遍历表元素列表
            for (int i = 0; i < tableNodes.getLength(); i++) {
                Element tableElement = (Element) tableNodes.item(i);

                // 检查条件匹配
                boolean matchesConditions = true;
                for (Map.Entry<String, String> condition : conditions.entrySet()) {
                    String conditionColumnName = condition.getKey();
                    String conditionValue = condition.getValue();
                    NodeList conditionColumnNodes = tableElement.getElementsByTagName(conditionColumnName);
                    if (conditionColumnNodes.getLength() > 0) {
                        String conditionColumnValue = conditionColumnNodes.item(0).getTextContent();
                        if (!conditionColumnValue.equals(conditionValue)) {
                            matchesConditions = false;
                            break;
                        }
                    } else {
                        matchesConditions = false;
                        break;
                    }
                }

                // 如果条件匹配，则处理列
                if (matchesConditions) {
                    Map<String, String> row = new HashMap<>();
                    if (columnNames.isEmpty() || columnNames.contains("*")) {
                        // 如果列名列表为空或包含 "*"，则选择所有列
                        NodeList columns = tableElement.getChildNodes();
                        for (int j = 0; j < columns.getLength(); j++) {
                            Node column = columns.item(j);
                            if (column.getNodeType() == Node.ELEMENT_NODE) {
                                // 将列名和值添加到结果中
                                row.put(column.getNodeName(), column.getTextContent());
                            }
                        }
                    } else {
                        // 否则，只选择指定的列
                        for (String columnName : columnNames) {
                            NodeList columnNodes = tableElement.getElementsByTagName(columnName);
                            if (columnNodes.getLength() > 0) {
                                // 获取列的值
                                String columnValue = columnNodes.item(0).getTextContent();
                                // 将列名和值添加到结果中
                                row.put(columnName, columnValue);
                            }
                        }
                    }
                    result.add(row);
                }
            }

            if (result.isEmpty()) {
                System.out.println("Data not found.");
            }

        return result;
    }


    public void saveXML(Document doc, Transformer transformer) throws TransformerException {
        // 创建一个 DOMSource 实例。
        DOMSource domSource = new DOMSource(doc);

       if(this.filename.endsWith(".xml")){
           this.filename = this.filename.substring(0,this.filename.length() - 4);
       }
        // 创建一个 StreamResult 实例。
        StreamResult streamResult = new StreamResult(new File(this.filename + ".xml"));

        // 将 DOMSource 实例转换为 XML 文件。
        transformer.transform(domSource, streamResult);
    }

    public void saveXML(Document doc, Transformer transformer,String filepath) throws TransformerException {
        // 创建一个 DOMSource 实例。
        DOMSource domSource = new DOMSource(doc);
        if(filepath.endsWith(".xml")){
            filepath = filepath.substring(0,filepath.length() - 4);
        }


        // 创建一个 StreamResult 实例。
        StreamResult streamResult = new StreamResult(new File(filepath + ".xml"));

        // 将 DOMSource 实例转换为 XML 文件。
        transformer.transform(domSource, streamResult);
    }
    private void saveXML(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        if(this.filename.endsWith(".xml")){
            this.filename = this.filename.substring(0,this.filename.length() - 4);
        }
        StreamResult result = new StreamResult(new File(this.filename + ".xml")); // 添加后缀名
        transformer.transform(source, result);
    }


    // 保存 XML 文件
    private void saveXML(String filePath, Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        if(filePath.endsWith(".xml")){
            filePath = filePath.substring(0,filePath.length() - 4);
        }
        StreamResult result = new StreamResult(new File(filePath + ".xml")); // 添加后缀名
        transformer.transform(source, result);
    }


}

