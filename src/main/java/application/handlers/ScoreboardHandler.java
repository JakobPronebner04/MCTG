package application.handlers;

import application.interfaces.RequestHandleable;
import persistence.repositories.CardRepository;
import persistence.repositories.GameRepository;
import persistence.repositories.UserRepository;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.services.GameService;

public class ScoreboardHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req)
    {
        GameService service = new GameService(UserRepository.getInstance(), GameRepository.getInstance(), CardRepository.getInstance());
        if(req.getMethod().equalsIgnoreCase("GET")) return service.showScoreboard(req);
        return new HTTPResponse("404","Wrong Method","plain/text");
    }
}
