import javax.swing.*;

public class GUIApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIApp::createAndShowGUI);

    }
    public static void createAndShowGUI() {
        NumberleModel model = new NumberleModel();
        NumberleController controller = new NumberleController(model);
        NumberleView view = new NumberleView(model, controller);
        controller.setView(view);
        controller.startNewGame();
        JFrame frame = new JFrame("Numberle Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
