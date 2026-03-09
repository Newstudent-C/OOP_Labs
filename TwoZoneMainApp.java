import javax.swing.*;
import java.awt.*;

public class TwoZoneMainApp extends MainApp {

    // Нові текстові поля для 4-х показників двозонного лічильника
    private JTextField tfDayStart;
    private JTextField tfDayEnd;
    private JTextField tfNightStart;
    private JTextField tfNightEnd;

    public TwoZoneMainApp() {
        super();
        setTitle("Energy Counter Manager - Двозонний лічильник");
    }

    @Override
    protected void buildInputPanel() {
        // Оскільки полів багато, використовуємо GridLayout у два рядки для естетики
        inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        tfDayStart = new JTextField("1000", 6);
        tfDayEnd = new JTextField("1100", 6);
        tfNightStart = new JTextField("500", 6);
        tfNightEnd = new JTextField("530", 6);

        // Перший рядок: Загальні дані
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Рік:")); row1.add(tfYear);
        row1.add(new JLabel("Місяць:")); row1.add(cbMonth);
        row1.add(new JLabel("Базовий тариф (грн):")); row1.add(tfPrice);

        // Другий рядок: Показники лічильника
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("День (Початок):")); row2.add(tfDayStart);
        row2.add(new JLabel("День (Кінець):")); row2.add(tfDayEnd);
        row2.add(new JLabel("Ніч (Початок):")); row2.add(tfNightStart);
        row2.add(new JLabel("Ніч (Кінець):")); row2.add(tfNightEnd);
        row2.add(btnAdd);

        inputPanel.add(row1);
        inputPanel.add(row2);

        add(inputPanel, BorderLayout.NORTH);
    }

    @Override
    protected void addRecordAction() {
        try {
            int y = Integer.parseInt(tfYear.getText());
            Month m = (Month) cbMonth.getSelectedItem();
            int dStart = Integer.parseInt(tfDayStart.getText());
            int dEnd = Integer.parseInt(tfDayEnd.getText());
            int nStart = Integer.parseInt(tfNightStart.getText());
            int nEnd = Integer.parseInt(tfNightEnd.getText());
            double price = Double.parseDouble(tfPrice.getText());

            // 1. Перевірка на дублікат місяця (виклик методу з базового класу MainApp)
            if (isRecordExists(m, y)) {
                JOptionPane.showMessageDialog(this, "Помилка: Запис за " + m + " " + y + " вже існує!", "Дублікат", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Логічна перевірка: Кінець не може бути меншим за початок
            if (dEnd < dStart || nEnd < nStart) {
                JOptionPane.showMessageDialog(this, "Помилка: Кінцеві показники не можуть бути меншими за початкові.", "Помилка лічильника", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Хронологічна перевірка з іншими місяцями
            if (!isTwoZoneMeterLogicValid(y, m, dStart, dEnd, nStart, nEnd)) {
                return;
            }

            // 4. Створення об'єкта та розрахунки
            TwoZoneMonth tzm = new TwoZoneMonth(m, y);
            tzm.dayStartReading = dStart;
            tzm.dayEndReading = dEnd;
            tzm.nightStartReading = nStart;
            tzm.nightEndReading = nEnd;
            tzm.currKWhPrice = price;

            tzm.calculateUsage();      // Розраховує різницю (кіловати) для дня і ночі
            tzm.calculateFinalCheck(); // Розраховує фінальну суму з урахуванням знижки 50% на ніч

            allDataStorage.add(tzm);
            updateTable(allDataStorage);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Помилка формату чисел! Вводьте лише цифри (використовуйте крапку для дробів).", "Помилка вводу", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Кастомний метод валідації саме для двозонного лічильника
    private boolean isTwoZoneMeterLogicValid(int targetYear, Month targetMonth, double dStart, double dEnd, double nStart, double nEnd) {
        long targetIndex = (long) targetYear * 12 + targetMonth.getValue();

        for (CurrentMonth item : allDataStorage) {
            if (item instanceof TwoZoneMonth tzItem) {
                long itemIndex = (long) tzItem.currYear * 12 + tzItem.currMonth.getValue();

                // СУВОРА ПЕРЕВІРКА зон
                if (itemIndex == targetIndex - 1) {
                    if (Math.abs(dStart - tzItem.dayEndReading) > 0.001 || Math.abs(nStart - tzItem.nightEndReading) > 0.001) {
                        JOptionPane.showMessageDialog(this,
                                String.format("Помилка! Має бути:\nДень початок: %.2f\nНіч початок: %.2f", tzItem.dayEndReading, tzItem.nightEndReading),
                                "Незбіг з попереднім місяцем", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                // Базова перевірка логіки (щоб не скрутили лічильник у порівнянні з іншими місяцями)
                else if (itemIndex < targetIndex && (tzItem.dayEndReading > dStart || tzItem.nightEndReading > nStart)) {
                    JOptionPane.showMessageDialog(this, "Показники менші за історію!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    return false;
                } else if (itemIndex > targetIndex && (tzItem.dayStartReading < dEnd || tzItem.nightStartReading < nEnd)) {
                    JOptionPane.showMessageDialog(this, "Показники більші за майбутнє!", "Помилка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void autoFillStartReadings(CurrentMonth prevMonth) {
        if (prevMonth instanceof TwoZoneMonth tzItem) {
            double dVal = tzItem.dayEndReading;
            double nVal = tzItem.nightEndReading;

            // Відрізаємо ".0" для дня
            String dText = (dVal % 1 == 0) ? String.valueOf((long) dVal) : String.valueOf(dVal);
            tfDayStart.setText(dText);

            // Відрізаємо ".0" для ночі
            String nText = (nVal % 1 == 0) ? String.valueOf((long) nVal) : String.valueOf(nVal);
            tfNightStart.setText(nText);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TwoZoneMainApp().setVisible(true));
    }
}