package com.locusintellect.crawler

import com.locusintellect.domain.DomainLinks
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains
import static org.hamcrest.core.Is.is
import static org.mockito.BDDMockito.given


@RunWith(MockitoJUnitRunner)
class WebCrawlerTest {

    private static final String STARTING_URL = "http://127.0.0.1"

    @Mock
    PageCrawler pageCrawler

    WebCrawler underTest

    @Before
    public void setUp() {
        underTest = new WebCrawler(pageCrawler)
    }

    @Test
    public void shouldVisitAllPagesForADomain() {
        Set<String> visitedPages = []
        def mainDomain = new DomainLinks()
        mainDomain.setDomain(STARTING_URL)
        mainDomain.setLinksWithinDomain(["/blog": null])
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/")).willReturn(mainDomain)

        def blogLinks = new DomainLinks()
        blogLinks.setDomain("${STARTING_URL}/blog")
        blogLinks.setLinksWithinDomain([:])
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/blog")).willReturn(blogLinks)

        underTest.crawlDomainForLinks(STARTING_URL, "/", visitedPages)

        assertThat(visitedPages, contains("http://127.0.0.1/", "http://127.0.0.1/blog"))
    }

    @Test
    public void shouldReturnDomainLinksForVisitedPages() {
        givenNestedDomainLinksExistForRootDomain()

        DomainLinks actualDomainLinks = underTest.crawlDomainForLinks(STARTING_URL, "/", [] as Set)

        assertDomainLinksToBeSame(actualDomainLinks, getExpectedNestedDomainLinks())
    }

    private DomainLinks getExpectedNestedDomainLinks() {
        DomainLinks contactDomain = new DomainLinks().builder()
                                                     .domain("${STARTING_URL}/contact")
                                                     .staticContentLinks(["/contact/static/link"] as Set)
                                                     .externalLinks(["https://google.com/builditdigital"] as Set)
                                                     .linksWithinDomain(["/contact1": emptyDomainFor("${STARTING_URL}/contact1")])
                                                     .build()

        DomainLinks blogDomain = new DomainLinks().builder()
                                                  .domain("${STARTING_URL}/blog")
                                                  .staticContentLinks(["/blog/static/link"] as Set)
                                                  .externalLinks(["https://google.com/builditdigital"] as Set)
                                                  .linksWithinDomain(["/blog1": emptyDomainFor("${STARTING_URL}/blog1"), "blog2": emptyDomainFor("${STARTING_URL}/blog2")])
                                                  .build()

        DomainLinks paymentDomain = new DomainLinks().builder()
                                                     .domain("${STARTING_URL}/payment")
                                                     .staticContentLinks(["/frontend/Styles/style.css"] as Set)
                                                     .externalLinks(["https://twitter.com/builditdigital"] as Set)
                                                     .linksWithinDomain(["/contact": contactDomain, "/blog": blogDomain])
                                                     .build()

        return new DomainLinks().builder()
                                .domain("${STARTING_URL}/")
                                .staticContentLinks(["/frontend/images/logo.png"] as Set)
                                .externalLinks(["https://www.facebook.com/bidtulsa/"] as Set)
                                .linksWithinDomain(["/payment": paymentDomain])
                                .build()
    }

    private void givenNestedDomainLinksExistForRootDomain() {
        DomainLinks contactDomain = new DomainLinks().builder()
                                                     .domain("${STARTING_URL}/contact")
                                                     .staticContentLinks(["/contact/static/link"] as Set)
                                                     .externalLinks(["https://google.com/builditdigital"] as Set)
                                                     .linksWithinDomain(["/contact1": null])
                                                     .build()
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/contact")).willReturn(contactDomain)
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/contact1")).willReturn(emptyDomainFor("${STARTING_URL}/contact1"))

        DomainLinks blogDomain = new DomainLinks().builder()
                                                  .domain("${STARTING_URL}/blog")
                                                  .staticContentLinks(["/blog/static/link"] as Set)
                                                  .externalLinks(["https://google.com/builditdigital"] as Set)
                                                  .linksWithinDomain(["/blog1": null, "blog2": null])
                                                  .build()
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/blog")).willReturn(blogDomain)
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/blog1")).willReturn(emptyDomainFor("${STARTING_URL}/blog1"))
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/blog2")).willReturn(emptyDomainFor("${STARTING_URL}/blog2"))

        DomainLinks paymentDomain = new DomainLinks().builder()
                                                     .domain("${STARTING_URL}/payment")
                                                     .staticContentLinks(["/frontend/Styles/style.css"] as Set)
                                                     .externalLinks(["https://twitter.com/builditdigital"] as Set)
                                                     .linksWithinDomain(["/contact": null, "/blog": null])
                                                     .build()
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/payment")).willReturn(paymentDomain)

        DomainLinks rootDomain = new DomainLinks().builder()
                                                  .domain("${STARTING_URL}/")
                                                  .staticContentLinks(["/frontend/images/logo.png"] as Set)
                                                  .externalLinks(["https://www.facebook.com/bidtulsa/"] as Set)
                                                  .linksWithinDomain(["/payment": null])
                                                  .build()
        given(pageCrawler.crawlPageForLinks("${STARTING_URL}/")).willReturn(rootDomain)
    }

    private DomainLinks emptyDomainFor(final GString url) {
        return new DomainLinks().builder().domain(url)
                                .linksWithinDomain([:])
                                .externalLinks([] as Set)
                                .staticContentLinks([] as Set).build()
    }

    private void assertDomainLinksToBeSame(final DomainLinks actual, final DomainLinks expected) {
        assertThat(actual.domain, is(expected.domain))
        if (expected.linksWithinDomain.keySet().size() > 0) {
            assertThat(actual.linksWithinDomain.keySet(), contains(expected.linksWithinDomain.keySet().toArray()))
        }
        if (expected.externalLinks.size() > 0) {
            assertThat(actual.externalLinks, contains(expected.externalLinks.toArray()))
        }
        if (expected.staticContentLinks.size() > 0) {
            assertThat(actual.staticContentLinks, contains(expected.staticContentLinks.toArray()))
        }
        expected.linksWithinDomain.each {
            assertDomainLinksToBeSame(it.value, actual.linksWithinDomain.get(it.key))
        }
    }
}
