package com.auction.api;

import com.auction.model.Bid;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ProcessedBidTest {

    private static final String ID = "JohnDoe123";
    private static final String STARTING_BID = "$2.00";
    private static final String MAX_BID = "$14.00";
    private static final String AUTO_INCREMENT = "$1.50";
    private static final Integer AUCTION_ID = 1;
    private static final BigDecimal STARTING_BID_DEC = new BigDecimal(2.00);
    private static final BigDecimal MAX_BID_DEC = new BigDecimal(14);
    private static final BigDecimal AUTO_INCREMENT_DEC = new BigDecimal(1.50);

    private ProcessedBid processedBid;

    @Before
    public void setup() throws Exception {
        Bid bid = new Bid(ID, STARTING_BID, MAX_BID, AUTO_INCREMENT, AUCTION_ID);
        this.processedBid = new ProcessedBid(HttpStatus.CREATED_201, bid);
    }

    @Test
    public void testEquals() {
        assertEquals(processedBid.getCode(), HttpStatus.CREATED_201);
        Bid bid = processedBid.getBid();
        assertEquals(bid.getId(), ID);
        assertEquals(bid.getStartingBid().compareTo(STARTING_BID_DEC), 0);
        assertEquals(bid.getMaxBid().compareTo(MAX_BID_DEC), 0);
        assertEquals(bid.getAutoIncrement().compareTo(AUTO_INCREMENT_DEC), 0);
        assertEquals(bid.getAuctionId(), AUCTION_ID);
    }
}
