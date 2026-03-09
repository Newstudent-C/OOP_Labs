import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends JFrame {
    protected final List<CurrentMonth> allDataStorage = new ArrayList<>();
    protected final EnergyTableModel tableModel = new EnergyTableModel();
    protected final JTable table = new JTable(tableModel);

    protected final JLabel lblTotalUsage = new JLabel("Всього кВт: 0.00");
    protected final JLabel lblTotalPrice = new JLabel("Всього грн: 0.00");

    protected JPanel inputPanel;
    protected JTextField tfYear = new JTextField("2026", 4);
    protected JComboBox<Month> cbMonth = new JComboBox<>(Month.values());

    protected JTextField tfStartReading = new JTextField("1000", 6);
    protected JTextField tfEndReading = new JTextField("1100", 6);
    protected JTextField tfPrice = new JTextField("4.32", 4);
    protected JButton btnAdd = new JButton("Додати запис");

    protected JPanel bottomPanel;
    protected JTextField tfStartYear = new JTextField("2026", 4);
    protected JComboBox<Month> cbStartMonth = new JComboBox<>(Month.values());
    protected JTextField tfEndYear = new JTextField("2026", 4);
    protected JComboBox<Month> cbEndMonth = new JComboBox<>(Month.values());
    protected JButton btnFilter = new JButton("Фільтрувати");
    protected JButton btnShowAll = new JButton("Показати всі");

    public MainApp() {
        setTitle("Energy Counter Manager");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        buildInputPanel();
        add(new JScrollPane(table), BorderLayout.CENTER);
        buildBottomPanel();
        setupActions();
    }

    protected void buildInputPanel() {
        inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("Рік:")); inputPanel.add(tfYear);
        inputPanel.add(new JLabel("Місяць:")); inputPanel.add(cbMonth);
        inputPanel.add(new JLabel("Початок:")); inputPanel.add(tfStartReading);
        inputPanel.add(new JLabel("Кінець:")); inputPanel.add(tfEndReading);
        inputPanel.add(new JLabel("Тариф:")); inputPanel.add(tfPrice);
        inputPanel.add(btnAdd);
        add(inputPanel, BorderLayout.NORTH);
    }

    protected void buildBottomPanel() {
        bottomPanel = new JPanel(new GridLayout(2, 1));

        JPanel filterPanel = new JPanel(new FlowLayout());
        filterPanel.add(new JLabel("Від:"));
        filterPanel.add(tfStartYear);
        filterPanel.add(cbStartMonth);
        filterPanel.add(new JLabel("До:"));
        filterPanel.add(tfEndYear);
        filterPanel.add(cbEndMonth);
        filterPanel.add(btnFilter);
        filterPanel.add(btnShowAll);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalUsage.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalPrice.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(lblTotalUsage);
        statsPanel.add(Box.createHorizontalStrut(20));
        statsPanel.add(lblTotalPrice);

        bottomPanel.add(filterPanel);
        bottomPanel.add(statsPanel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    protected void setupActions() {
        btnAdd.addActionListener(e -> addRecordAction());
        btnFilter.addActionListener(e -> filterRecordsAction());
        btnShowAll.addActionListener(e -> updateTable(allDataStorage));

        cbMonth.addActionListener(e -> {
            try {
                int y = Integer.parseInt(tfYear.getText());
                Month m = (Month) cbMonth.getSelectedItem();
                long prevIndex = ((long) y * 12 + m.getValue()) - 1;

                for (CurrentMonth item : allDataStorage) {
                    long itemIndex = (long) item.currYear * 12 + item.currMonth.getValue();
                    if (itemIndex == prevIndex) {
                        // Виклик віртуального методу. Поліморфізм зробить всю магію.
                        autoFillStartReadings(item);
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                // Ігноруємо, якщо користувач ще не ввів рік
            }
        });
    }

    protected void addRecordAction() {
        try {
            int y = Integer.parseInt(tfYear.getText());
            Month m = (Month) cbMonth.getSelectedItem();
            int startRead = Integer.parseInt(tfStartReading.getText());
            int endRead = Integer.parseInt(tfEndReading.getText());
            double price = Double.parseDouble(tfPrice.getText());

            if (isRecordExists(m, y)) {
                JOptionPane.showMessageDialog(this, "Запис за цей період вже існує.");
                return;
            }

            if (!isMeterLogicValid(y, m, startRead, endRead)) {
                return; // Переривання, якщо валідація лічильника не пройдена
            }

            CurrentMonth cm = new CurrentMonth(m, y);
            cm.startMeterReading = startRead;
            cm.endMeterReading = endRead;
            cm.currKWhPrice = price;

            cm.calculateUsage();      // Спочатку дельта
            cm.calculateFinalCheck(); // Потім сума

            allDataStorage.add(cm);
            updateTable(allDataStorage);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Помилка формату чисел!");
        }
    }

    protected void filterRecordsAction() {
        try {
            int startY = Integer.parseInt(tfStartYear.getText());
            Month startM = (Month) cbStartMonth.getSelectedItem();
            int endY = Integer.parseInt(tfEndYear.getText());
            Month endM = (Month) cbEndMonth.getSelectedItem();

            List<CurrentMonth> filteredList = new ArrayList<>();
            long startIndex = (long) startY * 12 + startM.getValue();
            long endIndex = (long) endY * 12 + endM.getValue();

            for (CurrentMonth item : allDataStorage) {
                long itemIndex = (long) item.currYear * 12 + item.currMonth.getValue();
                if (itemIndex >= startIndex && itemIndex <= endIndex) {
                    filteredList.add(item);
                }
            }
            updateTable(filteredList);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Перевірте дати фільтру!");
        }
    }

    protected void updateTable(List<CurrentMonth> data) {
        // Використовуємо лінійний індекс часу для порівняння
        data.sort((itemA, itemB) -> {
            int indexA = itemA.currYear * 12 + itemA.currMonth.getValue();
            int indexB = itemB.currYear * 12 + itemB.currMonth.getValue();

            // Порівнюємо B з A (а не A з B), щоб отримати зворотній порядок (від найновішого до найстарішого)
            return Integer.compare(indexB, indexA);
        });

        tableModel.setRows(data);

        double totalKw = 0;
        double totalMoney = 0;

        for (CurrentMonth item : data) {
            totalKw += item.getCalculatedConsumedEnergyAmount();
            totalMoney += item.getCalculatedFinalCheck();
        }

        lblTotalUsage.setText(String.format("Всього кВт: %.2f", totalKw));
        lblTotalPrice.setText(String.format("Всього грн: %.2f", totalMoney));
    }

    protected boolean isRecordExists(Month m, int y) {
        for (CurrentMonth item : allDataStorage) {
            if (item.currMonth == m && item.currYear == y) {
                return true;
            }
        }
        return false;
    }

    protected boolean isMeterLogicValid(int targetYear, Month targetMonth, double startReading, double endReading) {
        if (startReading > endReading) {
            JOptionPane.showMessageDialog(this, "Помилка: Початковий показник більший за кінцевий.", "Помилка лічильника", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        long targetIndex = (long) targetYear * 12 + targetMonth.getValue();

        for (CurrentMonth item : allDataStorage) {
            long itemIndex = (long) item.currYear * 12 + item.currMonth.getValue();

            // СУВОРА ПЕРЕВІРКА: Якщо це рівно попередній місяць
            if (itemIndex == targetIndex - 1) {
                if (Math.abs(startReading - item.endMeterReading) > 0.001) {
                    JOptionPane.showMessageDialog(this,
                            "Помилка: Показник початку не збігається з кінцем попереднього місяця (" + item.endMeterReading + ").",
                            "Хронологічна помилка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            // Захист від введення неможливих даних для інших періодів
            else if (itemIndex < targetIndex && item.endMeterReading > startReading) {
                JOptionPane.showMessageDialog(this, "Помилка: Показник менший за історію.", "Помилка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else if (itemIndex > targetIndex && item.startMeterReading < endReading) {
                JOptionPane.showMessageDialog(this, "Помилка: Показник більший за майбутні записи.", "Помилка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }

    protected void autoFillStartReadings(CurrentMonth prevMonth) {
        if (tfStartReading != null) {
            double val = prevMonth.getEndReading();
            // Якщо остача від ділення на 1 дорівнює 0, значить число ціле.
            // Приводимо до (long), щоб відрізати ".0"
            String text = (val % 1 == 0) ? String.valueOf((long) val) : String.valueOf(val);
            tfStartReading.setText(text);
        }
    }
}