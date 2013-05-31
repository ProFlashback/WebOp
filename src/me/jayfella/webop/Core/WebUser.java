package me.jayfella.webop.Core;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Objects;

public final class WebUser
{
    private final InetAddress address;
    private final String username;

    private long lastActivity;

    public WebUser(InetAddress address, String username)
    {
        this.address = address;
        this.username = username;
    }

    public InetAddress getAddress() { return this.address; }
    public String getUsername() { return this.username; }

    public long getLastActivity() { return this.lastActivity; }
    public void flagLastActivity() { this.lastActivity = Calendar.getInstance().getTimeInMillis(); }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || (!(other instanceof WebUser)))
        {
            return false;
        }
        
        return (this.hashCode() == other.hashCode());
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.username);
        return hash;
    }
}
