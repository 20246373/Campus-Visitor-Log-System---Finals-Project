import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

class GuardDashboardPanel extends JPanel {
  private final GuardDirectory guardDirectory;
  private final AdminDirectory adminDirectory;
  private final VisitorLogPanel logPanel;
  private final JLabel guardInfoLabel = new JLabel();

  GuardDashboardPanel(GuardDirectory guardDirectory, AdminDirectory adminDirectory) {
    this.guardDirectory = guardDirectory;
    this.adminDirectory = adminDirectory;
    setLayout(new BorderLayout(12, 12));
    setBackground(UITheme.LIGHT_BG);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

    // Guard should not see admin controls — use adminMode = false
    logPanel = new VisitorLogPanel(guardDirectory.getGuards(), adminDirectory.getAdmins(), false);

    guardInfoLabel.setFont(UITheme.BODY_FONT);
    guardInfoLabel.setOpaque(true);
    guardInfoLabel.setBackground(UITheme.PANEL_BG);
    guardInfoLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)));
    // Add a clearer title and short help text
    JLabel title = new JLabel("Guard Dashboard");
    title.setFont(UITheme.SECTION_FONT);
    title.setForeground(UITheme.PRIMARY);
    title.setOpaque(false);
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(UITheme.LIGHT_BG);
    header.add(title, BorderLayout.WEST);
    header.add(guardInfoLabel, BorderLayout.CENTER);

    add(header, BorderLayout.NORTH);
    add(logPanel, BorderLayout.CENTER);
  }

  void setActiveGuard(Guard guard) {
    guardInfoLabel.setText(String.format("%s | Post: %s | Shift: %s",
        guard.getName(),
        guard.getPost(),
        guard.getShift()));
  }

  void refreshPersonnel() {
    logPanel.refreshPersonnel(guardDirectory.getGuards(), adminDirectory.getAdmins());
  }
}
