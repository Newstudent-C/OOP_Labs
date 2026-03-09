public class CurrentMonth {
    public final Month currMonth;
    public int currYear;

    public int startMeterReading;
    public int endMeterReading;

    public int amountOfUsedElectricity;
    public double currKWhPrice;
    public double totalMonthCheck;

    public CurrentMonth(Month month, int year) {
        this.currMonth = month;
        this.currYear = year;
    }

    // Розрахунок дельти
    public void calculateUsage() {
        this.amountOfUsedElectricity = endMeterReading - startMeterReading;
    }

    public void calculateFinalCheck() {
        double rawTotal = amountOfUsedElectricity * currKWhPrice;
        this.totalMonthCheck = Math.round(rawTotal * 100.0) / 100.0;
    }

    public double getCalculatedConsumedEnergyAmount() {
        return amountOfUsedElectricity;
    }

    public double getCalculatedFinalCheck() {
        return totalMonthCheck;
    }

    public double getStartReading() {
        return startMeterReading;
    }

    public double getEndReading() {
        return endMeterReading;
    }
}