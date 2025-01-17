package services;

import domain.services.DeckService;
import domain.services.PackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import persistence.repositories.CardRepository;
import persistence.repositories.PackageRepository;
import persistence.repositories.UserRepository;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.models.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeckServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private DeckService deckService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        cardRepository = mock(CardRepository.class);
        deckService = new DeckService(userRepository, cardRepository);
    }

    @Test
    void testConfigureDeckSuccess() throws Exception {
        // Arrange
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("testuser-mtcgToken");
        when(request.getBody()).thenReturn("[\"card1\", \"card2\", \"card3\",\"card4\"]");

        User mockUser = new User("testuser", "password");
        when(userRepository.getUserByToken("testuser-mtcgToken")).thenReturn(Optional.of(mockUser));
        when(cardRepository.configureDeck(eq(mockUser), anyList())).thenReturn(true);

        // Act
        HTTPResponse response = deckService.configureDeck(request);

        // Assert
        assertEquals("200", response.getStatus());
        assertEquals("Changed deck!", response.getStatusMessage());
    }

    @Test
    void testConfigureDeckUserNotFound() throws Exception {
        // Arrange
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("invalid-token");

        when(userRepository.getUserByToken("invalid-token")).thenReturn(Optional.empty());

        // Act
        HTTPResponse response = deckService.configureDeck(request);

        // Assert
        assertEquals("404", response.getStatus());
        assertEquals("User not found!", response.getStatusMessage());
    }

    @Test
    void testConfigureDeckFailureToChangeDeck() throws Exception {
        // Arrange
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("testuser-mtcgToken");
        when(request.getBody()).thenReturn("[\"card2\", \"card3\", \"card4\"]");

        User mockUser = new User("test", "t23123");
        when(userRepository.getUserByToken("testuser-mtcgToken")).thenReturn(Optional.of(mockUser));
        when(cardRepository.configureDeck(eq(mockUser), anyList())).thenReturn(false);

        // Act
        HTTPResponse response = deckService.configureDeck(request);

        // Assert
        assertEquals("404", response.getStatus());
        assertEquals("Could not change deck!", response.getStatusMessage());
    }
}
