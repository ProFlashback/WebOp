package me.jayfella.webop.Core;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.jayfella.webop.WebOpContext;

public final class SessionManager
{
    private final WebOpContext context;

    private List<String> whitelistedUsers = new ArrayList<>();
    private final List<WebUser> loggedInUsers = new ArrayList<>();

    private final String salt;

    public SessionManager(WebOpContext context)
    {
        this.context = context;

        salt = generateSalt();
    }

    private String generateSalt()
    {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public boolean isWhitelisted(String name)
    {
        for (String str : whitelistedUsers)
        {
            if (str.equalsIgnoreCase(name))
            {
                return true;
            }
        }

        return false;
    }

    public void addToWhitelist(String name)
    {
        if (!isWhitelisted(name))
        {
            this.whitelistedUsers.add(name);
        }

        context.getPluginSettings().getFileConfig().set("whitelist", whitelistedUsers);
        context.getPlugin().saveConfig();
    }

    public void setWhitelist(List<String> list)
    {
        this.whitelistedUsers = list;
    }

    public void removeFromWhitelist(String name)
    {
        if (isWhitelisted(name))
            this.whitelistedUsers.remove(name);

        if (isLoggedIn(name))
            logUserOut(name);

        context.getPluginSettings().getFileConfig().set("whitelist", whitelistedUsers);
        context.getPlugin().saveConfig();
    }

    public String[] getWhitelistedUsers()
    {
        return whitelistedUsers.toArray(new String[whitelistedUsers.size()]);
    }

    public void addLoggedInUser(InetAddress ip, String username)
    {
        boolean alreadyAdded = false;

        for (WebUser w : loggedInUsers)
        {
            if (w.getUsername().equalsIgnoreCase(username))
            {
                alreadyAdded = true;
            }
        }

        if (!alreadyAdded)
        {
            this.loggedInUsers.add(new WebUser(ip, username));
        }
    }

    public void logUserOut(WebUser user)
    {
        loggedInUsers.remove(user);
    }

    public void logUserOut(String username)
    {
        Iterator<WebUser> iterator = loggedInUsers.iterator();

        while (iterator.hasNext())
        {
            WebUser wu = iterator.next();

            if (wu.getUsername().equalsIgnoreCase(username))
            {
                iterator.remove();
                break;
            }
        }
    }

    public boolean isLoggedIn(String username)
    {
        for (WebUser w : loggedInUsers)
        {
            if (w.getUsername().equalsIgnoreCase(username))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isLoggedIn(InetAddress address)
    {
        for (WebUser w : loggedInUsers)
        {
            if (w.getAddress().getHostAddress().equalsIgnoreCase(address.getHostAddress()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isValidCookie(HttpExchange he)
    {
        WebUser webUser = getWebUser(he.getRemoteAddress().getAddress());
        if (webUser == null) return false;

        Headers headers = he.getRequestHeaders();
        List<String> cookies = headers.get("Cookie");

        String cookieVal = "";

        if (cookies == null)
            return false;

        for (String str : cookies)
        {
            if (str.contains("session="))
            {
                cookieVal = str;
            }
        }

        int sessionIndex = cookieVal.indexOf("session=");

        if (sessionIndex == -1 || sessionIndex + 8 > cookieVal.length())
            return false;

        String cookieHash = cookieVal.substring(sessionIndex);

        int breakIndex = cookieHash.indexOf(";");

        if (breakIndex != -1)
        {
            cookieHash = cookieHash.substring(0, breakIndex);
        }

        cookieHash = cookieHash.replace("session=", "").trim();

        String hash = getHash(webUser.getUsername());

        return cookieHash.equals(hash);
    }

    public boolean isAuthorised(HttpExchange he)
    {

        if (!this.isLoggedIn(he.getRemoteAddress().getAddress()))
            return false;

        if (!this.isValidCookie(he))
            return false;

        if (!this.isWhitelisted(this.getWebUser(he.getRemoteAddress().getAddress()).getUsername()))
            return false;

        return true;
    }

    public WebUser getWebUser(InetAddress address)
    {
        for (WebUser w : loggedInUsers)
        {
            if (w.getAddress().getHostAddress().equalsIgnoreCase(address.getHostAddress()))
            {
                return w;
            }
        }

        return null;
    }

    public String getHash(String str)
    {
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");

            String hash = str + salt;

            byte[] hashOne = sha.digest(hash.getBytes());
            return hexEncode(hashOne);
        }
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(SessionManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private String hexEncode( byte[] aInput)
    {
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'};
        for (int idx = 0; idx < aInput.length; ++idx)
        {
            byte b = aInput[idx];
            result.append( digits[ (b&0xf0) >> 4 ] );
            result.append( digits[ b&0x0f] );
        }

        return result.toString();
    }

}
