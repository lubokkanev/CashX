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

package bisq.asset.coins;

import bisq.asset.Base58BitcoinCashAddressValidator;
import bisq.asset.Coin;

import org.bitcoincashj.core.NetworkParameters;
import org.bitcoincashj.params.MainNetParams;
import org.bitcoincashj.params.RegTestParams;
import org.bitcoincashj.params.TestNet3Params;

public class BitcoinCash extends Coin {

    public BitcoinCash(Network network, NetworkParameters networkParameters) {
        super("Bitcoin Cash", "BCH", new Base58BitcoinCashAddressValidator(networkParameters), network);
    }

    public static class Mainnet extends BitcoinCash {

        public Mainnet() {
            super(Network.MAINNET, MainNetParams.get());
        }
    }


    public static class Testnet extends BitcoinCash {

        public Testnet() {
            super(Network.TESTNET, TestNet3Params.get());
        }
    }


    public static class Regtest extends BitcoinCash {

        public Regtest() {
            super(Network.REGTEST, RegTestParams.get());
        }
    }
}
