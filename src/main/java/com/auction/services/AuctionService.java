package com.auction.services;

import com.auction.model.Bid;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionService {

    private Map<Integer, Bid> auctionWinners = new ConcurrentHashMap<>();

    public Bid getWinner(Integer auctionId) {
        if (auctionId == null || auctionId < 0) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        Bid winner = auctionWinners.get(auctionId);
        try {
            if (winner == null) {
                winner = new Bid("No entries.", "0",
                        "0", "0", auctionId);
            }
            return winner;
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public void processBid(Bid newBid) {

        if (isNullOrContainsAnyNullFields(newBid)) {
            return ;
        }

        // Initialize winning Bid.
        if (!auctionWinners.containsKey(newBid.getAuctionId())) {
            auctionWinners.put(newBid.getAuctionId(), newBid);
            return ;
        }

        // Dupe Handling: Don't compare winning duplicate against itself if the following fields are equal:
        // id, startingBid, maxBid, autoIncrement
        if (auctionWinners.get(newBid.getAuctionId()).equals(newBid)) {
            return ;
        }

        Bid winningBid = auctionWinners.get(newBid.getAuctionId());

        // If the newBid's maxBid cannot overtake the winningBid's winningPrice, winningBid should stay the same.
        if (winningBid.getWinningPrice().compareTo(newBid.getMaxBid()) >= 0) {
            return ;
        }


        // There is a distinction between maxBid and the maximumPotentialBid.
        // maximumPotentialBid <= maxBid because maximumBid is the upper limit, and the autoIncrement values added to
        // the startingBid might not fall exactly on the maxBid value.

        // There are 3 main pieces of logic below:
        //
        //          if winnerBid is the definite winner, update WinningPrice to winnerBidRelativePotential
        //
        //          if newBid is the definite winner, update WinningPrice to newBidRelativePotential and replace the
        //              winningBid
        //
        //          if there is no clear winner, compare the winnerBidMaxPotential with the newBidMaxPotential, and
        //              update the WinningPrice and the winningBid appropriately
        //
        //
        // 4 main values utilized:
        //         winnerBidMaxPotential        = winnerBid's maximumPotentialBid relative to its maxBid
        //         newBidMaxPotential           = newBid's maximumPotentialBid relative to its maxBid
        //         winnerBidRelativePotential   = winnerBid's maximumPotentialBid relative to newBidMaxPotential
        //         newBidRelativePotential      = newBid's maximumPotentialBid relative to winnerBidMaxPotential



        // if winnerBid's maximumPotentialBid >= newBid's maximumPotential's bid
        // then maintain winnerBid as the winner for the auction and update its WinnerPrice with a new WinningPrice
        // relative to newBid's maximumPotentialBid.
        // This will maintain winnerBid as the winner, while also updating it's winnerPrice as if it competed against
        // newBid up until newBid's final, maximum bid. --see helper functions for example scenario--
        if (lowerBoundMaxPotentialSafe(winningBid, newBid) >= 0) {

            // newBid is the losing bid. Calculate its highest bid it was able to make.
            BigDecimal newBidMaxPotential = maximumPotentialBid(newBid);

            // calculate winningBid's new relativeWinningPrice. this will be the minimumPotentialBid value greater than
            // newBid's maximumPotentialBid
            BigDecimal relativeWinningPrice = relativeWinningPrice(winningBid, newBidMaxPotential);

            // update the winningBid's winningPrice to simulate outbidding the losing bid.
            winningBid.setWinningPrice(relativeWinningPrice);
            return ;
        }


        // if newBid's maximumPotentialBid > winningBid's maximumPotential's bid
        // then replace the winnerBid for the auction with newBid and update newBid's WinnerPrice with a new WinningPrice
        // relative to the old winningBid's maximumPotentialBid
        // This will simulate newBid outbidding old winningBid up until old winningBid's final, maximum bid.
        if (lowerBoundMaxPotentialSafe(newBid, winningBid) > 0) {

            // winningBid is the losing bid. Calculate its highest bid it was able to make.
            BigDecimal winningBidMaxPotential = maximumPotentialBid(winningBid);

            // calculate newBid's new relativeWinningPrice. This will be the minimumPotentialBid value greater than
            // winningBid's maximumPotentialBid
            BigDecimal relativeWinningPrice = relativeWinningPrice(newBid, winningBidMaxPotential);

            // check edge case for if the relativeWinningPrice is equal to the previous winningPrice.
            // if it is equal, add another autoincrement round since ties result in the original winner winning.
            if ((relativeWinningPrice.compareTo(winningBidMaxPotential)) == 0) {
                relativeWinningPrice = relativeWinningPrice.add(newBid.getAutoIncrement());
            }

            // update the newBid's winningPrice to simulate outbidding the losing bid.
            newBid.setWinningPrice(relativeWinningPrice);

            // replace the previous winningBid with newBid as the winningBid for this auction
            auctionWinners.put(newBid.getAuctionId(), newBid);
            return;
        }


        // Otherwise, both Bid's maximumPotentialBid bounds are too close. Calculate the true maximumPotentialBids
        // for both bids.
        // Compare and update the winningBid to the Bid with the greater value, and update its winningPrice to its
        // maximumPotentialBid.

        // Highest, valid bid original winningBid can make
        BigDecimal winningBidMaxPotential = maximumPotentialBid(winningBid);

        // Highest, valid bid newBid can make
        BigDecimal newBidMaxPotential = maximumPotentialBid(newBid);

        if (newBidMaxPotential.compareTo(winningBidMaxPotential) > 0) {
            newBid.setWinningPrice(newBidMaxPotential);
            auctionWinners.put(newBid.getAuctionId(), newBid);
        } else {
            winningBid.setWinningPrice(winningBidMaxPotential);
        }
    }

/////////////////////////////////////////////////// HELPER FUNCTIONS ///////////////////////////////////////////////////

    // This helper function determines if there is an obvious winner between the two compared Bids without having to
    // compute the maximumPotential values for both Bids. It performs a simple , naive check to see if the compared
    // Bids could have maximumPotential values close enough within range to warrant precise calculation of both
    // maximum. If the values are far enough, then there is a clear winner.
    //
    // rightBid maximumPotential <= rightBid.getMaxBid()
    // rightBidMaxUpperBound = rightBid.getMaxBid()
    //
    // (leftBid.getMaxBid() - leftBid.getAutoIncrement()) <= leftBid maximumPotential <= leftBid.getMaxBid()
    // leftBidMaxLowerBound  = leftBid.getMaxBid() - leftBid.getAutoIncrement()
    //
    // if (leftBidMaxLowerBound > rightBidMaxUpperBound) then left is a clear winner, return 1
    //
    // if (leftBidMaxLowerBound == rightBidMaxUpperBound) then leftBid maximumPotential >= rightBid maximumPotential,
    //      return 0
    //
    // if (leftBidMaxLowerBound < rightBidMaxUpperBound) then it is uncertain who is the winner, return -1

    private int lowerBoundMaxPotentialSafe(@NotNull Bid left, @NotNull Bid right) {
        return left.getMaxBid().subtract(left.getAutoIncrement()).compareTo(right.getMaxBid());
    }


    // To find the maximumPotentialBid, we need to find X
    // X is the amount of times to autoIncrement the bid prior to the bid exceeding its maximumBid value.
    //
    // maximumPotentialBid = startingBid + autoIncrement*X <= maximumBid
    //
    // Ex Bid: starting bid = $1, autoIncrement = $3, max bid = $5.
    // maximumPotentialBid = $1 + $3*X <= $5
    //
    // X <= 4/3. Since X is a whole number and maximum_potential_bid <= maximumBid, X = 1
    // maximumPotentialBid = $1 + $3*1 <= $5
    // maximumPotentialBid = $4 <= $5 = maximumBid
    //
    // X = ((maximumBid - startingBid) / autoIncrement)
    // maximumPotentialBid = startingBid + autoIncrement*((maximumBid - startingBid) / autoIncrement)

    private BigDecimal maximumPotentialBid(@NotNull Bid bid) {
        BigDecimal notLessThanOrEqualToZero = bid.getMaxBid().subtract(bid.getStartingBid());
        if ((new BigDecimal(0).compareTo(notLessThanOrEqualToZero)) == 0) {
            return bid.getStartingBid();
        }
        return notLessThanOrEqualToZero
                .divide(bid.getAutoIncrement(),0, RoundingMode.DOWN)
                .multiply(bid.getAutoIncrement()).add(bid.getStartingBid());
    }


    // To find the relativeWinningPrice for a winner Bid against a loser Bid, we need to find Y such that:
    //
    // relativeWinningPrice = currentWinningPrice + autoIncrement*Y <= max bid
    //
    // Y is a whole number representing the amount of rounds of incrementing needed for
    // winning Bid's currentWinningPrice >= loser Bid's loserMaximumPotential
    //
    // currentWinningPrice + autoIncrement * Y >= losersMaximumPotential
    // Y >= (losersMaximumPotential - currentWinningPrice) / autoIncrement
    //
    // Ex winner Bid: starting bid = $2, autoIncrement = $2, max bid = $7, currentWinningPrice = $2.
    //     loser Bid: loserMaxPotential = $4.
    //
    // relativeWinningPrice = $2 + $2*Y
    // Y >= ($4 - $2) / $2
    // Y >= 2
    // relativeWinningPrice = $2 + $2*2 = $6 <= $7 = max bid
    //
    // relativeWinningPrice = currentWinningPrice + autoIncrement*(
    //                        (losersMaximumPotential - currentWinningPrice) / autoIncrement)
    //
    // NOTE: relativeWinningPrice can be equal to current WinningPrice. If newBid is the winner, add another round
    // of autoIncrementing so there is no tie, allowing newBid to replace winningBid.

    private BigDecimal relativeWinningPrice(@NotNull Bid winnerBid, @NotNull BigDecimal loserMaxPotential) {
        BigDecimal notLessThanOrEqualToZero = loserMaxPotential.subtract(winnerBid.getWinningPrice());
        if ((new BigDecimal(0).compareTo(notLessThanOrEqualToZero)) > 0) {
            return winnerBid.getWinningPrice();
        }

        if ((new BigDecimal(0).compareTo(notLessThanOrEqualToZero)) == 0) {
            return winnerBid.getWinningPrice().add(winnerBid.getAutoIncrement());
        }

        return notLessThanOrEqualToZero
                .divide(winnerBid.getAutoIncrement(),0, RoundingMode.UP)
                .multiply(winnerBid.getAutoIncrement()).add(winnerBid.getWinningPrice());
    }


    private boolean isNullOrContainsAnyNullFields(Bid bid) {
        return (bid == null || bid.getId() == null || bid.getStartingBid() == null ||
                bid.getMaxBid() == null || bid.getAutoIncrement() == null || bid.getAuctionId() == null ||
                bid.getWinningPrice() == null);
    }
}
