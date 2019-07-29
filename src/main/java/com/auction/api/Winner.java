package com.auction.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Winner {

    private Long code;

    private String bidderId;

    private String winningPrice;

    private Integer auctionId;

    public Winner() {
        // Jackson deserialization
    }

    public Winner(long code, String bidderId, BigDecimal winningPrice, Integer auctionId) {
        DecimalFormat df = new DecimalFormat("#,###.00");
        this.code = code;
        this.bidderId = bidderId;
        this.winningPrice = "$" + df.format(winningPrice);
        this.auctionId = auctionId;
    }

    @JsonProperty
    public long getCode() {
        return code;
    }

    @JsonProperty
    public String getBidderId(){
        return bidderId;
    }

    @JsonProperty
    public String getWinningPrice() {
        return winningPrice;
    }

    @JsonProperty
    public Integer getAuctionId() {
        return auctionId;
    }
}
