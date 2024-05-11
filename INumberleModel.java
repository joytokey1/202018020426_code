public interface INumberleModel {
    int MAX_ATTEMPTS = 6;
    void loadEquations();
    void initialize();
    boolean isGameOver();
    boolean isGameWon();
    String getTargetEquation();
    boolean processInput(String input);
    boolean isValidInput(String input);
    boolean isCorrectEquation(String input);
    int evaluateExpression(String expression);
    StringBuilder getCurrentGuess();
    void updateGuess(String guess);
    int getRemainingAttempts();
    void startNewGame();
}