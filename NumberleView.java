import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.util.List;

public class NumberleView implements Observer {
    private final NumberleController controller;
    private final JFrame frame = new JFrame("Numberle");
    private final JTextField inputTextField = new JTextField(20);
    private final JPanel guessPanel = new JPanel(new GridLayout(6, 7, 2, 2)); // 6行7列的面板
    private JButton newGameButton;
    private int currentGuessIndex = 0;
    private final Map<Character, JButton> keyboardButtons;
    private final Map<Character, Color> keyColors;

    private boolean enableRestart = false;

    public NumberleView(INumberleModel model, NumberleController controller) {
        this.controller = controller;
        ((NumberleModel) model).addObserver(this);
        keyboardButtons = new HashMap<>();
        keyColors = new HashMap<>();
        initialize();
        this.controller.setView(this);
        update((NumberleModel) model, null);

    }

    private void initialize() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel();
        inputTextField.setEditable(false);
        inputTextField.setFont(new Font("Arial", Font.BOLD, 24));
        inputPanel.add(inputTextField);
        initializeGuessPanel();
        JPanel keyboardPanel = initializeKeyboard();
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(guessPanel, BorderLayout.CENTER);
        frame.add(keyboardPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }


    private void initializeGuessPanel() {
        guessPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 6 * 7; i++) {
            JLabel label = new JLabel("", JLabel.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            label.setFont(new Font("Arial", Font.BOLD, 30));
            label.setOpaque(true);
            guessPanel.add(label);
        }
    }

    private JPanel initializeKeyboard() {
        // Create a keyboard panel
        JPanel keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new GridLayout(2, 9, 5, 5));
        // Define labels for all keys
        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "+", "-", "*", "/", "=", "C", "Enter"};
        // Create each button on the keyboard and add event listeners to it
        for (String key : keys) {
            JButton button = new JButton(key);
            button.setBackground(Color.LIGHT_GRAY); // Set the initial color to gray
            keyboardButtons.put(key.charAt(0), button);
            keyColors.put(key.charAt(0), Color.LIGHT_GRAY);
            keyboardPanel.add(button);
            button.setFont(new Font("Arial", Font.BOLD, 20));
            button.addActionListener(e -> {
                String command = e.getActionCommand();
                if (command.equals("Enter")) {
                    controller.processInput(inputTextField.getText());
                    inputTextField.setText("");
                } else if (command.equals("C")) {
                    inputTextField.setText("");
                } else {
                    inputTextField.setText(inputTextField.getText() + command);
                }
            });
            keyboardPanel.add(button);
        }
        // Initialize the New Game button
        newGameButton = new JButton("Restart");
        if(!enableRestart){
        newGameButton.setEnabled(false);}  // Disable it initially
        keyboardPanel.add(newGameButton);  // Add to the keyboard panel
        newGameButton.setBackground(Color.LIGHT_GRAY);
        newGameButton.setFont(new Font("Arial", Font.BOLD, 20));
        newGameButton.addActionListener(e -> startNewGame());
        return keyboardPanel;
    }

    private void startNewGame() {
        inputTextField.setText("");
        // Reset all guess labels and colors
        Component[] labels = guessPanel.getComponents();
        for (Component label : labels) {
            if (label instanceof JLabel) {
                JLabel guessLabel = (JLabel) label;
                guessLabel.setText("");  // Clear text
                guessLabel.setBackground(Color.LIGHT_GRAY);  // Reset the background color to default
            }
        }

        for (JButton button : keyboardButtons.values()) {
            button.setBackground(Color.LIGHT_GRAY);  // Reset the keyboard color to default
        }

        // Disable the new game button until the current game ends
        newGameButton.setEnabled(false);
        currentGuessIndex = 0;
        this.controller.startNewGame();

    }

    private void showGameOverDialog() {
        // Build a message based on the win or loss status of the game
        String message = controller.isGameWon() ? "Congratulations! You've won! Would you like to play again?" : "Game Over! Would you like to try again?";
        int response = JOptionPane.showConfirmDialog(frame, message, "Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            startNewGame();  // Restart the game
        } else {
            System.exit(0);  // Exit the program
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Map.Entry) {
            Map.Entry<Integer, List<String>> entry = (Map.Entry<Integer, List<String>>) arg;
            List<String> feedback = entry.getValue(); // Get the feedback list
            updateGuessGrid(feedback); // Update the grid showing the guesses
            Map<Character, String> keyFeedback = parseKeyFeedback(feedback); // Parse the keyboard feedback
            updateKeyboard(keyFeedback); // Update the keyboard colors
            if(!enableRestart){
                newGameButton.setEnabled(true);}
        }
        if (controller.isGameOver()) {
            SwingUtilities.invokeLater(this::showGameOverDialog);
        }
    }

    private Map<Character, String> parseKeyFeedback(List<String> feedback) {
        Map<Character, String> keyFeedback = new HashMap<>();
        for (String f : feedback) {
            if (f.contains("is not in the target equation at all.")) {
                keyFeedback.put(f.charAt(0), "not");
            } else if (f.contains("is in the equation, but in the wrong spot.")) {
                keyFeedback.put(f.charAt(0), "wrong");
            } else if (f.contains("is in the equation and in the correct spot")) {
                keyFeedback.put(f.charAt(0), "correct");
            }
        }
        return keyFeedback;
    }

    private void updateGuessGrid(List<String> feedback) {
        int startIndex = currentGuessIndex * 7; // 每行7个字符
        Component[] labels = guessPanel.getComponents();
        for (int i = startIndex; i < startIndex + feedback.size(); i++) {
            JLabel label = (JLabel) labels[i];
            label.setText(feedback.get(i - startIndex).split(" ")[0]);
            label.setBackground(getColorFromFeedback(feedback.get(i - startIndex)));
        }
        currentGuessIndex++; // Update guesses
    }

    private Color getColorFromFeedback(String feedback) {
        if (feedback.contains("correct")) return Color.GREEN;
        if (feedback.contains("wrong")) return Color.ORANGE;
        return Color.GRAY;
    }

    private void updateKeyboard(Map<Character, String> feedback) {
        // Iterate over each entry in the feedback map
        for (Map.Entry<Character, String> entry : feedback.entrySet()) {
            // Get the character key and the feedback
            char keyChar = entry.getKey();
            String status = entry.getValue();

            // Find the corresponding button in the keyboardButtons map
            JButton button = keyboardButtons.get(keyChar);
            if (button != null) {
                switch (status) {
                    case "correct":
                        button.setBackground(Color.GREEN);  // Set background to green if correct
                        break;
                    case "wrong":
                        button.setBackground(Color.ORANGE);  // Set background to orange if wrong but present
                        break;
                    default:
                        button.setBackground(Color.LIGHT_GRAY);  // Default background
                }
            }
        }
    }
}