# Tax calculator api

This module provides a web interface for [capital gains calcualator](../capital-gains-calculator) and [dividend calculator](../dividend-calculator).

Currently, the following deployments are configured for this service:
* **Amazon ECS Fargate cluster** : ~~http://3.143.159.123:10101/healthcheck~~ (currently is down due to relatively high Amazon fees for a load balancer usage)
* **Heroku** cloud application platform: https://tax-calculator-api.herokuapp.com/healthcheck 
  (the free tier is used so accessing this endpoint may take a while since the heroku puts the dyno asleep after 30 minutes of inactivity)

## Build and run
Build an executable jar with the command:
```bash
./gradlew bootJar
```

Run it with the command:

```bash
java -jar tax-calculator-api/build/libs/tax-calculator-api-0.0.1-SNAPSHOT.jar
```

By default, application is available on port 8080.  
You can generate capital gains report as follows:
```bash
curl --request POST \
  --url 'http://localhost:8080/capital-gains-report?=' \
  --header 'Content-Type: multipart/form-data' \
  --form reportFiles=@/Users/test/activity-reports/interactive-broker-activity-report-part-1.csv \
  --form reportFiles=@/Users/test/activity-reports/interactive-broker-activity-report-part-2.csv \
  --form 'request={
  "taxYear": "2020",
  "taxCurrency": "RUB",
  "taxRate": "13.00",
  "taxZone": "+03",
  "brokerType": "ib",
  "reportType": "csv"
}'

< HTTP/1.1 200 
< Content-Disposition: attachment; filename=capital-gains-report-2020.csv
< Transfer-Encoding: chunked
< Date: Sun, 20 Jun 2021 09:18:03 GMT
```

You can generate dividend gains report as follows:
```bash
curl --request POST \
  --url 'http://localhost:8080/dividend-report?=' \
  --header 'Content-Type: multipart/form-data' \
  --form reportFiles=@/Users/test/dividend-reports/interactive-broker-dividend-report-part-1.csv \
  --form reportFiles=@/Users/test/dividend-reports/interactive-broker-dividend-report-part-2.csv \
  --form 'request={
  "taxYear": "2020",
  "taxCurrency": "RUB",
  "taxRate": "13.00",
  "brokerType": "ib",
  "reportType": "csv"
}'

< HTTP/1.1 200 
< Content-Disposition: attachment; filename=dividend-report-2020.csv
< Transfer-Encoding: chunked
< Date: Sun, 20 Jun 2021 09:19:03 GMT
```

