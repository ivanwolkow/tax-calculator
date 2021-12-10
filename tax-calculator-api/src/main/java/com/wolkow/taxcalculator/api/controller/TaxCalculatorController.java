package com.wolkow.taxcalculator.api.controller;

import com.wolkow.taxcalculator.api.dto.CapitalGainsRequest;
import com.wolkow.taxcalculator.api.dto.DividendRequest;
import com.wolkow.taxcalculator.api.service.CapitalGainsCalculatorService;
import com.wolkow.taxcalculator.api.service.DividendCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TaxCalculatorController {

    private final CapitalGainsCalculatorService capitalGainsCalculatorService;
    private final DividendCalculatorService dividendCalculatorService;

    @PostMapping(path = "/capital-gains-report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public void generateCapitalGainsReport(@RequestPart("request") CapitalGainsRequest rq,
                                           @RequestPart("reportFiles") MultipartFile[] reportFiles,
                                           HttpServletResponse response) {

        log.debug("Capital gains report request: {}, files: {}", rq,
                StreamEx.of(reportFiles).map(MultipartFile::getOriginalFilename).toList());

        PrintWriter writer = response.getWriter();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=capital-gains-report-%d.%s", rq.getTaxYear(), rq.getReportType()));

        Reader[] brokerReports = StreamEx.of(reportFiles)
                .map(TaxCalculatorController::createReader)
                .toArray(Reader.class);

        capitalGainsCalculatorService.generateCapitalGainsReport(writer, rq, brokerReports);
    }

    @PostMapping(path = "/dividend-report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public void generateCapitalGainsReport(@RequestPart("request") DividendRequest rq,
                                           @RequestPart("reportFiles") MultipartFile[] reportFiles,
                                           HttpServletResponse response) {

        log.debug("Dividend report request: {}, files: {}", rq,
                StreamEx.of(reportFiles).map(MultipartFile::getOriginalFilename).toList());

        PrintWriter writer = response.getWriter();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=dividend-report-%d.%s", rq.getTaxYear(), rq.getReportType()));

        Reader[] brokerReports = StreamEx.of(reportFiles)
                .map(TaxCalculatorController::createReader)
                .toArray(Reader.class);

        dividendCalculatorService.generateDividendReport(writer, rq, brokerReports);
    }

    @Nullable
    @SneakyThrows
    private static Reader createReader(MultipartFile file) {
        if (file == null) return null;
        if (file.isEmpty()) return null;

        return new InputStreamReader(file.getInputStream());
    }
}
