package domain.services;
import application.exceptions.FailureResponse;
import persistence.repositories.PackageRepository;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;
import domain.models.Deck;
import domain.models.User;
import persistence.repositories.CardRepository;
import persistence.repositories.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DeckService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public DeckService(UserRepository userRepository, CardRepository cardRepository){
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }
    public synchronized HTTPResponse configureDeck(HTTPRequest req) {
        try {
            Optional<User> u = userRepository.getUserByToken(req.getToken());
            if (u.isEmpty()) {
                return new HTTPResponse("404", "User not found!", "text/plain");
            }

            List<String> cardIDs = extractCardIDs(req);

            if (cardRepository.configureDeck(u.get(), cardIDs)) {
                return new HTTPResponse("200", "Changed deck!", "text/plain");
            }

            return new HTTPResponse("404", "Could not change deck!", "text/plain");
        } catch (SQLException | IllegalStateException e) {
            return FailureResponse.getHTTPException(e);
        }
    }

    public HTTPResponse showDeck(HTTPRequest req, boolean plain) {
        try {
            Optional<User> u = userRepository.getUserByToken(req.getToken());
            if(u.isEmpty()){
                return new HTTPResponse("404","User not found!","text/plain");
            }

            Optional<Deck> deck = cardRepository.getDeck(u.get());
            String deckOutput;
            if(deck.isEmpty()) deckOutput = "empty"; else
                deckOutput= plain ? deck.get().formattedPlainCards() : deck.get().formattedCards();

            return new HTTPResponse("200","All cards in current deck:","text/plain",deckOutput);
        }
        catch (SQLException | IllegalStateException e) {
            return FailureResponse.getHTTPException(e);
        }
    }

    private List<String> extractCardIDs(HTTPRequest req) {
        String jsonString = req.getBody();

        JSONParser parser = new JSONParser();
        List<String> cardIDs = parser.readValueAsStringList(jsonString);
        return cardIDs;
    }

}