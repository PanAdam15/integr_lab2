package org.example;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
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
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String[] row = new String[columnNames.length];
                    for (int j = 0; j < columnNames.length; j++) {
                        row[j] = (String) tableModel.getValueAt(i, j);
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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


