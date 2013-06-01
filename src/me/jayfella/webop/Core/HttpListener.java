package me.jayfella.webop.Core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.jayfella.webop.WebOpContext;
import org.bukkit.ChatColor;

public final class HttpListener implements HttpHandler
{
    private final WebOpContext context;
    private final RequestHandler requestHandler;
    private HttpServer httpServer;

    private final boolean isInitialized;

    public HttpListener(WebOpContext context)
    {
        this.context = context;
        this.isInitialized = initialize();
        this.requestHandler = new RequestHandler(context);
    }

    private boolean initialize()
    {
        try
        {
            int port = context.getPluginSettings().portNumber();
            InetAddress ipaddr = InetAddress.getByName(context.getPluginSettings().ipAddress());

            this.httpServer = HttpServer.create(new InetSocketAddress(ipaddr, port), 0);
            this.httpServer.createContext("/", this);
            this.httpServer.setExecutor(null);
            this.httpServer.start();

            this.context.getConsole().sendMessage(new StringBuilder().append("[").append(this.context.getPlugin().getName()).append("] ").append("HTTP Server initialized on port ").append(ChatColor.GREEN).append(port).toString());
        }
        catch (IOException ex)
        {
            this.context.getLogger().log(Level.SEVERE, "Error initializing HTTP Server.", ex);
            return false;
        }

        return true;
    }
    
    public boolean shutdown()
    {
    	this.context.getLogger().log(Level.INFO, "Shutting down HttpServer.");
		this.httpServer.stop(0);
		return true;
    }

    public boolean isInitialized() { return this.isInitialized; }

    @Override
    public void handle(final HttpExchange he) throws IOException
    {
        context.getPlugin().getServer().getScheduler().runTaskAsynchronously(context.getPlugin(), new Runnable()
        {
           @Override
           public void run()
           {
               try
               {
                   requestHandler.handleRequest(he);
               } catch (IOException ex)
               {
                   Logger.getLogger(HttpListener.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
        });


    }

}
