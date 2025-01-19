package repositories;

import domain.models.Card;
import domain.models.Deck;
import domain.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.repositories.GameRepository;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class GameRepositoryTest {

    private GameRepository gameRepository;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        gameRepository = spy(GameRepository.getInstance());
        user1 = new User("Player1", "password1");
        user2 = new User("Player2", "password2");
    }

    @Test
    void testStartMethod_drawOutput() throws SQLException {
        Deck deck1 = new Deck();
        deck1.addCard(new Card("Dragon", 50, "1"));
        deck1.addCard(new Card("FireElf", 40, "2"));

        Deck deck2 = new Deck();
        deck2.addCard(new Card("Dragon", 50, "3"));
        deck2.addCard(new Card("FireElf", 40, "4"));

        user1.setDeck(deck1);
        user2.setDeck(deck2);

        doNothing().when(gameRepository).updateUserStats(any(User.class), any(User.class), any());
        String battleLog = gameRepository.start(user1, user2);

        System.out.println("Battle Log Output:");
        System.out.println(battleLog);

        verify(gameRepository, atLeastOnce()).updateUserStats(any(User.class), any(User.class), any());
        assertNotNull(battleLog);
        assertTrue(battleLog.contains("draw"));
    }

    @Test
    void testStartMethod_WinOutput() throws SQLException {
        Deck deck1 = new Deck();
        deck1.addCard(new Card("Dragon", 50, "1"));
        deck1.addCard(new Card("FireElf", 40, "2"));

        Deck deck2 = new Deck();
        deck2.addCard(new Card("Goblin", 10, "5"));
        deck2.addCard(new Card("WaterSpell", 20, "6"));

        user1.setDeck(deck1);
        user2.setDeck(deck2);

        doNothing().when(gameRepository).updateUserStats(any(User.class), any(User.class), any());
        String battleLog = gameRepository.start(user1, user2);

        System.out.println("Battle Log Output:");
        System.out.println(battleLog);

        verify(gameRepository, atLeastOnce()).updateUserStats(any(User.class), any(User.class), any());
        assertNotNull(battleLog);
        assertTrue(battleLog.contains("Player1 won match!"));
    }
}
