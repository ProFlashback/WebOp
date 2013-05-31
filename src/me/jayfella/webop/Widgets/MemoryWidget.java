package me.jayfella.webop.Widgets;

import java.util.ArrayList;
import java.util.List;
import me.jayfella.webop.Core.IWidget;
import me.jayfella.webop.WebOpContext;

public class MemoryWidget implements IWidget
{
    private final WebOpContext context;

    private final List<Double> freeMem = new ArrayList<>();
    private final int maxSize = 30;

    private double memUsed = 0;
    private double memMax = 0;
    private double memFree = 0;
    private double percentageFree = 0;

    public MemoryWidget(WebOpContext context)
    {
        this.context = context;

        context.getPlugin().getServer().getScheduler().runTaskTimer(context.getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                memUsed = ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L);
                memMax = (Runtime.getRuntime().maxMemory() / 1048576L);
                memFree = (memMax - memUsed);
                percentageFree = (100.0D / memMax * memFree);

                addFreeMem(memFree);
            }
        }, 0L, 20L);
    }

    private void addFreeMem(double mem)
    {
        this.freeMem.add(mem);

        while (this.freeMem.size() > maxSize)
        {
            this.freeMem.remove(0);
        }
    }

    @Override
    public String getWidgetData()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < freeMem.size(); i++)
        {
            sb.append(freeMem.get(i));

            if (i < freeMem.size() - 1)
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
                    .append("var memfunc = function() {\n")
                        .append("$.plot('#freememwidget', [memData], { yaxis: { min: 0, max: ").append(memMax).append(" }, 'lines': { 'show': true }, 'points': { 'show': false }, colors: ['#9e253b'] });\n")
                        // .append("setTimeout(memfunc, 100);")
                    .append("};")
                    // .append("setTimeout(memfunc, 100);")
                    .append("setInterval(memfunc, 1000);")
                .append("</script>");

        return graph.toString();
    }

    @Override
    public String getWidgetContainer()
    {
        return "<div class='widgetTitle'>Available Memory<div class='widget' id='freememwidget'></div></div>";
    }

}
