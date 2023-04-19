package org.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.example.FileUtils.*;

public class Main {
    private static JTable table;

    private static DefaultTableModel tableModel;

    private static String[] columnNames = {"", "Producent", "Przekatna", "Rozdzielczosc", "Powierzchnia", "Dotyk", "Nazwa Proc", "l. rdzeni", "Taktowanie", "RAM", "Poj. dysku",
            "Rodzaj dysku", "Grafika", "VRAM", "System", "Napęd"};

    private static ArrayList<String[]> dataTxt = new ArrayList<>();
    private static ArrayList<String[]> dataXml = new ArrayList<>();
    private static ArrayList<String[]> dataDB = new ArrayList<>();
    private static ArrayList<String[]> currentData = new ArrayList<>();

    private static JScrollPane scrollPane, scrollPaneXml, scrollPaneDB;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tabela z pliku txt - Adam Pankowski");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1700, 500));
        JButton importButton = new JButton("Importuj");
        JButton importButtonXML = new JButton("Importuj z xml");
        JButton exportButtonXML = new JButton("Eskportuj do xml");
        JButton importButtonDB = new JButton("Importuj z DB");
        JButton exportButtonDB = new JButton("Eskportuj do DB");

        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dataTxt = readDataFromFile("katalog.txt");
                Set<Integer> rowsToHighlight = new HashSet<>();
                Set<Integer> editedRows = new HashSet<>();
                int j = 0;
                for (String[] arr1 : currentData) {

                    for (String[] arr2 : dataTxt) {
                        if (Arrays.equals(arr1, arr2)) {
                            rowsToHighlight.add(j);
                        }
                    }
                    j++;
                }
                tableModel = new DefaultTableModel(dataTxt.toArray(new String[0][0]), columnNames);
                table = new JTable(tableModel);

