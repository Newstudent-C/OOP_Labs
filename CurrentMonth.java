public class CurrentMonth {
    public CurrentMonth(Month month, int year) {
        this.currMonth = month;
        this.currYear = year;
    }

    public final Month currMonth;
    public int currYear = 2026;
    public double amountOfUsedElectricity;
    public double currKWhPrice;
    public double totalMonthCheck;

    public void calculateFinalCheck() {
        totalMonthCheck = amountOfUsedElectricity * currKWhPrice;
    }

    public double getCalculatedConsumedEnergyAmount() {
        return amountOfUsedElectricity;
    }

    public double getCalculatedFinalCheck() {
        return totalMonthCheck;
    }
}
