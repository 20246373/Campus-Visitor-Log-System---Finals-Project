import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
  private static final String[] POST_OPTIONS = {
      "Select Post...",
      "Gate A",
      "Gate B",
      "UB Square",
      "F Main Entrance",
      "Gate D",
      "Legacy",
      "Science High",
      "RCB",
      "Centennial"
  };
  private static final String[] SHIFT_OPTIONS = {"Select Shift...", "Morning", "Afternoon", "Night"};
  private final JComboBox<String> postCombo = new JComboBox<>(POST_OPTIONS);
  private final JComboBox<String> shiftCombo = new JComboBox<>(SHIFT_OPTIONS);
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
      String displayText = value.getName() + " - " + value.getPost() + " (" + value.getShift() + ")";
      JLabel label = new JLabel(displayText);
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
    addField(panel, gbc, "Post", postCombo, 1);
    addField(panel, gbc, "Shift", shiftCombo, 2);
    addField(panel, gbc, "Username", usernameField, 3);
    addField(panel, gbc, "Password", passwordField, 4);

    JButton addButton = new JButton("Add Guard");
    stylePrimaryButton(addButton);
    addButton.addActionListener(e -> addGuard());

    JButton editButton = new JButton("Edit Selected");
    stylePrimaryButton(editButton);
    editButton.addActionListener(e -> editSelectedGuard());

    JButton removeButton = new JButton("Remove Selected");
    styleSecondaryButton(removeButton);
    removeButton.addActionListener(e -> removeSelectedGuard());

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridwidth = 2;
    panel.add(addButton, gbc);
    gbc.gridy = 6;
    panel.add(editButton, gbc);
    gbc.gridy = 7;
    panel.add(removeButton, gbc);

    return panel;
  }

  private void addField(JPanel panel, GridBagConstraints gbc, String labelText, java.awt.Component field, int row) {
    gbc.gridx = 0;
    gbc.gridy = row;
    JLabel label = new JLabel(labelText);
    label.setFont(UITheme.BODY_FONT);
    panel.add(label, gbc);
    gbc.gridx = 1;
    if (field instanceof javax.swing.JTextField textField) {
      textField.setFont(UITheme.BODY_FONT);
      textField.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(UITheme.BORDER),
          BorderFactory.createEmptyBorder(6, 8, 6, 8)));
      panel.add(textField, gbc);
    } else if (field instanceof javax.swing.JComponent component) {
      component.setFont(UITheme.BODY_FONT);
      panel.add(component, gbc);
    } else {
      panel.add(field, gbc);
    }
  }

  private void addGuard() {
    String name = nameField.getText().trim();
    String post = (String) postCombo.getSelectedItem();
    String shift = (String) shiftCombo.getSelectedItem();
    String username = usernameField.getText().trim();
    String password = passwordField.getText().trim();

    boolean postValid = post != null && postCombo.getSelectedIndex() > 0;
    boolean shiftValid = shift != null && shiftCombo.getSelectedIndex() > 0;

    if (name.isEmpty() || username.isEmpty() || password.isEmpty() || !postValid || !shiftValid) {
      JOptionPane.showMessageDialog(this, "Name, post, shift, username, and password are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
      return;
    }

    guardDirectory.addGuard(new Guard(name, post, shift, username, password));
    refreshGuardList();
    clearFields();
    rosterChangedCallback.run();
  }

  private void editSelectedGuard() {
    Guard selected = guardJList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "Select a guard to edit.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    JTextField editNameField = new JTextField(selected.getName(), 15);
    JComboBox<String> editPostCombo = new JComboBox<>(POST_OPTIONS);
    JComboBox<String> editShiftCombo = new JComboBox<>(SHIFT_OPTIONS);
    JTextField editUsernameField = new JTextField(selected.getUsername(), 12);
    JTextField editPasswordField = new JTextField(selected.getPassword(), 12);

    for (int i = 0; i < POST_OPTIONS.length; i++) {
      if (POST_OPTIONS[i].equals(selected.getPost())) {
        editPostCombo.setSelectedIndex(i);
        break;
      }
    }

    for (int i = 0; i < SHIFT_OPTIONS.length; i++) {
      if (SHIFT_OPTIONS[i].equals(selected.getShift())) {
        editShiftCombo.setSelectedIndex(i);
        break;
      }
    }

    JPanel editPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    addField(editPanel, gbc, "Name", editNameField, 0);
    addField(editPanel, gbc, "Post", editPostCombo, 1);
    addField(editPanel, gbc, "Shift", editShiftCombo, 2);
    addField(editPanel, gbc, "Username", editUsernameField, 3);
    addField(editPanel, gbc, "Password", editPasswordField, 4);

    int result = JOptionPane.showConfirmDialog(this, editPanel, "Edit Guard", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return;
    }

    String name = editNameField.getText().trim();
    String post = (String) editPostCombo.getSelectedItem();
    String shift = (String) editShiftCombo.getSelectedItem();
    String username = editUsernameField.getText().trim();
    String password = editPasswordField.getText().trim();

    boolean postValid = post != null && editPostCombo.getSelectedIndex() > 0;
    boolean shiftValid = shift != null && editShiftCombo.getSelectedIndex() > 0;

    if (name.isEmpty() || username.isEmpty() || password.isEmpty() || !postValid || !shiftValid) {
      JOptionPane.showMessageDialog(this, "Name, post, shift, username, and password are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
      return;
    }

    guardDirectory.updateGuard(selected, new Guard(name, post, shift, username, password));
    refreshGuardList();
    rosterChangedCallback.run();
  }

  private void removeSelectedGuard() {
    Guard selected = guardJList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "Select a guard to remove.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    int choice = JOptionPane.showConfirmDialog(
        this,
        String.format("Remove %s from the guard roster?", selected.getName()),
        "Confirm Guard Removal",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) {
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
    postCombo.setSelectedIndex(0);
    shiftCombo.setSelectedIndex(0);
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
