package com.auction.health;

import com.codahale.metrics.health.HealthCheck;
import com.auction.api.Winner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class AuctionServiceAppHealthCheck extends HealthCheck {

    private final Client client;

    private static final String UNHEALTHY = "Auction Service Failed";

    public AuctionServiceAppHealthCheck(Client client) {
        super();
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {
        WebTarget webTarget = client.target("http://localhost:8080/auction").queryParam("auctionId","0");
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();
        @SuppressWarnings("rawtypes")
        Winner winner = response.readEntity(Winner.class);
        if((winner !=null) && (winner.getBidderId() != null) && (!winner.getBidderId().isEmpty())){
            return Result.healthy();
        }
        return Result.unhealthy(UNHEALTHY);
    }
}