/*
 * Copyright (c) 2026, SeismicRy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package getaxhelper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
        name = "GE Tax Helper",
        description = "Shows your post-tax proceeds while setting up a Grand Exchange sell offer, plus flipping margin and break-even price",
        tags = {"grand", "exchange", "ge", "tax", "sell", "flipping", "margin", "merching"}
)
public class GETaxHelperPlugin extends Plugin
{
    // Value of the GE_NEWOFFER_TYPE varbit while the offer being set up is a
    // sale (0 = buy). The tax only applies to sales.
    private static final int OFFER_TYPE_SELL = 1;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private GETaxHelperOverlay overlay;

    @Inject
    private ItemManager itemManager;

    @Inject
    private GETaxHelperConfig config;

    // Snapshot of the sell offer currently being set up, recomputed each game
    // tick and only read by the overlay; null whenever no sell setup is open.
    private SellOffer offer;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        log.debug("GE Tax Helper started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        offer = null;
        log.debug("GE Tax Helper stopped!");
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        // Game ticks stop firing outside the game, so clear the snapshot here
        // or the overlay would linger over the login screen after a logout.
        if (event.getGameState() != GameState.LOGGED_IN)
        {
            offer = null;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        offer = readOffer();
    }

    /**
     * Reads the in-progress offer from the GE offer-setup screen, or null if
     * no sell offer is currently being set up.
     */
    private SellOffer readOffer()
    {
        if (!config.showOverlay())
        {
            return null;
        }

        Widget setup = client.getWidget(InterfaceID.GeOffers.SETUP);
        if (setup == null || setup.isHidden())
        {
            return null;
        }

        if (client.getVarbitValue(VarbitID.GE_NEWOFFER_TYPE) != OFFER_TYPE_SELL)
        {
            return null;
        }

        int itemId = client.getVarpValue(VarPlayerID.TRADINGPOST_SEARCH);
        if (itemId <= 0)
        {
            return null;
        }

        int price = client.getVarbitValue(VarbitID.GE_NEWOFFER_PRICE);
        int quantity = client.getVarbitValue(VarbitID.GE_NEWOFFER_QUANTITY);
        String itemName = itemManager.getItemComposition(itemId).getName();
        int guidePrice = itemManager.getItemPrice(itemId);

        return new SellOffer(itemId, itemName, price, quantity, guidePrice);
    }

    SellOffer getOffer()
    {
        return offer;
    }

    @Provides
    GETaxHelperConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(GETaxHelperConfig.class);
    }

    /**
     * Immutable snapshot of the sell offer being set up, with the tax math
     * precomputed so the overlay only has to format and draw it.
     */
    static final class SellOffer
    {
        final String itemName;
        final long price;
        final long quantity;
        final long taxPerItem;
        final long netPerItem;
        final long totalTax;
        final long totalNet;
        final long guidePrice;
        final long marginPerItem;
        final long breakEvenPrice;

        SellOffer(int itemId, String itemName, int price, int quantity, int guidePrice)
        {
            this.itemName = itemName;
            this.price = price;
            this.quantity = quantity;
            this.taxPerItem = GeTax.taxPerItem(itemId, price);
            this.netPerItem = price - this.taxPerItem;
            this.totalTax = this.taxPerItem * quantity;
            this.totalNet = this.netPerItem * quantity;
            this.guidePrice = guidePrice;
            this.marginPerItem = this.netPerItem - guidePrice;
            this.breakEvenPrice = GeTax.breakEvenPrice(itemId, guidePrice);
        }
    }
}
