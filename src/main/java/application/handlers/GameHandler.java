package application.handlers;

import application.interfaces.RequestHandleable;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.services.GameService;

public class GameHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req)
    {
        GameService service = new GameService();
        if(req.getMethod().equalsIgnoreCase("POST")) return service.battle(req);
        if(req.getMethod().equalsIgnoreCase("GET")) return service.showStats(req);
        return new HTTPResponse("404","Wrong Method","plain/text");
    }
}
