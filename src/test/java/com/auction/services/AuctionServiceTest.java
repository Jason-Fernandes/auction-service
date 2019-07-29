package com.auction.services;

import com.auction.model.Bid;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import javax.ws.rs.WebApplicationException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class AuctionServiceTest {

    private final String NO_ENTRIES = "No entries.";
    private final int EQUAL = 0;
    private AuctionService auctionService = new AuctionService();

    @BeforeEach
    public void setup() {
    }

    /////////////////////////////////////////////
    // Get Winner //
    /////////////////

    // Get Winner of actionId 1 given that the bid exists.
    @Test
    public void testGetWinnerBidExists() throws Exception {
        Bid bid = new Bid("Tom", "$200.0", "$1,000", "10.51", 1);
        auctionService.processBid(bid);
        Bid resultBid = auctionService.getWinner(bid.getAuctionId());
        assertEquals(bid, resultBid);
    }

    // Get Different Winner from autionId 2 given that the bid exists.
    @Test
    public void testGetDifferentWinnerInDifferentAuction() throws Exception {
        Bid bid = new Bid("Janet", "$1,000.0", "$1,020", "9.23", 2);
        auctionService.processBid(bid);
        Bid resultBid = auctionService.getWinner(bid.getAuctionId());
        assertEquals(bid, resultBid);
    }

    // Get another bid from same bidderId from  Auction Id 3 given that the bid exists.
    @Test
    public void testGetSameWinnerBidExists() throws Exception {
        Bid bid = new Bid("Tom", "$200.0", "$1,000", "10.51", 3);
        auctionService.processBid(bid);
        Bid resultBid = auctionService.getWinner(bid.getAuctionId());
        assertEquals(bid, resultBid);
    }

    // Get Winner of an auction give that the bid does not exist.
    @Test
    public void testGetWinnerBidDoesNotExists() throws Exception {
        Bid noWinnerBid = new Bid("No entries.", "0", "0", "0", 28);
        Bid resultBid = auctionService.getWinner(28);
        assertEquals(noWinnerBid, resultBid);
    }

    // Get Winner without any auctionId.
    @Test(expected = WebApplicationException.class)
    public void testGetWinnerNullAuctionId() throws Exception {
        auctionService.getWinner(null);
    }

    // Get Winner with negative auctionId.
    @Test(expected = WebApplicationException.class)
    public void testGetWinnerNegativeAuctionId() throws Exception {
        auctionService.getWinner(-3);
    }

    /////////////////////////////////////////////
    // Process Bid //
    /////////////////

    // Initialize Winner. Previous winner is null entry.
    @Test
    public void testInitializeWinner() throws Exception {
        Bid noWinnerBid = new Bid("No entries.", "0", "0", "0", 4);
        Bid bid = new Bid("Jane", "$200.0", "$1,000", "10.51", 4);
        Bid originalBid = auctionService.getWinner(bid.getAuctionId());
        auctionService.processBid(bid);
        Bid winnerBid = auctionService.getWinner(bid.getAuctionId());
        assertEquals(noWinnerBid, originalBid);
        assertEquals(bid, winnerBid);
    }

    // Duplicate entry comes in with same information.
    @Test
    public void testDupeBidEntry() throws Exception {
        Bid sameBid = new Bid("Alex132", "$2", "$6", "$.50", 5);
        Bid resultBidOne = auctionService.getWinner(sameBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(sameBid);
        Bid resultBidTwo = auctionService.getWinner(sameBid.getAuctionId());
        assertNotEquals(resultBidOne, resultBidTwo);

        auctionService.processBid(sameBid);
        Bid resultBidThree = auctionService.getWinner(sameBid.getAuctionId());
        assertEquals(resultBidTwo, resultBidThree);
    }


    // Same bidderId, different bid information comes in.
    @Test
    public void testSameBidIdDifferentValues() throws Exception {
        Bid originalBid = new Bid("John", "$2", "$6", "$.50", 6);
        Bid updatedBid = new Bid("John", "$2", "$8", "$2", 6);
        Bid resultBidOne = auctionService.getWinner(originalBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(originalBid);
        Bid resultBidTwo = auctionService.getWinner(originalBid.getAuctionId());
        assertNotEquals(resultBidOne, resultBidTwo);
        assertEquals(resultBidTwo.getWinningPrice(), new BigDecimal(2));

        auctionService.processBid(updatedBid);
        Bid resultBidThree = auctionService.getWinner(originalBid.getAuctionId());
        assertNotEquals(resultBidTwo, resultBidThree);
        assertEquals(resultBidThree.getId(), resultBidTwo.getId());
        assertEquals(resultBidThree.getStartingBid(), resultBidTwo.getStartingBid());
        assertEquals(resultBidThree.getWinningPrice(), new BigDecimal(8));
    }


    // NewBid's maximumBid is negligible compared to current winningBid.
    @Test
    public void testNewBidCannotOverTakeWinningBid() throws  Exception {
        Bid winningBid = new Bid("Winner7", "$100.00", "$500.000", "2", 7);
        Bid smallBid = new Bid("Loser7", "50", "$90.00", "$25.00", 7);
        auctionService.processBid(winningBid);
        auctionService.processBid(smallBid);
        Bid result = auctionService.getWinner(winningBid.getAuctionId());

        assertEquals(result, winningBid);

        // Overridden equals method for Bid does not compare .winningPrice field.
        assertEquals(result.getWinningPrice(), winningBid.getWinningPrice());
    }

    // NewBid's maximumPotentialBid causes winningBid to update its price relative to NewBid's maximumPotentialBid.
    @Test
    public void testSameWinnerUpdateRelativeWinningPrice() throws  Exception {
        Bid winningBid = new Bid("Winner8", "2", "10", "2", 8);
        Bid smallBid = new Bid("Loser8", "0", "4", "4", 8);
        Bid resultBidOne = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(winningBid);
        Bid resultBidThree = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(new BigDecimal(2).compareTo(resultBidThree.getWinningPrice()), EQUAL);

        auctionService.processBid(smallBid);
        Bid resultBidTwo = auctionService.getWinner(winningBid.getAuctionId());

        assertEquals(winningBid, resultBidTwo);
        assertEquals(new BigDecimal(4).compareTo(resultBidTwo.getWinningPrice()), EQUAL);
    }


    // Tie. NewBid's maximumPotentialBid causes winningBid to update its price relative to NewBid's maximumPotentialBid.
    @Test
    public void testTieSameWinnerUpdateRelativeWinningPrice() throws Exception {
        Bid winningBid = new Bid("Winner9", "2", "6", "2", 9);
        Bid smallBid = new Bid("Loser9", "2", "4", "2", 9);
        Bid resultBidOne = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(winningBid);
        Bid resultBidThree = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(new BigDecimal(2).compareTo(resultBidThree.getWinningPrice()), EQUAL);

        auctionService.processBid(smallBid);
        Bid resultBidTwo = auctionService.getWinner(winningBid.getAuctionId());

        assertEquals(winningBid, resultBidTwo);
        assertEquals(new BigDecimal(4).compareTo(resultBidTwo.getWinningPrice()), EQUAL);
    }

    // NewBid's maximumPotential overtakes Winning Bid's maxBid. Relatively update winner and winningPrice.
    @Test
    public void testReplaceWinnerUpdateRelativeWinningPrice() throws Exception {
        Bid winningBid = new Bid("Winner10", "2", "6", "2", 10);
        Bid newBid = new Bid("newBid10", "2", "15", "2", 10);
        Bid resultBidOne = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(winningBid);
        Bid resultBidThree = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(new BigDecimal(2).compareTo(resultBidThree.getWinningPrice()), EQUAL);

        auctionService.processBid(newBid);
        Bid resultBidTwo = auctionService.getWinner(winningBid.getAuctionId());

        assertEquals(newBid, resultBidTwo);
        assertEquals(new BigDecimal(8).compareTo(resultBidTwo.getWinningPrice()), EQUAL);
    }

    // Newbid and winningBid's maximumPotentials are compared. Winner stays winner. WinningPrice updates.
    @Test
    public void testSameWinnerUpdateMaximumWinningPrice() throws Exception {
        Bid winningBid = new Bid("Winner11", "2", "16", "2", 11);
        Bid newBid = new Bid("newBid11", "2", "15", "2", 11);
        Bid resultBidOne = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(winningBid);
        Bid resultBidThree = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(new BigDecimal(2).compareTo(resultBidThree.getWinningPrice()), EQUAL);

        auctionService.processBid(newBid);
        Bid resultBidTwo = auctionService.getWinner(winningBid.getAuctionId());

        assertEquals(winningBid, resultBidTwo);
        assertEquals(new BigDecimal(16).compareTo(resultBidTwo.getWinningPrice()), EQUAL);

    }

    // Newbid and winningBid's maximumPotentials tie. Winner stays winner. WinningPrice updates.
    @Test
    public void testTieSameWinnerUpdateMaximumWinningPrice() throws Exception {
        Bid winningBid = new Bid("Winner12", "2", "16", "2", 12);
        Bid newBid = new Bid("newBid12", "2", "16", "2", 12);
        Bid resultBidOne = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(winningBid);
        Bid resultBidThree = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(new BigDecimal(2).compareTo(resultBidThree.getWinningPrice()), EQUAL);

        auctionService.processBid(newBid);
        Bid resultBidTwo = auctionService.getWinner(winningBid.getAuctionId());

        assertEquals(winningBid, resultBidTwo);
        assertEquals(new BigDecimal(16).compareTo(resultBidTwo.getWinningPrice()), EQUAL);

    }

    // Newbid and winningBid's maximumPotentials are compared. Newbid becomes winner. WinningPrice updates.
    @Test
    public void testNewWinnerUpdateMaximumWinningPrice() throws Exception {
        Bid winningBid = new Bid("Winner11", "6", "17", "2", 11);
        Bid newBid = new Bid("newBid11", "1", "18", "2", 11);
        Bid resultBidOne = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(resultBidOne.getId(), NO_ENTRIES);

        auctionService.processBid(winningBid);
        Bid resultBidThree = auctionService.getWinner(winningBid.getAuctionId());
        assertEquals(new BigDecimal(6).compareTo(resultBidThree.getWinningPrice()), EQUAL);

        auctionService.processBid(newBid);
        Bid resultBidTwo = auctionService.getWinner(winningBid.getAuctionId());

        assertEquals(newBid, resultBidTwo);
        assertEquals(new BigDecimal(17).compareTo(resultBidTwo.getWinningPrice()), EQUAL);
    }

}
