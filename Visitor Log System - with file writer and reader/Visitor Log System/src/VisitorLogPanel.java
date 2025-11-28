import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

class VisitorLogPanel extends JPanel {
  private final VisitorTableModel tableModel;
  private final JTable logTable;
  private TableRowSorter<VisitorTableModel> sorter;
  // advanced filter state
  private String currentQuery = "";
  private int currentSearchColumn = -1; // -1 = all columns
  private java.time.LocalDate dateFrom = null;
  private java.time.LocalDate dateTo = null;
  private boolean dateUseCheckOut = false; // whether date filter applies to check-out instead of check-in

  // pagination state
  private int pageSize = 10;
  private int currentPage = 1;
  private java.util.List<Integer> currentMatches = new java.util.ArrayList<>();
  private final VisitorFormPanel formPanel;
  private final boolean adminMode;

  private JButton deleteButton;
  private JButton approveButton;
  private JButton denyButton;
  private JButton noteButton;

  VisitorLogPanel(List<Guard> guards, List<Admin> admins, boolean adminMode) {
    this.adminMode = adminMode;
    this.formPanel = new VisitorFormPanel(guards, admins);
    this.tableModel = new VisitorTableModel(guards, admins);
    this.logTable = createLogTable();
    setLayout(new BorderLayout(12, 12));
    setBackground(UITheme.LIGHT_BG);
    setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    

    add(createHeaderPanel(), BorderLayout.NORTH);
    // only show the entry form to guards (not admin viewers)
    if (!adminMode) {
      add(formPanel, BorderLayout.WEST);
      formPanel.setFormListener(this::handleFormSubmission);
    }
    add(createTablePanel(), BorderLayout.CENTER);
  }

  void refreshPersonnel(List<Guard> guards, List<Admin> admins) {
    formPanel.refreshPersonnel(guards, admins);
  }

