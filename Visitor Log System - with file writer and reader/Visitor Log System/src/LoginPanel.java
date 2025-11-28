import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

class LoginPanel extends JPanel {
  interface LoginHandler {
    void handleLogin(UserRole role, String username, String password);
  }

  private final LoginHandler handler;
  private final JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
  private final JTextField usernameField = new JTextField(18);
  private final JPasswordField passwordField = new JPasswordField(18);

  LoginPanel(LoginHandler handler) {
    this.handler = handler;
    setLayout(new BorderLayout(24, 0));
    setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
    setBackground(UITheme.LIGHT_BG);

    add(buildHeroPanel(), BorderLayout.WEST);
    add(buildLoginCard(), BorderLayout.CENTER);
  }

  private JPanel buildHeroPanel() {
    return new GradientPanel();
  }

  private JPanel buildLoginCard() {
    JPanel card = new JPanel(new GridBagLayout());
    card.setBackground(UITheme.PANEL_BG);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(UITheme.BORDER),
        BorderFactory.createEmptyBorder(32, 32, 32, 32)));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 8, 10, 8);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;

    JLabel title = new JLabel("Secure Portal Login");
    title.setFont(UITheme.SECTION_FONT);
    title.setForeground(UITheme.PRIMARY);
    card.add(title, gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    card.add(new JLabel("Role"), gbc);
    gbc.gridx = 1;
    roleCombo.setFont(UITheme.BODY_FONT);
    card.add(roleCombo, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    card.add(new JLabel("Username"), gbc);
    gbc.gridx = 1;
    card.add(usernameField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    card.add(new JLabel("Password"), gbc);
    gbc.gridx = 1;
    card.add(passwordField, gbc);

    JButton loginButton = new JButton("Sign In");
    loginButton.setBackground(UITheme.PRIMARY);
    loginButton.setForeground(Color.BLACK);
    loginButton.setFont(UITheme.BODY_FONT);
    loginButton.addActionListener(e -> handler.handleLogin(
        (UserRole) roleCombo.getSelectedItem(),
        usernameField.getText().trim(),
        new String(passwordField.getPassword())));

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(20, 8, 0, 8);
    card.add(loginButton, gbc);

    return card;
  }

  private static class GradientPanel extends JPanel {
    GradientPanel() {
      setPreferredSize(new java.awt.Dimension(360, 0));
      setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      var g2 = (java.awt.Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      var gradient = new java.awt.GradientPaint(0, 0, UITheme.PRIMARY, getWidth(), getHeight(), UITheme.PRIMARY_DARK);
      g2.setPaint(gradient);
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

      g2.setColor(Color.WHITE);
      g2.setFont(UITheme.TITLE_FONT);
      g2.drawString("University of Baguio", 32, 80);
      g2.setFont(UITheme.SECTION_FONT);
      g2.drawString("Visitor Security Portal", 32, 120);
      g2.setFont(UITheme.BODY_FONT);
      g2.drawString("Monitor guests, secure campuses,", 32, 170);
      g2.drawString("and empower your team in one hub.", 32, 194);
    }
  }
}
