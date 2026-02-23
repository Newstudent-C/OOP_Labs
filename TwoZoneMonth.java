public class TwoZoneMonth extends CurrentMonth {
    public double dayElectricityUsed;
    public double nightElectricityUsed;

    public TwoZoneMonth(Month month, int year) {
        super(month, year);
    }

    @Override
    public void calculateFinalCheck() {
        double dayCost = dayElectricityUsed * currKWhPrice;
        double nightCost = nightElectricityUsed * (currKWhPrice / 2.0);

        this.totalMonthCheck = dayCost + nightCost;
        this.amountOfUsedElectricity = dayElectricityUsed + nightElectricityUsed;
    }
}