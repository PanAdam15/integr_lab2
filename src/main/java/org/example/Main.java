package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Main {
    private static JTable table;

    private static DefaultTableModel tableModel;

    private static String[] columnNames = {"", "Producent", "Przekatna", "Rozdzielczosc", "Powierzchnia", "Dotyk", "Nazwa Proc", "l. rdzeni", "Taktowanie", "RAM", "Poj. dysku",
            "Rodzaj dysku", "Grafika", "VRAM", "System", "Napęd"};

    private static ArrayList<String[]> data = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tabela z pliku txt - Adam Pankowski");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1700, 500));
        JButton importButton = new JButton("Importuj");
        JButton importButtonXML = new JButton("Importuj z xml");
        JButton exportButtonXML = new JButton("Eskportuj do xml");

        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data = readDataFromFile("katalog.txt");
                tableModel = new DefaultTableModel(data.toArray(new String[0][0]), columnNames);
                table = new JTable(tableModel);
                table.setFont(new Font("Courier", Font.BOLD, 10));
                TableColumnModel columnModel = table.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth(15);
                columnModel.getColumn(0).setMaxWidth(15);
                columnModel.getColumn(12).setPreferredWidth(120);
                columnModel.getColumn(11).setMaxWidth(120);
                columnModel.getColumn(14).setPreferredWidth(110);
                columnModel.getColumn(13).setMaxWidth(110);

                table.getModel().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        String columnName = table.getColumnName(column);
                        String value = (String) table.getValueAt(row, column);
                        if (columnName.equals("Powierzchnia")) {
                            if (!hasNoDigits(value)) {
                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
                                table.setValueAt(data.get(row)[column], row, column);
                                return;
                            }
                        }
                        if (!value.equals("brak danych") && columnName.equals("l. rdzeni")) {
                            if (!isNumeric(value)) {
                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
                                table.setValueAt(data.get(row)[column], row, column);
                                return;
                            }
                        }
                        if (value.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "Nie można zapisać pustych danych.");
                            table.setValueAt(data.get(row)[column], row, column);
                            return;
                        }
                        data.get(row)[column] = value;
                    }
                });
                JScrollPane scrollPane = new JScrollPane(table);
                frame.add(scrollPane, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
            }
        });

        JButton exportButton = new JButton("Eksportuj");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    String[] row = new String[columnNames.length];
                    for (int j = 0; j < columnNames.length; j++) {
                        row[j] = (String) table.getValueAt(i, j);
                    }
                    data.set(i, row);
                }
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("katalog.txt"))) {
                    for (String[] row : data) {
                        for (int i = 0; i < row.length; i++) {
                            bufferedWriter.write(row[i]);
                            bufferedWriter.write(";");
                        }
                        bufferedWriter.newLine();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                JOptionPane.showMessageDialog(frame, "Dane zostały zapisane do pliku.");
            }
        });
        importButtonXML.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object [][] dataXml = readXMLFile("katalog.xml");
                tableModel = new DefaultTableModel(dataXml, columnNames);
                table = new JTable(tableModel);
                table.setFont(new Font("Courier", Font.BOLD, 10));
                TableColumnModel columnModel = table.getColumnModel();
                columnModel.getColumn(0).setPreferredWidth(15);
                columnModel.getColumn(0).setMaxWidth(15);
                columnModel.getColumn(12).setPreferredWidth(120);
                columnModel.getColumn(11).setMaxWidth(120);
                columnModel.getColumn(14).setPreferredWidth(110);
                columnModel.getColumn(13).setMaxWidth(110);
