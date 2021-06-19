package com.wolkow.taxcalculator.rateprovider.cbrf;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class CbrRateProviderTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
                .port(4567)
                .bindAddress("localhost")
                .notifier(new ConsoleNotifier(true)));
        wireMockServer.start();

        wireMockServer.stubFor(get(urlPathEqualTo("/scripts/XML_valFull.asp"))
                .willReturn(aResponse().withStatus(200).withBodyFile("XML_val_ful.xml")));

        wireMockServer.stubFor(get(urlPathEqualTo("/scripts/XML_dynamic.asp"))
                .withQueryParam("date_req1", equalTo("01/01/2020"))
                .withQueryParam("date_req2", equalTo("31/12/2020"))
                .withQueryParam("VAL_NM_RQ", equalTo("R01235"))
                .willReturn(aResponse().withStatus(200).withBodyFile("XML_dynamic_2020.xml")));

        wireMockServer.stubFor(get(urlPathEqualTo("/scripts/XML_dynamic.asp"))
                .withQueryParam("date_req1", equalTo("01/01/2019"))
                .withQueryParam("date_req2", equalTo("31/12/2019"))
                .withQueryParam("VAL_NM_RQ", equalTo("R01235"))
                .willReturn(aResponse().withStatus(200).withBodyFile("XML_dynamic_2019.xml")));
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetRequests();
    }

    @Test
    void rateProviderMustPopulateCache() {
        CbrRateProvider rateProvider = new CbrRateProvider("http://localhost:4567");
        wireMockServer.verify(0, getRequestedFor(UrlPattern.ANY));

        BigDecimal rate1 = rateProvider.getRateByDateAndCurrency(LocalDate.of(2020, Month.JANUARY, 15), "USD");
        BigDecimal rate2 = rateProvider.getRateByDateAndCurrency(LocalDate.of(2020, Month.DECEMBER, 31), "USD");

        assertEquals(new BigDecimal("61.4140"), rate1);
        assertEquals(new BigDecimal("73.8757"), rate2);
        wireMockServer.verify(2, getRequestedFor(UrlPattern.ANY));
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/scripts/XML_valFull.asp")));
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/scripts/XML_dynamic.asp"))
                .withQueryParam("date_req1", equalTo("01/01/2020"))
                .withQueryParam("date_req2", equalTo("31/12/2020"))
                .withQueryParam("VAL_NM_RQ", equalTo("R01235")));

        BigDecimal rate3 = rateProvider.getRateByDateAndCurrency(LocalDate.of(2019, Month.SEPTEMBER, 20), "USD");
        BigDecimal rate4 = rateProvider.getRateByDateAndCurrency(LocalDate.of(2019, Month.OCTOBER, 22), "USD");
        BigDecimal rate5 = rateProvider.getRateByDateAndCurrency(LocalDate.of(2019, Month.NOVEMBER, 20), "USD");

        assertEquals(new BigDecimal("64.2199"), rate3);
        assertEquals(new BigDecimal("63.7606"), rate4);
        assertEquals(new BigDecimal("63.7730"), rate5);
        wireMockServer.verify(3, getRequestedFor(UrlPattern.ANY));
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/scripts/XML_dynamic.asp"))
                .withQueryParam("date_req1", equalTo("01/01/2019"))
                .withQueryParam("date_req2", equalTo("31/12/2019"))
                .withQueryParam("VAL_NM_RQ", equalTo("R01235")));
    }

    @Test
    void rateProviderMustFetchLastAvailableRateWhenNoRateFound() {
        CbrRateProvider rateProvider = new CbrRateProvider("http://localhost:4567");
        wireMockServer.verify(0, getRequestedFor(UrlPattern.ANY));

        BigDecimal rate = rateProvider.getRateByDateAndCurrency(LocalDate.of(2020, Month.JANUARY, 2), "USD");

        assertEquals(new BigDecimal("61.9057"), rate);
        wireMockServer.verify(3, getRequestedFor(UrlPattern.ANY));
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/scripts/XML_valFull.asp")));
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/scripts/XML_dynamic.asp"))
                .withQueryParam("date_req1", equalTo("01/01/2020"))
                .withQueryParam("date_req2", equalTo("31/12/2020"))
                .withQueryParam("VAL_NM_RQ", equalTo("R01235")));
        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/scripts/XML_dynamic.asp"))
                .withQueryParam("date_req1", equalTo("01/01/2019"))
                .withQueryParam("date_req2", equalTo("31/12/2019"))
                .withQueryParam("VAL_NM_RQ", equalTo("R01235")));

    }
}