package application.handlers;

import application.interfaces.RequestHandleable;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;

public class StatsHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req)
    {
        return new HTTPResponse("","","","");
    }
}
