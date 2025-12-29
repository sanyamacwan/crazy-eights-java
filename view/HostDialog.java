package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HostDialog extends JDialog {
    private JTextField portField;
    private JTextField nameField;
    private JButton okButton;
    private JButton cancelButton;
    private boolean succeeded;

    public HostDialog(Frame parent) {
        super(parent, "Host Game", true); // modal dialog
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        cs.fill = GridBagConstraints.HORIZONTAL;

        // Port input
        JLabel portLabel = new JLabel("Port: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(portLabel, cs);

        portField = new JTextField(10);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(portField, cs);

        // Name input
        JLabel nameLabel = new JLabel("Your Name: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(nameLabel, cs);

        nameField = new JTextField(10);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(nameField, cs);

        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Buttons
        okButton = new JButton("Host");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int port = Integer.parseInt(portField.getText().trim());
                    if (port < 10000 || port > 65535) {
                        JOptionPane.showMessageDialog(HostDialog.this,
                                "Port must be between 10000 and 65535", "Error", JOptionPane.ERROR_MESSAGE);
                        portField.setText("");
                        succeeded = false;
                    } else {
                        succeeded = true;
                        dispose();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(HostDialog.this,
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
        bp.add(okButton);
        bp.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // Getters for user input:
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
