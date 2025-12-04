import java.awt.Color;
import java.awt.Font;

class UITheme {
  static final Color PRIMARY = new Color(0x861313);
  static final Color PRIMARY_DARK = new Color(0x5C0C0C);
  static final Color ACCENT = new Color(0xC99700);
  static final Color LIGHT_BG = new Color(0xF5F6FA);
  static final Color PANEL_BG = Color.WHITE;
  static final Color BORDER = new Color(0xD9DCE3);

  static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
  static final Font SECTION_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 18);
  static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

  private UITheme() {
  }
}
