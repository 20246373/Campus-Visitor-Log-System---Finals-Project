import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class VisitorFormPanel extends JPanel {
  private static final String[] ID_TYPES = {
    "Driver's License",
    "Passport",
    "National ID",
    "Student ID",
    "Employee ID",
    "SSS ID",
    "PhilHealth ID",
    "Postal ID",
    "Voter's ID",
    "PRC ID",
    "Senior Citizen ID",
    "PWD ID",
    "Others"
  };

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

  static String[] getCampusAreas() {
    return CAMPUS_AREAS.clone();
  }

  private final JTextField visitorNameField = new JTextField(20);
  private final JTextField contactField = new JTextField(15);
  private final JComboBox<String> idTypeCombo = new JComboBox<>(ID_TYPES);
  private final JTextField idOthersField = new JTextField(20);
  private final JTextArea purposeArea = new JTextArea(3, 20);
  private final JComboBox<String> campusAreaCombo = new JComboBox<>(CAMPUS_AREAS);

  private VisitorFormListener listener;

  VisitorFormPanel() {
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

    // Add centered header
    JLabel header = new JLabel("Add a New Visitor", JLabel.CENTER);
    header.setFont(UITheme.SECTION_FONT);
    header.setForeground(UITheme.PRIMARY);
    header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    add(header, gbc);
    gbc.gridy++;

    // Instruction helper for the form
    JLabel formHelp = new JLabel("Fill out the form and click 'Add Visitor'. The signed-in guard is assigned automatically.");
    formHelp.setFont(UITheme.BODY_FONT.deriveFont(12f));
    formHelp.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    gbc.gridwidth = 2;
    add(formHelp, gbc);
    gbc.gridwidth = 1;
    gbc.gridy++;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    addFieldRow("Visitor Name", visitorNameField, gbc);
    addFieldRow("Contact #", contactField, gbc);
    addIdTypeField(gbc);
    addPurposeField(gbc);
    addFieldRow("Campus Area", campusAreaCombo, gbc);
    addButtonRow(gbc);
  }

  void setFormListener(VisitorFormListener listener) {
    this.listener = listener;
  }

  void resetFields() {
    visitorNameField.setText("");
    contactField.setText("");
    idTypeCombo.setSelectedIndex(0);
    idOthersField.setText("");
    idOthersField.setVisible(false);
    purposeArea.setText("");
    campusAreaCombo.setSelectedIndex(0);
    revalidate();
    repaint();
  }

  private void addIdTypeField(GridBagConstraints gbc) {
    JLabel label = new JLabel("Presented ID");
    label.setFont(UITheme.BODY_FONT);
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    add(label, gbc);
    
    gbc.gridx = 1;
    idTypeCombo.setFont(UITheme.BODY_FONT);
    idTypeCombo.setToolTipText("Select the type of ID presented");
    add(idTypeCombo, gbc);
    gbc.gridy++;
    
    gbc.gridx = 1;
    idOthersField.setFont(UITheme.BODY_FONT);
    idOthersField.setToolTipText("Specify other ID type");
    idOthersField.setVisible(false);
    add(idOthersField, gbc);
    
    idTypeCombo.addActionListener(e -> {
      boolean isOthers = "Others".equals(idTypeCombo.getSelectedItem());
      idOthersField.setVisible(isOthers);
      if (isOthers) {
        gbc.gridy++;
        idOthersField.requestFocusInWindow();
      } else {
        idOthersField.setText("");
      }
      revalidate();
      repaint();
    });
    
    if (!idOthersField.isVisible()) {
      gbc.gridy++;
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
    String selectedIdType = (String) idTypeCombo.getSelectedItem();
    String presentedId;
    if ("Others".equals(selectedIdType)) {
      presentedId = idOthersField.getText().trim().isEmpty() ? "Others" : idOthersField.getText().trim();
    } else {
      presentedId = selectedIdType;
    }
    
    return new VisitorFormData(
      visitorNameField.getText(),
      contactField.getText(),
      presentedId,
      purposeArea.getText(),
      (String) campusAreaCombo.getSelectedItem(),
      null
    );
  }
}
