package application.handlers;

import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import application.interfaces.RequestHandleable;
import persistence.repositories.CardRepository;
import persistence.repositories.UserRepository;
import domain.services.CardService;

public class CardHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req){
        CardService cardService = new CardService(UserRepository.getInstance(), CardRepository.getInstance());
        if(req.getMethod().equals("GET")){
            return cardService.showCards(req);
        }
        return new HTTPResponse("400","Wrong Request","text/plain");
    }
}
