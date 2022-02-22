package com.wolkow.taxcalculator.rateprovider.cbrf;

import com.wolkow.taxcalculator.rateprovider.RateProvider;
import com.wolkow.taxcalculator.rateprovider.cbrf.common.CbrXmlMatcher;
import com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.CbrCurrencyResolver;
import com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.CurrencyResolver;
import com.wolkow.taxcalculator.rateprovider.cbrf.model.CbrRateRecord;
import com.wolkow.taxcalculator.rateprovider.cbrf.model.CbrRateResponse;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
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
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.resilience4j.retry.Retry.decorateCheckedSupplier;
import static java.math.BigDecimal.ONE;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.util.Objects.requireNonNull;

/**
 * Rate provider which fetched rates from Central Bank of Russia web service.
 * This implementation is thread-safe. All methods achieve their effects atomically using internal
 * locks or other forms of concurrency control.
 * This implementation supports caching and fetching rates in chunks.
 */
@Slf4j
public class CbrRateProvider implements RateProvider {

    private final static DateTimeFormatter CB_RF_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final static String CB_RF_BASE_CURRENCY = "RUB";

    private final Map<Map.Entry<String, Integer>, NavigableMap<LocalDate, BigDecimal>> cache;

    private final Persister xmlPersister;
    private final HttpClient httpClient;
    private final String cbrHost;
    private final CurrencyResolver currencyResolver;
    private final Retry retry;

    public CbrRateProvider(String cbrHost) {
        this.cbrHost = cbrHost;
        this.xmlPersister = new Persister(new CbrXmlMatcher());
        this.cache = new ConcurrentHashMap<>();
        this.httpClient = HttpClients.createDefault();
        this.currencyResolver = new CbrCurrencyResolver(cbrHost);
        this.retry = Retry.of("cbrf-retry", RetryConfig.ofDefaults());
    }

    @Override
    @SneakyThrows
    public BigDecimal getRateByDateAndCurrency(LocalDate date, String currency) {
        if (CB_RF_BASE_CURRENCY.equalsIgnoreCase(currency)) return ONE.round(new MathContext(2));

        SimpleEntry<String, Integer> cacheKey = new SimpleEntry<>(currency, date.getYear());
        if (!cache.containsKey(cacheKey)) {
            log.trace("Cache miss for currency {} , date {}, filling cache...", currency, date);
            fillCache(cacheKey);
        }

        NavigableMap<LocalDate, BigDecimal> sortedRates = cache.get(cacheKey);
        Map.Entry<LocalDate, BigDecimal> closestRate = sortedRates.floorEntry(date);

        if (closestRate != null) return closestRate.getValue();

        log.warn("Unable to find closest rate during current year for {}:{}, fetching last rate for previous year", currency, date);
        fillCache(new SimpleEntry<>(currency, date.getYear() - 1));
        return cache.get(new SimpleEntry<>(currency, date.getYear() - 1)).lastEntry().getValue();
    }

    @SneakyThrows
    private void fillCache(Map.Entry<String, Integer> cacheKey) {
        String currency = cacheKey.getKey();
        Integer year = cacheKey.getValue();
        NavigableMap<LocalDate, BigDecimal> sortedRatesByCurrencyAndYear = cache.computeIfAbsent(cacheKey,
                key -> Collections.synchronizedNavigableMap(new TreeMap<>()));

        synchronized (sortedRatesByCurrencyAndYear) {
            if (!sortedRatesByCurrencyAndYear.isEmpty()) return;

            log.debug("Loading conversion rates for currency={}, year={} ...", currency, year);
            String currencyCode = requireNonNull(currencyResolver.resolveCodeByName(currency));

            URI uri = new URIBuilder("/scripts/XML_dynamic.asp")
                    .addParameter("date_req1", YearMonth.of(year, JANUARY).atDay(1).format(CB_RF_FORMATTER))
                    .addParameter("date_req2", YearMonth.of(year, DECEMBER).atDay(31).format(CB_RF_FORMATTER))
                    .addParameter("VAL_NM_RQ", currencyCode)
                    .build();

            var httpHost = HttpHost.create(cbrHost);
            var httpGet = new HttpGet(uri);

            log.trace("Executing http request {}", httpGet);
            HttpResponse response = decorateCheckedSupplier(retry, () -> httpClient.execute(httpHost, httpGet)).apply();

            var cbrRateResponse = xmlPersister.read(CbrRateResponse.class, response.getEntity().getContent());

            StreamEx.of(cbrRateResponse.getRecords())
                    .mapToEntry(CbrRateRecord::getDate, CbrRateRecord::getValue)
                    .forKeyValue(sortedRatesByCurrencyAndYear::put);
        }
    }

    @Override
    public String getBaseCurrency() {
        return CB_RF_BASE_CURRENCY;
    }
}
