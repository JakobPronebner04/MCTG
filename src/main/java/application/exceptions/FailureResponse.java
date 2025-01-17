package application.exceptions;

import presentation.http.HTTPResponse;

import java.sql.SQLException;

public class FailureResponse {
    public static HTTPResponse getHTTPException(Exception e) {
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
