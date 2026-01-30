import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class MainCode extends JPanel implements ActionListener {
    private final JTextField display;
    private String currentInput = "";
    private boolean resetNext = false;

    public MainCode() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.LIGHT_GRAY);

        JLabel title = new JLabel("Calculator", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Arial", Font.BOLD, 22));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setBackground(Color.GRAY);
        display.setForeground(Color.WHITE);
        display.setPreferredSize(new Dimension(0, 50));
        add(display, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(5, 4, 5, 5));
        String[] keys = {
            "C", "()", "%", "÷",
            "7", "8", "9", "x",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "B", "0", ".", "="
        };

        for (String key : keys) {
            JButton btn = new JButton(key);
            btn.setFont(new Font("Arial", Font.BOLD, 18));
            btn.setBackground(specialColor(key));
            btn.setForeground(Color.BLACK);
            btn.addActionListener(this);
            buttons.add(btn);
        }

        add(buttons, BorderLayout.SOUTH);
    }

    private Color specialColor(String key) {
        if (key.equals("C")) return Color.YELLOW;
        if ("÷x-+=()%".contains(key)) return Color.CYAN;
        if (key.equals("B")) return Color.GREEN.darker();
        return new Color(180, 180, 180);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        if (cmd.equals("C")) {
            currentInput = "";
            display.setText("");
        } else if (cmd.equals("B")) {
            if (!currentInput.isEmpty())
                currentInput = currentInput.substring(0, currentInput.length() - 1);
            display.setText(currentInput);
        } else if (cmd.equals("=")) {
            calculateResult();
        } else {
            if (resetNext) {
                currentInput = "";
                resetNext = false;
            }
            currentInput += cmd;
            display.setText(currentInput);
        }
    }

   private void calculateResult() {
    try {
        String expr = currentInput.replace("x", "*").replace("÷", "/").replaceAll("\\s", "");

        // Match expression like: -3*-3 or -20.5/2.5 or 10.2+3
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(-?\\d+(\\.\\d+)?)([+\\-*/])(-?\\d+(\\.\\d+)?)$");
        java.util.regex.Matcher matcher = pattern.matcher(expr);

        if (!matcher.matches()) {
            display.setText("Invalid");
            resetNext = true;
            return;
        }

        double left = Double.parseDouble(matcher.group(1));
        String operator = matcher.group(3);
        double right = Double.parseDouble(matcher.group(4));

        double result;

        switch (operator) {
            case "+":
                result = left + right;
                break;
            case "-":
                result = left - right;
                break;
            case "*":
                result = left * right;
                break;
            case "/":
                if (right == 0) {
                    display.setText("Division by zero");
                    resetNext = true;
                    return;
                }
                result = left / right;
                break;
            default:
                display.setText("Error");
                resetNext = true;
                return;
        }

        if (result == (int) result)
            display.setText(String.valueOf((int) result));
        else
            display.setText(String.valueOf(result));

    } catch (Exception e) {
        display.setText("Error");
    } finally {
        resetNext = true;
    }
}


}
