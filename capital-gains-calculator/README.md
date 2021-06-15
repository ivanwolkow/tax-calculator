# Capital gains calculator
When you sell some of your shares for more than you paid for it, the result is a capital gain which is taxable.

This tool aims to help with stock transactions accounting and is able to calculate total tax amount for financial year.
It provides the following functionality:
* read activity reports provided by your broker
* map sell transactions to the corresponding buy transactions (currently supports only FIFO/Queue order - you sell the earliest bought share)
* calculate profit for each sell operation regarding tax currency, tax timezone and conversion rates by transaction day
* calculate total profit (loss) and tax amount to be paid
* generate CSV report showing details of the above steps (might be suitable when filling a tax form)

### Lot-matching method (FIFO/LIFO)
Picking which shares you want to sell can make a significant difference in how much you owe in taxes 
([read more](https://guides.interactivebrokers.com/ibto/ibto.htm#ibto/lotmatchingmethods.htm)).

This CLI supports only FIFO (First In, First Out) method being a default method in most software packages: 
sales are paired with the earliest purchases sequentially.
FIFO assumes that assets remaining in inventory are matched to the most recently purchased or produced assets.

### Input data format
Currently, this CLI supports reading only Interactive Brokers activity reports 
(though it can easily be extended to support other broker activity report formats).  

These reports can be downloaded from [Interactive Brokers website](https://www.interactivebrokers.co.uk/) 
under "Reports" -> "Statements" -> "Activity" with the following parameters:

```
Period: Annual
Date: [select year]
Format: CSV
Language: English
```

Please notice that it is required to download activity reports with all the buy/sell operations since the account opening date!

## Build
```bash
./gradlew shadowJar
```
For a convenience, let's export path to a jar file to env variable:
```bash
export CG_JAR=`pwd`/capital-gains-calculator/build/libs/capital-gains-calculator-0.0.1-SNAPSHOT-all.jar
```

## Run
Typical usage example:
```bash
java -jar $CG_JAR --in ./activity-reports/ --out ./capital-gains-tax-report.csv --tax-currency RUB --tax-rate 13 --tax-tz +03 --year 2020
```

This CLI tool can be configured using options passed as command line arguments.
Help information contains complete list of options:
```bash
java -jar $CG_JAR -h
```

You can see some options below:

| option       | description                                                                                          |
|--------------|------------------------------------------------------------------------------------------------------|
| in           | Path to directory with broker activity reports (default = ./)                                        |
| out          | Name of the csv file this calculator will print result to (default = ./capital-gains-tax-report.csv) |
| tax-currency | Main currency of the country of residence (default = RUB)                                            |
| tax-rate     | Tax rate in country of residence (default = 13)                                                      |
| tax-tz       | Tax timezone (default = +03)                                                                         |
| year         | Financial year (default = previous year)                                                             |

