package me.jayfella.webop.Widgets;

import me.jayfella.webop.Core.IWidget;
import java.util.ArrayList;
import java.util.List;
import me.jayfella.webop.WebOpContext;

public class TPSWidget implements IWidget
{
    private final WebOpContext context;

    private final List<Float> ticksPerSecond = new ArrayList<>();
    private final List<Float> averageTicks = new ArrayList<>();
    private final int maxSize = 30;

    // data
    private long lastPoll = System.currentTimeMillis();
    private long polls = 0L;

    public TPSWidget(WebOpContext context)
    {
        this.context = context;

        ticksPerSecond.add(20f);

        // fake some data
        context.getPlugin().getServer().getScheduler().runTaskTimer(context.getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                long now = System.currentTimeMillis();
                long timeSpent = (now - lastPoll);

                float tps = timeSpent / 50L;

                if (tps > 20L) { tps = 20L; }

                addTps(tps);

                addAverage();

                lastPoll = now;
                polls += 1L;
            }
        }, 20L, 20L);
    }

    private float getAverage()
    {
        float total = 0.0F;

        for (Float f : this.ticksPerSecond)
        {
            if (f != null)
            {
                total += f.floatValue();
            }
        }

        if (total != 0.0F)
        {
            return total / this.ticksPerSecond.size();
        }

        return 0.0F;
    }

    private void addTps(float tps)
    {
        this.ticksPerSecond.add(tps);

        while (this.ticksPerSecond.size() > 5)
        {
            this.ticksPerSecond.remove(0);
        }
    }

    private void addAverage()
    {
        this.averageTicks.add(getAverage());

        while (this.averageTicks.size() > maxSize)
        {
            this.averageTicks.remove(0);
        }
    }

    @Override
    public String getWidgetData()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < averageTicks.size(); i++)
        {
            sb.append(averageTicks.get(i));

            if (i < averageTicks.size() - 1)
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
                    .append("var tpsfunc = function() {\n")
                        .append("$.plot('#tpswidget', [tpsData], { yaxis: { min: 0, max: 22, tickSize:5, tickDecimals:0 }, 'lines': { 'show': true }, 'points': { 'show': false }, colors: ['#1c5ea0'] });\n")
                        // .append("setTimeout(tpsfunc, 100);")
                    .append("};")
                    // .append("setTimeout(tpsfunc, 100);")
                    .append("setInterval(tpsfunc, 1000);")
                .append("</script>");

        return graph.toString();
    }

    @Override
    public String getWidgetContainer()
    {
        return "<div class='widgetTitle'>Average Ticks Per Second<div class='widget' id='tpswidget'></div></div>";
    }


}
