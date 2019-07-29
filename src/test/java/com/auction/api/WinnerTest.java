package com.auction.api;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class WinnerTest {

    private static final String BIDDER_ID = "Unique Bidder Id";
    private static final String WINNING_PRICE_STR = ("$1,000.00");
    private static final BigDecimal WINNING_PRICE_DEC = new BigDecimal(1000.00);
    private static final Integer AUCTION_ID = 1;

    private Winner winner;

    @Before
    public void setup() {
        this.winner = new Winner(HttpStatus.OK_200, BIDDER_ID, WINNING_PRICE_DEC, AUCTION_ID);
    }

    @Test
    public void testEquals() {
        assertEquals(this.winner.getCode(), HttpStatus.OK_200);
        assertEquals(this.winner.getBidderId(), BIDDER_ID);
        assertEquals(this.winner.getWinningPrice(), WINNING_PRICE_STR);
        assertEquals(this.winner.getAuctionId(), AUCTION_ID);
    }
}