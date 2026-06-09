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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.runelite.api.gameval.ItemID;

/**
 * Grand Exchange sell-tax math, per the OSRS Wiki ("Grand Exchange", Tax
 * section), current as of June 2026:
 *
 * <ul>
 * <li>2% of the sale price is withheld per item, rounded down. Sales only —
 *     buy offers are never taxed.</li>
 * <li>The tax is capped at 5,000,000 coins per item, so any price of
 *     250,000,000 or more pays a flat 5m per item.</li>
 * <li>Items priced below 50 coins pay nothing, simply because 2% of 49 rounds
 *     down to 0. Since the tax rose from 1% to 2% on 29 May 2025 there is no
 *     separate low-value exemption beyond this rounding.</li>
 * <li>A fixed list of items is fully exempt regardless of price (see
 *     {@link #EXEMPT_ITEMS}).</li>
 * </ul>
 */
final class GeTax
{
    static final int TAX_PERCENT = 2;
    static final long PER_ITEM_TAX_CAP = 5_000_000L;

    /**
     * Items never taxed regardless of price: the Old school bond, basic tools,
     * and the new-player staples (low-level food, basic ammunition, mind
     * runes, energy potions and common teleport items) added alongside the 2%
     * rate on 29 May 2025.
     */
    static final Set<Integer> EXEMPT_ITEMS = ImmutableSet.of(
        ItemID.OSRS_BOND,
        // Tools
        ItemID.CHISEL,
        ItemID.GARDENING_TROWEL,
        ItemID.GLASSBLOWINGPIPE,
        ItemID.HAMMER,
        ItemID.NEEDLE,
        ItemID.PESTLE_AND_MORTAR,
        ItemID.RAKE,
        ItemID.POH_SAW,
        ItemID.SECATEURS,
        ItemID.DIBBER,
        ItemID.SHEARS,
        ItemID.SPADE,
        ItemID.WATERING_CAN_0,
        // Low-level food
        ItemID.BASS,
        ItemID.BREAD,
        ItemID.CAKE,
        ItemID.COOKED_CHICKEN,
        ItemID.COOKED_MEAT,
        ItemID.HERRING,
        ItemID.LOBSTER,
        ItemID.MACKEREL,
        ItemID.MEAT_PIE,
        ItemID.PIKE,
        ItemID.SALMON,
        ItemID.SHRIMP,
        ItemID.TUNA,
        // Basic ammunition
        ItemID.BRONZE_ARROW,
        ItemID.IRON_ARROW,
        ItemID.STEEL_ARROW,
        ItemID.BRONZE_DART,
        ItemID.IRON_DART,
        ItemID.STEEL_DART,
        // Runes and potions
        ItemID.MINDRUNE,
        ItemID._1DOSE1ENERGY,
        ItemID._2DOSE1ENERGY,
        ItemID._3DOSE1ENERGY,
        ItemID._4DOSE1ENERGY,
        // Teleport jewellery and tablets
        ItemID.RING_OF_DUELING_8,
        ItemID.NECKLACE_OF_MINIGAMES_8,
        ItemID.POH_TABLET_VARROCKTELEPORT,
        ItemID.POH_TABLET_LUMBRIDGETELEPORT,
        ItemID.POH_TABLET_FALADORTELEPORT,
        ItemID.POH_TABLET_CAMELOTTELEPORT,
        ItemID.POH_TABLET_ARDOUGNETELEPORT,
        ItemID.POH_TABLET_TELEPORTTOHOUSE,
        ItemID.POH_TABLET_KOURENDTELEPORT,
        ItemID.POH_TABLET_FORTISTELEPORT
    );

    private GeTax()
    {
    }

    static boolean isExempt(int itemId)
    {
        return EXEMPT_ITEMS.contains(itemId);
    }

    /**
     * Tax withheld per item sold at the given price: floor(2%), capped at 5m,
     * 0 for exempt items.
     */
    static long taxPerItem(int itemId, long pricePerItem)
    {
        if (pricePerItem <= 0 || isExempt(itemId))
        {
            return 0;
        }
        return Math.min(pricePerItem * TAX_PERCENT / 100, PER_ITEM_TAX_CAP);
    }

    /**
     * Coins actually received per item sold at the given price.
     */
    static long netPerItem(int itemId, long pricePerItem)
    {
        return pricePerItem - taxPerItem(itemId, pricePerItem);
    }

    /**
     * The lowest sell price whose post-tax proceeds still cover targetNet
     * (e.g. the price the item was bought for). Selling at or above the
     * returned price is break-even or profit; anything below is a loss.
     */
    static long breakEvenPrice(int itemId, long targetNet)
    {
        if (targetNet <= 0)
        {
            return 0;
        }
        if (isExempt(itemId))
        {
            return targetNet;
        }

        // Net proceeds rise monotonically with price (each +1 of price adds
        // +1 net, except on multiples of 50 where it adds 0), so binary
        // search for the lowest price netting at least the target. The tax
        // never exceeds the 5m cap, so targetNet + cap always suffices.
        long lo = targetNet;
        long hi = targetNet + PER_ITEM_TAX_CAP;
        while (lo < hi)
        {
            long mid = (lo + hi) >>> 1;
            if (netPerItem(itemId, mid) >= targetNet)
            {
                hi = mid;
            }
            else
            {
                lo = mid + 1;
            }
        }
        return lo;
    }
}
