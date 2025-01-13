package domain.models;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

public class Token
{
    private static final int EXPIRATION_TIME = 60 * 15 * 1000;
    private final Key key;

    public Token()
    {
        this.key =  Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
    public String generateTokenStr(String username)
    {
        return username + "-mtcgToken";
    }

    public String generateRandomToken()
    {
        String randomId = UUID.randomUUID().toString();

        Date now = new Date();

        return Jwts.builder()
                .setSubject(randomId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean validateToken(String token)
    {

            return !isTokenExpired(token);
    }

    private static boolean isTokenExpired(String token)
    {
        return false;
    }

    private static void refreshTokenValidation(String token) {
        if (validateToken(token))
        {

        }
    }

    public void invalidateToken(String token) {

    }
}
