#https://github.com/ivanwolkow/tax-calculator

PREVIOUS_YEAR=$(($(date +"%Y") - 1))

echo "Calculating capital gains tax for $PREVIOUS_YEAR..."
cd capital-gains || exit
java -jar capital-gains-calculator-0.0.1-SNAPSHOT-all.jar \
  --in ./activity-reports/ \
  --out ./capital-gains-tax-report-$PREVIOUS_YEAR.csv \
  --tax-currency RUB \
  --tax-rate 13 \
  --tax-tz +03 \
  --year $PREVIOUS_YEAR || exit

cd - || exit

echo "Calculating dividend tax for $PREVIOUS_YEAR..."
cd dividends || exit
java -jar dividend-calculator-0.0.1-SNAPSHOT-all.jar \
  --in ./dividend-reports/ \
  --out ./dividend-tax-report-$PREVIOUS_YEAR.csv \
  --tax-currency RUB \
  --tax-rate 13 \
  --year $PREVIOUS_YEAR || exit

