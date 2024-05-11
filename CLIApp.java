import java.util.Scanner;
public class CLIApp {
    private static NumberleController controller;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        controller = new NumberleController(new NumberleModel());
        controller.startNewGame(); // Start a new game
        System.out.println("Welcome to Numberle!");
        System.out.println("Target equation: " + controller.getTargetEquation());
        while (!controller.isGameOver()) {
            System.out.println("Enter your guess (Remaining attempts: " + controller.getRemainingAttempts() + "):");
            String input = scanner.nextLine();
            boolean correct = controller.processInput(input); // Process input and obtain the result

            if (correct) {
                if (controller.isGameWon()) {
                    System.out.println("Congratulations! You've guessed the equation correctly: " + controller.getTargetEquation());
                    break; // Exit the loop if the correct equation is guessed
                } else {
                    controller.updateGuess(input); // Update the guess state and display feedback
                    System.out.println("Correct format and valid equation but not the exact match. Try again.");
                }
            }

            if (controller.isGameOver() && !controller.isGameWon()) {
                System.out.println("Game Over! The correct equation was: " + controller.getTargetEquation());
                break; // If the game ends without a win, display the correct equation
            }
        }
        restartGameOption(); // Provide an option to restart the game
    }

    private static void restartGameOption() {
        System.out.println("Do you want to play again? (yes/no)");
        String decision = scanner.nextLine();
        if (decision.equalsIgnoreCase("yes")) {
            main(new String[0]); // Restart the game
        } else {
            System.out.println("Thank you for playing Numberle!");
        }
    }
}
