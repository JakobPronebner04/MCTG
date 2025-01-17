package services;
import domain.services.PackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;
import domain.models.Card;
import domain.models.Package;
import domain.models.User;
import persistence.repositories.PackageRepository;
import persistence.repositories.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PackageServiceTest {

    private PackageService packageService;
    private UserRepository userRepositoryMock;
    private PackageRepository packageRepositoryMock;
    private JSONParser jsonParserMock;

    @BeforeEach
    void setUp() {
        userRepositoryMock = mock(UserRepository.class);
        packageRepositoryMock = mock(PackageRepository.class);
        packageService = new PackageService(userRepositoryMock, packageRepositoryMock);
    }

    @Test
    void addPackage_adminUserSuccessfullyAddsPackage_returns200Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("admin-mtcgToken");
        when(request.getBody()).thenReturn("[{\"id\":\"card1\",\"name\":\"WaterGoblin\",\"damage\":50.0}," +
                                              "{\"id\":\"card2\",\"name\":\"WaterSpell\",\"damage\":20.0}," +
                                              "{\"id\":\"card3\",\"name\":\"KNIGHT\",\"damage\":20.0}," +
                                              "{\"id\":\"card4\",\"name\":\"Dragon\",\"damage\":20.0}," +
                                              "{\"id\":\"card5\",\"name\":\"ORK\",\"damage\":20.0}]");

        when(packageRepositoryMock.addPackage(any(Package.class))).thenReturn(true);

        User user = new User();
        user.setToken("admin-mtcgToken");
        when(userRepositoryMock.getUserByToken("admin-mtcgToken"))
                .thenReturn(Optional.of(user));

        HTTPResponse response = packageService.addPackage(request);

        assertEquals("200", response.getStatus());
        assertEquals("Successfully created package!", response.getStatusMessage());
    }

    @Test
    void addPackage_nonAdminUser_returns404Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("user-mtcgToken");
        when(request.getBody()).thenReturn("[{\"id\":\"card1\",\"name\":\"WaterGoblin\",\"damage\":50.0}," +
                "{\"id\":\"card2\",\"name\":\"WaterSpell\",\"damage\":20.0}," +
                "{\"id\":\"card3\",\"name\":\"KNIGHT\",\"damage\":20.0}," +
                "{\"id\":\"card4\",\"name\":\"Dragon\",\"damage\":20.0}," +
                "{\"id\":\"card5\",\"name\":\"ORK\",\"damage\":20.0}]");

        User user = new User();
        user.setToken("user-mtcgToken");
        when(userRepositoryMock.getUserByToken("user-mtcgToken"))
                .thenReturn(Optional.of(user));

        HTTPResponse response = packageService.addPackage(request);

        assertEquals("404", response.getStatus());
        assertEquals("User not found or wrong user!", response.getStatusMessage());
    }

    @Test
    void addPackage_sqlException_returns500Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("admin-mtcgToken");
        when(request.getBody()).thenReturn("[{\"name\":\"Card1\",\"damage\":50.0}]");

        User user = new User();
        user.setToken("admin-mtcgToken");
        when(userRepositoryMock.getUserByToken("admin-mtcgToken"))
                .thenReturn(Optional.of(user));
        when(packageRepositoryMock.addPackage(any(Package.class))).thenThrow(new SQLException("DB error"));

        HTTPResponse response = packageService.addPackage(request);

        assertEquals("500", response.getStatus());
        assertEquals("DB error", response.getStatusMessage());
    }

    @Test
    void aquirePackage_userSuccessfullyBuysPackage_returns200Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("user-mtcgToken");

        User user = new User("user", "1234");
        user.setCoins(10);
        when(userRepositoryMock.getUserByToken("user-mtcgToken"))
                .thenReturn(Optional.of(user));
        when(packageRepositoryMock.getPackage(user)).thenReturn(true);

        HTTPResponse response = packageService.aquirePackage(request);

        assertEquals("200", response.getStatus());
        assertEquals("Successfully bought new package!", response.getStatusMessage());
        verify(userRepositoryMock).updateCoins(user);
    }

    @Test
    void aquirePackage_userNotFound_returns404Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("invalid-token");

        when(userRepositoryMock.getUserByToken("invalid-token")).thenReturn(Optional.empty());

        HTTPResponse response = packageService.aquirePackage(request);

        assertEquals("404", response.getStatus());
        assertEquals("User not found! Package could not be bought", response.getStatusMessage());
    }

    @Test
    void aquirePackage_insufficientCoins_returns404Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("user-mtcgToken");

        User user = new User("user","1234");
        user.setCoins(3);
        when(userRepositoryMock.getUserByToken("user-mtcgToken"))
                .thenReturn(Optional.of(user));

        HTTPResponse response = packageService.aquirePackage(request);

        assertEquals("404", response.getStatus());
        assertEquals("Could not buy package!", response.getStatusMessage());
    }

    @Test
    void aquirePackage_sqlException_returns500Response() throws SQLException {
        HTTPRequest request = mock(HTTPRequest.class);
        when(request.getToken()).thenReturn("user-mtcgToken");

        User user = new User("user", "1234");
        user.setCoins(10);
        when(userRepositoryMock.getUserByToken(anyString()))
                .thenReturn(Optional.of(user));
        when(packageRepositoryMock.getPackage(user)).thenThrow(new SQLException("DB error"));

        HTTPResponse response = packageService.aquirePackage(request);

        assertEquals("500", response.getStatus());
        assertEquals("DB error", response.getStatusMessage());
    }
}
