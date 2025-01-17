package domain.services;

import application.exceptions.FailureResponse;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.models.User;
import persistence.repositories.CardRepository;
import persistence.repositories.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

public class CardService
{
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public CardService(UserRepository userRepository, CardRepository cardRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    public HTTPResponse showCards(HTTPRequest req)
    {
        try
        {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            if(user.isEmpty()){
                return new HTTPResponse("404","User not found!","text/plain");
            }
            String cardsOutput= cardRepository.getCards(user.get());
            return new HTTPResponse("200","All your cards:","text/plain",cardsOutput);
        }
        catch ( SQLException | IllegalStateException e)
        {
            return FailureResponse.getHTTPException(e);
        }
    }
}