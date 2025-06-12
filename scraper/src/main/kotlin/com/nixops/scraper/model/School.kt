package com.nixops.scraper.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "school")
data class School(
    @Id
    @Column(name = "org_id")
    var orgId: Int = 0,

    @Column(name = "org_name", nullable = false)
    var orgName: String = "",

    @Column(name = "org_name_en", nullable = false)
    var orgNameEn: String = "",

    @Column(name = "org_type", nullable = false)
    var orgType: String = "",

    @Column(name = "org_code")
    var orgCode: String? = null,

    @Column(name = "org_url")
    var orgUrl: String? = null,

    @Column(name = "org_nameshort")
    var orgNameshort: String? = null,

    @Column(name = "org_nameshort_en")
    var orgNameshortEn: String? = null,

    @Column(name = "org_email")
    var orgEmail: String? = null,

    @Column(name = "deleted")
    var deleted: OffsetDateTime? = null
)
