package com.auction.resources;

import com.codahale.metrics.annotation.Timed;
import com.auction.api.ProcessedBid;
import com.auction.api.Winner;
import com.auction.model.Bid;
import com.auction.services.AuctionService;
import org.eclipse.jetty.http.HttpStatus;

import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("/auction")
@Produces(MediaType.APPLICATION_JSON)
public class AuctionResource {
    private final AuctionService auctionService;;

    public AuctionResource(AuctionService auctionService) {
        this.auctionService = auctionService;
    }


    @GET
    @Timed
    public Winner getWinner(@QueryParam("auctionId") @Min(0) @Valid Integer auctionId) {
        Bid winner = auctionService.getWinner(auctionId);
        return new Winner(HttpStatus.OK_200, winner.getId(), winner.getWinningPrice(), winner.getAuctionId());
    }


    @POST
    @Timed
    public ProcessedBid createBid(@NotNull @Valid Bid bid) {
        auctionService.processBid(bid);
        return new ProcessedBid(HttpStatus.CREATED_201, bid);
    }
}