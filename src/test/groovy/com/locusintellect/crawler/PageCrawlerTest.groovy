package com.locusintellect.crawler

import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.locusintellect.domain.DomainLinks
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.exactly
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.client.WireMock.verify
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat

class PageCrawlerTest {

    private static final String DEFAULT_HOST = "127.0.0.1"

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Options.DYNAMIC_PORT)

    PageCrawler underTest

    @Before
    public void setUp() {
        underTest = new PageCrawler()
    }

    @Test
    public void shouldGetLinksWithinAPage() {
        givenThat(get(urlEqualTo("/")).willReturn(aResponse().withBodyFile("homepage.xml")))

        DomainLinks domainLinks = underTest.crawlPageForLinks(getDomainUrl())

        assertThat(domainLinks.domain, is(getDomainUrl()))
        assertThat(domainLinks.linksWithinDomain.keySet(), contains("/blog", "/contact", "/tulsa-website-design", "/Website-Design-Custom-Software", "/Custom-Software-Development",
                                                                    "/Website-Design-Tulsa", "/tulsa-website-design-portfolio", "/testimonials", "/Website-Design-Tulsa/Website-Development-Tulsa",
                                                                    "/SEO-Tulsa", "/Internet-Marketing", "/Graphic-Design", "/Facebook-Application-Design--Development", "/Multimedia-Solutions",
                                                                    "/web-development-company-tulsa-dallas#show", "/pleasing-cooperation#show", "/open-communication#show", "/best-practices#show",
                                                                    "/high-roi#show", "/web-development-services-tulsa-dallas#show", "/page(89a1813c-1564-436c-97ae-c9346f04799a)", "/payment"))
        verify(exactly(1), getRequestedFor(urlEqualTo("/")))
    }

    @Test
    public void shouldGetExternalLinksInAPage() {
        givenThat(get(urlEqualTo("/")).willReturn(aResponse().withBodyFile("homepage.xml")))

        DomainLinks domainLinks = underTest.crawlPageForLinks(getDomainUrl())

        assertThat(domainLinks.externalLinks, contains("https://www.facebook.com/bidtulsa/", "https://twitter.com/builditdigital"))
    }

    @Test
    public void shouldGetStaticContentInAPage() {
        givenThat(get(urlEqualTo("/")).willReturn(aResponse().withBodyFile("homepage.xml")))

        DomainLinks domainLinks = underTest.crawlPageForLinks(getDomainUrl())

        assertThat(domainLinks.staticContentLinks, contains("/frontend/images/logo.png", "/frontend/images/banner.jpg", "/frontend/images/clients/client1.png", "/frontend/images/clients/client2.png",
                                                            "/frontend/images/clients/client3.png", "/frontend/images/clients/client4.png", "/frontend/images/clients/client5.png", "/frontend/images/clients/client6.png",
                                                            "/frontend/js/jquery.min.js", "/frontend/js/bootstrap.min.js", "/frontend/js/jquery.flexslider-min.js", "/frontend/js/owl.carousel.min.js",
                                                            "/frontend/js/jquery.smoothZoom.min.js", "/frontend/js/jquery.rateit.min.js", "/frontend/js/jquery.colorbox.1.6.0.js", "/frontend/js/scripts.js",
                                                            "/Frontend/Styles/VisualEditor.common.css", "/frontend/images/favicon.png", "/frontend/Styles/bootstrap.css", "/frontend/Styles/flexslider.css",
                                                            "/frontend/Styles/owl.transitions.css", "/frontend/Styles/owl.carousel.css", "/frontend/Styles/font-awesome.min.css", "/frontend/Styles/gallery_v2_bid.css",
                                                            "/frontend/Styles/colorbox.1.6.0.css", "/frontend/Styles/style.css"))
    }

    @Test
    public void shouldReturnEmptyDomainListOn404Response() {
        final DomainLinks expectedDomainObject = DomainLinks.EMPTY_DOMAIN(getDomainUrl())
        givenThat(get(urlEqualTo("/")).willReturn(aResponse().withStatus(404).withStatusMessage("Requested document not found.")))

        DomainLinks domainLinks = underTest.crawlPageForLinks(getDomainUrl())

        domainLinks == expectedDomainObject
        verify(exactly(1), getRequestedFor(urlEqualTo("/")))
    }

    @Test
    public void shouldRetryOnSocketTimeouts() {
        givenThat(get(urlEqualTo("/")).inScenario("retry at socket timeout").whenScenarioStateIs(Scenario.STARTED)
                                      .willSetStateTo("one time requested")
                                      .willReturn(aResponse().withFixedDelay(3000).withStatus(500)))
        givenThat(get(urlEqualTo("/")).inScenario("retry at socket timeout").whenScenarioStateIs("one time requested")
                                      .willReturn(aResponse().withBodyFile("homepage.xml")))

        DomainLinks domainLinks = underTest.crawlPageForLinks(getDomainUrl())

        assertThat(domainLinks.domain, is(notNullValue()))
        assertThat(domainLinks.linksWithinDomain, is(notNullValue()))
        assertThat(domainLinks.externalLinks, is(notNullValue()))
        assertThat(domainLinks.staticContentLinks, is(notNullValue()))
        verify(exactly(2), getRequestedFor(urlEqualTo("/")))
    }

    private String getDomainUrl() {
        return "http://${DEFAULT_HOST}:${wireMockRule.port()}"
    }


}
