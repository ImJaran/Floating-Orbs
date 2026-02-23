package com.floatingorbs;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FloatingOrbsPluginLauncher
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(FloatingOrbsPlugin.class);
        RuneLite.main(args);
    }
}
