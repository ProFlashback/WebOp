package me.jayfella.webop.Widgets;

import java.util.ArrayList;
import java.util.List;
import me.jayfella.webop.Core.IWidget;
import me.jayfella.webop.WebOpContext;
import org.bukkit.World;

public class ChunksWidget implements IWidget
{
    private final WebOpContext context;

    private final int maxSize = 30;
    private final List<Integer> chunkCount = new ArrayList<>();

    public ChunksWidget(final WebOpContext context)
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
                    total += world.getLoadedChunks().length;
                }

                addChunkCount(total);
            }
        }, 0L, 20L);
    }

    private void addChunkCount(int amount)
    {
        this.chunkCount.add(amount);

        while (this.chunkCount.size() > maxSize)
        {
            this.chunkCount.remove(0);
        }
    }

    @Override
    public String getWidgetData()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < chunkCount.size(); i++)
        {
            sb.append(chunkCount.get(i));

            if (i < chunkCount.size() - 1)
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
                    .append("var chunkfunc = function() {\n")
                        // .append("$('.chunkswidget').sparkline(cnkData, { type: 'line', width: '300', height: '100', lineColor: '#804d31', fillColor: '#e7bea8', tooltipSuffix: ' Chunks', chartRangeMin: 0 });\n")
                        .append("$.plot('#chunkswidget', [cnkData], { yaxis: { tickDecimals:0, min: 0 }, 'lines': { 'show': true }, 'points': { 'show': false }, colors: ['#af772e'] });\n")
                        // .append("setTimeout(chunkfunc, 100);")
                    .append("};")
                    // .append("setTimeout(chunkfunc, 100);")
                    .append("setInterval(chunkfunc, 1000);")
                .append("</script>");

        return graph.toString();
    }

    @Override
    public String getWidgetContainer()
    {
        return "<div class='widgetTitle'>Total Chunks Loaded<div class='widget' id='chunkswidget'></div></div>";
    }
}
