package domain.services;

import application.exceptions.FailureResponse;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;
import domain.models.User;
import domain.models.UserProperties;
import persistence.repositories.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

public class UserService
{
    private final UserRepository userRepository;
    private final JSONParser jsonParser;
    public UserService(UserRepository userRepository, JSONParser jsonParser)
    {
        this.userRepository = userRepository;
        this.jsonParser = jsonParser;
    }

    public HTTPResponse register(HTTPRequest req)
    {
        try
        {
            User user = jsonParser.readValue(req.getBody(),User.class);
            if(userRepository.addUser(user))
            {
                return new HTTPResponse("200","User successfully registered!","text/plain");
            }

            return new HTTPResponse("404","There is already a user registered with this name!","text/plain");
        }
        catch (SQLException | IllegalStateException e)
        {
            return FailureResponse.getHTTPException(e);
        }
    }

    public HTTPResponse login(HTTPRequest req)
    {
        try
        {
            User user = jsonParser.readValue(req.getBody(),User.class);

            String token = userRepository.getUser(user);
            if(!token.isEmpty())
            {
                return new HTTPResponse("200","User successfully logged in!","text/plain",token);
            }

            return new HTTPResponse("404","User not found!","text/plain");
        }
        catch (SQLException | IllegalStateException e)
        {
            return FailureResponse.getHTTPException(e);
        }
    }

    public HTTPResponse logout(HTTPRequest req)
    {
        try {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(() -> new IllegalStateException("User not found!"));

            if(userRepository.removeToken(user.get()))
                return new HTTPResponse("200","User successfully logged out!","text/plain");
            return new HTTPResponse("404","Could not logout!","text/plain");
        }
        catch (SQLException | IllegalStateException e) {
            return FailureResponse.getHTTPException(e);
        }
    }
    public HTTPResponse changeUserData(HTTPRequest req)
    {
        try {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(() -> new IllegalStateException("User not found!"));

            String[] pathParts = req.getPath().split("/");
            String name = (pathParts.length > 2) ? pathParts[2] : "";

            if (!name.equals(user.get().getUsername())) throw new IllegalStateException("Username does not match!");

            Optional<UserProperties> up = getBodyAsProperties(req);
            up.orElseThrow(()->new IllegalStateException("Properties empty"));
            System.out.println(up.get().getImage());
            if(userRepository.editData(user.get(),up.get()))
                return new HTTPResponse("200","Userdata succesfully changed!","text/plain");

            return new HTTPResponse("404","Userdata could not be changed!","text/plain");
        }
        catch ( SQLException | IllegalStateException e) {
            return FailureResponse.getHTTPException(e);
        }
    }

    public HTTPResponse showUserData(HTTPRequest req)
    {
        try{
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(() -> new IllegalStateException("User not found!"));

            String[] pathParts = req.getPath().split("/");
            String name = (pathParts.length > 2) ? pathParts[2] : "";

            if (!name.equals(user.get().getUsername())) throw new IllegalStateException("Username does not match!");

            Optional<UserProperties> up = userRepository.getProperties(user.get());
            String upOutput = "empty";

            if(up.isPresent())
                upOutput = up.get().toString();

            return new HTTPResponse("200","Current Userdata:","text/plain",upOutput);
        }
        catch (SQLException | IllegalStateException e) {
            return FailureResponse.getHTTPException(e);
        }

    }

    private Optional<UserProperties> getBodyAsProperties(HTTPRequest req)
    {
        JSONParser parser = new JSONParser();
        UserProperties up = parser.readValue(req.getBody(), UserProperties.class);
        if(up == null) return Optional.empty();
        return Optional.of(up);
    }
}
