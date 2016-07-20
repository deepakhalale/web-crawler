package com.locusintellect.crawler

import com.locusintellect.domain.DomainLinks
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class WebCrawler {

    private static final Logger LOG = LoggerFactory.getLogger(WebCrawler)

    private final PageCrawler pageCrawler

    @Autowired
    public WebCrawler(final PageCrawler pageCrawler) {
        this.pageCrawler = pageCrawler
    }

    public DomainLinks crawlDomainForLinks(final String domainUrl, final String relativeUrl,
                                           final Set<String> visitedPages) {
        DomainLinks domainLinks = null
        final String url = buildUrl(domainUrl, relativeUrl)

        if (domainUrl && !visitedPages.contains(url)) {
            LOG.debug("Visiting page ${url}")
            domainLinks = pageCrawler.crawlPageForLinks(url)
            LOG.info("No. of links within domain ${url}: ${domainLinks?.linksWithinDomain?.size()}")
            visitedPages.add(url)

            // Delay to avoid huge load on servers
            // todo duration need to be configurable.
            Thread.sleep(200)

            domainLinks.linksWithinDomain.each { key, value ->
                DomainLinks links = crawlDomainForLinks(domainUrl, key, visitedPages)
                domainLinks.linksWithinDomain.put(key, links)
            }
        }

        return domainLinks
    }

    private static String buildUrl(String... components) {
        return components.collect { it.replaceAll('^/|/$', '') }.join('/')
    }

}
