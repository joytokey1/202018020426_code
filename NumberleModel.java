// NumberleModel.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NumberleModel extends Observable implements INumberleModel {
    private String targetEquation;
    private List<String> equations = new ArrayList<>();
    private List<String> guesses = new ArrayList<>();
    private StringBuilder currentGuess;
    private int remainingAttempts= MAX_ATTEMPTS;
    private boolean gameWon;

    @Override
    public void loadEquations() {
        try (BufferedReader reader = new BufferedReader(new FileReader("equations.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                equations.add(line.trim()); // Add each line read to the list
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading the equations file.");
        }
    }

    @Override
    public void initialize() {
        if (!equations.isEmpty()) {
            Random rand = new Random();
            targetEquation = equations.get(rand.nextInt(equations.size())); // Choose a random equation from the list
            assert targetEquation != null : "Target equation must not be null";
        }
        remainingAttempts = MAX_ATTEMPTS;
        gameWon = false;
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean processInput(String input) {
        assert input != null && !input.isEmpty() : "Input cannot be null or empty";
        // Verify that the input is properly formatted
        if (!isValidInput(input)) {
            System.out.println("Invalid input format.");
            return false;
        }
        // Verify that the input equation is mathematically correct
        if (!isCorrectEquation(input)) {
            System.out.println("Incorrect equation.");
            return false;
        }
        // Check if user input exactly matches the target equation
        if (input.equals(targetEquation)) {
            gameWon = true;
            updateGuess(input);
            return true;
        } else {
            remainingAttempts--; // Reduce attempts only if the input is valid but does not exactly match the target equation
            assert remainingAttempts >= 0 : "Remaining attempts should never be negative";
            System.out.println("Not the exact match. Try again.");
            updateGuess(input);
        }

        if (remainingAttempts <= 0) {
            gameWon = false;
        }
        return false;
    }

    @Override
    public boolean isValidInput(String input) {
        // Check if there is only one equal sign
        if (input.chars().filter(ch -> ch == '=').count() != 1) {
            return false;
        }
        // Check if input length is 7
        return input.length() == 7;
    }

    @Override
    public boolean isCorrectEquation(String input) {
        String[] parts = input.split("=");
        if (parts.length != 2) {
            return false; // Make sure the input is split correctly
        }
        try {
            int leftResult = evaluateExpression(parts[0]);
            int rightResult = Integer.parseInt(parts[1].trim());
            return leftResult == rightResult;
        } catch (NumberFormatException e) {
            System.out.println("Number format error: " + e.getMessage());
            return false;
        } catch (ArithmeticException e) {
            System.out.println("Math error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int evaluateExpression(String expression) {
        Stack<Integer> numbers = new Stack<>();
        Stack<Character> operations = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                int num = 0;
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    num = num * 10 + (expression.charAt(i) - '0');
                    i++;
                }
                i--;
                numbers.push(num);
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operations.isEmpty() && (c == '+' || c == '-' || operations.peek() == '*' || operations.peek() == '/')) {
                    int b = numbers.pop();
                    int a = numbers.pop();
                    switch (operations.pop()) {
                        case '+':
                            numbers.push(a + b);
                            break;
                        case '-':
                            numbers.push(a - b);
                            break;
                        case '*':
                            numbers.push(a * b);
                            break;
                        case '/':
                            if (b == 0) throw new ArithmeticException("Cannot divide by zero");
                            numbers.push(a / b);
                            break;
                    }
                }
                operations.push(c);
            }
        }
        while (!operations.isEmpty()) {
            int b = numbers.pop();
            int a = numbers.pop();
            switch (operations.pop()) {
                case '+':
                    numbers.push(a + b);
                    break;
                case '-':
                    numbers.push(a - b);
                    break;
                case '*':
                    numbers.push(a * b);
                    break;
                case '/':
                    if (b == 0) throw new ArithmeticException("Cannot divide by zero");
                    numbers.push(a / b);
                    break;
            }
        }
        return numbers.pop();
    }

    @Override
    public boolean isGameOver() {
        return remainingAttempts <= 0 || gameWon;
    }

    @Override
    public boolean isGameWon() {
        return gameWon;
    }

    @Override
    public String getTargetEquation() {
        return targetEquation;
    }

    @Override
    public StringBuilder getCurrentGuess() {
        return currentGuess;
    }

    @Override
    public void updateGuess(String guess) {
        guesses.add(guess);
        currentGuess = new StringBuilder(guess);
        List<String> guessFeedback = new ArrayList<>(Collections.nCopies(guess.length(), ""));
        boolean[] matchedTarget = new boolean[targetEquation.length()];

        // Check for an exact match
        for (int i = 0; i < guess.length(); i++) {
            if (i < targetEquation.length() && guess.charAt(i) == targetEquation.charAt(i)) {
                guessFeedback.set(i, guess.charAt(i) + " is in the equation and in the correct spot");
                matchedTarget[i] = true;
            }
        }

        // Check for existing but incorrectly located cases
        for (int i = 0; i < guess.length(); i++) {
            if (guessFeedback.get(i).isEmpty()) {
                for (int j = 0; j < targetEquation.length(); j++) {
                    if (!matchedTarget[j] && guess.charAt(i) == targetEquation.charAt(j)) {
                        guessFeedback.set(i, guess.charAt(i) + " is in the equation, but in the wrong spot.");
                        matchedTarget[j] = true;
                        break;
                    }
                }
                if (guessFeedback.get(i).isEmpty()) {
                    guessFeedback.set(i, guess.charAt(i) + " is not in the target equation at all.");
                }
            }
        }
        System.out.println("Feedback:");
        for (String feedback : guessFeedback) {
            if (!feedback.isEmpty()) {
                System.out.println(feedback);
            }
        }
        // Notify observers with the complete feedback data
        setChanged();
        notifyObservers(new AbstractMap.SimpleEntry<>(guesses.size(), guessFeedback));
    }

    @Override
    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    @Override
    public void startNewGame() {
        loadEquations();
        initialize();
        setChanged();
        notifyObservers();
    }
}
