package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionTest {

    @Test
    public void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};

        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.leftBound(), 0.0001);
        assertEquals(4.0, function.rightBound(), 0.0001);
    }

    @Test
    public void testConstructorWithFunction() {
        MathFunction source = new SqrFunction();
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 0.0, 4.0, 5);

        assertEquals(5, function.getCount());
        assertEquals(0.0, function.getX(0), 0.0001);
        assertEquals(4.0, function.getX(4), 0.0001);
        assertEquals(0.0, function.getY(0), 0.0001);  // 0² = 0
        assertEquals(16.0, function.getY(4), 0.0001); // 4² = 16
    }

    @Test
    public void testConstructorWithReversedBounds() {
        MathFunction source = x -> x + 1;
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 5.0, 1.0, 5);

        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(5.0, function.getX(4), 0.0001);
    }

    @Test
    public void testConstructorWithEqualBounds() {
        MathFunction source = new SqrFunction();
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(source, 3.0, 3.0, 4);

        assertEquals(4, function.getCount());
        assertEquals(3.0, function.getX(0), 0.0001);
        assertEquals(3.0, function.getX(3), 0.0001);
        assertEquals(9.0, function.getY(0), 0.0001); // 3² = 9
        assertEquals(9.0, function.getY(3), 0.0001);
    }

    @Test
    public void testGetXGetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(2.0, function.getX(1), 0.0001);
        assertEquals(3.0, function.getX(2), 0.0001);

        assertEquals(10.0, function.getY(0), 0.0001);
        assertEquals(20.0, function.getY(1), 0.0001);
        assertEquals(30.0, function.getY(2), 0.0001);
    }

    @Test
    public void testSetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), 0.0001);
    }

    @Test
    public void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(-1, function.indexOfX(1.5));
        assertEquals(-1, function.indexOfX(5.0));
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfY(1.0));
        assertEquals(2, function.indexOfY(9.0));
        assertEquals(-1, function.indexOfY(5.0));
    }

    @Test
    public void testFloorIndexOfX() {
        double[] xValues = {1.0, 3.0, 5.0, 7.0};
        double[] yValues = {1.0, 9.0, 25.0, 49.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(0.5));  // Меньше всех
        assertEquals(0, function.floorIndexOfX(1.0));  // Равно первому
        assertEquals(0, function.floorIndexOfX(2.0));  // Между 1 и 3
        assertEquals(1, function.floorIndexOfX(4.0));  // Между 3 и 5
        assertEquals(2, function.floorIndexOfX(6.0));  // Между 5 и 7
        assertEquals(3, function.floorIndexOfX(7.0));  // Равно последнему
        assertEquals(4, function.floorIndexOfX(8.0));  // Больше всех
    }

    @Test
    public void testApplyExactMatch() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.apply(1.0), 0.0001);
        assertEquals(9.0, function.apply(3.0), 0.0001);
    }

    @Test
    public void testApplyInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Интерполяция между 2 и 3: при x=2.5 должно быть 6.5
        assertEquals(6.5, function.apply(2.5), 0.0001);
    }

    @Test
    public void testApplyExtrapolationLeft() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Экстраполяция слева: продолжение линии между 1 и 2
        // Линия через (1,1) и (2,4): y = 3x - 2
        // При x=0: y = 3*0 - 2 = -2
        assertEquals(-2.0, function.apply(0.0), 0.0001);
    }

    @Test
    public void testApplyExtrapolationRight() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        // Экстраполяция справа: продолжение линии между 3 и 4
        // Линия через (3,9) и (4,16): y = 7x - 12
        // При x=5: y = 7*5 - 12 = 23
        assertEquals(23.0, function.apply(5.0), 0.0001);
    }

    @Test
    public void testInvalidConstructor() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0};

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(xValues, yValues);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(new double[]{1.0}, new double[]{1.0});
        });

        double[] badXValues = {2.0, 1.0};  // Не упорядочены
        double[] goodYValues = {1.0, 2.0};
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(badXValues, goodYValues);
        });
    }
}