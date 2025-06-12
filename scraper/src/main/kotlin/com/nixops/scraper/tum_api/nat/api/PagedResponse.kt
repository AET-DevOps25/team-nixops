package com.nixops.scraper.tum_api.nat.api

import com.fasterxml.jackson.annotation.JsonProperty

data class PagedResponse<T>(
    @JsonProperty("hits")
    val hits: List<T>,

    @JsonProperty("count")
    val count: Int,

    @JsonProperty("total_count")
    val totalCount: Int,

    @JsonProperty("offset")
    val offset: Int,

    @JsonProperty("next_offset")
    val nextOffset: Int?
)