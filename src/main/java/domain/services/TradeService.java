package domain.services;

import domain.enums.TradeState;
import domain.models.Card;
import domain.models.Trade;
import domain.models.User;
import persistence.repositories.TradeRepository;
import persistence.repositories.UserRepository;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;

import java.sql.SQLException;
import java.util.Optional;

public class TradeService {
    private final UserRepository userRepository;
    private final TradeRepository tradeRepository;

    public TradeService(UserRepository userRepository, TradeRepository tradeRepository) {
        this.userRepository = userRepository;
        this.tradeRepository = tradeRepository;
    }

    public HTTPResponse createTrade(HTTPRequest req)
    {
        try
        {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(()-> new IllegalArgumentException("User not found"));

            JSONParser parser = new JSONParser();
            Trade trade = parser.readValue(req.getBody(), Trade.class);
            if(tradeRepository.addTrade(user.get(),trade))
                return new HTTPResponse("200","Added your offer!","text/plain");
            return new HTTPResponse("404","Could not add your offer!","text/plain");
        }catch (SQLException | IllegalStateException e) {
            String status = "500";

            if(e instanceof IllegalStateException)
            {
                status = "404";
            }

            if(e instanceof SQLException)
            {
                return new HTTPResponse(status, "DB error","plain/text");
            }

            return new HTTPResponse(status, e.getMessage(),"plain/text");
        }
    }

    public HTTPResponse showTrades(HTTPRequest req)
    {
        try
        {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(()->new IllegalStateException("User not found!"));

            String tradeOutput = tradeRepository.getTrades();
            return new HTTPResponse("200","All available trades:","text/plain",tradeOutput);
        }catch (SQLException | IllegalStateException e) {
            String status = "500";

            if(e instanceof IllegalStateException)
            {
                status = "404";
            }

            if(e instanceof SQLException)
            {
                return new HTTPResponse(status, "DB error","plain/text");
            }

            return new HTTPResponse(status, e.getMessage(),"plain/text");
        }
    }

    public HTTPResponse trading(HTTPRequest req) {
        String[] pathParts = req.getPath().split("/");
        String tradeId = (pathParts.length > 2) ? pathParts[2] : "";
        String cardToTrade = req.getBody().replace("\"","");
        try {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(()->new IllegalStateException("User not found!"));

            Optional<Trade> tradeOffer = tradeRepository.getTradeById(tradeId);
            tradeOffer.orElseThrow(()->new IllegalStateException("Could not find tradeID: "+ tradeId));

            Optional<Card> cardToGive = tradeRepository.getCardToTradeById(user.get(),cardToTrade);
            cardToGive.orElseThrow(()->new IllegalStateException("Could not find given cardID: " + cardToTrade));

            TradeState state = tradeRepository.startTrade(user.get(),tradeOffer.get(),cardToGive.get());

            if(state.equals(TradeState.NO_SUCCESS)) return new HTTPResponse("400","Trade was not succesfull due not fullfilled requirements!","text/plain");
            if(state.equals(TradeState.SAME_USER)) return new HTTPResponse("400","User cannot be offerer and accepter of trade!","text/plain");

            return new HTTPResponse("200","Trade was successfull! ","text/plain");
        } catch (SQLException | IllegalStateException e) {
            String status = "500";

            if(e instanceof IllegalStateException)
            {
                status = "404";
            }

            if(e instanceof SQLException)
            {
                return new HTTPResponse(status, "DB error","plain/text");
            }

            return new HTTPResponse(status, e.getMessage(),"plain/text");
        }
    }

    public HTTPResponse deleteTrade(HTTPRequest req){
        String[] pathParts = req.getPath().split("/");
        String tradeId = (pathParts.length > 2) ? pathParts[2] : "";
        if(tradeId.isEmpty()) return new HTTPResponse("400","No tradeID provided!","text/plain");

        try
        {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            user.orElseThrow(()->new IllegalStateException("User not found!"));
            if(!tradeRepository.removeOffer(user.get(),tradeId)) throw new IllegalStateException("Trade could not be removed!");
            return new HTTPResponse("200","Succesfully removed your offer!","text/plain");
        }catch (SQLException | IllegalStateException e) {
            String status = "500";

            if(e instanceof IllegalStateException)
            {
                status = "404";
            }

            /*if(e instanceof SQLException)
            {
                return new HTTPResponse(status, "DB error","plain/text");
            }*/

            return new HTTPResponse(status, e.getMessage(),"plain/text");
        }
    }
}