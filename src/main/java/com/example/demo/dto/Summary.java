package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Summary {

    @JsonProperty("total_elements_received")
    private int totalElementsReceived;

    @JsonProperty("valid_elements_processed")
    private int validElementsProcessed;

    @JsonProperty("invalid_elements_ignored")
    private int invalidElementsIgnored;

    public Summary() {}

    public Summary(int totalElementsReceived, int validElementsProcessed, int invalidElementsIgnored) {
        this.totalElementsReceived = totalElementsReceived;
        this.validElementsProcessed = validElementsProcessed;
        this.invalidElementsIgnored = invalidElementsIgnored;
    }

    public int getTotalElementsReceived() { return totalElementsReceived; }
    public int getValidElementsProcessed() { return validElementsProcessed; }
    public int getInvalidElementsIgnored() { return invalidElementsIgnored; }
}