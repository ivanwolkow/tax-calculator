# Rate Provider

This module provides a common rate-conversion functionality. 
The module API is defined by means of [RateProvider](src/main/java/com/wolkow/taxcalculator/rateprovider/RateProvider.java) interface.

### CbrRateProvider
[Implementation](src/main/java/com/wolkow/taxcalculator/rateprovider/cbrf/CbrRateProvider.java) 
that is able to fetch rates from Central Bank of Russia web-service (http://www.cbr.ru). 
This rate provider only works when a tax currency is "RUB".
