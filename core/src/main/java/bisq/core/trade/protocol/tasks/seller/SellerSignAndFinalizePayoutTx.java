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

package bisq.core.trade.protocol.tasks.seller;

import bisq.core.btc.model.AddressEntry;
import bisq.core.btc.wallet.BtcWalletService;
import bisq.core.offer.Offer;
import bisq.core.trade.Trade;
import bisq.core.trade.protocol.TradingPeer;
import bisq.core.trade.protocol.tasks.TradeTask;

import bisq.common.taskrunner.TaskRunner;

import org.bitcoincashj.core.Coin;
import org.bitcoincashj.core.Transaction;
import org.bitcoincashj.crypto.DeterministicKey;

import java.util.Arrays;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class SellerSignAndFinalizePayoutTx extends TradeTask {

    @SuppressWarnings({"WeakerAccess", "unused"})
    public SellerSignAndFinalizePayoutTx(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();

            checkNotNull(trade.getTradeAmount(), "trade.getTradeAmount() must not be null");

            Offer offer = trade.getOffer();
            TradingPeer tradingPeer = processModel.getTradingPeer();
            BtcWalletService walletService = processModel.getBtcWalletService();
            String id = processModel.getOffer().getId();

            final byte[] buyerSignature = tradingPeer.getSignature();

            Coin buyerPayoutAmount = offer.getBuyerSecurityDeposit().add(trade.getTradeAmount());
            Coin sellerPayoutAmount = offer.getSellerSecurityDeposit();

            final String buyerPayoutAddressString = tradingPeer.getPayoutAddressString();
            String sellerPayoutAddressString = walletService.getOrCreateAddressEntry(id,
                    AddressEntry.Context.TRADE_PAYOUT).getAddressString();

            final byte[] buyerMultiSigPubKey = tradingPeer.getMultiSigPubKey();
            byte[] sellerMultiSigPubKey = processModel.getMyMultiSigPubKey();

            Optional<AddressEntry> MultiSigAddressEntryOptional = walletService.getAddressEntry(id, AddressEntry.Context.MULTI_SIG);
            checkArgument(MultiSigAddressEntryOptional.isPresent() && Arrays.equals(sellerMultiSigPubKey,
                    MultiSigAddressEntryOptional.get().getPubKey()),
                    "sellerMultiSigPubKey from AddressEntry must match the one from the trade data. trade id =" + id);

            DeterministicKey multiSigKeyPair = walletService.getMultiSigKeyPair(id, sellerMultiSigPubKey);

            Transaction transaction = processModel.getTradeWalletService().sellerSignsAndFinalizesPayoutTx(
                    trade.getDepositTx(),
                    buyerSignature,
                    buyerPayoutAmount,
                    sellerPayoutAmount,
                    buyerPayoutAddressString,
                    sellerPayoutAddressString,
                    multiSigKeyPair,
                    buyerMultiSigPubKey,
                    sellerMultiSigPubKey,
                    trade.getArbitratorBtcPubKey()
            );

            trade.setPayoutTx(transaction);

            walletService.swapTradeEntryToAvailableEntry(id, AddressEntry.Context.MULTI_SIG);

            complete();
        } catch (Throwable t) {
            failed(t);
        }
    }
}
