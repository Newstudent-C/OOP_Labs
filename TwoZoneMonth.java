public class TwoZoneMonth extends CurrentMonth {

    public int dayStartReading;
    public int dayEndReading;
    public int nightStartReading;
    public int nightEndReading;

    public int dayElectricityUsed;
    public int nightElectricityUsed;

    public TwoZoneMonth(Month month, int year) {
        super(month, year);
    }

    @Override
    public void calculateUsage() {
        this.dayElectricityUsed = dayEndReading - dayStartReading;
        this.nightElectricityUsed = nightEndReading - nightStartReading;
        this.amountOfUsedElectricity = dayElectricityUsed + nightElectricityUsed;
    }

    @Override
    public void calculateFinalCheck() {
        double dayCost = dayElectricityUsed * currKWhPrice;
        double nightCost = nightElectricityUsed * (currKWhPrice / 2.0);

        double rawTotal = dayCost + nightCost;
        this.totalMonthCheck = Math.round(rawTotal * 100.0) / 100.0;
        this.amountOfUsedElectricity = (int)( Math.round(this.amountOfUsedElectricity * 100.0) / 100.0 );
    }

    @Override
    public double getStartReading() {
        return dayStartReading + nightStartReading;
    }

    @Override
    public double getEndReading() {
        return dayEndReading + nightEndReading;
    }
}