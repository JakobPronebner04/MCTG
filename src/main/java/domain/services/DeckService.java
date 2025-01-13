package domain.services;
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

    public synchronized HTTPResponse configureDeck(HTTPRequest req) {
        CardRepository cardRepository = CardRepository.getInstance();
        UserRepository userRepository = UserRepository.getInstance();

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
            String status = "500";

            if (e instanceof IllegalStateException) {
                status = "404";
            }

            if (e instanceof SQLException) {
                return new HTTPResponse(status, "DB error", "plain/text");
            }

            return new HTTPResponse(status, e.getMessage(), "plain/text");
        }
    }

    public HTTPResponse showDeck(HTTPRequest req, boolean plain) {
        CardRepository cardRepository = CardRepository.getInstance();
        UserRepository userRepository = UserRepository.getInstance();
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
            String status = "500";

            if(e instanceof IllegalStateException) {
                status = "404";
            }

            if(e instanceof SQLException) {
                return new HTTPResponse(status, "DB error","plain/text");
            }

            return new HTTPResponse(status, e.getMessage(),"plain/text");
        }
    }

    private List<String> extractCardIDs(HTTPRequest req) {
        String jsonString = req.getBody();

        JSONParser parser = new JSONParser();
        List<String> cardIDs = parser.readValueAsStringList(jsonString);
        return cardIDs;
    }

}