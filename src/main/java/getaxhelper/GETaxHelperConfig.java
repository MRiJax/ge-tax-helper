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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("getaxhelper")
public interface GETaxHelperConfig extends Config
{
    enum DisplayMode
    {
        PER_ITEM("Per item"),
        TOTALS("Totals"),
        BOTH("Both");

        private final String label;

        DisplayMode(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    @ConfigItem(
            position = 0,
            keyName = "showOverlay",
            name = "Show overlay",
            description = "Master toggle for the sell offer tax overlay"
    )
    default boolean showOverlay()
    {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "displayMode",
            name = "Show values",
            description = "Show per-item values, whole-offer totals, or both"
    )
    default DisplayMode displayMode()
    {
        return DisplayMode.BOTH;
    }

    @ConfigItem(
            position = 2,
            keyName = "showTaxAmounts",
            name = "Show tax amounts",
            description = "Also show the tax being deducted; turn off to only show what you'll receive"
    )
    default boolean showTaxAmounts()
    {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "showMargin",
            name = "Show flipping margin",
            description = "Show the post-tax margin against the GE guide buy price and the break-even sell price"
    )
    default boolean showMargin()
    {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "highlightLoss",
            name = "Color losses red",
            description = "Color the margin and total received red when the sale is a post-tax loss (green when profitable)"
    )
    default boolean highlightLoss()
    {
        return true;
    }
}
