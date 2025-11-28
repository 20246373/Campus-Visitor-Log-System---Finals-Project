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

class GuardManagementPanel extends JPanel {
  private final GuardDirectory guardDirectory;
  private final DefaultListModel<Guard> guardListModel = new DefaultListModel<>();
  private final JList<Guard> guardJList = new JList<>(guardListModel);
  private final Runnable rosterChangedCallback;

  private final JTextField nameField = new JTextField(15);
  private final JTextField postField = new JTextField(12);
  private final JTextField shiftField = new JTextField(10);
  private final JTextField usernameField = new JTextField(12);
  private final JTextField passwordField = new JTextField(12);

  GuardManagementPanel(GuardDirectory guardDirectory, Runnable rosterChangedCallback) {
    this.guardDirectory = guardDirectory;
    this.rosterChangedCallback = rosterChangedCallback;
    setLayout(new BorderLayout(12, 12));
    setBackground(UITheme.PANEL_BG);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(16, 16, 16, 16)));

    guardJList.setVisibleRowCount(8);
    guardJList.setFont(UITheme.BODY_FONT);
    guardJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
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

    JScrollPane listScroll = new JScrollPane(guardJList);
    listScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
    add(listScroll, BorderLayout.CENTER);
    add(createEditorPanel(), BorderLayout.SOUTH);

    refreshGuardList();
  }

  private JPanel createEditorPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    addField(panel, gbc, "Name", nameField, 0);
    addField(panel, gbc, "Post", postField, 1);
    addField(panel, gbc, "Shift", shiftField, 2);
    addField(panel, gbc, "Username", usernameField, 3);
    addField(panel, gbc, "Password", passwordField, 4);

    JButton addButton = new JButton("Add Guard");
    stylePrimaryButton(addButton);
    addButton.addActionListener(e -> addGuard());

    JButton removeButton = new JButton("Remove Selected");
    styleSecondaryButton(removeButton);
    removeButton.addActionListener(e -> removeSelectedGuard());

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    panel.add(addButton, gbc);
    gbc.gridy = 6;
    panel.add(removeButton, gbc);

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

  private void addGuard() {
    String name = nameField.getText().trim();
    String post = postField.getText().trim();
    String shift = shiftField.getText().trim();
    String username = usernameField.getText().trim();
    String password = passwordField.getText().trim();

    if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Name, username, and password are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
      return;
    }

    guardDirectory.addGuard(new Guard(name, post, shift, username, password));
    refreshGuardList();
    clearFields();
    rosterChangedCallback.run();
  }

  private void removeSelectedGuard() {
    Guard selected = guardJList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "Select a guard to remove.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    guardDirectory.removeGuard(selected);
    refreshGuardList();
    rosterChangedCallback.run();
  }

  private void refreshGuardList() {
    guardListModel.clear();
    guardDirectory.getGuards().forEach(guardListModel::addElement);
  }

  private void clearFields() {
    nameField.setText("");
    postField.setText("");
    shiftField.setText("");
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

  private void styleSecondaryButton(JButton button) {
    button.setFont(UITheme.BODY_FONT);
    button.setBackground(UITheme.PRIMARY_DARK);
    button.setForeground(java.awt.Color.WHITE);
    button.setFocusPainted(false);
    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setUI(new BasicButtonUI());
    button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
  }
}
