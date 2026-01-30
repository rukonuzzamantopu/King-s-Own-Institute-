
import javax.swing.JFrame;

public class CalculatorRun extends JFrame {

    public CalculatorRun() {
        setTitle("Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 350);
        setLocationRelativeTo(null);
        setResizable(false);
        add(new MainCode());
        setVisible(true);
    }

    public static void main(String[] args) {
        new CalculatorRun();
    }
}
