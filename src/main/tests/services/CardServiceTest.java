package services;
import domain.services.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.models.User;
import persistence.repositories.CardRepository;
import persistence.repositories.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

public class CardServiceTest {

    private CardService cardService;
    private UserRepository userRepositoryMock;
    private CardRepository cardRepositoryMock;

    @BeforeEach
    void setUp() {
        userRepositoryMock = mock(UserRepository.class);
        cardRepositoryMock = mock(CardRepository.class);
        cardService = new CardService(userRepositoryMock, cardRepositoryMock);
    }

    @Test
    void showCards_userNotFound_returns404Response() throws SQLException {
        // Arrange
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("invalidToken");

        when(userRepositoryMock.getUserByToken("invalidToken"))
                .thenReturn(Optional.empty());

        // Act
        HTTPResponse response = cardService.showCards(request);

        // Assert
        assertEquals("404", response.getStatus());
        assertEquals("User not found!", response.getStatusMessage());
    }

    @Test
    void showCards_userFound_returnsCards() throws SQLException {
        // Arrange
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("validToken");

        User user = new User();
        user.setId("user123");
        when(userRepositoryMock.getUserByToken("validToken"))
                .thenReturn(Optional.of(user));

        when(cardRepositoryMock.getCards(user))
                .thenReturn("ID: Card1 | Name: Fireball | Damage: 50.0\nID: Card2 | Name: Frostbolt | Damage: 45.0\n");

        // Act
        HTTPResponse response = cardService.showCards(request);

        // Assert
        assertEquals("200", response.getStatus());
        assertEquals("All your cards:", response.getStatusMessage());
        assertEquals("ID: Card1 | Name: Fireball | Damage: 50.0\nID: Card2 | Name: Frostbolt | Damage: 45.0\n", response.getBody());
    }

    @Test
    void showCards_sqlException_returns500Response() throws SQLException {
        // Arrange
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("validToken");

        User user = new User();
        user.setId("user123");
        when(userRepositoryMock.getUserByToken("validToken"))
                .thenReturn(Optional.of(user));

        when(cardRepositoryMock.getCards(user))
                .thenThrow(new SQLException("Database error"));

        // Act
        HTTPResponse response = cardService.showCards(request);

        // Assert
        assertEquals("500", response.getStatus());
        assertEquals("DB error", response.getStatusMessage());
    }

    @Test
    void showCards_illegalStateException_returns404Response() throws SQLException {
        // Arrange
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("validToken");

        User user = new User();
        user.setId("user123");
        when(userRepositoryMock.getUserByToken("validToken"))
                .thenReturn(Optional.of(user));

        when(cardRepositoryMock.getCards(user))
                .thenThrow(new IllegalStateException("Illegal state"));

        // Act
        HTTPResponse response = cardService.showCards(request);

        // Assert
        assertEquals("404", response.getStatus());
        assertEquals("Illegal state", response.getStatusMessage());
    }
}
