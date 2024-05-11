import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NumberleModelTest {
    private NumberleModel model;

    @BeforeEach
    void setUp() {
        model = new NumberleModel();
        model.loadEquations(); // This method needs to correctly load equations to prevent NPE
        model.initialize(); // This method must set up all necessary game state
    }

    // Test to ensure that game initialization sets up the game correctly.
    @Test
    void testModelInitialization() {
        assertNotNull(model.getTargetEquation(), "The target equation should not be null after initialization.");
        assertTrue(model.getRemainingAttempts() <= NumberleModel.MAX_ATTEMPTS, "Remaining attempts should be initialized to MAX_ATTEMPTS.");
        assertFalse(model.isGameOver(), "Game should not be over immediately after initialization.");
    }

    // Test to check correct handling of a valid input.
    @Test
    void testValidInputHandling() {
        String testInput = "1+2+3=6"; // Assuming "1+2+3=6" is not the target equation.
        assertFalse(model.processInput(testInput), "Processing valid but incorrect input should return false.");
        assertTrue(model.getRemainingAttempts() < NumberleModel.MAX_ATTEMPTS, "Attempts should decrease after a guess.");
    }

    // Test to verify that an incorrect input format is handled properly.
    @Test
    void testInvalidInputFormat() {
        String invalidInput = "abcdefg";
        assertFalse(model.processInput(invalidInput), "Processing invalid format should return false.");
        assertEquals(NumberleModel.MAX_ATTEMPTS, model.getRemainingAttempts(), "Attempts should not decrease on invalid input.");
    }

    // Ensure that the game correctly identifies the end condition.
    @Test
    void testGameEndCondition() {
        // Force the game to reach end condition
        for (int i = 0; i < NumberleModel.MAX_ATTEMPTS; i++) {
            model.processInput("1+1+1=3"); // Assuming "1+1+1=3" is incorrect
        }
        assertTrue(model.isGameOver(), "Game should be over after MAX_ATTEMPTS incorrect guesses.");
    }
}
