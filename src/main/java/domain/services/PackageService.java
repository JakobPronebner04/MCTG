package domain.services;
import presentation.http.HTTPRequest;
import presentation.http.HTTPResponse;
import utils.json.JSONParser;
import domain.models.Card;
import domain.models.Package;
import domain.models.User;
import persistence.repositories.PackageRepository;
import persistence.repositories.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PackageService
{
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    public PackageService(UserRepository userRepository, PackageRepository packageRepository){
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }
    public HTTPResponse addPackage(HTTPRequest req)
    {
            try
            {
                Optional<Package> optPackage = getBodyAsPackage(req.getBody());
                optPackage.orElseThrow(() -> new IllegalStateException("Missing cards"));
                Optional<User> user = userRepository.getUserByToken(req.getToken());
                if(user.isEmpty() || !user.get().getToken().equals("admin-mtcgToken"))
                {
                    return new HTTPResponse("404","User not found or wrong user!","text/plain");
                }
                if(packageRepository.addPackage(optPackage.get()))
                {
                    return new HTTPResponse("200","Successfully created package!","text/plain");
                }

                return new HTTPResponse("404","Could not create Package!","text/plain");
            }
            catch (SQLException | IllegalStateException e)
            {
                String status = "500";

                if(e instanceof IllegalStateException)
                {
                    status = "404";
                }

                if(e instanceof SQLException)
                {
                    return new HTTPResponse(status, "DB error","plain/text");
                }

                return new HTTPResponse(status, e.getMessage(),"plain/text");
            }
    }

    public HTTPResponse aquirePackage(HTTPRequest req)
    {
        try
        {
            Optional<User> user = userRepository.getUserByToken(req.getToken());
            if(user.isEmpty()){
                return new HTTPResponse("404","User not found! Package could not be bought","text/plain");
            }

            User u = user.get();
            if(u.getCoins() >= 5)
            {
                if(packageRepository.getPackage(u))
                {
                    userRepository.updateCoins(u);
                    return new HTTPResponse("200","Successfully bought new package!","text/plain");
                }
            }

            return new HTTPResponse("404","Could not buy package!","text/plain");
        }
        catch (SQLException | IllegalStateException e)
        {
            String status = "500";

            if(e instanceof IllegalStateException)
            {
                status = "404";
            }

            if(e instanceof SQLException)
            {
                return new HTTPResponse(status, "DB error","plain/text");
            }

            return new HTTPResponse(status, e.getMessage(),"plain/text");
        }
    }

    public Optional<Package> getBodyAsPackage(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        List<Card> cards = jsonParser.readValueAsList(jsonString, Card.class);
        return Optional.of(new Package(cards));
    }
}