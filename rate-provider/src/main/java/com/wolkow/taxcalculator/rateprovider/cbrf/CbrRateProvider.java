package com.wolkow.taxcalculator.rateprovider.cbrf;

import com.wolkow.taxcalculator.rateprovider.RateProvider;
import com.wolkow.taxcalculator.rateprovider.cbrf.common.CbrXmlMatcher;
import com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.CbrCurrencyResolver;
import com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.CurrencyResolver;
import com.wolkow.taxcalculator.rateprovider.cbrf.model.CbrRateRecord;
import com.wolkow.taxcalculator.rateprovider.cbrf.model.CbrRateResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.simpleframework.xml.core.Persister;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static java.math.BigDecimal.ONE;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Objects.requireNonNull;

/**
 * Rate provider which fetched rates from Central Bank of Russia web service.
 * This rate provider is not thread-safe by design.
 * It supports caching and fetching rates in chunks.
 */
@Slf4j
public class CbrRateProvider implements RateProvider {

    private final static DateTimeFormatter CB_RF_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final static String CB_RF_BASE_CURRENCY = "RUB";

    private final Map<Map.Entry<String, Integer>, TreeMap<LocalDate, BigDecimal>> cache;
    private final Persister xmlPersister;
    private final HttpClient httpClient;
    private final String cbrHost;
    private final CurrencyResolver currencyResolver;

    public CbrRateProvider(String cbrHost) {

        this.cbrHost = cbrHost;
        this.xmlPersister = new Persister(new CbrXmlMatcher());
        this.cache = new HashMap<>();
        this.httpClient = HttpClients.createDefault();
        this.currencyResolver = new CbrCurrencyResolver(cbrHost);
    }

    @Override
    @SneakyThrows
    public BigDecimal getRateByDateAndCurrency(LocalDate date, String currency) {
        if (CB_RF_BASE_CURRENCY.equalsIgnoreCase(currency)) return ONE.round(new MathContext(2));

        SimpleEntry<String, Integer> cacheKey = new SimpleEntry<>(currency, date.getYear());
        if (!cache.containsKey(cacheKey)) fillCache(currency, date.getYear());

        TreeMap<LocalDate, BigDecimal> sortedRates = cache.get(cacheKey);

        Map.Entry<LocalDate, BigDecimal> closestRate = sortedRates.floorEntry(date);
        if (closestRate != null) return closestRate.getValue();

        log.warn("Unable to find closest rate during current year for {}:{}, loading rates for previous year (if necessary) " +
                "and grabbing the last entry", currency, date);
        fillCache(currency, date.getYear() - 1);
        return cache.get(new SimpleEntry<>(currency, date.getYear() - 1)).lastEntry().getValue();
    }

    @SneakyThrows
    private void fillCache(String currency, int year) {
        SimpleEntry<String, Integer> cacheKey = new SimpleEntry<>(currency, year);
        if (cache.containsKey(cacheKey)) return;

        String currencyCode = requireNonNull(currencyResolver.resolveCodeByName(currency));

        URI uri = new URIBuilder("/scripts/XML_dynamic.asp")
                .addParameter("date_req1", YearMonth.of(year, JANUARY).atDay(1).format(CB_RF_FORMATTER))
                .addParameter("date_req2", YearMonth.of(year, DECEMBER).atDay(31).format(CB_RF_FORMATTER))
                .addParameter("VAL_NM_RQ", currencyCode)
                .build();

        HttpResponse response = httpClient.execute(HttpHost.create(cbrHost), new HttpGet(uri));
        CbrRateResponse cbrRateResponse = xmlPersister.read(CbrRateResponse.class, response.getEntity().getContent());

        TreeMap<LocalDate, BigDecimal> sortedRates = StreamEx.of(cbrRateResponse.getRecords())
                .mapToEntry(CbrRateRecord::getDate, CbrRateRecord::getValue)
                .toCustomMap(TreeMap::new);

        cache.put(cacheKey, sortedRates);
    }

}
