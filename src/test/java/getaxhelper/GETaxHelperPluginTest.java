package getaxhelper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GETaxHelperPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(GETaxHelperPlugin.class);
        RuneLite.main(args);
    }
}
