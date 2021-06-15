package com.wolkow.taxcalculator.capitalgains;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.io.Resources;
import com.wolkow.taxcalculator.capitalgains.properties.ApplicationProperties;
import com.wolkow.taxcalculator.capitalgains.taxreport.DefaultTaxReportGenerator;
import com.wolkow.taxcalculator.capitalgains.taxreport.TaxReportGenerator;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.InteractiveBrokersTradeProvider;
import com.wolkow.taxcalculator.capitalgains.tradeprovider.TradeProvider;
import com.wolkow.taxcalculator.rateprovider.RateProvider;
import com.wolkow.taxcalculator.rateprovider.cbrf.CbrRateProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.ZoneId;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class CapitalGainsCalculatorApplicationTest {

    private final static String CBR_HOST = "http://localhost:4567";

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

    @SuppressWarnings("UnstableApiUsage")
    @Test
    @SneakyThrows
    void capitalGainsCalculatorShouldGenerateCorrectReport() {
        File reportsDir = new File(this.getClass().getClassLoader().getResource("interactive-brokers-activity-reports/").toURI());
        File outputFile = Files.createTempFile("capital-gains-report-2020-", ".csv").toFile();

        ApplicationProperties properties = ApplicationProperties.builder()
                .year(2020)
                .taxCurrency("RUB")
                .taxRate(new BigDecimal("13.00"))
                .taxTimeZone(ZoneId.of("+03"))
                .reportDir(reportsDir)
                .outputFile(outputFile)
                .build();

        TradeProvider tradeProvider = new InteractiveBrokersTradeProvider(properties);
        RateProvider rateProvider = new CbrRateProvider(CBR_HOST);
        TaxReportGenerator reportGenerator = new DefaultTaxReportGenerator(properties, rateProvider);

        new CapitalGainsCalculatorApplication(properties, tradeProvider, reportGenerator).run();

        String expectedReport = Resources.toString(Resources.getResource("capital-gains-tax-report.csv"), UTF_8);
        String actualReport = Files.readString(outputFile.toPath());
        assertEquals(expectedReport, actualReport);
    }
}