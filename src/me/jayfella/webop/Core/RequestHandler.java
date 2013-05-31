package me.jayfella.webop.Core;

import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import me.jayfella.webop.WebOpContext;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class RequestHandler
{
    private final WebOpContext context;

    public RequestHandler(WebOpContext context)
    {
        this.context = context;
    }

    public void handleRequest(HttpExchange he) throws IOException
    {
        switch (he.getRequestMethod())
        {
            case "GET":
            {
                handleGET(he);
                break;
            }
            case "POST":
            {
                handlePOST(he);
                break;
            }
            default: // unknown method
            {
                String pageResponse = "Error 503";

                he.sendResponseHeaders(503, pageResponse.length());
                try (OutputStream os = he.getResponseBody())
                {
                    os.write(pageResponse.getBytes());
                }
            }
        }
    }

    private void handleGET(HttpExchange he) throws IOException
    {
        byte[] pageData = new byte[0];
        int pageResponse;

        String requestedPath = he.getRequestURI().getPath().replace("/", "");

        switch (requestedPath)
        {
            case "":
            {
                pageData = context.getPageHandler().getIndexPage(he).getBytes();

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                break;
            }

            case "viewpage.php":
            {
                pageData = servePage(he).getBytes();

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                break;
            }
            case "viewdata.php":
            {
                pageData = serveData(he).getBytes();

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                break;
            }
            case "image.php":
            {
                pageData = serveImage(he);

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().set("Content-Encoding", "image/png; charset=utf-8");

                break;
            }
            case "jscript.php":
            {
                pageData = serveJS(he).getBytes();

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().add("Content-Type", "text/javascript; charset=utf-8");

                break;
            }
            case "stylesheet.php":
            {
                pageData = serveCss(he).getBytes();

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().add("Content-Type", "text/css; charset=utf-8");

                break;
            }
            case "logout.php":
            {
                WebUser wu = context.getSessionManager().getWebUser(he.getRemoteAddress().getAddress());
                context.getSessionManager().logUserOut(wu);

                pageData = context.getPageHandler().getLoginPage(new String[0], new String[0]).getBytes();

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            }
        }

        if (pageData == null || pageData.length == 0)
        {
            pageData = "Document requested has not been found".getBytes();

            he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            pageResponse = HttpURLConnection.HTTP_NOT_FOUND;
        }
        else
        {
            pageResponse = HttpURLConnection.HTTP_OK;
        }

        try
        {
            byte[] content = gzip(pageData);

            he.getResponseHeaders().set("Content-Encoding", "gzip");
            he.sendResponseHeaders(pageResponse, content.length);
            he.getResponseBody().write(content);
        }
        catch(Exception ex)
        {
            context.getPlugin().getLogger().log(Level.INFO, "Connection closed.");
        }
        finally
        {
            he.close();
        }
    }

    private void handlePOST(HttpExchange he) throws IOException
    {
        byte[] pageData;
        int pageResponse;

        String requestedPath = he.getRequestURI().getPath().replace("/", "");

        // if not logged in, and not trying to login, show login page.
        if (!context.getSessionManager().isLoggedIn(he.getRemoteAddress().getAddress()))
        {
            if (requestedPath.equalsIgnoreCase("login.php"))
            {
                pageData = attemptLogin(he).getBytes();

                if (pageData != null && pageData.length > 0)
                    he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            }
            else
            {
                pageData = new byte[0];
            }
        }
        else
        {
            switch (requestedPath)
            {
                // for logging in from another location and didnt logout
                case "login.php":
                {
                    pageData = attemptLogin(he).getBytes();

                    if (pageData != null && pageData.length > 0)
                        he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                }
                case "console.php":
                {
                    pageData = sendCommand(he).getBytes();

                    if (pageData != null && pageData.length > 0)
                        he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                    break;
                }
                case "searchlog.php":
                {
                    pageData = searchLog(he).getBytes();

                    if (pageData != null && pageData.length > 0)
                        he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                    break;
                }
                case "remwhitelist.php":
                {
                    pageData = removeFromWhitelist(he).getBytes();

                    if (pageData != null && pageData.length > 0)
                        he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                    break;
                }
                case "addwhitelist.php":
                {
                    pageData = addToWhitelist(he).getBytes();

                    if (pageData != null && pageData.length > 0)
                        he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                    break;
                }
                default:
                {
                    pageData = new byte[0];
                    break;
                }
            }
        }

        if (pageData == null || pageData.length == 0)
        {
            pageData = "Document requested has not been found".getBytes();

            he.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            pageResponse = HttpURLConnection.HTTP_NOT_FOUND;
        }
        else
        {
            pageResponse = HttpURLConnection.HTTP_OK;
        }

        try
        {
            byte[] content = gzip(pageData);

            he.getResponseHeaders().set("Content-Encoding", "gzip");
            he.sendResponseHeaders(pageResponse, content.length);
            he.getResponseBody().write(content);
        }
        catch(Exception ex)
        {
            context.getPlugin().getLogger().log(Level.INFO, "Connection closed.");
        }
        finally
        {
            he.close();
        }


        /*he.sendResponseHeaders(200, pageResponse.length());

        try (OutputStream os = he.getResponseBody())
        {
            os.write(pageResponse.getBytes());
        }

        he.close();*/


    }

    private byte[] gzip(byte[] data) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(bytes))
        {
            out.write(data);
        }

        return bytes.toByteArray();
    }

    private String addToWhitelist(HttpExchange he)
    {
        if (!context.getSessionManager().isAuthorised(he))
        {
            return "";
        }

        WebUser wu = context.getSessionManager().getWebUser(he.getRemoteAddress().getAddress());
        OfflinePlayer offlinePlayer = context.getPlugin().getServer().getOfflinePlayer(wu.getUsername());

        if (offlinePlayer == null)
        {
            return "";
        }
        else
        {
            if (!offlinePlayer.isOp())
            {
                return "";
            }
        }

        String postParams;

        try (InputStreamReader inStream = new InputStreamReader(he.getRequestBody()))
        {
            try (BufferedReader bufferedReader = new BufferedReader(inStream))
            {
                postParams = bufferedReader.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }

        Map<String, String> vars = parsePostResponse(postParams);

        String query = vars.get("addPlayers");

        try
        {
            query = URLDecoder.decode(query, "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }

        String[] players = query.trim().split(",");

        for (int i = 0; i < players.length; i++)
        {
            players[i] = players[i].trim();
        }

        for (String name : players)
        {
            context.getSessionManager().addToWhitelist(name);
        }

        return "OK";
    }

    private String removeFromWhitelist(HttpExchange he)
    {
        if (!context.getSessionManager().isAuthorised(he))
        {
            return "";
        }

        WebUser wu = context.getSessionManager().getWebUser(he.getRemoteAddress().getAddress());
        OfflinePlayer offlinePlayer = context.getPlugin().getServer().getOfflinePlayer(wu.getUsername());

        if (offlinePlayer == null)
        {
            return "";
        }
        else
        {
            if (!offlinePlayer.isOp())
            {
                return "";
            }
        }

        String postParams;

        try (InputStreamReader inStream = new InputStreamReader(he.getRequestBody()))
        {
            try (BufferedReader bufferedReader = new BufferedReader(inStream))
            {
                postParams = bufferedReader.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }

        Map<String, String> vars = parsePostResponse(postParams);

        String query = vars.get("removePlayers");

        try
        {
            // removePlayers=vixengirl%2C+
            query = URLDecoder.decode(query, "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }

        String[] players = query.trim().split(",");

        for (int i = 0; i < players.length; i++)
        {
            players[i] = players[i].trim();
        }

        for (String name : players)
        {
            context.getSessionManager().removeFromWhitelist(name);
        }

        return "OK";
    }

    private String getConsoleOutput(HttpExchange he)
    {
        // if (!context.getSessionManager().isLoggedIn(he.getRemoteAddress().getAddress()))
        if (!context.getSessionManager().isAuthorised(he))
        {
            return "";
        }

        return context.consoleOutput;
    }

    private Map<String, String> parsePostResponse(String query)
    {
        if (query == null || query.length() < 1)
        {
            return new HashMap<>();
        }

        Map<String, String> results = new HashMap<>();

        String[] pairs = query.split("&");

        for (String pair : pairs)
        {
            String[] param = pair.split("=");

            if (param.length == 2)
                results.put(param[0], param[1]);
            else
                results.put(param[0], "");
        }

        return results;
    }

    private String servePage(HttpExchange he)
    {
        String query = he.getRequestURI().getQuery();

        int index = query.indexOf("&");

        if (index > 0)
        {
            String unwanted = query.substring(index);
            query = query.replace(unwanted, "");
        }

        if (query == null)
            return "";

        switch (query)
        {
            case "login":
            {
                return context.getPageHandler().getLoginPage(new String[0], new String[0]);
            }

            case "index":
            {
                return context.getPageHandler().getIndexPage(he);
            }
            case "whitelist":
            {
                return context.getPageHandler().getWhitelistPage(he);
            }
        }

        return "";
    }

    private byte[] serveImage(HttpExchange he)
    {
        String query = he.getRequestURI().getQuery();

        InputStream inp = getClass().getClassLoader().getResourceAsStream("me/jayfella/webop/resources/Images/" + query);

        int bytesRead;
        byte[] buffer = new byte[8192];
        byte[] data = new byte[0];

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream())
        {
            while ((bytesRead = inp.read(buffer)) != -1)
            {
                bytes.write(buffer, 0, bytesRead);
            }

            data = bytes.toByteArray();
        }
        catch (Exception ex)
        {
            context.getPlugin().getLogger().log(Level.SEVERE, "Error reading image");
        }

        return data;
    }

    private String serveJS(HttpExchange he)
    {
        String query = he.getRequestURI().getQuery();

        return serveResource("me/jayfella/webop/resources/JavaScript/" + query);
    }

    private String serveCss(HttpExchange he)
    {
        String query = he.getRequestURI().getQuery();
        return serveResource("me/jayfella/webop/resources/Css/" + query);
    }

    private String serveData(HttpExchange he)
    {
        String query = he.getRequestURI().getQuery();
        String unwanted = query.substring(query.indexOf("&"));
        query = query.replace(unwanted, "");

        switch (query)
        {
            case "widgetdata":
            {
                return context.getWidgetHandler().generateWidgetDataPage(he);
            }
            case "consoledata":
            {
                return getConsoleOutput(he);
            }
            default:
            {
                return "";
            }

        }
    }

    private String serveResource(String path)
    {
        String output = "";

        // example path: "me/jayfella/webop/resources/jquery.flot.min.js"

        try(InputStream inp = getClass().getClassLoader().getResourceAsStream(path))
        {
            try(BufferedReader rd = new BufferedReader(new InputStreamReader(inp)))
            {
                String s;

                while (null != (s = rd.readLine()))
                {
                    output += s + "\n";
                }
            }
        }
        catch (Exception ex)
        {
            return "";
        }

        return output;
    }

    private String sendCommand(HttpExchange he)
    {
        String postParams;

        try (InputStreamReader inStream = new InputStreamReader(he.getRequestBody()))
        {
            try (BufferedReader bufferedReader = new BufferedReader(inStream))
            {
                postParams = bufferedReader.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }

        Map<String, String> vars = parsePostResponse(postParams);

        WebUser wu = context.getSessionManager().getWebUser(he.getRemoteAddress().getAddress());
        Player player = context.getPlugin().getServer().getPlayerExact(wu.getUsername());

        String useConsole = vars.get("asConsole");

        String cmd;
        try
        {
            cmd = URLDecoder.decode(vars.get("consoleText"), "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return "Exception Error";
        }

        boolean executeAsConsole = (useConsole != null && useConsole.equalsIgnoreCase("on"));

        if (!executeAsConsole)
        {
            if (player == null)
            {
                context.getPlugin().getLogger().log(Level.WARNING, "Player {0} tried to issue a command whilst not logged in!", wu.getUsername());
                return "Player not logged into game.";
            }
            else
            {
                player.chat(cmd);
                return "OK";
            }
        }
        else
        {
            boolean isOperator = (player != null)
                    ? player.isOp()
                    : context.getConsole().getServer().getOfflinePlayer(wu.getUsername()).isOp();

            if (!isOperator)
            {
                context.getPlugin().getLogger().log(Level.WARNING, "Player {0} tried to issue a command as console without required permission.", wu.getUsername());
                return "Attempted console command refused.";
            }
            else
            {
                context.getPlugin().getLogger().log(Level.INFO, "Player {0} issued command from console: {1}", new Object[]{wu.getUsername(), cmd});
                context.getPlugin().getServer().dispatchCommand(context.getPlugin().getServer().getConsoleSender(), cmd);
                return "OK";
            }


        }

    }

    private String searchLog(HttpExchange he)
    {
        File file = new File("server.log");

        String postParams;

        try (InputStreamReader inStream = new InputStreamReader(he.getRequestBody()))
        {
            try (BufferedReader bufferedReader = new BufferedReader(inStream))
            {
                postParams = bufferedReader.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }

        Map<String, String> vars = parsePostResponse(postParams);

        String searchTerm;
        String timeFrame;

        // URLDecoder.decode(vars.get("consoleText"), "UTF-8");
        List<String> results = new ArrayList<>();

        try
        {
            searchTerm = URLDecoder.decode(vars.get("searchTerm"), "UTF-8").trim();
            timeFrame = URLDecoder.decode(vars.get("timeFrame"), "UTF-8").trim();

            Date sinceDate = new Date(0);

            if (!timeFrame.isEmpty())
            {
                long since = context.parseDateDiff(timeFrame, Calendar.getInstance().getTime(), false);
                sinceDate = new Date(since);
            }

            if (searchTerm.length() >= 3)
            {
                try (BufferedReader in = new BufferedReader(new FileReader(file)))
                {
                    while (in.ready())
                    {
                        String s = in.readLine();

                        // parse le data
                        int index = s.indexOf("[");

                        if (index < 0) continue;

                        String dateString = s.substring(0, index -1).trim();

                        Date date;

                        try
                        {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
                        }
                        catch (ParseException ex)
                        {
                            continue;
                        }

                        if (date.before(sinceDate))
                            continue;

                        String parseMe = s.toLowerCase();

                        if (parseMe.contains(searchTerm.toLowerCase()))
                        {
                            String string = s;

                            /*string = StringEscapeUtils.escapeJava(string);

                            string = string.replace("\u001B[m", ""); // &0
                            string = string.replace("\u001B[0;34;22m", ""); // &1
                            string = string.replace("\u001B[0;32;22m", ""); // &2
                            string = string.replace("\u001B[0;36;22m", ""); // &3
                            string = string.replace("\u001B[0;31;22m", ""); // &4
                            string = string.replace("\u001B[0;35;22m", ""); // &5
                            string = string.replace("\u001B[0;33;22m", ""); // &6
                            string = string.replace("\u001B[0;37;22m", ""); // &7
                            string = string.replace("\u001B[0;30;1m", ""); // &8
                            string = string.replace("\u001B[0;34;1m", ""); // &9
                            string = string.replace("\u001B[0;32;1m", ""); // &A
                            string = string.replace("\u001B[0;36;1m", ""); // &b
                            string = string.replace("\u001B[0;31;1m", ""); // &c
                            string = string.replace("\u001B[0;35;1m", ""); // &d
                            string = string.replace("\u001B[0;33;1m", ""); // &e
                            string = string.replace("\u001B[0;37;1m", ""); // &f*/



                            results.add(string + "<br>");
                        }

                        // if (s.contains(searchTerm))
                            // results.add(s + "<br>");

                        // limit
                        if (results.size() >= 50)
                            break;

                    }
                }
            }
            else
            {
                results.add("Search term too short.");
            }
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        StringBuilder output = new StringBuilder();

        if (results.size() < 1)
        {
            output.append("No results found.");
        }
        else
        {
            output
                    .append("<br><strong>Displaying ")
                    .append(results.size())
                    .append(" (max 50) results</strong><br><br>");

            for (String s : results)
            {
                output.append(s);
            }
        }

        output
                .append("<br>").append("<br>")
                .append("<button class='blueButton' id='clearSearchResults' value='Clear Results'>Clear Results</button>")
                .append("<script type='text/javascript'>")
                .append("$('#clearSearchResults').click(function() { $('#searchOutput').empty(); });")
                .append("</script>")
                .toString();

        return output.toString();
    }

    private String attemptLogin(HttpExchange he)
    {
        String postParams;

        try (InputStreamReader inStream = new InputStreamReader(he.getRequestBody()))
        {
            try (BufferedReader bufferedReader = new BufferedReader(inStream))
            {
                postParams = bufferedReader.readLine();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
        }

        Map<String, String> vars = parsePostResponse(postParams);

        String valid = isValidLoginDetails(vars.get("username"), vars.get("password"));

        if (valid.isEmpty())
        {
            return "bad login";
        }
        else
        {
            if (!context.getSessionManager().isWhitelisted(valid))
            {
                return "Not Whitelisted";
            }

            // set cookies
            he.getResponseHeaders().add("Set-Cookie", "domain=" + he.getLocalAddress().getAddress().getCanonicalHostName());
            he.getResponseHeaders().add("Set-Cookie", "session=" + context.getSessionManager().getHash(valid));
            he.getResponseHeaders().add("Set-Cookie", "loadcrap=" + "nothing");

            this.context.getSessionManager().addLoggedInUser(he.getRemoteAddress().getAddress(), valid);
            return "OK";
        }
    }

    private String isValidLoginDetails(String username, String password)
    {
        try
        {
            URL mojangUrl = new URL("https://login.minecraft.net/?user=" + username + "&password=" + password + "&version=13");

            URLConnection urlConn = mojangUrl.openConnection();
            String resp = "";

            try(BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream())))
            {
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                {
                    resp += inputLine;
                }
            }

            String[] mojangResp = resp.split(":");

            if (mojangResp.length != 5) { return ""; }

            // String currentVer = mojangResp[0];
            // String downloadTicket = mojangResp[1];
            String gameName = mojangResp[2];
            String sessionId = mojangResp[3];
            // String UID = mojangResp[4];

            String serverId = context.getPlugin().getServer().getServerId();

            // validate the session.
            mojangUrl = new URL("http://session.minecraft.net/game/joinserver.jsp?user=" + gameName + "&sessionId=" + sessionId + "&serverId=" + serverId);
            urlConn = mojangUrl.openConnection();

            String resp2 = "";

            try(BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream())))
            {
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                {
                    resp2 += inputLine;
                }
            }

            if (resp2.equalsIgnoreCase("ok"))
            {
                return gameName;
            }

        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }


        return "";
    }

}
