package org.example;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class Main {
    private static JTable table;
    private static DefaultTableModel tableModel;
    private static String[] columnNames = {"Producent", "Przekatna", "Rozdzielczosc", "Powierzchnia", "Dotyk", "Nazwa Proc", "l. rdzeni", "Taktowanie", "RAM", "Poj. dysku",
            "Rodzaj dysku", "Grafika", "VRAM", "System", "Napęd"};
    private static ArrayList<String[]> data = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Dane z pliku txt");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1200, 500));
        JButton importButton = new JButton("Importuj z pliku");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data = readDataFromFile("katalog.txt");
                tableModel = new DefaultTableModel(data.toArray(new String[0][0]), columnNames);
                table = new JTable(tableModel);
                table.getModel().addTableModelListener(new TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent e) {
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        String columnName = table.getColumnName(column);
                        String value = (String) table.getValueAt(row, column);
                        data.get(row)[columnNamesToIndex(columnName)] = value;
                    }
                });
                JScrollPane scrollPane = new JScrollPane(table);
                frame.add(scrollPane, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
            }
        });

        JButton exportButton = new JButton("Eksportuj do pliku");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Read data from JTable and update ArrayList
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String[] row = new String[columnNames.length];
                    for (int j = 0; j < columnNames.length; j++) {
                        row[j] = (String) tableModel.getValueAt(i, j);
                    }
                    data.set(i, row);
                }
                try (BufferedWriter bw = new BufferedWriter(new FileWriter("katalog.txt"))) {
                    for (String[] row : data) {
                        for (int i = 0; i < row.length; i++) {
                            bw.write(row[i]);
                            if (i != row.length - 1) {
                                bw.write(";");
                            }
                        }
                        bw.newLine();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                JOptionPane.showMessageDialog(frame, "Dane zostały zapisane do pliku.");
            }
        });
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);

        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);
    }

    public static ArrayList<String[]> readDataFromFile(String filename) {
        ArrayList<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(";");
                data.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static int columnNamesToIndex(String columnName) {
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
}


