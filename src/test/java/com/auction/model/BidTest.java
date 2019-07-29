package com.auction.model;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BidTest {

    private static final String ID = "JohnDoe123";
    private static final String STARTING_BID = "$2.00";
    private static final String MAX_BID = "$14.00";
    private static final String AUTO_INCREMENT = "$1.50";
    private static final Integer AUCTION_ID = 1;
    private static final BigDecimal STARTING_BID_DEC = new BigDecimal(2.00);
    private static final BigDecimal MAX_BID_DEC = new BigDecimal(14);
    private static final BigDecimal AUTO_INCREMENT_DEC = new BigDecimal(1.50);
    private static final String AUTO_INCREMENT_BAD = "$1.00";
    private static final String NEG_STARTING_BID = "-$11.00";
    private static final String SMALL_MAXIMUM_BID = "$1.00";
    private static final Integer AUCTION_ID_NEG = -1;

    private Bid bid;

    @Before
    public void setup() throws Exception {
        this.bid = new Bid(ID, STARTING_BID, MAX_BID, AUTO_INCREMENT, AUCTION_ID);
    }

    @Test
    public void constructBidAllProperties() {
        assertEquals(bid.getId(), ID);
        assertEquals(bid.getStartingBid().compareTo(STARTING_BID_DEC), 0);
        assertEquals(bid.getMaxBid().compareTo(MAX_BID_DEC), 0);
        assertEquals(bid.getAutoIncrement().compareTo(AUTO_INCREMENT_DEC), 0);
        assertEquals(bid.getAuctionId(), AUCTION_ID);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInput() throws Exception {
        Bid bid = new Bid(null, null, null, null, null);
    }

    @Test(expected = IOException.class)
    public void testNegativeInput() throws Exception {
        Bid bid = new Bid(ID, NEG_STARTING_BID, MAX_BID, AUTO_INCREMENT, AUCTION_ID);
    }

    @Test
    public void testEqualBids() throws Exception {
        Bid other = new Bid(ID, STARTING_BID, MAX_BID, AUTO_INCREMENT, AUCTION_ID);
        assertEquals(bid, other);
    }

    @Test
    public void testUnequalBids() throws Exception {
        Bid other = new Bid(ID, STARTING_BID, MAX_BID, AUTO_INCREMENT_BAD, AUCTION_ID);
        assertNotEquals(bid, other);
    }

    @Test(expected = IOException.class)
    public void testMaximumBidLessThanStartingBid() throws Exception {
        Bid bid = new Bid(ID, STARTING_BID, SMALL_MAXIMUM_BID, AUTO_INCREMENT, AUCTION_ID);
    }

    @Test(expected = IOException.class)
    public void testAuctionIdIOException() throws Exception {
        Bid bid = new Bid(ID, STARTING_BID, MAX_BID, AUTO_INCREMENT, AUCTION_ID_NEG);
    }

}
