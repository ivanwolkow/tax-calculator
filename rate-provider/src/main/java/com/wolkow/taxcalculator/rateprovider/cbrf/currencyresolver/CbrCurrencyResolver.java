package com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver;


import com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.model.XMLValFullItem;
import com.wolkow.taxcalculator.rateprovider.cbrf.currencyresolver.model.XMLValFullResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.simpleframework.xml.core.Persister;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Loads currency mapping (for example USD -> R01235) from cbr service
 * http://www.cbr.ru/scripts/XML_valFull.asp
 */
@Slf4j
public class CbrCurrencyResolver implements CurrencyResolver {

    private final HttpClient httpClient;
    private final Persister persister;
    private final Map<String, String> codeByName;

    @SneakyThrows
    public CbrCurrencyResolver(String cbrHost) {
        this.persister = new Persister();
        this.httpClient = HttpClients.createDefault();
        this.codeByName = loadCurrencyMapping(cbrHost);
    }

    @Override
    public String resolveCodeByName(String name) {
        return Objects.requireNonNull(codeByName.get(name));
    }

    @SneakyThrows
    private Map<String, String> loadCurrencyMapping(String cbrHost) {
        URI uri = new URIBuilder("/scripts/XML_valFull.asp").build();

        HttpResponse response = httpClient.execute(HttpHost.create(cbrHost), new HttpGet(uri));
        checkArgument(response.getStatusLine().getStatusCode() == 200);

        XMLValFullResponse rs = persister.read(XMLValFullResponse.class, response.getEntity().getContent());

        Map<String, String> map = StreamEx.of(rs.getItems())
                .removeBy(XMLValFullItem::getIsoCharCode, null)
                .filter(item -> Objects.equals(item.getId(), StringUtils.trim(item.getParentCode())))
                .mapToEntry(XMLValFullItem::getIsoCharCode, XMLValFullItem::getParentCode)
                .mapValues(String::trim)
                .toImmutableMap();

        log.info("Loaded currency mapping for {} currencies", map.size());
        return map;
    }

}
