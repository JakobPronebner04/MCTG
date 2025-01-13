package application.interfaces;

import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;

import java.sql.SQLException;

public interface RequestHandleable
{
    HTTPResponse handle(HTTPRequest request) throws SQLException;
}
