import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;
import java.time.LocalDate;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Date;
import java.time.ZoneId;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import java.io.File;

class AdminDashboardPanel extends JPanel {
  private final GuardDirectory guardDirectory;
  private final AdminDirectory adminDirectory;
  private final VisitorLogPanel logPanel;
  private final JLabel adminInfoLabel = new JLabel();
  // page info label used for pagination status
  private final JLabel pageInfoLabel = new JLabel();

  AdminDashboardPanel(GuardDirectory guardDirectory,
                      AdminDirectory adminDirectory,
                      VisitorTableModel visitorTableModel,
                      Runnable onPersonnelChanged) {
    this.guardDirectory = guardDirectory;
    this.adminDirectory = adminDirectory;
    setLayout(new BorderLayout(12, 12));
    setBackground(UITheme.LIGHT_BG);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

    logPanel = new VisitorLogPanel(visitorTableModel, guardDirectory.getGuards(), adminDirectory.getAdmins(), true);
    GuardManagementPanel guardManagementPanel = new GuardManagementPanel(guardDirectory, () -> {
      logPanel.refreshPersonnel(guardDirectory.getGuards(), adminDirectory.getAdmins());
      onPersonnelChanged.run();
    });
    AdminManagementPanel adminManagementPanel = new AdminManagementPanel(adminDirectory, () -> {
      logPanel.refreshPersonnel(guardDirectory.getGuards(), adminDirectory.getAdmins());
      onPersonnelChanged.run();
    });

    adminInfoLabel.setFont(UITheme.BODY_FONT);
    adminInfoLabel.setOpaque(true);
    adminInfoLabel.setBackground(UITheme.PANEL_BG);
    adminInfoLabel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(UITheme.BORDER),
      BorderFactory.createEmptyBorder(12, 12, 12, 12)));
    adminInfoLabel.setText("Awaiting admin sign-in...");

    JTabbedPane managementTabs = new JTabbedPane();
    managementTabs.addTab("Guard Roster", guardManagementPanel);
    managementTabs.addTab("Admin Roster", adminManagementPanel);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPanel, managementTabs);
    splitPane.setResizeWeight(0.65);
    splitPane.setOpaque(false);
    splitPane.setBorder(BorderFactory.createEmptyBorder());

    // Top row: admin info on the left, search box on the right
    JPanel topRow = new JPanel(new BorderLayout(8, 0));
    topRow.setBackground(UITheme.LIGHT_BG);
    // Left side: title and info stacked vertically
    JLabel title = new JLabel("Admin Dashboard");
    title.setFont(UITheme.SECTION_FONT);
    title.setForeground(UITheme.PRIMARY);
    JPanel leftStack = new JPanel(new BorderLayout(0,6));
    leftStack.setBackground(UITheme.LIGHT_BG);
    leftStack.add(title, BorderLayout.NORTH);
    leftStack.add(adminInfoLabel, BorderLayout.CENTER);
    topRow.add(leftStack, BorderLayout.CENTER);

    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
    searchPanel.setBackground(UITheme.LIGHT_BG);
    JLabel searchLabel = new JLabel("Search logs:");
    searchLabel.setFont(UITheme.BODY_FONT);
    JTextField searchField = new JTextField(20);
    searchField.setToolTipText("Type to search logs (case-insensitive). Use the column chooser to search a single column.");
    // column chooser -- first element is All Columns
    String[] cols = logPanel.getColumnNames();
    String[] columnChoices = new String[cols.length + 1];
    columnChoices[0] = "All Columns";
    System.arraycopy(cols, 0, columnChoices, 1, cols.length);
    JComboBox<String> columnChooser = new JComboBox<>(columnChoices);
    columnChooser.setToolTipText("Choose a column to restrict searches or select 'All Columns'.");
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) { logPanel.setSearchQuery(searchField.getText()); updatePageLabel(); }

      @Override
      public void removeUpdate(DocumentEvent e) { logPanel.setSearchQuery(searchField.getText()); updatePageLabel(); }

      @Override
      public void changedUpdate(DocumentEvent e) { logPanel.setSearchQuery(searchField.getText()); updatePageLabel(); }
    });

    columnChooser.addActionListener(e -> {
      int idx = columnChooser.getSelectedIndex();
      logPanel.setSearchColumn(idx <= 0 ? -1 : idx - 1);
      updatePageLabel();
    });
    // Date range pickers (JSpinner date widgets) + enable checkbox
    JLabel fromLabel = new JLabel("From:");
    SpinnerDateModel fromModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    JSpinner fromSpinner = new JSpinner(fromModel);
    fromSpinner.setEditor(new JSpinner.DateEditor(fromSpinner, "yyyy-MM-dd"));

    JLabel toLabel = new JLabel("To:");
    SpinnerDateModel toModel = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    JSpinner toSpinner = new JSpinner(toModel);
    toSpinner.setEditor(new JSpinner.DateEditor(toSpinner, "yyyy-MM-dd"));

    JCheckBox enableDateFilter = new JCheckBox("Enable Date Filter");

    String[] dateFieldChoices = new String[] {"Check-In", "Check-Out"};
    JComboBox<String> dateFieldChooser = new JComboBox<>(dateFieldChoices);

    ChangeListener spinnerListener = new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (!enableDateFilter.isSelected()) return;
        Date fd = (Date) fromSpinner.getValue();
        Date td = (Date) toSpinner.getValue();
        LocalDate f = fd == null ? null : fd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate t = td == null ? null : td.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        boolean useCheckOut = dateFieldChooser.getSelectedIndex() == 1;
        logPanel.setDateRange(f, t, useCheckOut);
        updatePageLabel();
      }
    };

    fromSpinner.addChangeListener(spinnerListener);
    toSpinner.addChangeListener(spinnerListener);

    enableDateFilter.addActionListener(e -> {
      if (enableDateFilter.isSelected()) {
        spinnerListener.stateChanged(new ChangeEvent(enableDateFilter));
      } else {
        logPanel.setDateRange(null, null, dateFieldChooser.getSelectedIndex() == 1);
        updatePageLabel();
      }
    });

    dateFieldChooser.addActionListener(e -> {
      if (!enableDateFilter.isSelected()) return;
      spinnerListener.stateChanged(new ChangeEvent(dateFieldChooser));
    });

    // pagination controls
    JLabel pageSizeLabel = new JLabel("Show:");
    Integer[] pageSizes = new Integer[] {5, 10, 20, 50, 0}; // 0 == all
    JComboBox<Integer> pageSizeChooser = new JComboBox<>(pageSizes);
    pageSizeChooser.setSelectedItem(10);
    JButton prevPage = new JButton("◀");
    JButton nextPage = new JButton("▶");
    pageInfoLabel.setText("Page 1/1 (0)");

    pageSizeChooser.addActionListener(e -> {
      Integer sz = (Integer) pageSizeChooser.getSelectedItem();
      logPanel.setPageSize(sz == null ? 0 : sz);
      updatePageLabel();
    });
    prevPage.addActionListener(e -> { logPanel.prevPage(); updatePageLabel(); });
    nextPage.addActionListener(e -> { logPanel.nextPage(); updatePageLabel(); });

    // export and load buttons
    JButton loadFromFile = new JButton("Load from File");
    loadFromFile.setToolTipText("Load visitor logs from a previous day's exported file.");
    JButton exportTxt = new JButton("Export TXT");
    exportTxt.setToolTipText("Export the entire visitor log to a human-readable text file in working directory.");
    
    loadFromFile.addActionListener(e -> {
      File dataDir = new File("src/data");
      if (!dataDir.exists() || !dataDir.isDirectory()) {
        JOptionPane.showMessageDialog(this, "Data directory not found!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      
      File[] files = dataDir.listFiles((dir, name) -> name.startsWith("visitor_logs_") && name.endsWith(".txt"));
      if (files == null || files.length == 0) {
        JOptionPane.showMessageDialog(this, "No visitor log files found in src/data/", "Info", JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      
      java.util.Arrays.sort(files, (a, b) -> b.getName().compareTo(a.getName()));
      
      String[] fileNames = new String[files.length];
      for (int i = 0; i < files.length; i++) {
        fileNames[i] = files[i].getName();
      }
      
      String selectedFile = (String) JOptionPane.showInputDialog(
        this,
        "Select a file to load:",
        "Load Visitor Logs",
        JOptionPane.QUESTION_MESSAGE,
        null,
        fileNames,
        fileNames[0]
      );
      
      if (selectedFile != null) {
        logPanel.loadFromFile(selectedFile);
        JOptionPane.showMessageDialog(this, "Loaded " + selectedFile, "Success", JOptionPane.INFORMATION_MESSAGE);
        updatePageLabel();
      }
    });
    
    exportTxt.addActionListener(e -> {
      String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      String filename = "VisitorsLogs_" + date + ".txt";
      logPanel.exportReadableTxt(filename);
      JOptionPane.showMessageDialog(this, "Exported " + filename + " to src/data/", "Export", JOptionPane.INFORMATION_MESSAGE);
    });

    // add controls to the right side in a compact layout
    searchPanel.add(searchLabel);
    searchPanel.add(searchField);
    searchPanel.add(columnChooser);
    searchPanel.add(enableDateFilter);
    searchPanel.add(fromLabel);
    searchPanel.add(fromSpinner);
    searchPanel.add(toLabel);
    searchPanel.add(toSpinner);
    searchPanel.add(dateFieldChooser);
    searchPanel.add(pageSizeLabel);
    searchPanel.add(pageSizeChooser);
    searchPanel.add(prevPage);
    searchPanel.add(pageInfoLabel);
    searchPanel.add(nextPage);
    searchPanel.add(loadFromFile);
    searchPanel.add(exportTxt);

    topRow.add(searchPanel, BorderLayout.EAST);
    add(topRow, BorderLayout.NORTH);
    // set initial label and update function
    updatePageLabel();
    // ensure page info is shown
    // pageInfoLabel is updated by updatePageLabel() invoked from listeners
    add(splitPane, BorderLayout.CENTER);
  }

  void setActiveAdmin(Admin admin) {
    adminInfoLabel.setText(String.format("%s | Office: %s | Role: %s",
        admin.getName(),
        admin.getOffice(),
        admin.getRole()));
  }

  void refreshPersonnel() {
    logPanel.refreshPersonnel(guardDirectory.getGuards(), adminDirectory.getAdmins());
  }

  // parseDate removed - date pickers use JSpinner and produce LocalDate values directly

  private void updatePageLabel() {
    int cur = logPanel.getCurrentPage();
    int tot = logPanel.getTotalPages();
    int items = logPanel.getTotalMatches();
    pageInfoLabel.setText(String.format("Page %d/%d (%d)", cur, tot, items));
  }
}
