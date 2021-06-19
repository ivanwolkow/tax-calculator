package com.wolkow.taxcalculator.dividend;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.common.io.Resources;
import com.wolkow.taxcalculator.dividend.divprovider.DivProviders;
import com.wolkow.taxcalculator.dividend.taxreport.TaxReportGenerators;
import com.wolkow.taxcalculator.rateprovider.RateProviders;
import lombok.SneakyThrows;
import one.util.streamex.StreamEx;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DividendCalculatorApplicationTest {

    private static WireMockServer wireMockServer;

    private Reader[] sources;

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

    @SuppressWarnings("UnstableApiUsage")
    @BeforeEach
    @SneakyThrows
    void setUp() {
        wireMockServer.resetRequests();

        File reportsDir = new File(Resources.getResource("interactive-brokers-dividend-reports/").toURI());

        sources = StreamEx.of(FileUtils.listFiles(reportsDir, new String[]{"csv"}, false))
                .map(this::createReportReader)
                .toArray(Reader.class);
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        for (Reader source : sources) {
            source.close();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    @SneakyThrows
    void dividendCalculatorShouldGenerateCorrectReportForRate13() {
        var outputFile = Files.createTempFile("div-report-2020-", ".csv").toFile();
        var taxRate = new BigDecimal("13.00");
        var divProvider = DivProviders.getByName("ib");
        var rateProvider = RateProviders.getByBaseCurrency("RUB");
        var reportGenerator = TaxReportGenerators.createByName("csv", rateProvider, taxRate);

        new DividendCalculator(divProvider, reportGenerator)
                .generateTaxReport(new FileWriter(outputFile), 2020, sources);

        String expectedReport = Resources.toString(Resources.getResource("dividend-tax-report-rate-13.csv"), UTF_8);
        String actualReport = Files.readString(outputFile.toPath());
        assertEquals(expectedReport, actualReport);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    @SneakyThrows
    void dividendCalculatorShouldGenerateCorrectReportForRate15() {
        var outputFile = Files.createTempFile("div-report-2020-", ".csv").toFile();
        var taxRate = new BigDecimal("15.00");
        var divProvider = DivProviders.getByName("ib");
        var rateProvider = RateProviders.getByBaseCurrency("RUB");
        var reportGenerator = TaxReportGenerators.createByName("csv", rateProvider, taxRate);

        new DividendCalculator(divProvider, reportGenerator)
                .generateTaxReport(new FileWriter(outputFile), 2020, sources);

        String expectedReport = Resources.toString(Resources.getResource("dividend-tax-report-rate-15.csv"), UTF_8);
        String actualReport = Files.readString(outputFile.toPath());
        assertEquals(expectedReport, actualReport);
    }


    @SneakyThrows
    private Reader createReportReader(File file) {
        return new FileReader(file);
    }


}