package application.handlers;

import application.interfaces.RequestHandleable;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;
import persistence.repositories.UserRepository;
import domain.services.UserService;

import java.sql.SQLException;

public class UserActionHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req) throws SQLException
    {
        UserService service = new UserService(UserRepository.getInstance(), new JSONParser());
        if(req.getMethod().equalsIgnoreCase("PUT"))
        {
            return service.changeUserData(req);
        }

        if(req.getMethod().equalsIgnoreCase("POST"))
        {
            return service.register(req);
        }
        if(req.getMethod().equalsIgnoreCase("GET"))
        {
            return service.showUserData(req);
        }
        return new HTTPResponse("400","Wrong Request","text/plain","");

    }
}
