import javax.swing.*;
import java.awt.*;

public class TwoZoneMainApp extends MainApp {

    // Нові поля для вводу двох зон
    private JTextField tfDayUsage;
    private JTextField tfNightUsage;

    public TwoZoneMainApp() {
        super(); // Виклик конструктора базового класу
        setTitle("Energy Counter Manager - Двозонний лічильник");
    }

    @Override
    protected void buildInputPanel() {
        // Перевизначаємо панель вводу
        inputPanel = new JPanel(new FlowLayout());

        tfDayUsage = new JTextField("70", 4);
        tfNightUsage = new JTextField("30", 4);

        inputPanel.add(new JLabel("Рік:")); inputPanel.add(tfYear);
        inputPanel.add(new JLabel("Місяць:")); inputPanel.add(cbMonth);
        inputPanel.add(new JLabel("День (кВт):")); inputPanel.add(tfDayUsage);
        inputPanel.add(new JLabel("Ніч (кВт):")); inputPanel.add(tfNightUsage);
        inputPanel.add(new JLabel("Базовий тариф (грн):")); inputPanel.add(tfPrice);
        inputPanel.add(btnAdd);

        add(inputPanel, BorderLayout.NORTH);
    }

    @Override
    protected void addRecordAction() {
        try {
            int y = Integer.parseInt(tfYear.getText());
            Month m = (Month) cbMonth.getSelectedItem();
            double dayUsg = Double.parseDouble(tfDayUsage.getText());
            double nightUsg = Double.parseDouble(tfNightUsage.getText());
            double price = Double.parseDouble(tfPrice.getText());

            if (isRecordExists(m, y)) {
                JOptionPane.showMessageDialog(this, "Помилка: Запис за " + m + " " + y + " вже існує!", "Дублікат", JOptionPane.WARNING_MESSAGE);
                return; // Зупиняємо виконання, не додаємо об'єкт
            }

            // Використовуємо новий клас
            TwoZoneMonth tzm = new TwoZoneMonth(m, y);
            tzm.dayElectricityUsed = dayUsg;
            tzm.nightElectricityUsed = nightUsg;
            tzm.currKWhPrice = price;

            // Викличеться перевизначений метод, який врахує ділення тарифу на 2 для ночі
            tzm.calculateFinalCheck();

            allDataStorage.add(tzm); // Upcasting працює, колекція приймає нащадка
            updateTable(allDataStorage);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Помилка формату чисел!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TwoZoneMainApp().setVisible(true));
    }
}