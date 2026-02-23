import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

// Кастомна модель даних для JTable
class EnergyTableModel extends AbstractTableModel {
    // Назви колонок, які побачить користувач
    private final String[] columnNames = {"Рік", "Місяць", "Спожито (кВт)", "Тариф (грн)", "Сума (грн)"};

    // Список даних, які відображаються в таблиці ПРЯМО ЗАРАЗ
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
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CurrentMonth record = displayedRows.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> record.currYear;
            case 1 -> record.currMonth;
            case 2 -> record.amountOfUsedElectricity;
            case 3 -> record.currKWhPrice;
            case 4 -> record.totalMonthCheck;
            default -> null;
        };
    }
}