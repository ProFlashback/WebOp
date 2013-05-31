package me.jayfella.webop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.jayfella.webop.Core.HttpListener;
import me.jayfella.webop.Core.LogHandler;
import me.jayfella.webop.Core.PageHandler;
import me.jayfella.webop.Core.PluginSettings;
import me.jayfella.webop.Core.SessionManager;
import me.jayfella.webop.Core.WidgetHandler;
import org.bukkit.command.ConsoleCommandSender;

public final class WebOpContext
{
    private final WebOpPlugin plugin;
    private final HttpListener listener;

    private final SessionManager sessionManager;
    private final PluginSettings pluginSettings;

    private final PageHandler pageHandler;
    private final WidgetHandler widgetHandler;

    public final ArrayList<String> consoleMessages = new ArrayList<>();
    public String consoleOutput = "";

    public WebOpContext(WebOpPlugin plugin)
    {
        this.plugin = plugin;

        this.sessionManager = new SessionManager(this);
        this.pluginSettings = new PluginSettings(this);

        this.listener = new HttpListener(this);
        this.pageHandler = new PageHandler(this);
        this.widgetHandler = new WidgetHandler(this);


        if (!this.listener.isInitialized())
        {
            this.plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }

        hookupLog();
    }

    private void hookupLog()
    {
        Handler logHandler = new LogHandler(this);
        Logger log = Logger.getLogger("Minecraft-Server");
        log.addHandler(logHandler);
    }

    public WebOpPlugin getPlugin() { return this.plugin; }
    public ConsoleCommandSender getConsole() { return this.plugin.getServer().getConsoleSender(); }
    public Logger getLogger() { return this.plugin.getLogger(); }

    public SessionManager getSessionManager() { return this.sessionManager; }
    public PluginSettings getPluginSettings() { return this.pluginSettings; }
    public PageHandler getPageHandler() { return this.pageHandler; }
    public WidgetHandler getWidgetHandler() { return this.widgetHandler; }

    public String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }

    public long parseDateDiff(String time, Date beginDate, boolean future)
	{
		Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", 2);

	    Matcher m = timePattern.matcher(time);

	    int years = 0;
	    int months = 0;
	    int weeks = 0;
	    int days = 0;
	    int hours = 0;
	    int minutes = 0;
	    int seconds = 0;

	    boolean found = false;

	    while (m.find())
	    {
	    	if ((m.group() != null) && (!m.group().isEmpty()))
	    	{
	    		for (int i = 0; i < m.groupCount(); i++)
	    		{
	    			if ((m.group(i) != null) && (!m.group(i).isEmpty()))
	    			{
	    				found = true;
	    				break;
	    			}
	    		}
	    		if (found)
	    		{
	    			if ((m.group(1) != null) && (!m.group(1).isEmpty()))
	    			{
	    				years = Integer.parseInt(m.group(1));
	    			}
	    			if ((m.group(2) != null) && (!m.group(2).isEmpty()))
	    			{
	    				months = Integer.parseInt(m.group(2));
	    			}
	    			if ((m.group(3) != null) && (!m.group(3).isEmpty()))
	    			{
	    				weeks = Integer.parseInt(m.group(3));
	    			}
	    			if ((m.group(4) != null) && (!m.group(4).isEmpty()))
	    			{
	    				days = Integer.parseInt(m.group(4));
	    			}
	    			if ((m.group(5) != null) && (!m.group(5).isEmpty()))
	    			{
	    				hours = Integer.parseInt(m.group(5));
	    			}
	    			if ((m.group(6) != null) && (!m.group(6).isEmpty()))
	    			{
	    				minutes = Integer.parseInt(m.group(6));
	    			}
	    			if ((m.group(7) != null) && (!m.group(7).isEmpty()))
	    			{
	    				seconds = Integer.parseInt(m.group(7));
	    			}
	    		}
	    	}
	    }

	    if (!found)
	    {
	      // throw new Exception("illegalDate", new Object[0]));
	    	return 0;
	    }

	    Calendar c = new GregorianCalendar();
        c.setTime(beginDate);

	    if (years > 0)
	    {
	    	c.add(1, years * (future ? 1 : -1));
	    }
	    if (months > 0)
	    {
	    	c.add(2, months * (future ? 1 : -1));
	    }
	    if (weeks > 0)
	    {
	    	c.add(3, weeks * (future ? 1 : -1));
	    }
	    if (days > 0)
	    {
	    	c.add(5, days * (future ? 1 : -1));
	    }
	    if (hours > 0)
	    {
	    	c.add(11, hours * (future ? 1 : -1));
	    }
	    if (minutes > 0)
	    {
	    	c.add(12, minutes * (future ? 1 : -1));
	    }
	    if (seconds > 0)
	    {
	    	c.add(13, seconds * (future ? 1 : -1));
	    }

	    Calendar max = new GregorianCalendar();
	    max.setTime(beginDate);

	    max.add(1, 10);

	    if (c.after(max))
	    {
	    	return max.getTimeInMillis();
	    }

	    return c.getTimeInMillis();
	}

}
