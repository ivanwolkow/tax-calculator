package com.wolkow.taxcalculator.api;

import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.InputStream;

import static com.google.common.io.Resources.getResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(classes = TaxCalculatorApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class TaxCalculatorApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("UnstableApiUsage")
    @SneakyThrows
    @Test
    void taxCalculatorShouldGenerateCorrectCapitalGainsReport() {
        InputStream request = getResource("request/capital-gains-request.json").openStream();
        InputStream brokerReport1 = getResource("interactive-brokers/activity-reports/ib-activity-report-part-1.csv").openStream();
        InputStream brokerReport2 = getResource("interactive-brokers/activity-reports/ib-activity-report-part-2.csv").openStream();

        String expectedResponse = Resources.toString(getResource("response/capital-gains-tax-report.csv"), UTF_8);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/capital-gains-report")
                .file(new MockMultipartFile("request", "", APPLICATION_JSON_VALUE, request))
                .file(new MockMultipartFile("reportFiles", "ib-activity-report-part-1.csv", "text/csv", brokerReport1))
                .file(new MockMultipartFile("reportFiles", "ib-activity-report-part-2.csv", "text/csv", brokerReport2)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(expectedResponse));
    }


    @SuppressWarnings("UnstableApiUsage")
    @SneakyThrows
    @Test
    void taxCalculatorShouldGenerateCorrectDividendReport() {
        InputStream request = getResource("request/dividend-request.json").openStream();
        InputStream brokerReport1 = getResource("interactive-brokers/dividend-reports/ib-dividend-report-part-1.csv").openStream();
        InputStream brokerReport2 = getResource("interactive-brokers/dividend-reports/ib-dividend-report-part-2.csv").openStream();

        String expectedResponse = Resources.toString(getResource("response/dividend-tax-report.csv"), UTF_8);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/dividend-report")
                .file(new MockMultipartFile("request", "", APPLICATION_JSON_VALUE, request))
                .file(new MockMultipartFile("reportFiles", "ib-dividend-report-part-1.csv", "text/csv", brokerReport1))
                .file(new MockMultipartFile("reportFiles", "ib-dividend-report-part-2.csv", "text/csv", brokerReport2)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(expectedResponse));
    }
}
