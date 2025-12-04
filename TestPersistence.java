import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestPersistence {
  public static void main(String[] args) throws Exception {
    Path save = Path.of("visitor_logs.txt");
    if (Files.exists(save)) Files.delete(save);

    GuardDirectory gd = new GuardDirectory();
    AdminDirectory ad = new AdminDirectory();

    VisitorTableModel model = new VisitorTableModel(gd.getGuards(), ad.getAdmins());
    System.out.println("Loaded rows: " + model.getRowCount());

    VisitorFormData data = new VisitorFormData("Jane Doe", "555-0001", "UB-123456", "Meeting", "Main Campus", gd.getGuards().get(0));
    model.addVisitor(Visitor.fromFormData(data));

    System.out.println("After add: rows = " + model.getRowCount());

    if (Files.exists(save)) {
      System.out.println("Saved file contents:");
      List<String> lines = Files.readAllLines(save);
      for (String l : lines) System.out.println(l);
    } else {
      System.out.println("No save file created");
    }
  }
}