//                table.getModel().addTableModelListener(new TableModelListener() {
//                    @Override
//                    public void tableChanged(TableModelEvent e) {
//                        int row = e.getFirstRow();
//                        int column = e.getColumn();
//                        String columnName = table.getColumnName(column);
//                        String value = (String) table.getValueAt(row, column);
//                        if (columnName.equals("Powierzchnia")) {
//                            if (!hasNoDigits(value)) {
//                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
//                                table.setValueAt(data.get(row)[column], row, column);
//                                return;
//                            }
//                        }
//                        if (!value.equals("brak danych") && columnName.equals("l. rdzeni")) {
//                            if (!isNumeric(value)) {
//                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
//                                table.setValueAt(data.get(row)[column], row, column);
//                                return;
//                            }
//                        }
//                        if (value.trim().isEmpty()) {
//                            JOptionPane.showMessageDialog(frame, "Nie można zapisać pustych danych.");
//                            table.setValueAt(data.get(row)[column], row, column);
//                            return;
//                        }
//                        data.get(row)[column] = value;
//                    }
//                });
                JScrollPane scrollPane = new JScrollPane(table);
                frame.add(scrollPane, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
            }


        });
        exportButtonXML.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                    Document document = documentBuilder.newDocument();

                    Element laptops = document.createElement("laptops");
                    laptops.setAttribute("moddate", String.valueOf(LocalDateTime.now()));
                    document.appendChild(laptops);

                    for (int i = 0; i < table.getRowCount(); i++) {
                        Element root = document.createElement("laptop");
                        root.setAttribute("id", String.valueOf(table.getValueAt(i, 0)));
                        laptops.appendChild(root);

                        Element manufacturer = document.createElement("manufacturer");
                        manufacturer.appendChild(document.createTextNode("" + table.getValueAt(i, 1)));
                        root.appendChild(manufacturer);

                        Element screen = document.createElement("screen");
                        screen.setAttribute("touch", String.valueOf(table.getValueAt(i, 5)));
                        root.appendChild(screen);
                        Element screenSize = document.createElement("size");
                        screenSize.appendChild(document.createTextNode("" + table.getValueAt(i, 2)));
                        screen.appendChild(screenSize);
                        Element screenType = document.createElement("type");
                        screenType.appendChild(document.createTextNode("" + table.getValueAt(i, 4)));
                        screen.appendChild(screenType);

                        Element processor = document.createElement("processor");
                        root.appendChild(processor);
                        Element procName = document.createElement("name");
                        procName.appendChild(document.createTextNode("" + table.getValueAt(i, 6)));
                        processor.appendChild(procName);
                        Element cores = document.createElement("physical_cores");
                        cores.appendChild(document.createTextNode("" + table.getValueAt(i, 7)));
                        processor.appendChild(cores);
                        Element clock_speed = document.createElement("clock_speed");
                        clock_speed.appendChild(document.createTextNode("" + table.getValueAt(i, 8)));
                        processor.appendChild(clock_speed);

                        Element ram = document.createElement("ram");
                        ram.appendChild(document.createTextNode("" + table.getValueAt(i, 9)));
                        root.appendChild(ram);

                        Element disc = document.createElement("disc");
                        disc.setAttribute("type", String.valueOf(table.getValueAt(i, 11)));
                        root.appendChild(disc);
                        Element storage = document.createElement("storage");
                        storage.appendChild(document.createTextNode("" + table.getValueAt(i, 10)));
                        disc.appendChild(storage);

                        Element gpu = document.createElement("graphic_card");
                        root.appendChild(gpu);
                        Element gpuName = document.createElement("name");
                        gpuName.appendChild(document.createTextNode("" + table.getValueAt(i, 12)));
                        gpu.appendChild(gpuName);
                        Element memory = document.createElement("memory");
                        memory.appendChild(document.createTextNode("" + table.getValueAt(i, 13)));
                        gpu.appendChild(memory);

                        Element os = document.createElement("os");
                        os.appendChild(document.createTextNode("" + table.getValueAt(i, 14)));
                        root.appendChild(os);

                        Element disc_reader = document.createElement("disc_reader");
                        disc_reader.appendChild(document.createTextNode("" + table.getValueAt(i, 15)));
                        root.appendChild(disc_reader);

                    }

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(document);

                    StreamResult streamResult = new StreamResult(new File("katalog2.xml"));
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                    transformer.transform(domSource, streamResult);
                } catch (ParserConfigurationException | TransformerException pce) {
                    JOptionPane.showMessageDialog(null, "Error: " + pce.toString());
                }
                JOptionPane.showMessageDialog(frame, "Dane zostały zapisane do pliku.");
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButtonXML);
        buttonPanel.add(exportButtonXML);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);
    }

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

    private static String[][] readXMLFile(String filePath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(filePath);

            NodeList laptopList = doc.getElementsByTagName("laptop");

            String[][] data = new String[laptopList.getLength()][7];

            for (int i = 0; i < laptopList.getLength(); i++) {
                Element laptop = (Element) laptopList.item(i);


                String id = laptop.getAttribute("id");
                String manufacturer = laptop.getElementsByTagName("manufacturer").item(0).getTextContent();
                String screenSize = laptop.getElementsByTagName("size").item(0).getTextContent();
                String touchScreen = "brak danych";
                if(laptop.getElementsByTagName("screen").item(0).getAttributes().getNamedItem("touch").getNodeValue()!=null) {
                     touchScreen = laptop.getElementsByTagName("screen").item(0).getAttributes().getNamedItem("touch").getNodeValue();
                }
                String typeScreen = "brak danych";
                if(laptop.getElementsByTagName("type").item(0)!=null) {
                    typeScreen = laptop.getElementsByTagName("type").item(0).getTextContent();
                }
                String resolution = laptop.getElementsByTagName("resolution").item(0).getTextContent();
                String processor = laptop.getElementsByTagName("name").item(0).getTextContent();
                String cores = laptop.getElementsByTagName("physical_cores").item(0).getTextContent();
                String clockSpeed = "brak danych";
                if(laptop.getElementsByTagName("clock_speed").item(0).getTextContent()!=null) {
                     clockSpeed = laptop.getElementsByTagName("clock_speed").item(0).getTextContent();
                }
                String ram = laptop.getElementsByTagName("ram").item(0).getTextContent();
                String diskType = "brak danych";
                if(laptop.getElementsByTagName("disc").item(0).getAttributes().getNamedItem("type")!=null) {
                    diskType = laptop.getElementsByTagName("disc").item(0).getAttributes().getNamedItem("type").getNodeValue();
                }
                String storage = laptop.getElementsByTagName("storage").item(0).getTextContent();
                String graphicsCard = laptop.getElementsByTagName("name").item(1).getTextContent();
                String memory = laptop.getElementsByTagName("memory").item(0).getTextContent();
                String os = laptop.getElementsByTagName("os").item(0).getTextContent();
                String diskReader = laptop.getElementsByTagName("disc_reader").item(0).getTextContent();

                data[i] = new String[]{id, manufacturer, screenSize,resolution,typeScreen, touchScreen,processor,cores,clockSpeed, ram, storage,diskType,graphicsCard,memory, os,diskReader};
            }

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
    public void importToXML(JTable table) {
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


