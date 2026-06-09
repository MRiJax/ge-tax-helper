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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.QuantityFormatter;

public class GETaxHelperOverlay extends Overlay
{
    private static final Color PROFIT_COLOR = new Color(0, 180, 0);
    private static final Color LOSS_COLOR = Color.RED;

    private final GETaxHelperPlugin plugin;
    private final GETaxHelperConfig config;
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public GETaxHelperOverlay(GETaxHelperPlugin plugin, GETaxHelperConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showOverlay())
        {
            return null;
        }

        GETaxHelperPlugin.SellOffer offer = plugin.getOffer();
        if (offer == null)
        {
            return null;
        }

        GETaxHelperConfig.DisplayMode mode = config.displayMode();
        boolean perItem = mode != GETaxHelperConfig.DisplayMode.TOTALS;
        boolean totals = mode != GETaxHelperConfig.DisplayMode.PER_ITEM;
        boolean showTax = config.showTaxAmounts();
        boolean showMargin = config.showMargin() && offer.guidePrice > 0;
        boolean loss = offer.marginPerItem < 0;

        Color marginColor = Color.WHITE;
        Color totalColor = Color.WHITE;
        if (showMargin && config.highlightLoss())
        {
            marginColor = loss ? LOSS_COLOR : PROFIT_COLOR;
            if (loss)
            {
                totalColor = LOSS_COLOR;
            }
        }

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("GE Tax Helper")
                .color(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Selling")
                .right(offer.itemName)
                .build());

        if (perItem)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Price/item")
                    .right(fmt(offer.price))
                    .build());

            if (showTax)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Tax/item")
                        .right(fmt(offer.taxPerItem))
                        .build());
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Net/item")
                    .right(fmt(offer.netPerItem))
                    .build());
        }

        if (totals)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Quantity")
                    .right(fmt(offer.quantity))
                    .build());

            if (showTax)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Total tax")
                        .right(fmt(offer.totalTax))
                        .build());
            }

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Total received")
                    .right(fmt(offer.totalNet))
                    .rightColor(totalColor)
                    .build());
        }

        if (showMargin)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Guide buy")
                    .right(fmt(offer.guidePrice))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Margin/item")
                    .right((offer.marginPerItem > 0 ? "+" : "") + fmt(offer.marginPerItem))
                    .rightColor(marginColor)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Break-even")
                    .right(fmt(offer.breakEvenPrice))
                    .build());
        }

        return panelComponent.render(graphics);
    }

    private static String fmt(long value)
    {
        return QuantityFormatter.formatNumber(value);
    }
}
