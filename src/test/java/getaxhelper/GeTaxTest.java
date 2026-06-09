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

import net.runelite.api.gameval.ItemID;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeTaxTest
{
    // Any item that is not on the exemption list.
    private static final int TAXED_ITEM = ItemID.ABYSSAL_WHIP;

    @Test
    public void taxIsTwoPercentRoundedDown()
    {
        assertEquals(0, GeTax.taxPerItem(TAXED_ITEM, 0));
        assertEquals(0, GeTax.taxPerItem(TAXED_ITEM, 1));
        assertEquals(0, GeTax.taxPerItem(TAXED_ITEM, 49));
        assertEquals(1, GeTax.taxPerItem(TAXED_ITEM, 50));
        assertEquals(1, GeTax.taxPerItem(TAXED_ITEM, 99));
        assertEquals(2, GeTax.taxPerItem(TAXED_ITEM, 100));
        assertEquals(19, GeTax.taxPerItem(TAXED_ITEM, 999));
        assertEquals(20, GeTax.taxPerItem(TAXED_ITEM, 1_000));
        assertEquals(20_000, GeTax.taxPerItem(TAXED_ITEM, 1_000_000));
    }

    @Test
    public void taxIsCappedAtFiveMillionPerItem()
    {
        assertEquals(4_999_999, GeTax.taxPerItem(TAXED_ITEM, 249_999_999));
        assertEquals(5_000_000, GeTax.taxPerItem(TAXED_ITEM, 250_000_000));
        assertEquals(5_000_000, GeTax.taxPerItem(TAXED_ITEM, 1_000_000_000));
        assertEquals(5_000_000, GeTax.taxPerItem(TAXED_ITEM, Integer.MAX_VALUE));
    }

    @Test
    public void exemptItemsPayNoTaxAtAnyPrice()
    {
        assertEquals(0, GeTax.taxPerItem(ItemID.OSRS_BOND, 25_000_000));
        assertEquals(0, GeTax.taxPerItem(ItemID.SPADE, 1_000_000));
        assertEquals(0, GeTax.taxPerItem(ItemID._4DOSE1ENERGY, 10_000));
        assertEquals(0, GeTax.taxPerItem(ItemID.POH_TABLET_FORTISTELEPORT, 500_000_000));
        assertTrue(GeTax.isExempt(ItemID.OSRS_BOND));
        assertFalse(GeTax.isExempt(TAXED_ITEM));
    }

    @Test
    public void netIsPriceMinusTax()
    {
        assertEquals(980_000, GeTax.netPerItem(TAXED_ITEM, 1_000_000));
        assertEquals(49, GeTax.netPerItem(TAXED_ITEM, 49));
        assertEquals(245_000_000, GeTax.netPerItem(TAXED_ITEM, 250_000_000));
        assertEquals(1_000_000, GeTax.netPerItem(ItemID.OSRS_BOND, 1_000_000));
    }

    @Test
    public void breakEvenIsLowestPriceCoveringTarget()
    {
        // Selling at 999,999 nets exactly 980,000 (tax floors to 19,999), so
        // it undercuts the "obvious" 1,000,000 by one coin.
        assertEquals(999_999, GeTax.breakEvenPrice(TAXED_ITEM, 980_000));
        assertEquals(102, GeTax.breakEvenPrice(TAXED_ITEM, 100));
        assertEquals(0, GeTax.breakEvenPrice(TAXED_ITEM, 0));

        // Exempt items break even at the target itself.
        assertEquals(12_345, GeTax.breakEvenPrice(ItemID.OSRS_BOND, 12_345));
    }

    @Test
    public void breakEvenIsMinimalAcrossRanges()
    {
        long[] targets = {1, 49, 50, 99, 100, 12_345, 999_999, 4_900_000,
                244_999_999, 245_000_000, 250_000_000, 2_000_000_000};
        for (long target : targets)
        {
            long price = GeTax.breakEvenPrice(TAXED_ITEM, target);
            assertTrue("net at break-even covers target " + target,
                    GeTax.netPerItem(TAXED_ITEM, price) >= target);
            assertTrue("break-even is minimal for target " + target,
                    GeTax.netPerItem(TAXED_ITEM, price - 1) < target);
        }
    }
}
