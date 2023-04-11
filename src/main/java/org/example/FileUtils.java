package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class FileUtils {


    public static ArrayList<String[]> readDataFromFile(String filename) {
        ArrayList<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int rowNumber = 0;
            while ((line = br.readLine()) != null) {
                rowNumber++;
                String[] row = line.split(";");
                for (int i = 0; i < row.length; i++) {
                    if (row[i].trim().isEmpty()) {
                        row[i] = "brak danych";
                    }
                }
                if (row.length == 15) {
                    String[] newRow = new String[row.length + 1];
                    newRow[0] = Integer.toString(rowNumber);
                    System.arraycopy(row, 0, newRow, 1, row.length);
                    data.add(newRow);
                } else {
                    data.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void importToXML(JTable table) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("laptops");
            document.appendChild(root);

            for (int i = 0; i < table.getRowCount(); i++) {
                Element laptop = document.createElement("laptop");
                laptop.setAttribute("id", Integer.toString(i + 1));
                root.appendChild(laptop);

                // iterate over the columns of the table
                for (int j = 0; j < table.getColumnCount(); j++) {
                    String columnName = table.getColumnName(j);
                    Object cellValue = table.getValueAt(i, j);

                    Element element = document.createElement(columnName);
                    element.setTextContent(cellValue.toString());

                    laptop.appendChild(element);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            StreamResult streamResult = new StreamResult(new File("katalog2.xml"));
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException | TransformerException pce) {
            JOptionPane.showMessageDialog(null, "Error: " + pce.toString());
        }
    }

    public static ArrayList<String[]> readXMLFile(String filePath) {
        try {
            ArrayList<String[]> data = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(filePath);

            NodeList laptopList = doc.getElementsByTagName("laptop");

            //String[][] data = new String[laptopList.getLength()][7];

            for (int i = 0; i < laptopList.getLength(); i++) {
                Element laptop = (Element) laptopList.item(i);


                String id = laptop.getAttribute("id");
                String manufacturer = laptop.getElementsByTagName("manufacturer").item(0).getTextContent();
                String screenSize = "brak danych";
                if (!laptop.getElementsByTagName("size").item(0).getTextContent().equals("")) {
                    screenSize = laptop.getElementsByTagName("size").item(0).getTextContent();
                }
                String touchScreen = "brak danych";
                if (!laptop.getElementsByTagName("screen").item(0).getAttributes().getNamedItem("touch").getNodeValue().equals("")) {
                    touchScreen = laptop.getElementsByTagName("screen").item(0).getAttributes().getNamedItem("touch").getNodeValue();
                }
                String typeScreen = "brak danych";
                if (!laptop.getElementsByTagName("type").item(0).getTextContent().equals("")) {
                    typeScreen = laptop.getElementsByTagName("type").item(0).getTextContent();
                }
                String resolution = laptop.getElementsByTagName("resolution").item(0).getTextContent();
                String processor = laptop.getElementsByTagName("name").item(0).getTextContent();
                String cores = laptop.getElementsByTagName("physical_cores").item(0).getTextContent();
                String clockSpeed = "brak danych";
                if (!laptop.getElementsByTagName("clock_speed").item(0).getTextContent().equals("")) {
                    clockSpeed = laptop.getElementsByTagName("clock_speed").item(0).getTextContent();
                }
                String ram = laptop.getElementsByTagName("ram").item(0).getTextContent();
                String diskType = "brak danych";
                if (laptop.getElementsByTagName("disc").item(0).getAttributes().getNamedItem("type") != null) {
                    diskType = laptop.getElementsByTagName("disc").item(0).getAttributes().getNamedItem("type").getNodeValue();
                }
                String storage = laptop.getElementsByTagName("storage").item(0).getTextContent();
                String graphicsCard = laptop.getElementsByTagName("name").item(1).getTextContent();
                String memory = laptop.getElementsByTagName("memory").item(0).getTextContent();
                String os = laptop.getElementsByTagName("os").item(0).getTextContent();
                String diskReader = laptop.getElementsByTagName("disc_reader").item(0).getTextContent();

                String dataString[] = new String[]{id, manufacturer, screenSize, resolution, typeScreen, touchScreen, processor, cores, clockSpeed, ram, storage, diskType, graphicsCard, memory, os, diskReader};
                data.add(dataString);
            }

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        return pattern.matcher(str).matches();
    }

    public static boolean hasNoDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
