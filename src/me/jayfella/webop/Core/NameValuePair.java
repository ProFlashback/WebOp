package me.jayfella.webop.Core;

public final class NameValuePair
{
    private final String name;
    private final String value;

    public NameValuePair(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String name() { return this.name; }
    public String value() { return this.value; }
}
