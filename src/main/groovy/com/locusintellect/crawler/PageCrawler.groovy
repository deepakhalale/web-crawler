package com.locusintellect.crawler

import com.locusintellect.DelayedRetryExecutionWrapper
import com.locusintellect.domain.DomainLinks
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PageCrawler {

    private static final Logger LOG = LoggerFactory.getLogger(PageCrawler)

    public DomainLinks crawlPageForLinks(final String url) {
        final Document document
        try {
            document = new DelayedRetryExecutionWrapper<Document>().execute() {
                return Jsoup.connect(url).get()
            }
        } catch (HttpStatusException e) {
            LOG.warn("Could not fetch ${url}. Reason: ${e.getMessage()}")
            return new DomainLinks().builder().domain(url)
                                    .linksWithinDomain([:])
                                    .externalLinks([] as Set)
                                    .staticContentLinks([] as Set).build()
        }

        final def (Map<String, DomainLinks> linksWithinDomain, Set<String> externalLinks) = getLinksWithinDomainAndExternalLinks(document, url)

        final Set<String> staticContents = getStaticContentsFrom(document)

        return new DomainLinks().builder().domain(url)
                                .linksWithinDomain(linksWithinDomain)
                                .externalLinks(externalLinks)
                                .staticContentLinks(staticContents).build()
    }

    private List getLinksWithinDomainAndExternalLinks(final Document document, final String url) {
        String linkValue
        Map<String, DomainLinks> linksWithinDomain = [:]
        Set<String> externalLinks = []
        final URL domainUrl = new URL(url)
        final String startingDomainUrl = "${domainUrl.protocol}://${domainUrl.host}"

        Elements links = document.select("a[href]")

        LOG.debug("All links:")
        links.each {
            linkValue = it.attr('href')
            LOG.debug("${linkValue}")
            if (linkValue.matches("^/.+")) {
                linksWithinDomain.put(linkValue, null)
            } else if (linkValue.matches("^(http|https).+") && !linkValue.startsWith(startingDomainUrl)) {
                externalLinks.add(linkValue)
            }
        }
        return [linksWithinDomain, externalLinks]
    }

    private Set<String> getStaticContentsFrom(final Document document) {
        String linkValue
        String srcValue

        Elements media = document.select('[src]')
        Elements imports = document.select("link[href]")

        LOG.debug("All static contents:")
        Set<String> staticContents = []
        media.each {
            srcValue = it.attr('src')
            LOG.debug("${srcValue}")
            staticContents.add(srcValue)
        }
        imports.each {
            linkValue = it.attr('href')
            LOG.debug("${linkValue}")
            staticContents.add(linkValue)
        }
        return staticContents
    }
}
