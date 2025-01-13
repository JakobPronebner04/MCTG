package presentation.http;

public class HTTPResponse
{
    private final String status;
    private final String contentType;
    private final String statusMessage;
    private final String body;

    public HTTPResponse(String status, String statusMessage, String contentType, String body)
    {
                this.status = status;
                this.statusMessage = statusMessage;
                this.contentType = contentType;
                this.body = body;
    }
    public HTTPResponse(String status, String statusMessage, String contentType)
    {
        this(status, statusMessage, contentType, "");
    }

    public String getStatus()
    {
        return this.status;
    }
    public String getContentType()
    {
        return this.contentType;
    }
    public String getBody()
    {
        return this.body;
    }
    public String getStatusMessage()
    {
        return this.statusMessage;
    }
}
