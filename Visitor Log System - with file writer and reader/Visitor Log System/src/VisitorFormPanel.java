import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class VisitorFormPanel extends JPanel {
  private static final String[] CAMPUS_AREAS = {
    "School of Business Administration & Accountancy",
    "School of Criminal Justice & Public Safety",
    "School of Dentistry",
    "School of Engineering & Architecture",
    "School of Information Technology",
    "School of International Hospitality & Tourism Management",
    "School of Law",
    "School of Natural Sciences",
    "School of Nursing",
    "School of Teacher Education & Liberal Arts",
    "Graduate School",
    "High School (Preparatory / UB High School)",
    "Science High School",
    "Admission & Records Center (ARC)",
    "Athletics Office",
    "Campus Planning & Development Office",
    "Center for Counseling & Student Development",
    "Extension & Community Outreach Services",
    "Finance Department / Student Accounts / Cashier",
    "Human Resource Management Center",
    "Library / Learning Resources",
    "Linkages Office",
    "Management Information Systems (MIS) Office",
    "Medical & Dental Clinic",
    "Office of Student Affairs (OSA)",
    "Office of the Vice President for Academic Affairs",
    "Quality Assurance Office",
    "Research & Development Center",
    "Risk Management Office",
    "Security Office / Campus Security"
  };

  private final JTextField visitorNameField = new JTextField(20);
  private final JTextField contactField = new JTextField(15);
  private final JTextField hostField = new JTextField(20);
  private final JTextArea purposeArea = new JTextArea(3, 20);
  private final JComboBox<String> campusAreaCombo = new JComboBox<>(CAMPUS_AREAS);
  private final JComboBox<Guard> guardCombo = new JComboBox<>();
  private final JComboBox<Admin> adminCombo = new JComboBox<>();

  private VisitorFormListener listener;

  VisitorFormPanel(List<Guard> guards, List<Admin> admins) {
    super(new GridBagLayout());
    setBackground(UITheme.PANEL_BG);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(16, 16, 16, 16)));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(6, 6, 6, 6);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;

    // Instruction header for the form
    JLabel formHelp = new JLabel("Fill out the form and click 'Add Visitor' to register a visitor.");
    formHelp.setFont(UITheme.BODY_FONT.deriveFont(12f));
    formHelp.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    gbc.gridwidth = 2;
    add(formHelp, gbc);
    gbc.gridwidth = 1;
    gbc.gridy++;

    addFieldRow("Visitor Name", visitorNameField, gbc);
    addFieldRow("Contact #", contactField, gbc);
    addFieldRow("Host Office", hostField, gbc);
    addPurposeField(gbc);
    addFieldRow("Campus Area", campusAreaCombo, gbc);
    addFieldRow("Guard on Duty", guardCombo, gbc);
    addFieldRow("Admin Reviewer", adminCombo, gbc);
    addButtonRow(gbc);

    updatePersonnelModels(guards, admins);
  }

  void setFormListener(VisitorFormListener listener) {
    this.listener = listener;
  }

  void resetFields() {
    visitorNameField.setText("");
    contactField.setText("");
    hostField.setText("");
    purposeArea.setText("");
    campusAreaCombo.setSelectedIndex(0);
    if (guardCombo.getItemCount() > 0) {
      guardCombo.setSelectedIndex(0);
    }
    if (adminCombo.getItemCount() > 0) {
      adminCombo.setSelectedIndex(0);
    }
  }

  void refreshPersonnel(List<Guard> guards, List<Admin> admins) {
    updatePersonnelModels(guards, admins);
  }

  private void updatePersonnelModels(List<Guard> guards, List<Admin> admins) {
    guardCombo.setModel(new DefaultComboBoxModel<>(guards.stream().toArray(Guard[]::new)));
    adminCombo.setModel(new DefaultComboBoxModel<>(admins.stream().toArray(Admin[]::new)));
    if (guardCombo.getItemCount() > 0) {
      guardCombo.setSelectedIndex(0);
    }
    if (adminCombo.getItemCount() > 0) {
      adminCombo.setSelectedIndex(0);
    }
  }

  private void addFieldRow(String labelText, java.awt.Component field, GridBagConstraints gbc) {
    JLabel label = new JLabel(labelText);
    label.setFont(UITheme.BODY_FONT);
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    add(label, gbc);
    gbc.gridx = 1;
    if (field instanceof javax.swing.JComponent component) {
      component.setFont(UITheme.BODY_FONT);
      component.setToolTipText(labelText);
    }
    add(field, gbc);
    gbc.gridy++;
  }

  private void addPurposeField(GridBagConstraints gbc) {
    JLabel label = new JLabel("Purpose of Visit");
    label.setFont(UITheme.BODY_FONT);
    purposeArea.setLineWrap(true);
    purposeArea.setWrapStyleWord(true);
    purposeArea.setFont(UITheme.BODY_FONT);
    JScrollPane scrollPane = new JScrollPane(purposeArea);
    scrollPane.setPreferredSize(new Dimension(0, 80));

    gbc.gridx = 0;
    gbc.gridwidth = 2;
    add(label, gbc);
    gbc.gridy++;
    add(scrollPane, gbc);
    gbc.gridy++;
    gbc.gridwidth = 1;
  }

  private void addButtonRow(GridBagConstraints gbc) {
    JButton addButton = new JButton("Add Visitor");
    stylePrimaryButton(addButton);
    addButton.setToolTipText("Create a new visitor entry and add it to the log.");
    addButton.addActionListener(e -> notifySubmit());

    JButton clearButton = new JButton("Reset Form");
    clearButton.setFont(UITheme.BODY_FONT);
    clearButton.addActionListener(e -> resetFields());
    clearButton.setToolTipText("Clear all fields in the form.");

    JPanel row = new JPanel();
    row.setOpaque(false);
    row.add(addButton);
    row.add(clearButton);

    gbc.gridx = 0;
    gbc.gridwidth = 2;
    add(row, gbc);
  }

  private void stylePrimaryButton(JButton button) {
    button.setFont(UITheme.BODY_FONT);
    button.setBackground(UITheme.PRIMARY);
    button.setForeground(java.awt.Color.BLACK);
  }

  private void notifySubmit() {
    if (listener != null) {
      listener.onSubmit(collectFormData());
    }
  }

  private VisitorFormData collectFormData() {
    return new VisitorFormData(
      visitorNameField.getText(),
      contactField.getText(),
      hostField.getText(),
      purposeArea.getText(),
      (String) campusAreaCombo.getSelectedItem(),
        (Guard) guardCombo.getSelectedItem(),
        (Admin) adminCombo.getSelectedItem()
    );
  }
}
