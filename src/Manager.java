package javaapp;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class Manager extends JFrame implements Observer {

    private final JTextField urlText;
    private final DTableModel tableModel;
    private final JTable table;
    private final JButton cancelButton;
    private final JButton addButton;
    private final JPanel panel, downloadsPanel, buttonsPanel;
    private Download selectedDownload;
    public static final String STATUSES[] = {"Downloading", "Complete", "Cancelled", "Error", "Starting"};

    public Manager() {
        setTitle("Download Manager");
        setSize(640, 320);
        setDefaultCloseOperation(3);
        urlText = new JTextField(49);
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(110, 19));
        addButton = new JButton("Add");
        addButton.setPreferredSize(new Dimension(60, 19));
        panel = new JPanel();
        panel.add(urlText);
        addButton.addActionListener((ActionEvent e) -> {
            actionAdd();
        });
        panel.add(addButton);
        tableModel = new DTableModel();
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            tableSelectionChanged();
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        downloadsPanel = new JPanel();
        downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
        downloadsPanel.setLayout(new BorderLayout());
        downloadsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        buttonsPanel = new JPanel();
        cancelButton.addActionListener((ActionEvent e) -> {
            actionCancel();
        });
        cancelButton.setEnabled(false);
        buttonsPanel.add(cancelButton);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(downloadsPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

    }

    private void actionAdd() {
        URL verifiedUrl = verifyUrl(urlText.getText());
        if (verifiedUrl != null) {
            tableModel.addDownload(new Download(verifiedUrl));
            urlText.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private URL verifyUrl(String url) {
        // HTTP URLs only allowed.
        if (!url.toLowerCase().startsWith("http://")) {
            return null;
        }
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
        if (verifiedUrl.getFile().length() < 2) {
            return null;
        }
        return verifiedUrl;
    }

    private void tableSelectionChanged() {
        if (selectedDownload != null) {
            selectedDownload.deleteObserver(Manager.this);
        }
        selectedDownload = tableModel.getDownload(table.getSelectedRow());
        selectedDownload.addObserver(Manager.this);
        updateButtons();
    }

    private void actionCancel() {
        selectedDownload.cancel();
        updateButtons();
    }

    private void updateButtons() {
        if (selectedDownload != null) {
            int status = selectedDownload.getStatus();
            switch (status) {
                case Download.STARTING:
                    cancelButton.setEnabled(false);
                    break;
                case Download.DOWNLOADING:
                    cancelButton.setEnabled(true);
                    break;
                case Download.CANCELLED:
                    cancelButton.setEnabled(false);
                    break;
                case Download.ERROR:
                    cancelButton.setEnabled(false);
                    break;
                default:
                    cancelButton.setEnabled(false);
            }
        } else {
            cancelButton.setEnabled(false);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (selectedDownload != null && selectedDownload.equals(o)) {
            updateButtons();
        }
    }
}
