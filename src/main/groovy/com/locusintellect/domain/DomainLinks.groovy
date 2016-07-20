package com.locusintellect.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder

@EqualsAndHashCode
@ToString
@Builder
class DomainLinks {

    String domain
    Map<String, DomainLinks> linksWithinDomain
    Set<String> externalLinks
    Set<String> staticContentLinks
}
