/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Dries K. Aka Dries007 and the CCM modding crew.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ccm.pay2spawn;

import ccm.pay2spawn.cmd.CommandP2S;
import ccm.pay2spawn.cmd.CommandP2SPermissions;
import ccm.pay2spawn.cmd.CommandP2SServer;
import ccm.pay2spawn.configurator.ConfiguratorManager;
import ccm.pay2spawn.configurator.HTMLGenerator;
import ccm.pay2spawn.misc.DonationCheckerThread;
import ccm.pay2spawn.misc.P2SConfig;
import ccm.pay2spawn.misc.RewardsDB;
import ccm.pay2spawn.network.ConnectionHandler;
import ccm.pay2spawn.network.PacketHandler;
import ccm.pay2spawn.network.StatusPacket;
import ccm.pay2spawn.permissions.PermissionsHandler;
import ccm.pay2spawn.types.TypeBase;
import ccm.pay2spawn.types.TypeRegistry;
import ccm.pay2spawn.util.*;
import com.google.common.base.Strings;
import ccm.libs.com.jadarstudios.developercapes.DevCapesUtil;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.client.ClientCommandHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ccm.pay2spawn.util.Constants.*;

/**
 * The main mod class
 *
 * @author Dries007
 */
@Mod(modid = MODID, name = NAME)
public class Pay2Spawn
{
    @Mod.Instance(MODID)
    public static Pay2Spawn instance;
    public static boolean enable  = true;
    public static boolean forceOn = false;

    @Mod.Metadata(MODID)
    private ModMetadata           metadata;
    private RewardsDB             rewardsDB;
    private P2SConfig             config;
    private File                  configFolder;
    private Logger                logger;
    private DonationCheckerThread donationCheckerThread;

    public static String getVersion()
    {
        return instance.metadata.version;
    }

    public static RewardsDB getRewardsDB()
    {
        return instance.rewardsDB;
    }

    public static Logger getLogger() { return instance.logger; }

    public static P2SConfig getConfig() { return instance.config; }

    public static File getFolder()
    {
        return instance.configFolder;
    }

    public static File getRewardDBFile() { return new File(instance.configFolder, NAME + ".json"); }

    public static DonationCheckerThread getDonationCheckerThread() { return instance.donationCheckerThread; }

    public static boolean isConfiguredProperly()
    {
        return !Strings.isNullOrEmpty(getConfig().channel) && !Strings.isNullOrEmpty(getConfig().API_Key) && !getDonationCheckerThread().firstrun;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException
    {
        logger = event.getModLog();
        logger.setLevel(Level.ALL);

        configFolder = new File(event.getModConfigurationDirectory(), NAME);
        //noinspection ResultOfMethodCallIgnored
        configFolder.mkdirs();

        config = new P2SConfig(new File(configFolder, NAME + ".cfg"));

        logger.severe("Make sure you configure your PayPal account correctly BEFORE making bug reports!");

        TypeRegistry.preInit();
        Statistics.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) throws MalformedURLException
    {
        TickRegistry.registerScheduledTickHandler(ClientTickHandler.INSTANCE, Side.CLIENT);
        TickRegistry.registerTickHandler(ServerTickHandler.INSTANCE, Side.SERVER);
        TypeRegistry.doConfig(config.configuration);
        config.configuration.save();
        rewardsDB = new RewardsDB(getRewardDBFile());
        MetricsHelper.init();

        if (event.getSide().isClient())
        {
            donationCheckerThread = new DonationCheckerThread();
            donationCheckerThread.start();
            new EventHandler();
            ClientCommandHandler.instance.registerCommand(new CommandP2S());
        }

        DevCapesUtil.addFileUrl(Constants.CAPEURL);

        NetworkRegistry.instance().registerConnectionHandler(new ConnectionHandler());
        PacketHandler packetHandler = new PacketHandler();
        for (String channel : CHANNELS) NetworkRegistry.instance().registerChannel(packetHandler, channel);

        ClientTickHandler.INSTANCE.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        for (TypeBase base : TypeRegistry.getAllTypes()) base.printHelpList(configFolder);

        TypeRegistry.registerPermissions();
        try
        {
            HTMLGenerator.init();
        }
        catch (IOException e)
        {
            logger.severe("Error initializing the HTMLGenerator.");
            e.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) throws IOException
    {
        PermissionsHandler.init();
        try
        {
            StatusPacket.serverConfig = JSON_PARSER.parse(new FileReader(new File(instance.configFolder, NAME + ".json"))).toString();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        event.registerServerCommand(new CommandP2SPermissions());
        event.registerServerCommand(new CommandP2SServer());

        // for (String node : PermissionsHandler.getAllPermNodes()) logger.info(node);
    }

    public static void reloadDB()
    {
        instance.rewardsDB = new RewardsDB(new File(instance.configFolder, NAME + ".json"));
        ConfiguratorManager.reload();
        try
        {
            PermissionsHandler.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void reloadDB_Server() throws Exception
    {
        StatusPacket.serverConfig = JSON_PARSER.parse(new FileReader(new File(instance.configFolder, NAME + ".json"))).toString();
        StatusPacket.sendConfigToAllPlayers();
    }

    public static void reloadDBFromServer(String input)
    {
        instance.rewardsDB = new RewardsDB(input);
        ConfiguratorManager.reload();
    }
}
