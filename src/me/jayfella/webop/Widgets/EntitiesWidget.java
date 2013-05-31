package me.jayfella.webop.Widgets;

import java.util.ArrayList;
import java.util.List;
import me.jayfella.webop.Core.IWidget;
import me.jayfella.webop.WebOpContext;
import org.bukkit.World;

public final class EntitiesWidget implements IWidget
{
    private final WebOpContext context;

    private final int maxSize = 30;
    private final List<Integer> entityCount = new ArrayList<>();

    public EntitiesWidget(final WebOpContext context)
    {
        this.context = context;

        context.getPlugin().getServer().getScheduler().runTaskTimer(context.getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                int total = 0;

                for (World world : context.getPlugin().getServer().getWorlds())
                {
                    total += world.getEntities().size();
                }

                addEntityCount(total);

            }
        }, 0L, 20L);
    }

    private void addEntityCount(int amount)
    {
        this.entityCount.add(amount);

        while (this.entityCount.size() > maxSize)
        {
            this.entityCount.remove(0);
        }
    }

    @Override
    public String getWidgetData()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < entityCount.size(); i++)
        {
            sb.append(entityCount.get(i));

            if (i < entityCount.size() - 1)
            {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    @Override
    public String getWidgetHeadData()
    {
        StringBuilder graph = new StringBuilder()
                .append("<script type=\"text/javascript\">")
                    .append("var entityfunc = function() {\n")
                        .append("$.plot('#entitywidget', [entData], { yaxis: { tickDecimals:0, min: 0 }, 'lines': { 'show': true }, 'points': { 'show': false }, colors: ['#4ec85d'] });\n")
                        // .append("setTimeout(entityfunc, 100);")
                    .append("};")
                    // .append("setTimeout(entityfunc, 100);")
                    .append("setInterval(entityfunc, 1000);")
                .append("</script>");

        return graph.toString();
    }

    @Override
    public String getWidgetContainer()
    {
        return "<div class='widgetTitle'>Total Entities Loaded<div class='widget' id='entitywidget'></div></div>";
    }
}
