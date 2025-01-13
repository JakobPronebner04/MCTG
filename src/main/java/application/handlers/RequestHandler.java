package application.handlers;

import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import presentation.http.HTTPRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements Runnable
{
    private static final Logger logger = LogManager.getLogger(RequestHandler.class);
    private final Socket socket;
    private final HTTPRouter router;

    public RequestHandler(Socket socket, HTTPRouter router) {
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        try
        {
            handleRequest();
        }
        catch (IOException e)
        {
            logger.error(e);
        }
    }

    private void handleRequest() throws IOException
    {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        try
        {
            HTTPRequest request = formatRequest();
            if (request == null) return;

            HTTPResponse response = router.route(request);

            out.println("HTTP/1.1 " + response.getStatus() + " " + response.getStatusMessage());
            out.println("Content-Type: " + response.getContentType());
            out.println("Content-Length: " + response.getBody().length());
            out.println();
            out.println(response.getBody());
            out.flush();

        }
        catch (IOException e)
        {
            logger.error(e);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                out.close();
                socket.close();
            }
            catch (IOException e)
            {
                logger.error(e);
            }
        }
    }

    private HTTPRequest formatRequest() throws IOException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = in.readLine();
        if (requestLine == null) return null;

        Map<String, String> headers = new HashMap<>();
        String header;
        while (!(header = in.readLine()).isEmpty()) {
            String[] parts = header.split(": ", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                headers.put(key, value);
            }
        }

        int contentLength = 0;
        if (headers.containsKey("Content-Length")) {
            try {
                contentLength = Integer.parseInt(headers.get("Content-Length"));
            } catch (NumberFormatException e) {
                System.err.println("Invalid Content-Length format");
            }
        }

        StringBuilder body = new StringBuilder();
        if (contentLength > 0)
        {
            for (int i = 0; i < contentLength; i++)
            {
                body.append((char) in.read());
            }
        }

        return new HTTPRequest(requestLine,headers, body.toString());
    }
}
