import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicButtonUI;

class AdminManagementPanel extends JPanel {
  private final AdminDirectory adminDirectory;
  private final DefaultListModel<Admin> adminListModel = new DefaultListModel<>();
  private final JList<Admin> adminJList = new JList<>(adminListModel);
  private final Runnable rosterChangedCallback;

  private final JTextField nameField = new JTextField(15);
  private final JTextField officeField = new JTextField(15);
  private final JTextField roleField = new JTextField(12);
  private final JTextField usernameField = new JTextField(12);
  private final JTextField passwordField = new JTextField(12);

  AdminManagementPanel(AdminDirectory adminDirectory, Runnable rosterChangedCallback) {
    this.adminDirectory = adminDirectory;
    this.rosterChangedCallback = rosterChangedCallback;
    setLayout(new BorderLayout(12, 12));
    setBackground(UITheme.PANEL_BG);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(16, 16, 16, 16)));

    adminJList.setVisibleRowCount(8);
    adminJList.setFont(UITheme.BODY_FONT);
    adminJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      JLabel label = new JLabel(value.getDisplayLabel());
      label.setFont(UITheme.BODY_FONT);
      label.setOpaque(true);
      if (isSelected) {
        label.setBackground(UITheme.ACCENT);
        label.setForeground(java.awt.Color.BLACK);
      } else {
        label.setBackground(UITheme.PANEL_BG);
        label.setForeground(java.awt.Color.BLACK);
      }
      return label;
    });

    JScrollPane listScroll = new JScrollPane(adminJList);
    listScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
    add(listScroll, BorderLayout.CENTER);
    add(createEditorPanel(), BorderLayout.SOUTH);

    refreshAdminList();
  }

  private JPanel createEditorPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    addField(panel, gbc, "Name", nameField, 0);
    addField(panel, gbc, "Office", officeField, 1);
    addField(panel, gbc, "Role", roleField, 2);
    addField(panel, gbc, "Username", usernameField, 3);
    addField(panel, gbc, "Password", passwordField, 4);

    JButton addButton = new JButton("Add Admin");
    stylePrimaryButton(addButton);
    addButton.addActionListener(e -> addAdmin());

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    panel.add(addButton, gbc);

    return panel;
  }

  private void addField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField field, int row) {
    gbc.gridx = 0;
    gbc.gridy = row;
    JLabel label = new JLabel(labelText);
    label.setFont(UITheme.BODY_FONT);
    panel.add(label, gbc);
    gbc.gridx = 1;
    field.setFont(UITheme.BODY_FONT);
    field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    panel.add(field, gbc);
  }

  private void addAdmin() {
    String name = nameField.getText().trim();
    String office = officeField.getText().trim();
    String role = roleField.getText().trim();
    String username = usernameField.getText().trim();
    String password = passwordField.getText().trim();

    if (name.isEmpty() || office.isEmpty() || role.isEmpty() || username.isEmpty() || password.isEmpty()) {
      JOptionPane.showMessageDialog(this, "All admin fields are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
      return;
    }

    boolean usernameTaken = adminDirectory.getAdmins().stream()
        .anyMatch(existing -> existing.getUsername().equalsIgnoreCase(username));
    if (usernameTaken) {
      JOptionPane.showMessageDialog(this, "Username already in use.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
      return;
    }

    adminDirectory.addAdmin(new Admin(name, office, role, username, password));
    refreshAdminList();
    clearFields();
    rosterChangedCallback.run();
  }

  private void refreshAdminList() {
    adminListModel.clear();
    adminDirectory.getAdmins().forEach(adminListModel::addElement);
  }

  private void clearFields() {
    nameField.setText("");
    officeField.setText("");
    roleField.setText("");
    usernameField.setText("");
    passwordField.setText("");
  }

  private void stylePrimaryButton(JButton button) {
    button.setFont(UITheme.BODY_FONT);
    button.setBackground(UITheme.PRIMARY);
    button.setForeground(java.awt.Color.WHITE);
    button.setFocusPainted(false);
    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setUI(new BasicButtonUI());
    button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
  }
}
