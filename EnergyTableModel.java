import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

// Кастомна модель даних для JTable
class EnergyTableModel extends AbstractTableModel {
    // Назви колонок, які побачить користувач
    private final String[] columnNames = {"Рік", "Місяць", "Початок", "Кінець", "Спожито (кВт)", "Тариф (грн)", "Сума (грн)"};
    private List<CurrentMonth> displayedRows;

    public EnergyTableModel() {
        this.displayedRows = new ArrayList<>();
    }

    // Метод для оновлення даних у таблиці (наприклад, після фільтрації)
    public void setRows(List<CurrentMonth> newRows) {
        this.displayedRows = newRows;
        fireTableDataChanged(); // Повідомляємо таблицю, що дані змінилися і треба перемалюватись
    }

    // Метод для отримання поточного списку (щоб порахувати суму на екрані)
    public List<CurrentMonth> getRows() {
        return displayedRows;
    }

    @Override
    public int getRowCount() {
        return displayedRows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    // Головний метод: каже таблиці, що писати в конкретній клітинці
    private String formatKw(double value) {
        return (value % 1 == 0) ? String.valueOf((long) value) : String.valueOf(value);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CurrentMonth record = displayedRows.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> record.currYear;
            case 1 -> record.currMonth;
            case 2 -> formatKw(record.getStartReading()); // Розумний формат
            case 3 -> formatKw(record.getEndReading());   // Розумний формат
            case 4 -> formatKw(record.getCalculatedConsumedEnergyAmount()); // Розумний формат
            case 5 -> String.format("%.2f", record.currKWhPrice); // Гроші завжди з 2 нулями
            case 6 -> String.format("%.2f", record.getCalculatedFinalCheck()); // Гроші завжди з 2 нулями
            default -> null;
        };
    }
}