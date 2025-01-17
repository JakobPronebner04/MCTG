package domain.services;

import application.exceptions.FailureResponse;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import domain.models.Deck;
import domain.models.User;
import persistence.repositories.CardRepository;
import persistence.repositories.GameRepository;
import persistence.repositories.UserRepository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameService {
    private final static BlockingQueue<User> lobby = new LinkedBlockingQueue<>(2);
    private final int TIMEOUT = 10;
    public synchronized HTTPResponse battle(HTTPRequest request) {
        UserRepository userRepository = UserRepository.getInstance();
            try {
                Optional<User> user = userRepository.getUserByToken(request.getToken());
                user.orElseThrow(() -> new IllegalStateException("User not found"));
                if (!lobby.offer(user.get())) {
                    return new HTTPResponse("400","Lobby full","text/plain");
                }

                if (lobby.size() == 1) {

                    synchronized (lobby) {
                        lobby.wait(TIMEOUT * 1000);
                    }

                    if (lobby.size() == 1) {
                        lobby.take();
                        return new HTTPResponse("400","No opponents currently available","text/plain");
                    }
                }
                return startBattle();

            } catch ( SQLException | IllegalStateException | InterruptedException e){
                return FailureResponse.getHTTPException(e);
            }
        }

    private synchronized HTTPResponse startBattle() throws SQLException {

        User user1 = lobby.poll();
        User user2 = lobby.poll();

        if (user1 == null || user2 == null) {
            return new HTTPResponse("500", "Unexpected error: Lobby state invalid", "text/plain");
        }

        if (user1.equals(user2)) {
            return new HTTPResponse("400", "You can't fight yourself", "text/plain");
        }

        CardRepository cardRepository = CardRepository.getInstance();
        Optional<Deck> deck_user1 = cardRepository.getDeck(user1);
        Optional<Deck> deck_user2 = cardRepository.getDeck(user2);

        if (deck_user1.isEmpty() || deck_user2.isEmpty()) {
            return new HTTPResponse("404", "One or both players have no valid deck", "text/plain");
        }

        user1.setDeck(deck_user1.get());
        user2.setDeck(deck_user2.get());

        GameRepository gameRepository = GameRepository.getInstance();
        String outcome = gameRepository.start(user1, user2);

        synchronized (lobby) {
            lobby.notify();
        }
        lobby.remove(user1);
        lobby.remove(user2);

        return new HTTPResponse("200", "Battlelog:", "text/plain", outcome);
    }

    public HTTPResponse showStats(HTTPRequest request) {
        UserRepository userRepository = UserRepository.getInstance();
        GameRepository gameRepository = GameRepository.getInstance();
        try{
            Optional<User> user = userRepository.getUserByToken(request.getToken());
            user.orElseThrow(() -> new IllegalStateException("User not found"));
            String statsOutput = gameRepository.getStats(user.get());
            return new HTTPResponse("200", "Current user stats:", "text/plain", statsOutput);
        }
        catch ( SQLException | IllegalStateException e){
            return FailureResponse.getHTTPException(e);
        }
    }

    public HTTPResponse showScoreboard(HTTPRequest req) {
        UserRepository userRepository = UserRepository.getInstance();
        GameRepository gameRepository = GameRepository.getInstance();
        try{
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(() -> new IllegalStateException("User not found"));
            String scores = gameRepository.getScores();
            return new HTTPResponse("200", "Highscores", "text/plain", scores);
        }
        catch ( SQLException | IllegalStateException e){
            return FailureResponse.getHTTPException(e);
        }
    }
}
