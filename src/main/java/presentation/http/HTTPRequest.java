package presentation.http;

import java.util.Map;

public class HTTPRequest
{
    private final String method;
    private final String path;
    private final String body;
    private String query;
    private final Map<String,String> headers;

    public HTTPRequest(String requestLine, Map<String,String> headers,String body)
    {
        String[] parts = requestLine.split(" ");
        this.method = parts[0];
        this.path = parts[1];
        if(path.contains("?")) this.query = path.split("\\?")[1]; else this.query = "";
        this.body = body;
        this.headers = headers;
    }

    public String getMethod()
    {
        return this.method;
    }
    public String getPath()
    {
        return this.path;
    }
    public String getBody()
    {
        return this.body;
    }
    public String getToken() {
        String authorizationHeader = this.headers.get("Authorization");
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return "";
        }
        String[] parts = authorizationHeader.split("Bearer ");
        return parts.length > 1 ? parts[1] : "";
    }
    public String getQuery(){return this.query;}
}
