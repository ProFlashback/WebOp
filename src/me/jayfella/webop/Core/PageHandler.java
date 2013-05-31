package me.jayfella.webop.Core;

import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import me.jayfella.webop.WebOpContext;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class PageHandler
{
    private final WebOpContext context;

    public PageHandler(WebOpContext context)
    {
        this.context = context;
    }

    private String loadResource(String path)
    {
        String output = "";

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

    private String getPageHeader(String title, List<String> headIncludes)
    {
        StringBuilder includes = new StringBuilder();

        if (headIncludes != null)
        {
            for (String s : headIncludes)
            {
                includes.append(s).append("\n");
            }
        }

        String pageHeader = loadResource("me/jayfella/webop/resources/overall_header.html")
                .replace("{title}", title)
                .replace("{includes}", includes);

        return pageHeader;
    }

    private String getPageFooter()
    {
        return loadResource("me/jayfella/webop/resources/overall_footer.html");
    }

    public String getLoginPage(String[] error, String[] warning)
    {
        StringBuilder page = new StringBuilder();

        page.append(getPageHeader("Login", null));

        page.append(loadResource("me/jayfella/webop/resources/login.html"));

        page.append(getPageFooter());

        return page.toString();
    }

    public String getIndexPage(HttpExchange he)
    {
        if (!context.getSessionManager().isAuthorised(he))
        {
            return getLoginPage(new String[0], new String[0]);
        }

        // header includes
        String flot = "<script type='text/javascript'>"
                + loadResource("me/jayfella/webop/resources/JavaScript/jquery.flot.js")
                + "</script>";

        String exCanvas = "<!--[if lte IE 8]><script type='text/javascript'>"
                + "loadResource(\"me/jayfella/webop/resources/JavaScript/excanvas.js\")"
                + "</script><![endif]-->";

        String widgetProvider = "<script type='text/javascript'>"
                + loadResource("me/jayfella/webop/resources/JavaScript/WidgetDataProvider.js")
                + "</script>";

        String consoleProvider = "<script type='text/javascript'>"
                + loadResource("me/jayfella/webop/resources/JavaScript/ConsoleDataProvider.js")
                + "</script>";

        List<String> containers = new ArrayList<>();
        List<String> includes = new ArrayList<>();

        includes.add(flot);
        includes.add(widgetProvider);
        includes.add(consoleProvider);

        for (IWidget w : context.getWidgetHandler().geWidgets())
        {
            includes.add(w.getWidgetHeadData());
            containers.add(w.getWidgetContainer());
        }

        // body

        String bukkitVersion = context.getPlugin().getServer().getVersion();
        int pluginCount = context.getPlugin().getServer().getPluginManager().getPlugins().length;


        StringBuilder runningPlugins = new StringBuilder();

        for (int i = 0; i < pluginCount; i++)
        {
            Plugin pl = context.getPlugin().getServer().getPluginManager().getPlugins()[i];

            runningPlugins
                    .append((pl.isEnabled()) ? "<span style='color: #0d8022' title='enabled'>" : "<span style='color: #800d0d' title='disabled'>")
                    .append(pl.getName())
                    .append("</span>");

            if (i != pluginCount - 1)
            {
                runningPlugins.append(", ");
            }
        }

        String pluginList = runningPlugins.toString();

        StringBuilder widgets = new StringBuilder();

        for (int i = 0; i < containers.size(); i ++)
        {
            widgets.append(containers.get(i));

            if (i < containers.size() -1)
            {
                widgets.append("<div class='widgetSpacer'></div>");
            }
        }

        String widgetData = widgets.toString();

        String head = getPageHeader("WebOp Control Panel", includes);

        String body = loadResource("me/jayfella/webop/resources/index.html")
                .replace("{username}", context.getSessionManager().getWebUser(he.getRemoteAddress().getAddress()).getUsername())
                .replace("{bukkitversion}", bukkitVersion)
                .replace("{plugincount}", Integer.toString(pluginCount))
                .replace("{pluginlist}", pluginList)
                .replace("{widgetdata}", widgetData);

        String footer = getPageFooter();


        return new StringBuilder()
                .append(head)
                .append(body)
                .append(footer)
                .toString();
    }

    public String getWhitelistPage(HttpExchange he)
    {
        //if (!context.getSessionManager().isLoggedIn(he.getRemoteAddress().getAddress()))
        if (!context.getSessionManager().isAuthorised(he))
        {
            return "";
        }

        WebUser webuser = context.getSessionManager().getWebUser(he.getRemoteAddress().getAddress());
        OfflinePlayer offlinePlayer = context.getPlugin().getServer().getOfflinePlayer(webuser.getUsername());

        if (!offlinePlayer.isOp())
        {
            return "Permission denied.";
        }

        StringBuilder whitelistedUsers = new StringBuilder();

        // for (String user : context.getSessionManager().getWhitelistedUsers())
        for (int i = 0; i < context.getSessionManager().getWhitelistedUsers().length; i++)
        {
            if (!context.getSessionManager().getWhitelistedUsers()[i].equalsIgnoreCase(webuser.getUsername()))
            {
                whitelistedUsers
                        .append("<li class='ui-widget-content'>")
                        .append(context.getSessionManager().getWhitelistedUsers()[i])
                        .append("</li>");

                if ( i < context.getSessionManager().getWhitelistedUsers().length -1)
                {
                    //whitelistedUsers.append("<br>");
                }
            }
        }

        StringBuilder page = new StringBuilder()
                // .append(getPageHeader("Whitelist", null))
                .append(loadResource("me/jayfella/webop/resources/whitelist.html").replace("{whitelistedusers}", whitelistedUsers.toString()))
                ;//.append(getPageFooter());

        return page.toString();
    }

}
