# Dividend tax calculator

Even if you don't sell shares, you still might receive a taxable dividend as a shareholder.
This CLI tool is capable to calculate total dividend tax and build tax report.
It provides the following functionality:

* read dividend reports provided by your broker
* calculate profit for each dividend accrual regarding tax currency, conversion rate, broker withhold etc
* calculate total profit and tax amount to be paid
* generate CSV report showing details of the above steps (might be suitable when filling a tax form)

### Input data format
Currently, this CLI supports only Interactive Brokers dividend reports
(though it can easily be extended to support other broker activity report formats).  

These reports can be downloaded from [Interactive Brokers website](https://www.interactivebrokers.co.uk/)
under "Reports" -> "Tax documents" -> "Tax Information Statements for [year]" -> "Dividend Report" (CSV).


## Build and run

Below you can find instructions on how to use this tool as a standalone CLI application. For using as web service, please
go to [web api](../tax-calculator-api) page.

```bash
./gradlew shadowJar
```
For a convenience, let's export path to a jar file to env variable:
```bash
export DIV_JAR=`pwd`/dividend-calculator/build/libs/dividend-calculator-0.0.1-SNAPSHOT-all.jar
```

Typical usage example:
```bash
java -jar DIV_JAR --in ./dividend-reports/ --out ./dividend-tax-report.csv --tax-currency RUB --tax-rate 13 --year 2020
```

This CLI tool can be configured using options passed as command line arguments.
Help information contains complete list of options:
```bash
java -jar DIV_JAR -h
```

You can see some options below:

| option       | description                                                                                        |
|--------------|----------------------------------------------------------------------------------------------------|
| in           | Path to directory with broker activity reports (default = ./)                                      |
| out          | Name of the csv file this calculator will print result to (default = ./dividend-tax-report.csv)    |
| tax-currency | Main currency of the country of residence (default = RUB)                                          |
| tax-rate     | Tax rate in country of residence (default = 13)                                                    |
| year         | Financial year (default = previous year)                                                           |

