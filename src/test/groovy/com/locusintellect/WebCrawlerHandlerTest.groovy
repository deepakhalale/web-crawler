package com.locusintellect

import com.locusintellect.crawler.WebCrawler
import com.locusintellect.domain.DomainLinks
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.mockito.BDDMockito.given
import static org.mockito.Matchers.anySet
import static org.mockito.Matchers.anyString

@RunWith(MockitoJUnitRunner)
class WebCrawlerHandlerTest {

    private static final String DEFAULT_DOMAIN = "http://127.0.0.1"

    @Mock
    WebCrawler webCrawler

    WebCrawlerHandler underTest

    @Before
    public void setUp() {
        underTest = new WebCrawlerHandler(DEFAULT_DOMAIN, webCrawler)
    }

    @Test
    public void shouldThrowExceptionIfDomainUrlIsNull() {
        underTest = new WebCrawlerHandler(null, webCrawler)

        try {
            underTest.handle()
            fail("Expected to throw exception")
        } catch (MissingRequiredPropertiesException e) {
            assertThat(e.getMessage(), is("Missing required property starting.url"))
        }
    }

    @Test
    public void shouldThrowExceptionIfDomainUrlIsBlank() {
        underTest = new WebCrawlerHandler('  ', webCrawler)

        try {
            underTest.handle()
            fail("Expected to throw exception")
        } catch (MissingRequiredPropertiesException e) {
            assertThat(e.getMessage(), is("Missing required property starting.url"))
        }
    }

    @Test
    public void shouldMergeCrawledDomainLinks() {
        DomainLinks expectedDomainLinks = getMergedDomainLinksSingleNested()
        given(webCrawler.crawlDomainForLinks(anyString(), anyString(), anySet())).willReturn(getUnMergedDomainLinksSingleNested())

        underTest.handle()

        assert underTest.domainLinks == expectedDomainLinks
    }

    @Test
    public void shouldMergeDeeplyNestedCrawledDomainLinks() {
        DomainLinks expectedDomainLinks = getMergedDomainLinksDoubleNested()
        given(webCrawler.crawlDomainForLinks(anyString(), anyString(), anySet())).willReturn(getUnMergedDomainLinksDoubleNested())

        underTest.handle()

        assert underTest.domainLinks == expectedDomainLinks
    }

    DomainLinks getMergedDomainLinksDoubleNested() {
        return new DomainLinks().builder()
                                .domain(DEFAULT_DOMAIN)
                                .staticContentLinks(["/frontend/Styles/style.css", "/frontend/images/logo.png", "/contact/static/link"] as Set)
                                .externalLinks(["https://twitter.com/builditdigital", "https://www.facebook.com/bidtulsa/", "https://google.com/builditdigital"] as Set)
                                .linksWithinDomain(["/contact": null, "/payment": null])
                                .build()

    }

    DomainLinks getUnMergedDomainLinksDoubleNested() {
        DomainLinks contactDomain = new DomainLinks().builder()
                                                     .domain("${DEFAULT_DOMAIN}/contact")
                                                     .staticContentLinks(["/contact/static/link"] as Set)
                                                     .externalLinks(["https://google.com/builditdigital"] as Set)
                                                     .build()

        DomainLinks paymentDomain = new DomainLinks().builder()
                                                     .domain("${DEFAULT_DOMAIN}/payment")
                                                     .staticContentLinks(["/frontend/Styles/style.css"] as Set)
                                                     .externalLinks(["https://twitter.com/builditdigital"] as Set)
                                                     .linksWithinDomain(["/contact": contactDomain])
                                                     .build()

        return new DomainLinks().builder()
                                .domain(DEFAULT_DOMAIN)
                                .staticContentLinks(["/frontend/images/logo.png"] as Set)
                                .externalLinks(["https://www.facebook.com/bidtulsa/"] as Set)
                                .linksWithinDomain(["/payment": paymentDomain])
                                .build()
    }

    DomainLinks getMergedDomainLinksSingleNested() {
        return new DomainLinks().builder()
                                .domain(DEFAULT_DOMAIN)
                                .staticContentLinks(["/frontend/Styles/style.css", "/frontend/images/logo.png"] as Set)
                                .externalLinks(["https://twitter.com/builditdigital", "https://www.facebook.com/bidtulsa/"] as Set)
                                .linksWithinDomain(["/contact": null, "/payment": null]).build()
    }

    DomainLinks getUnMergedDomainLinksSingleNested() {
        DomainLinks childDomain = new DomainLinks().builder()
                                                   .domain("${DEFAULT_DOMAIN}/payment")
                                                   .staticContentLinks(["/frontend/Styles/style.css"] as Set)
                                                   .externalLinks(["https://twitter.com/builditdigital"] as Set)
                                                   .linksWithinDomain(["/contact": null])
                                                   .build()


        return new DomainLinks().builder()
                                .domain(DEFAULT_DOMAIN)
                                .staticContentLinks(["/frontend/images/logo.png"] as Set)
                                .externalLinks(["https://www.facebook.com/bidtulsa/"] as Set)
                                .linksWithinDomain(["/payment": childDomain])
                                .build()
    }

}
