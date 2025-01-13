package presentation.http;

import application.handlers.*;
import application.interfaces.RequestHandleable;

import java.sql.SQLException;
import java.util.*;

public class HTTPRouter
{
    private final Map<String, RequestHandleable> routes;

    public HTTPRouter()
    {
        routes = new HashMap<>();
        initRoutes();
    }
    private void initRoutes()
    {
        this.routes.put("/users",new UserActionHandler());
        this.routes.put("/transactions/packages",new TransactionHandler());
        this.routes.put("/sessions",new UserSessionHandler());
        this.routes.put("/packages",new PackageHandler());
        this.routes.put("/cards",new CardHandler());
        this.routes.put("/deck",new DeckHandler());
        this.routes.put("/stats",new GameHandler());
        this.routes.put("/scoreboard",new ScoreboardHandler());
        this.routes.put("/battles",new GameHandler());
        this.routes.put("/tradings",new TradingHandler());
    }

    public HTTPResponse route(HTTPRequest request) throws SQLException
    {
        List<String>pathComponents = new ArrayList<>(Arrays.asList(request.getPath().split("/")));

        if(pathComponents.isEmpty())
        {
            return new HTTPResponse("404", "text/plain", "Path empty!","");
        }

        pathComponents.removeFirst();
        String prevPath = "";
        StringBuilder pathBuilder = new StringBuilder();
        for(String pathComponent : pathComponents) {
            if(pathComponent.contains("?")) pathComponent = pathComponent.split("\\?")[0];
            if(!prevPath.equals("users") && !prevPath.equals("tradings")) pathBuilder.append("/").append(pathComponent);
            prevPath = pathComponent;
        }


        RequestHandleable handler = routes.get(pathBuilder.toString());
        if(handler != null)
        {
            return handler.handle(request);
        }
        return new HTTPResponse("404", "text/plain", "Path not found!","");
    }
}
