package com.example.myrobotapp.Class;

import com.example.myrobotapp.Class.DeviceTable.DeviceInfo.DeviceTableRowModel;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    public List<DeviceTableRowModel> parseXML(InputStream inputStream) {
        List<DeviceTableRowModel> tableDataList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            // Extract RxPDO entries
            NodeList rxPDOEntries = document.getElementsByTagName("RxPDO").item(0).getChildNodes();
            extractEntries(rxPDOEntries, tableDataList);

            // Extract TxPDO entries
            NodeList txPDOEntries = document.getElementsByTagName("TxPDO").item(0).getChildNodes();
            extractEntries(txPDOEntries, tableDataList);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableDataList;
    }

    private void extractEntries(NodeList entries, List<DeviceTableRowModel> tableDataList) {
        for (int i = 0; i < entries.getLength(); i++) {
            Node node = entries.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String id = getTagValue("ID", element);
                String group = getTagValue("Group", element);
                String index = getTagValue("Index", element);
                String subIndex = getTagValue("SubIndex", element);
                String size = getTagValue("Size", element);
                String role = getTagValue("Role", element);

                DeviceTableRowModel rowData = new DeviceTableRowModel(id, group, index, subIndex, size, role);
                tableDataList.add(rowData);
            }
        }
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
}

