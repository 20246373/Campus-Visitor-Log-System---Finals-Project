import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

class VisitorLogApp extends JFrame implements LoginPanel.LoginHandler {
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardPanel = new JPanel(cardLayout);

  private final GuardDirectory guardDirectory = new GuardDirectory();
  private final AdminDirectory adminDirectory = new AdminDirectory();

  private GuardDashboardPanel guardDashboard;
  private AdminDashboardPanel adminDashboard;

  VisitorLogApp() {
    super("University of Baguio Security Portal");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 720);
    setLocationRelativeTo(null);

    add(cardPanel);

    LoginPanel loginPanel = new LoginPanel(this);
    cardPanel.add(loginPanel, "login");

    buildDashboards();
    cardLayout.show(cardPanel, "login");
  }

  private void buildDashboards() {
    guardDashboard = new GuardDashboardPanel(guardDirectory, adminDirectory);
    adminDashboard = new AdminDashboardPanel(guardDirectory, adminDirectory, this::refreshDashboards);

    cardPanel.add(wrapWithNav(guardDashboard, "Guard Dashboard", () -> cardLayout.show(cardPanel, "login")), "guard");
    cardPanel.add(wrapWithNav(adminDashboard, "Admin Dashboard", () -> cardLayout.show(cardPanel, "login")), "admin");
  }

  private JPanel wrapWithNav(JPanel content, String title, Runnable logoutAction) {
    JPanel wrapper = new JPanel(new BorderLayout());
    JPanel nav = new JPanel(new BorderLayout());
    nav.setBackground(UITheme.PRIMARY);
    nav.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(UITheme.SECTION_FONT);
    titleLabel.setForeground(java.awt.Color.WHITE);
    nav.add(titleLabel, BorderLayout.WEST);

    JButton logoutButton = new JButton("Sign Out");
    logoutButton.setBackground(UITheme.PRIMARY_DARK);
    logoutButton.setForeground(java.awt.Color.BLACK);
    logoutButton.addActionListener(e -> logoutAction.run());
    nav.add(logoutButton, BorderLayout.EAST);

    wrapper.add(nav, BorderLayout.NORTH);
    wrapper.add(content, BorderLayout.CENTER);
    return wrapper;
  }

  @Override
  public void handleLogin(UserRole role, String username, String password) {
    if (role == UserRole.ADMIN) {
      Admin admin = adminDirectory.authenticate(username, password);
      if (admin == null) {
        JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Access Denied", JOptionPane.ERROR_MESSAGE);
        return;
      }
      adminDashboard.setActiveAdmin(admin);
      refreshDashboards();
      cardLayout.show(cardPanel, "admin");
      return;
    }

    Guard guard = guardDirectory.authenticate(username, password);
    if (guard == null) {
      JOptionPane.showMessageDialog(this, "Invalid guard credentials.", "Access Denied", JOptionPane.ERROR_MESSAGE);
      return;
    }
    guardDashboard.setActiveGuard(guard);
    guardDashboard.refreshPersonnel();
    cardLayout.show(cardPanel, "guard");
  }

  private void refreshDashboards() {
    guardDashboard.refreshPersonnel();
    adminDashboard.refreshPersonnel();
  }
}
