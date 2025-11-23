package concurrent;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SynchronizedTabulatedFunctionTest {

    @Test
    public void testSynchronizedArrayFunction() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(arrayFunction);
        // проверка основных методов
        assertEquals(4, synchronizedFunction.getCount());
        assertEquals(1.0, synchronizedFunction.getX(0));
        assertEquals(4.0, synchronizedFunction.getY(1));
        assertEquals(1.0, synchronizedFunction.leftBound());
        assertEquals(4.0, synchronizedFunction.rightBound());
        // изменение значений
        synchronizedFunction.setY(2, 10.0);
        assertEquals(10.0, synchronizedFunction.getY(2));
        // поиск индексов
        assertEquals(1, synchronizedFunction.indexOfX(2.0));
        assertEquals(3, synchronizedFunction.indexOfY(16.0));
        // применение функции
        assertEquals(4.0, synchronizedFunction.apply(2.0));
    }

    @Test
    public void testSynchronizedLinkedListFunction() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};

        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);
        TabulatedFunction synchronizedFunction = new SynchronizedTabulatedFunction(linkedListFunction);
        // основные методы
        assertEquals(4, synchronizedFunction.getCount());
        assertEquals(1.0, synchronizedFunction.getX(0));
        assertEquals(4.0, synchronizedFunction.getY(1));
        // изменение значений
        synchronizedFunction.setY(3, 10.0);
        assertEquals(10.0, synchronizedFunction.getY(3));
    }
}