package application.handlers;

import application.interfaces.RequestHandleable;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;
import persistence.repositories.UserRepository;
import domain.services.UserService;
import java.sql.SQLException;

public class UserSessionHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req) throws SQLException
    {
        UserService service = new UserService(UserRepository.getInstance(), new JSONParser());
        if(req.getMethod().equalsIgnoreCase("POST"))
        {
            if(req.getPath().split("/").length == 3) {
                return service.logout(req);
            }
            return service.login(req);
        }
        return new HTTPResponse("400","Wrong Request","text/plain","");

    }
}
