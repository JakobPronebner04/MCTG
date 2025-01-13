package application.handlers;

import application.interfaces.RequestHandleable;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.services.GameService;

public class ScoreboardHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req)
    {
        GameService service = new GameService();
        if(req.getMethod().equalsIgnoreCase("GET")) return service.showScoreboard(req);
        return new HTTPResponse("404","Wrong Method","plain/text");
    }
}
