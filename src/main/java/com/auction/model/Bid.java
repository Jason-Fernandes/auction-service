package com.auction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.math.BigDecimal;


public class Bid {

    @NotBlank
    private final String id;

    @DecimalMin("0")
    private final BigDecimal startingBid;

    @DecimalMin("0")
    private final BigDecimal maxBid;

    @DecimalMin("0")
    private final BigDecimal autoIncrement;

    @Min(0)
    private final Integer auctionId;

    private BigDecimal winningPrice;


    @JsonProperty("bidderId")
    public String getId() {
        return id;
    }

    @JsonProperty("startingBid")
    public BigDecimal getStartingBid() {
        return startingBid;
    }

    @JsonProperty("maxBid")
    public BigDecimal getMaxBid() {
        return maxBid;
    }

    @JsonProperty("autoIncrement")
    public BigDecimal getAutoIncrement() {
        return autoIncrement;
    }

    @JsonProperty("auctionId")
    public Integer getAuctionId() {
        return auctionId;
    }

    @JsonIgnore
    public BigDecimal getWinningPrice() {
        return winningPrice;
    }

    public void setWinningPrice(BigDecimal winningPrice) {
        this.winningPrice = winningPrice;
    }

    @JsonCreator
    public Bid(@JsonProperty("bidderId") String id,
               @JsonProperty("startingBid") String startingBid,
               @JsonProperty("maxBid") String maxBid,
               @JsonProperty("autoIncrement") String autoIncrement,
               @JsonProperty("auctionId") Integer auctionId) throws  Exception {
        if (id == null || startingBid == null || maxBid == null || autoIncrement == null || auctionId == null) {
            throw new NullPointerException("Null field(s) detected.");
        }
        this.id = id;
        this.startingBid = new BigDecimal(startingBid.replaceAll("[$,]", ""));
        if (this.startingBid.signum() < 0) {
            throw new IOException("Starting bid is negative.");
        }
        this.maxBid = new BigDecimal(maxBid.replaceAll("[$,]", ""));
        if (this.maxBid.signum() < 0) {
            throw new IOException("Maximum bid is negative.");
        }

        if ((this.maxBid.compareTo(this.startingBid)) < 0) {
            throw new IOException("Maximum bid is less than starting bid");
        }

        this.autoIncrement = new BigDecimal(autoIncrement.replaceAll("[$,]", ""));
        if (this.autoIncrement.signum() < 0) {
            throw new IOException("Auto-increment is negative.");
        }

        if (auctionId < 0) {
            throw new IOException("AuctionId is invalid. Must be a positive, whole number.");
        }
        this.auctionId = auctionId;
        this.winningPrice = this.startingBid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!Bid.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Bid other = (Bid) obj;

        if ((this.getId() == null) ? (other.getId() != null) : !this.getId().equals(other.getId())) {
            return false;
        }

        if (this.getStartingBid().compareTo(other.getStartingBid()) != 0) {
            return false;
        }

        if (this.getMaxBid().compareTo(other.getMaxBid()) != 0) {
            return false;
        }

        if (this.getAutoIncrement().compareTo(other.getAutoIncrement()) != 0) {
            return false;
        }

        return true;
    }
}
