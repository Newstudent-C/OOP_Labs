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
    protected JTextField tfUsage = new JTextField("100", 5);
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
        inputPanel.add(new JLabel("Рік:"));
        inputPanel.add(tfYear);
        inputPanel.add(new JLabel("Місяць:"));
        inputPanel.add(cbMonth);
        inputPanel.add(new JLabel("Спожито (кВт):"));
        inputPanel.add(tfUsage);
        inputPanel.add(new JLabel("Ціна (грн):"));
        inputPanel.add(tfPrice);
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
    }

    protected void addRecordAction() {
        try {
            int y = Integer.parseInt(tfYear.getText());
            Month m = (Month) cbMonth.getSelectedItem();
            double usage = Double.parseDouble(tfUsage.getText());
            double price = Double.parseDouble(tfPrice.getText());

            if (isRecordExists(m, y)) {
                JOptionPane.showMessageDialog(this, "Помилка: Запис за " + m + " " + y + " вже існує!", "Дублікат", JOptionPane.WARNING_MESSAGE);
                return; // Зупиняємо виконання, не додаємо об'єкт
            }

            CurrentMonth cm = new CurrentMonth(m, y);
            cm.amountOfUsedElectricity = usage;
            cm.currKWhPrice = price;
            cm.calculateFinalCheck();

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}