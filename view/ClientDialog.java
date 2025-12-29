package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientDialog extends JDialog {
    private JTextField addressField;
    private JTextField portField;
    private JTextField nameField;
    private JButton connectButton;
    private JButton cancelButton;
    private boolean succeeded;

    public ClientDialog(Frame parent) {
        super(parent, "Join Game", true); // modal dialog
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;

        // Server Address input
        JLabel addressLabel = new JLabel("Server Address: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(addressLabel, cs);

        addressField = new JTextField(15);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(addressField, cs);

        // Port input
        JLabel portLabel = new JLabel("Port: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(portLabel, cs);

        portField = new JTextField(10);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(portField, cs);

        // Name input
        JLabel nameLabel = new JLabel("Your Name: ");
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        panel.add(nameLabel, cs);

        nameField = new JTextField(10);
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 2;
        panel.add(nameField, cs);

        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Buttons
        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int port = Integer.parseInt(portField.getText().trim());
                    if (port < 10000 || port > 65535) {
                        JOptionPane.showMessageDialog(ClientDialog.this,
                                "Port must be between 10000 and 65535", "Error", JOptionPane.ERROR_MESSAGE);
                        portField.setText("");
                        succeeded = false;
                    } else {
                        succeeded = true;
                        dispose();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ClientDialog.this,
                            "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
                    portField.setText("");
                    succeeded = false;
                }
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        JPanel bp = new JPanel();
        bp.add(connectButton);
        bp.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // Getters for user input:
    public String getAddress() {
        return addressField.getText().trim();
    }

    public String getPort() {
        return portField.getText().trim();
    }

    public String getName() {
        return nameField.getText().trim();
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