                table.getModel().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        String columnName = table.getColumnName(column);
                        String value = (String) table.getValueAt(row, column);
                        if (!Objects.equals(value, dataTxt.get(row)[column])) {
                            editedRows.add(row);
                        }
                        if (columnName.equals("Powierzchnia")) {
                            if (!hasNoDigits(value)) {
                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
                                table.setValueAt(dataTxt.get(row)[column], row, column);
                                return;
                            }
                        }
                        if (!value.equals("brak danych") && columnName.equals("l. rdzeni")) {
                            if (!isNumeric(value)) {
                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
                                table.setValueAt(dataTxt.get(row)[column], row, column);
                                return;
                            }
                        }
                        if (value.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "Nie można zapisać pustych danych.");
                            table.setValueAt(dataTxt.get(row)[column], row, column);
                            return;
                        }
                        dataTxt.get(row)[column] = value;
                    }
                });
                table.setDefaultRenderer(Object.class, new MyTableCellRenderer(rowsToHighlight, editedRows));
                for (int i = 0; i < table.getRowCount(); i++) {
                    if (rowsToHighlight.contains(i)) {
                        table.getCellRenderer(i, 0).getTableCellRendererComponent(table, null, true, false, i, 0);
                    }

                }
                setTableView(table);

                removeScrollPanels(frame);
                scrollPane = new JScrollPane(table);
                frame.add(scrollPane, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
                JOptionPane.showMessageDialog(frame, "Znalazłem " + (dataTxt.size() - rowsToHighlight.size()) + " nowe rekordy, pozostałe " + rowsToHighlight.size() + " to duplikaty");
                currentData = dataTxt;
            }
        });

        JButton exportButton = new JButton("Eksportuj");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    String[] row = new String[table.getColumnCount()];
                    for (int j = 0; j < table.getColumnCount(); j++) {
                        row[j] = (String) table.getValueAt(i, j);
                    }
                    dataTxt.set(i, row);
                }
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("katalog.txt"))) {
                    for (String[] row : dataTxt) {
                        for (String s : row) {
                            bufferedWriter.write(s);
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
                dataXml = readXMLFile("katalog.xml");
                Set<Integer> rowsToHighlight = new HashSet<>();
                Set<Integer> editedRows = new HashSet<>();
                int j = 0;
                for (String[] arr1 : currentData) {

                    for (String[] arr2 : dataXml) {
                        if (Arrays.equals(arr1, arr2)) {
                            rowsToHighlight.add(j);
                        }
                    }
                    j++;
                }
                String[][] dataArray = new String[dataXml.size()][];
                for (int i = 0; i < dataXml.size(); i++) {
                    dataArray[i] = dataXml.get(i);
                }
                tableModel = new DefaultTableModel(dataArray, columnNames);
                table = new JTable(tableModel);

                table.getModel().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        String columnName = table.getColumnName(column);
                        String value = (String) table.getValueAt(row, column);
                        if (!Objects.equals(value, dataXml.get(row)[column])) {
                            editedRows.add(row);
                        }
                        if (columnName.equals("Powierzchnia")) {
                            if (!hasNoDigits(value)) {
                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
                                table.setValueAt(dataXml.get(row)[column], row, column);
                                return;
                            }
                        }
                        if (!value.equals("brak danych") && columnName.equals("l. rdzeni")) {
                            if (!isNumeric(value)) {
                                JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
                                table.setValueAt(dataXml.get(row)[column], row, column);
                                return;
                            }
                        }
                        if (value.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(frame, "Nie można zapisać pustych danych.");
                            if (!dataXml.get(row)[column].isEmpty()) {
                                table.setValueAt(dataXml.get(row)[column], row, column);
                            }
                            return;
                        }
                        dataXml.get(row)[column] = value;

                    }
                });
                table.setDefaultRenderer(Object.class, new MyTableCellRenderer(rowsToHighlight, editedRows));
                for (int i = 0; i < table.getRowCount(); i++) {
                    if (rowsToHighlight.contains(i)) {
                        table.getCellRenderer(i, 0).getTableCellRendererComponent(table, null, true, false, i, 0);
                    }
                }
                setTableView(table);
                removeScrollPanels(frame);
                scrollPaneXml = new JScrollPane(table);
                frame.add(scrollPaneXml, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
                JOptionPane.showMessageDialog(frame, "Znalazłem " + (dataXml.size() - rowsToHighlight.size()) + " nowe rekordy, pozostałe " + rowsToHighlight.size() + " to duplikaty");
                currentData = dataXml;
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
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'T' HH:mm");
                    String formatDateTime = now.format(formatter);
                    laptops.setAttribute("moddate", formatDateTime);
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
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                    transformer.transform(domSource, streamResult);
                } catch (ParserConfigurationException | TransformerException pce) {
                    JOptionPane.showMessageDialog(null, "Error: " + pce.toString());
                }
                JOptionPane.showMessageDialog(frame, "Dane zostały zapisane do pliku.");
            }
        });

        importButtonDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Create the new JTable
                    dataDB = readFromDB(columnNames);
                    Set<Integer> rowsToHighlight = new HashSet<>();
                    Set<Integer> editedRows = new HashSet<>();
                    int j = 0;
                    for (String[] arr1 : currentData) {

                        for (String[] arr2 : dataDB) {
                            if (Arrays.equals(arr1, arr2)) {
                                rowsToHighlight.add(j);
                            }
                        }
                        j++;
                    }

                    tableModel = new DefaultTableModel(dataDB.toArray(new String[0][0]), columnNames);
                    table = new JTable(tableModel);

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
                                    table.setValueAt(dataDB.get(row)[column], row, column);
                                    return;
                                }else if (!Objects.equals(value, dataDB.get(row)[column])) {
                                    editedRows.add(row);
                                }
                            }else if (!value.equals("brak danych") && columnName.equals("l. rdzeni")) {
                                if (!isNumeric(value)) {
                                    JOptionPane.showMessageDialog(frame, "Nieprawidłowa wartość");
                                    table.setValueAt(dataDB.get(row)[column], row, column);
                                    return;
                                }else if (!Objects.equals(value, dataDB.get(row)[column])) {
                                    editedRows.add(row);
                                }
                            }else if (value.trim().isEmpty()) {
                                JOptionPane.showMessageDialog(frame, "Nie można zapisać pustych danych.");
                                if (!dataDB.get(row)[column].isEmpty()) {
                                    table.setValueAt(dataDB.get(row)[column], row, column);
                                }else if (!Objects.equals(value, dataDB.get(row)[column])) {
                                    editedRows.add(row);
                                }
                                return;
                            }else if (!Objects.equals(value, dataDB.get(row)[column])) {
                                editedRows.add(row);
                            }
                            dataDB.get(row)[column] = value;

                        }
                    });
                    table.setDefaultRenderer(Object.class, new MyTableCellRenderer(rowsToHighlight, editedRows));
                    for (int i = 0; i < table.getRowCount(); i++) {
                        if (rowsToHighlight.contains(i)) {
                            table.getCellRenderer(i, 0).getTableCellRendererComponent(table, null, true, false, i, 0);
                        }
                    }
                    setTableView(table);
                    removeScrollPanels(frame);
                    scrollPaneDB = new JScrollPane(table);
                    frame.add(scrollPaneDB, BorderLayout.CENTER);
                    frame.pack();
                    frame.setVisible(true);
                    JOptionPane.showMessageDialog(frame, "Znalazłem " + (dataDB.size() - rowsToHighlight.size()) + " nowe rekordy, pozostałe " + rowsToHighlight.size() + " to duplikaty");
                    currentData = dataDB;
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        exportButtonDB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/integrationdb", "root", "root");
                    Statement statement = conn.createStatement();
                    String sql = "TRUNCATE TABLE laptops_table";
                    statement.executeUpdate(sql);
                    for (int i = 0; i < table.getRowCount(); i++) {
                        String col1 = table.getValueAt(i, 0).toString();
                        String col2 = table.getValueAt(i, 1).toString();
                        String col3 = table.getValueAt(i, 2).toString();
                        String col4 = table.getValueAt(i, 3).toString();
                        String col5 = table.getValueAt(i, 4).toString();
                        String col6 = table.getValueAt(i, 5).toString();
                        String col7 = table.getValueAt(i, 6).toString();
                        String col8 = table.getValueAt(i, 7).toString();
                        String col9 = table.getValueAt(i, 8).toString();
                        String col10 = table.getValueAt(i, 9).toString();
                        String col11 = table.getValueAt(i, 10).toString();
                        String col12 = table.getValueAt(i, 11).toString();
                        String col13 = table.getValueAt(i, 12).toString();
                        String col14 = table.getValueAt(i, 13).toString();
                        String col15 = table.getValueAt(i, 14).toString();
                        String col16 = table.getValueAt(i, 15).toString();

                        String sql2 = "INSERT INTO laptops_table (id, Producent  ,  Przekatna  ,  Rozdzielczosc  ,  Powierzchnia  ,  Dotyk  ,  Nazwa_Proc  ,  l_rdzeni  ,  Taktowanie  ,  RAM  ,  Poj_dysku  ," +
                                "             Rodzaj_dysku  ,  Grafika  ,  VRAM  ,  System_nazwa  ,  Napęd ) VALUES( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement stmt = conn.prepareStatement(sql2);

                        stmt.setString(1, col1);
                        stmt.setString(2, col2);
                        stmt.setString(3, col3);
                        stmt.setString(4, col4);
                        stmt.setString(5, col5);
                        stmt.setString(6, col6);
                        stmt.setString(7, col7);
                        stmt.setString(8, col8);
                        stmt.setString(9, col9);
                        stmt.setString(10, col10);
                        stmt.setString(11, col11);
                        stmt.setString(12, col12);
                        stmt.setString(13, col13);
                        stmt.setString(14, col14);
                        stmt.setString(15, col15);
                        stmt.setString(16, col16);

                        stmt.executeUpdate();

                    }
                    conn.close();

                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButtonXML);
        buttonPanel.add(exportButtonXML);
        buttonPanel.add(importButtonDB);
        buttonPanel.add(exportButtonDB);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);
    }


    public static boolean hasNoDigits(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static void removeScrollPanels(Frame frame) {
        if (scrollPane != null) {
            frame.remove(scrollPane);
        }
        if (scrollPaneXml != null) {
            frame.remove(scrollPaneXml);
        }
        if (scrollPaneDB != null) {
            frame.remove(scrollPaneDB);
        }
    }

    public static void setTableView(JTable table) {
        table.setFont(new Font("Courier", Font.BOLD, 10));
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(15);
        columnModel.getColumn(0).setMaxWidth(15);
        columnModel.getColumn(12).setPreferredWidth(120);
        columnModel.getColumn(11).setMaxWidth(120);
        columnModel.getColumn(14).setPreferredWidth(110);
        columnModel.getColumn(13).setMaxWidth(110);
    }

}


