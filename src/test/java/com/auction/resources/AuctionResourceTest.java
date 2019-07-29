package com.auction.resources;

import com.auction.api.ProcessedBid;
import com.auction.api.Winner;
import com.auction.model.Bid;
import com.auction.services.AuctionService;
import io.dropwizard.testing.junit.ResourceTestRule;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AuctionResourceTest {

    private static final String ID = "JohnDoe123";
    private static final String STARTING_BID = "$2.00";
    private static final String MAX_BID = "$14.00";
    private static final String AUTO_INCREMENT = "$1.50";
    private static final Integer AUCTION_ID = 1;
    private static final BigDecimal STARTING_BID_DEC = new BigDecimal(2.00);
    private static final BigDecimal MAX_BID_DEC = new BigDecimal(14);
    private static final BigDecimal AUTO_INCREMENT_DEC = new BigDecimal(1.50);



    private static final AuctionService service = Mockito.mock(AuctionService.class);

    @Rule
    public final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new AuctionResource(service))
            .build();
    private Bid bid;


    @Before
    public void setUp() throws Exception {
        this.bid = new Bid(ID, STARTING_BID, MAX_BID, AUTO_INCREMENT, AUCTION_ID);
    }

    @Test
    public void testGetWinner() {
        when(service.getWinner(AUCTION_ID)).thenReturn(this.bid);

        final Response response = RESOURCES.target("/auction")
                .queryParam("auctionId", "1")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();
        Winner winner = response.readEntity(Winner.class);

        assertEquals(winner.getCode(), HttpStatus.OK_200);
        assertEquals(winner.getBidderId(), ID);
        assertEquals(winner.getWinningPrice(), STARTING_BID);
        assertEquals(winner.getAuctionId(), AUCTION_ID);
    }

    @Test
    public void testCreateBid() {
        Mockito.doNothing().when(service).processBid(bid);

        final Response response = RESOURCES.target("/auction")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(bid, MediaType.APPLICATION_JSON_TYPE));
        ProcessedBid processedBid = response.readEntity(ProcessedBid.class);
        Bid responseBid = processedBid.getBid();

        assertEquals(processedBid.getCode(), HttpStatus.CREATED_201);
        assertEquals(responseBid.getId(), ID);
        assertEquals(responseBid.getStartingBid().compareTo(STARTING_BID_DEC), 0);
        assertEquals(responseBid.getMaxBid().compareTo(MAX_BID_DEC), 0);
        assertEquals(responseBid.getAutoIncrement().compareTo(AUTO_INCREMENT_DEC), 0);
        assertEquals(responseBid.getAuctionId(), AUCTION_ID);
    }
}
