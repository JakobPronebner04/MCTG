package application.handlers;

import application.interfaces.RequestHandleable;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import persistence.repositories.PackageRepository;
import persistence.repositories.UserRepository;
import domain.services.PackageService;

public class PackageHandler implements RequestHandleable
{
    @Override
    public HTTPResponse handle(HTTPRequest req)
    {
        PackageService service = new PackageService(UserRepository.getInstance(),
                                                    PackageRepository.getInstance());
        if(req.getMethod().equals("POST"))
        {
            return service.addPackage(req);
        }
        return new HTTPResponse("400","Wrong Request","text/plain","");
    }
}
