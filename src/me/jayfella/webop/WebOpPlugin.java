package me.jayfella.webop;

import me.jayfella.webop.Core.HttpListener;

import org.bukkit.plugin.java.JavaPlugin;

public class WebOpPlugin extends JavaPlugin
{
    private WebOpContext context;

    @Override
    public void onEnable()
    {
        this.context = new WebOpContext(this);
    }

    @Override
    public void onDisable()
    {
    	this.context.shutdownListener();
    }

    public WebOpContext getContext() { return this.context; }
}
