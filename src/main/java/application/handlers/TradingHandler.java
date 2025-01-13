package application.handlers;

import application.interfaces.RequestHandleable;
import domain.services.TradeService;
import persistence.repositories.TradeRepository;
import persistence.repositories.UserRepository;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;

public class TradingHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req) {
        TradeService tradeService = new TradeService(UserRepository.getInstance(), TradeRepository.getInstance());

        if(req.getMethod().equalsIgnoreCase("POST")){
            if(req.getPath().split("/").length == 3) {
                return tradeService.trading(req);
            }
            return tradeService.createTrade(req);
        }

        if(req.getMethod().equalsIgnoreCase("GET"))
            return tradeService.showTrades(req);
        return new HTTPResponse("404","Wrong Request","text/plain");
    }
}