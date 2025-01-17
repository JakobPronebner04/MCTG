package application.handlers;

import application.interfaces.RequestHandleable;
import persistence.repositories.CardRepository;
import persistence.repositories.UserRepository;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.services.DeckService;

public class DeckHandler implements RequestHandleable {
    @Override
    public HTTPResponse handle(HTTPRequest req) {

        DeckService deckService = new DeckService(UserRepository.getInstance(), CardRepository.getInstance());
        if(req.getMethod().equals("PUT")) {

            return deckService.configureDeck(req);
        }
        if(req.getMethod().equals("GET")) {
            return req.getQuery().contains("format=plain") ?
                    deckService.showDeck(req,true):
                    deckService.showDeck(req,false);
        }
        return new HTTPResponse("404","Wrong Request","text/plain");
    }
}