  private JTable createLogTable() {
    JTable table = new JTable(tableModel);
    table.setFillsViewportHeight(true);
    table.setAutoCreateRowSorter(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setRowHeight(28);
    table.setFont(UITheme.BODY_FONT);
    table.setForeground(java.awt.Color.BLACK);
    table.setBackground(UITheme.PANEL_BG);
    JTableHeader header = table.getTableHeader();
    sorter = new TableRowSorter<>(tableModel);
    table.setRowSorter(sorter);
    header.setFont(UITheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
    header.setReorderingAllowed(false);
    header.setDefaultRenderer(new DefaultTableCellRenderer() {
      {
        setHorizontalAlignment(JLabel.CENTER);
      }

      @Override
      public Component getTableCellRendererComponent(
          JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
        setBackground(UITheme.PRIMARY);
        setForeground(java.awt.Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, UITheme.PRIMARY_DARK));
        return this;
      }
    });
    table.setSelectionBackground(UITheme.ACCENT);
    table.setSelectionForeground(java.awt.Color.BLACK);
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public java.awt.Component getTableCellRendererComponent(
          JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        java.awt.Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
        comp.setForeground(java.awt.Color.BLACK);
        comp.setBackground(isSelected ? UITheme.ACCENT : UITheme.PANEL_BG);
        return comp;
      }
    });
    return table;
  }

  /**
   * Apply a case-insensitive substring search across the table.
   * Passing `null` or empty string clears the filter.
   */
  void applySearchFilter(String query) {
    // legacy single-argument search operates across all columns
    this.currentQuery = query == null ? "" : query.trim();
    this.currentSearchColumn = -1;
    this.currentPage = 1;
    rebuildFilter();
  }

  void setSearchQuery(String query) {
    this.currentQuery = query == null ? "" : query.trim();
    this.currentPage = 1;
    rebuildFilter();
  }

  void setSearchColumn(int columnIndex) {
    this.currentSearchColumn = columnIndex;
    this.currentPage = 1;
    rebuildFilter();
  }

  void setDateRange(java.time.LocalDate from, java.time.LocalDate to, boolean useCheckOut) {
    this.dateFrom = from;
    this.dateTo = to;
    this.dateUseCheckOut = useCheckOut;
    this.currentPage = 1;
    rebuildFilter();
  }

  void setPageSize(int size) {
    this.pageSize = Math.max(0, size);
    this.currentPage = 1;
    rebuildFilter();
  }

  void nextPage() {
    int totalPages = getTotalPages();
    if (currentPage < totalPages) {
      currentPage++;
      rebuildFilter();
    }
  }

  void prevPage() {
    if (currentPage > 1) {
      currentPage--;
      rebuildFilter();
    }
  }

  int getCurrentPage() { return currentPage; }

  int getTotalPages() { return pageSize <= 0 ? 1 : (int) Math.max(1, Math.ceil((double) currentMatches.size() / pageSize)); }

  int getTotalMatches() { return currentMatches.size(); }

  void exportJson(String filename) { tableModel.exportToJsonFile(filename); }

  void exportReadableTxt(String filename) { tableModel.exportToReadableTxtFile(filename); }

  private void rebuildFilter() {
    currentMatches.clear();
    int n = tableModel.getRowCount();
    String q = currentQuery == null ? "" : currentQuery.toLowerCase(java.util.Locale.ROOT);
    for (int i = 0; i < n; i++) {
      Visitor v = tableModel.getVisitor(i);
      if (v == null) continue;
      if (!passesQuery(v, q, currentSearchColumn, i)) continue;
      if (!passesDateRange(v)) continue;
      currentMatches.add(i);
    }

    if (currentMatches.isEmpty()) {
      // a filter that matches nothing
      sorter.setRowFilter(new RowFilter<VisitorTableModel, Integer>() {
        @Override
        public boolean include(Entry<? extends VisitorTableModel, ? extends Integer> entry) {
          return false;
        }
      });
      return;
    }

    java.util.Set<Integer> allowed = new java.util.HashSet<>();
    if (pageSize <= 0) {
      allowed.addAll(currentMatches);
    } else {
      int total = currentMatches.size();
      int totalPages = (int) Math.max(1, Math.ceil((double) total / pageSize));
      if (currentPage > totalPages) currentPage = totalPages;
      int start = (currentPage - 1) * pageSize;
      for (int k = start; k < Math.min(total, start + pageSize); k++) {
        allowed.add(currentMatches.get(k));
      }
    }

    final java.util.Set<Integer> allowedModelIndices = allowed;
    sorter.setRowFilter(new RowFilter<VisitorTableModel, Integer>() {
      @Override
      public boolean include(Entry<? extends VisitorTableModel, ? extends Integer> entry) {
        Integer modelIndex = entry.getIdentifier();
        return allowedModelIndices.contains(modelIndex);
      }
    });
  }

  private boolean passesDateRange(Visitor v) {
    if (dateFrom == null && dateTo == null) return true;
    java.time.LocalDateTime dt = dateUseCheckOut ? v.getCheckOut() : v.getCheckIn();
    if (dt == null) return false;
    java.time.LocalDate d = dt.toLocalDate();
    if (dateFrom != null && d.isBefore(dateFrom)) return false;
    if (dateTo != null && d.isAfter(dateTo)) return false;
    return true;
  }

  private boolean passesQuery(Visitor v, String qLower, int columnIndex, int modelIndex) {
    if (qLower == null || qLower.isEmpty()) return true;
    if (columnIndex >= 0) {
      Object val = tableModel.getValueAt(modelIndex, columnIndex);
      return val != null && val.toString().toLowerCase(java.util.Locale.ROOT).contains(qLower);
    }
    if (v.getVisitorName().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getContactNumber().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getHostOffice().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getPurpose().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getCampusArea().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getGuardLabel().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getAdminLabel().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getAdminStatus().getLabel().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    if (v.getAdminNotes().toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    String checkIn = v.getCheckIn() == null ? "" : v.getCheckIn().toString();
    if (checkIn.toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    String checkOut = v.getCheckOut() == null ? "" : v.getCheckOut().toString();
    if (checkOut.toLowerCase(java.util.Locale.ROOT).contains(qLower)) return true;
    return false;
  }

  String[] getColumnNames() {
    int c = tableModel.getColumnCount();
    String[] names = new String[c];
    for (int i = 0; i < c; i++) names[i] = tableModel.getColumnName(i);
    return names;
  }

  private JPanel createHeaderPanel() {
    JLabel title = new JLabel("Visitor Log", JLabel.CENTER);
    title.setFont(UITheme.SECTION_FONT);
    title.setForeground(UITheme.PRIMARY);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(UITheme.LIGHT_BG);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
    
    // subtitle changes depending on the current mode (admin vs guard)
    JLabel subtitle = new JLabel(adminMode
      ? "Admin view — search and manage visitor logs. Use the controls to the right to take action."
      : "Guard view — enter new visitors using the form on the left and mark check-outs here.", JLabel.CENTER);
    subtitle.setFont(UITheme.BODY_FONT.deriveFont(12f));
    subtitle.setForeground(java.awt.Color.DARK_GRAY);

    JPanel headerStack = new JPanel(new BorderLayout());
    headerStack.setOpaque(false);
    headerStack.add(title, BorderLayout.NORTH);
    headerStack.add(subtitle, BorderLayout.SOUTH);
    panel.add(headerStack, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createTablePanel() {
    JScrollPane tableScroll = new JScrollPane(logTable);
    tableScroll.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)));
    tableScroll.getViewport().setBackground(UITheme.PANEL_BG);

    JButton checkoutButton = new JButton("Mark Check-Out");
    checkoutButton.setToolTipText("Record the current time as the visitor's check-out time.");
    checkoutButton.addActionListener(e -> markCheckout());

    deleteButton = new JButton("Remove Entry");
    deleteButton.setToolTipText("Permanently remove the selected visitor entry (admin only).");
    deleteButton.addActionListener(e -> removeEntry());
    deleteButton.setVisible(adminMode);

    approveButton = new JButton("Approve Visit");
    approveButton.setToolTipText("Mark the visitor as approved and optionally add a note.");
    approveButton.addActionListener(e -> changeAdminStatus(AdminStatus.APPROVED, "Approval Note"));
    approveButton.setVisible(adminMode);

    denyButton = new JButton("Deny Visit");
    denyButton.setToolTipText("Deny the visit and add a refusal reason (admin only).");
    denyButton.addActionListener(e -> changeAdminStatus(AdminStatus.DENIED, "Denial Reason"));
    denyButton.setVisible(adminMode);

    noteButton = new JButton("Add Admin Note");
    noteButton.setToolTipText("Attach an administrative note to the visitor's log.");
    noteButton.addActionListener(e -> addAdminNote());
    noteButton.setVisible(adminMode);

    JPanel actionPanel = new JPanel();
    actionPanel.setToolTipText("Use these actions to manage the selected visitor entry.");
    actionPanel.setBackground(UITheme.PANEL_BG);
    actionPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)));
    actionPanel.add(checkoutButton);
    if (adminMode) {
      actionPanel.add(deleteButton);
      actionPanel.add(approveButton);
      actionPanel.add(denyButton);
      actionPanel.add(noteButton);
    }

    JPanel container = new JPanel(new BorderLayout(0, 8));
    container.setOpaque(false);
    // add a short descriptive label above the table to improve clarity
    JLabel tableHelp = new JLabel(adminMode ? "Admin view — search and manage visitor logs below." : "Guard view — create new entries using the form on the left and manage check-outs here.");
    tableHelp.setFont(UITheme.BODY_FONT.deriveFont(12f));
    tableHelp.setForeground(java.awt.Color.DARK_GRAY);
    tableHelp.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
    container.add(tableHelp, BorderLayout.NORTH);
    container.add(tableScroll, BorderLayout.CENTER);
    container.add(actionPanel, BorderLayout.SOUTH);
    return container;
  }

  private void handleFormSubmission(VisitorFormData data) {
    String validationMessage = validate(data);
    if (validationMessage != null) {
      JOptionPane.showMessageDialog(this, validationMessage, "Missing Information", JOptionPane.WARNING_MESSAGE);
      return;
    }

    tableModel.addVisitor(Visitor.fromFormData(data));
    formPanel.resetFields();
  }

  private String validate(VisitorFormData data) {
    if (data.getVisitorName().isEmpty()) {
      return "Visitor name is required.";
    }
    if (data.getHostOffice().isEmpty()) {
      return "Host office is required.";
    }
    if (data.getPurpose().isEmpty()) {
      return "Purpose of visit is required.";
    }
    if (data.getGuard() == null) {
      return "Please assign a guard on duty.";
    }
    if (data.getAdmin() == null) {
      return "Please assign an admin reviewer.";
    }
    return null;
  }

  private void markCheckout() {
    int modelRow = getSelectedModelRow();
    if (modelRow == -1) {
      return;
    }

    boolean updated = tableModel.markCheckout(modelRow, LocalDateTime.now());
    if (!updated) {
      JOptionPane.showMessageDialog(this, "Visitor already checked out.", "Action Blocked", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void removeEntry() {
    if (!adminMode) {
      return;
    }
    int modelRow = getSelectedModelRow();
    if (modelRow == -1) {
      return;
    }

    int choice = JOptionPane.showConfirmDialog(this, "Remove selected visitor?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
    if (choice == JOptionPane.YES_OPTION) {
      tableModel.removeVisitor(modelRow);
    }
  }

  private void changeAdminStatus(AdminStatus status, String dialogTitle) {
    if (!adminMode) {
      return;
    }
    int modelRow = getSelectedModelRow();
    if (modelRow == -1) {
      return;
    }

    String note = JOptionPane.showInputDialog(this, dialogTitle + " (optional)", "Admin Action", JOptionPane.PLAIN_MESSAGE);
    tableModel.updateAdminStatus(modelRow, status, note == null ? null : note);
  }

  private void addAdminNote() {
    if (!adminMode) {
      return;
    }
    int modelRow = getSelectedModelRow();
    if (modelRow == -1) {
      return;
    }

    String note = JOptionPane.showInputDialog(this, "Enter note for admin log", "Admin Note", JOptionPane.PLAIN_MESSAGE);
    if (note != null && !note.trim().isEmpty()) {
      tableModel.appendAdminNote(modelRow, note.trim());
    }
  }

  private int getSelectedModelRow() {
    int selectedRow = logTable.getSelectedRow();
    if (selectedRow == -1) {
      JOptionPane.showMessageDialog(this, "Select a visitor entry first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
      return -1;
    }
    return logTable.convertRowIndexToModel(selectedRow);
  }
}
