package me.jayfella.webop.Core;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import me.jayfella.webop.WebOpContext;
import org.apache.commons.lang.StringEscapeUtils;

public class LogHandler extends Handler
{
    private final WebOpContext context;

    public LogHandler(WebOpContext context)
    {
        this.context = context;
    }

    @Override
    public void publish(LogRecord lr)
    {


        Object[] params = lr.getParameters();

        String output = lr.getMessage();

        if (params != null)
        {
            for (int i = 0; i <  params.length; i++)
            {
                if (params[i] instanceof String)
                {
                    output = output.replace("{" + i + "}", (String)params[i]);
                }

            }
        }

        output = StringEscapeUtils.escapeJava(output);

        output = "[" + lr.getLevel().getName() + "] " + output;

        output = parseLog(output);

        context.consoleMessages.add(output);
        pushConsoleOutput();
    }

    @Override public void flush() { }
    @Override public void close() throws SecurityException { }

    private String parseLog(String string)
    {

        // colors
        string = string.replace("\\u001B[m", "<span style='color: #c0c0c0'>"); // &0
        string = string.replace("\\u001B[0;34;22m", "<span style='color: #0000aa'>"); // &1
        string = string.replace("\\u001B[0;32;22m", "<span style='color: #00aa00'>"); // &2
        string = string.replace("\\u001B[0;36;22m", "<span style='color: #00aaaa'>"); // &3
        string = string.replace("\\u001B[0;31;22m", "<span style='color: #aa0000'>"); // &4
        string = string.replace("\\u001B[0;35;22m", "<span style='color: #aa00aa'>"); // &5
        string = string.replace("\\u001B[0;33;22m", "<span style='color: #ffaa00'>"); // &6
        string = string.replace("\\u001B[0;37;22m", "<span style='color: #aaaaaa'>"); // &7
        string = string.replace("\\u001B[0;30;1m", "<span style='color: #555555'>"); // &8
        string = string.replace("\\u001B[0;34;1m", "<span style='color: #5555ff'>"); // &9
        string = string.replace("\\u001B[0;32;1m", "<span style='color: #55ff55'>"); // &A
        string = string.replace("\\u001B[0;36;1m", "<span style='color: #55ffff'>"); // &b
        string = string.replace("\\u001B[0;31;1m", "<span style='color: #ff5555'>"); // &c
        string = string.replace("\\u001B[0;35;1m", "<span style='color: #ff55ff'>"); // &d
        string = string.replace("\\u001B[0;33;1m", "<span style='color: #ffff55'>"); // &e
        string = string.replace("\\u001B[0;37;1m", "<span style='color: #ffffff'>"); // &f


        string = string.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        string = string.replace("\r\n", "<br>");


        // close all spans.
        int count = 0;
        String term = "<span style=";
        int result = string.indexOf(term);

        while(result !=-1)
        {
            result = string.indexOf(term, result+1);
            count++;
        }

        for (int i = 0; i < count; i++)
        {
            string = string + "</span>";
        }

        return string;
    }

    private void pushConsoleOutput()
    {
        StringBuilder output = new StringBuilder();

        for (String str : context.consoleMessages)
        {
            output.append(str).append("<br />");
        }

        context.consoleOutput = output.toString();
    }


}
