package me.jayfella.webop.Core;

import java.util.List;
import me.jayfella.webop.WebOpContext;
import org.bukkit.configuration.file.FileConfiguration;

public final class PluginSettings
{
    private final WebOpContext context;

    private final FileConfiguration fileConfig;

    private final int portNumber;

    private final int widgetDataUpdate;
    private final int widgetUpdate;

    private final int consoleDataUpdate;
    private final int consoleUpdate;

    public PluginSettings(WebOpContext context)
    {
        this.context = context;

        context.getPlugin().saveDefaultConfig();

        fileConfig = this.context.getPlugin().getConfig();

        this.portNumber = fileConfig.getInt("settings.port");

        this.widgetDataUpdate = fileConfig.getInt("settings.widget-data-update");
        this.widgetUpdate = fileConfig.getInt("settings.widget-update");

        this.consoleDataUpdate = fileConfig.getInt("settings.console-data-update");
        this.consoleUpdate = fileConfig.getInt("settings.console-update");

        // ConfigurationSection whitelist = fileConfig.getConfigurationSection("whitelist");
        // Set<String> keys = whitelist.getKeys(false);

        List<String> whitelist = fileConfig.getStringList("whitelist");

        context.getSessionManager().setWhitelist(whitelist);
        /*for (String name : whitelist)
        {
            context.getSessionManager().addToWhitelist(name);
        }*/
    }

    public FileConfiguration getFileConfig() { return this.fileConfig; }

    public int portNumber() { return this.portNumber; }

    public int widgetDataUpdate() { return this.widgetDataUpdate; }
    public int widgetUpdate() { return this.widgetUpdate; }

    public int consoleDataUpdate() { return this.consoleDataUpdate; }
    public int consoleUpdate() { return this.consoleUpdate; }
}
