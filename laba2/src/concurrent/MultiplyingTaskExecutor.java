package concurrent;
import functions.LinkedListTabulatedFunction;
import functions.UnitFunction;
import functions.TabulatedFunction;
import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {
    public static void main(String[] args) {
        TabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), 1, 1000, 1000);
        // список потоков
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            Thread thread = new Thread(task);
            threads.add(thread);
        }
        // запуск
        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // вывод
        System.out.println(function.toString());

        double expectedValue = Math.pow(2, 10); // 1024
        double actualValue = function.getY(0);
        System.out.println(expectedValue);
        System.out.println(actualValue);
        System.out.println((Math.abs(actualValue - expectedValue) < 0.001));
    }
}