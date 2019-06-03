/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.bch.nodes;

import bisq.core.bch.nodes.BtcNodes.BtcNode;
import bisq.core.user.Preferences;

import java.util.List;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import static bisq.core.bch.nodes.BtcNodes.BitcoinNodesOption.CUSTOM;
import static bisq.core.bch.nodes.BtcNodes.BitcoinNodesOption.PUBLIC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Preferences.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class BtcNodesSetupPreferencesTest {
    @Test
    public void testSelectPreferredNodesWhenPublicOption() {
        Preferences delegate = mock(Preferences.class);
        when(delegate.getBitcoinNodesOptionOrdinal()).thenReturn(PUBLIC.ordinal());

        BtcNodesSetupPreferences preferences = new BtcNodesSetupPreferences(delegate);
        List<BtcNode> nodes = preferences.selectPreferredNodes(mock(BtcNodes.class));

        assertTrue(nodes.isEmpty());
    }

    @Test
    public void testSelectPreferredNodesWhenCustomOption() {
        Preferences delegate = mock(Preferences.class);
        when(delegate.getBitcoinNodesOptionOrdinal()).thenReturn(CUSTOM.ordinal());
        when(delegate.getBitcoinNodes()).thenReturn("aaa.onion,bbb.onion");

        BtcNodesSetupPreferences preferences = new BtcNodesSetupPreferences(delegate);
        List<BtcNode> nodes = preferences.selectPreferredNodes(mock(BtcNodes.class));

        assertEquals(2, nodes.size());
    }
}
