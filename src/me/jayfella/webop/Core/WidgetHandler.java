package me.jayfella.webop.Core;

import com.sun.net.httpserver.HttpExchange;
import java.util.ArrayList;
import java.util.List;
import me.jayfella.webop.WebOpContext;
import me.jayfella.webop.Widgets.ChunksWidget;
import me.jayfella.webop.Widgets.EntitiesWidget;
import me.jayfella.webop.Widgets.MemoryWidget;
import me.jayfella.webop.Widgets.TPSWidget;

public class WidgetHandler
{
    private final WebOpContext context;
    private final List<IWidget> widgets = new ArrayList<>();

    public WidgetHandler(WebOpContext context)
    {
        this.context = context;

        this.widgets.add(new TPSWidget(context));
        this.widgets.add(new MemoryWidget(context));
        this.widgets.add(new EntitiesWidget(context));
        this.widgets.add(new ChunksWidget(context));
    }

    public List<IWidget> geWidgets() { return this.widgets; }

    public IWidget getTPSWidget(Class widgetType)
    {
        for (int i = 0; i < widgets.size(); i++)
        {
            if (widgets.get(i).getClass().isInstance(widgetType))
            {
                return widgets.get(i);
            }
        }

        return null;
    }

    public String generateWidgetDataPage(HttpExchange he)
    {
        StringBuilder sb = new StringBuilder();

        // if (!context.getSessionManager().isLoggedIn(he.getRemoteAddress().getAddress()))
        if (!context.getSessionManager().isAuthorised(he))
        {
            return sb.toString();
        }

        for (IWidget w : widgets)
        {
            sb.append(w.getWidgetData()).append("\n");
        }

        return sb.toString().trim();
    }

}
