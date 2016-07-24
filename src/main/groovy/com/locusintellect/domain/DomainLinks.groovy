package com.locusintellect.domain

import groovy.transform.Canonical
import groovy.transform.builder.Builder

@Canonical
@Builder
class DomainLinks {

    String domain
    Map<String, DomainLinks> linksWithinDomain
    Set<String> externalLinks
    Set<String> staticContentLinks

    public static DomainLinks EMPTY_DOMAIN(final String domainUrl) {
        return builder().domain(domainUrl)
                        .linksWithinDomain([:])
                        .externalLinks([] as Set)
                        .staticContentLinks([] as Set).build()
    }
}
