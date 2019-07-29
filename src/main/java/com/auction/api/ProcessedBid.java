package com.auction.api;

import org.hibernate.validator.constraints.Length;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.auction.model.Bid;

public class ProcessedBid {
    private long code;

    @Length(max = 5)
    private Bid bid;

    public ProcessedBid() {
        // Jackson deserialization
    }

    public ProcessedBid(long code, Bid bid) {
        this.code = code;
        this.bid = bid;
    }

    @JsonProperty
    public long getCode() {
        return code;
    }

    @JsonProperty
    public Bid getBid() {
        return bid;
    }
}