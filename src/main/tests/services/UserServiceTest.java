package services;
import domain.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;
import domain.models.User;
import persistence.repositories.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

public class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private JSONParser jsonParserMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepositoryMock, jsonParserMock);
    }

    @Test
    void register_userSuccessfullyAdded_returns200Response() throws SQLException, JsonProcessingException {
        HTTPRequest request = mock(HTTPRequest.class);

        when(request.getBody()).thenReturn("{\"username\":\"testUser\",\"password\":\"password123\"}");

        User user = new User("testUser", "password123");
        when(jsonParserMock.readValue(anyString(), eq(User.class))).thenReturn(user);
        when(userRepositoryMock.addUser(user)).thenReturn(true);

        HTTPResponse response = userService.register(request);

        assertEquals("200", response.getStatus());
        assertEquals("User successfully registered!", response.getStatusMessage());
    }

    @Test
    void register_userAlreadyExists_returns404Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);

        when(request.getBody()).thenReturn("{\"username\":\"testUser\",\"password\":\"password123\"}");

        User user = new User("testUser", "password123");
        when(jsonParserMock.readValue(anyString(), eq(User.class))).thenReturn(user);
        when(userRepositoryMock.addUser(user)).thenReturn(false);

        HTTPResponse response = userService.register(request);

        assertEquals("404", response.getStatus());
        assertEquals("There is already a user registered with this name!", response.getStatusMessage());
    }

    @Test
    void register_sqlException_returns500Response() throws SQLException, JsonProcessingException {
        HTTPRequest request = mock(HTTPRequest.class);

        when(request.getBody()).thenReturn("{\"username\":\"testUser\",\"password\":\"password123\"}");

        User user = new User("testUser", "password123");
        when(userRepositoryMock.addUser(eq(user))).thenThrow(new SQLException("DB error"));
        when(jsonParserMock.readValue(anyString(), eq(User.class))).thenReturn(user);
        HTTPResponse response = userService.register(request);

        assertEquals("500", response.getStatus());
        assertEquals("DB error", response.getStatusMessage());
    }


    @Test
    void login_validCredentials_returns200ResponseWithToken() throws SQLException, JsonProcessingException {
        HTTPRequest request = mock(HTTPRequest.class);

        when(request.getBody()).thenReturn("{\"username\":\"testUser\",\"password\":\"password123\"}");

        User user = new User("testUser", "password123");
        when(jsonParserMock.readValue(anyString(), eq(User.class))).thenReturn(user);
        when(userRepositoryMock.getUser(eq(user))).thenReturn("validToken");

        HTTPResponse response = userService.login(request);

        assertEquals("200", response.getStatus());
        assertEquals("User successfully logged in!", response.getStatusMessage());
    }

    @Test
    void login_invalidCredentials_returns404Response() throws SQLException, JsonProcessingException {
        HTTPRequest request = mock(HTTPRequest.class);

        when(request.getBody()).thenReturn("{\"username\":\"testUser\",\"password\":\"wrongPassword\"}");

        User user = new User("testUser", "wrongPassword");
        when(jsonParserMock.readValue(anyString(), eq(User.class))).thenReturn(user);
        when(userRepositoryMock.getUser(eq(user))).thenReturn("");

        HTTPResponse response = userService.login(request);

        assertEquals("404", response.getStatus());
        assertEquals("User not found!", response.getStatusMessage());
    }

    @Test
    void login_sqlException_returns500Response() throws SQLException, JsonProcessingException {
        HTTPRequest request = mock(HTTPRequest.class);

        when(request.getBody()).thenReturn("{\"username\":\"testUser\",\"password\":\"password123\"}");

        User user = new User("testUser", "password123");
        when(userRepositoryMock.getUser(eq(user))).thenThrow(new SQLException("DB error"));
        when(jsonParserMock.readValue(anyString(), eq(User.class))).thenReturn(user);
        HTTPResponse response = userService.login(request);

        assertEquals("500", response.getStatus());
        assertEquals("DB error", response.getStatusMessage());
    }
}
