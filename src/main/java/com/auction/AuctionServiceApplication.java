package com.auction;

import com.auction.health.AuctionServiceAppHealthCheck;
import com.auction.resources.AuctionResource;
import com.auction.services.AuctionService;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;


public class AuctionServiceApplication extends Application<AuctionServiceConfiguration> {

    public static void main(final String[] args) throws Exception {
        new AuctionServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "AuctionService";
    }

    @Override
    public void initialize(final Bootstrap<AuctionServiceConfiguration> bootstrap) {
    }

    @Override
    public void run(final AuctionServiceConfiguration configuration,
                    final Environment environment) throws Exception {
        final Client client = new JerseyClientBuilder(environment).build("HealthRESTClient");
        environment.healthChecks().register("APIHealthCheck", new AuctionServiceAppHealthCheck(client));
        final AuctionService auctionService = new AuctionService();
        final AuctionResource resource = new AuctionResource(auctionService);
        environment.jersey().register(resource);
    }

}
