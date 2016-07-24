package com.locusintellect

import com.google.common.base.Strings
import com.locusintellect.crawler.WebCrawler
import com.locusintellect.domain.DomainLinks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WebCrawlerHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WebCrawlerHandler)

    private final String startingUrl
    private final WebCrawler crawler
    DomainLinks domainLinks

    @Autowired
    public WebCrawlerHandler(@Value('${starting.url}') final String startingUrl, final WebCrawler crawler) {
        this.startingUrl = startingUrl
        this.crawler = crawler
    }

    public void handle() {
        validateDomainUrl(startingUrl)
        domainLinks = crawler.crawlDomainForLinks(startingUrl, "/", [] as Set)
        domainLinks = mergeDomainLinks(domainLinks, null)
        printSiteMap(domainLinks)
    }

    // todo move to a separate class specific to view
    private void printSiteMap(final DomainLinks domainLinks) {
        println("Site map for domain ${domainLinks.domain}")
        println("Links within domain:")
        domainLinks.linksWithinDomain.each {
            println("\t\t${it.key}")
        }
        println("External links:")
        domainLinks.externalLinks.each {
            println("\t\t${it}")
        }
        println("Static contents:")
        domainLinks.staticContentLinks.each {
            println("\t\t${it}")
        }
    }

    // todo move to a separate class specific to view
    private DomainLinks mergeDomainLinks(final DomainLinks domainLinks, DomainLinks mergedDomainLinks) {
        if (!mergedDomainLinks) {
            mergedDomainLinks = new DomainLinks().builder().domain(domainLinks.domain)
                                                 .linksWithinDomain([:])
                                                 .externalLinks([] as Set)
                                                 .staticContentLinks([] as Set).build()
        }

        if (domainLinks) {
            mergedDomainLinks.externalLinks.addAll(domainLinks.externalLinks)
            mergedDomainLinks.staticContentLinks.addAll(domainLinks.staticContentLinks)

            domainLinks.linksWithinDomain.each { key, value ->
                domainLinks.linksWithinDomain.each {
                    mergedDomainLinks.linksWithinDomain.put(it.key, null)
                }

                mergeDomainLinks(value, mergedDomainLinks)
            }
        }

        return mergedDomainLinks
    }

    private void validateDomainUrl(final String domainUrl) {
        if (Strings.isNullOrEmpty(domainUrl) || domainUrl.trim().isEmpty()) {
            LOG.error("Property starting.url is either null or empty.")
            throw new MissingRequiredPropertiesException('Missing required property starting.url')
        }

    }

}
