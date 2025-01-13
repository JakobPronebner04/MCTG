package presentation.server;

import application.handlers.RequestHandler;
import presentation.http.HTTPRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    private static final Logger logger = LogManager.getLogger(Server.class);
    private static final int PORT = 10001;
    private static final int THREAD_POOL_SIZE = 10;
    private final HTTPRouter router = new HTTPRouter();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void start()
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            logger.info("Server is listening on port " + PORT);

            while (true)
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    threadPool.execute(new RequestHandler(socket, router));
                }
                catch (IOException e)
                {
                    logger.error(e);
                }
            }
        }
        catch (IOException e)
        {
            logger.error(e);
        }
        finally
        {
            threadPool.shutdown();
        }
    }
}