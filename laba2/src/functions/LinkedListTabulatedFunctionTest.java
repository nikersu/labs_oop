package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionTest {
    @Test
    public void testConstructorWithArrays() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {2.0, 4.0, 6.0, 8.0};

        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.leftBound(), 0.0001);
        assertEquals(4.0, function.rightBound(), 0.0001);
    }

    @Test
    public void testConstructorWithFunction() {
        MathFunction source = new SqrFunction();
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(source, 0.0, 4.0, 5);

        assertEquals(5, function.getCount());
        assertEquals(0.0, function.getX(0), 0.0001);
        assertEquals(4.0, function.getX(4), 0.0001);
        assertEquals(0.0, function.getY(0), 0.0001);
        assertEquals(16.0, function.getY(4), 0.0001);
    }

    @Test
    public void testConstructorWithReversedBounds() {
        MathFunction source = x -> x + 1;
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(source, 5.0, 1.0, 5);

        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(5.0, function.getX(4), 0.0001);
    }

    @Test
    public void testConstructorWithEqualBounds() {
        MathFunction source = new SqrFunction();
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(source, 3.0, 3.0, 4);

        assertEquals(4, function.getCount());
        assertEquals(3.0, function.getX(0), 0.0001);
        assertEquals(3.0, function.getX(3), 0.0001);
        assertEquals(9.0, function.getY(0), 0.0001);
        assertEquals(9.0, function.getY(3), 0.0001);
    }

    @Test
    public void testGetXGetY() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

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
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.setY(1, 25.0);
        assertEquals(25.0, function.getY(1), 0.0001);
    }

    @Test
    public void testIndexOfX() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfX(1.0));
        assertEquals(2, function.indexOfX(3.0));
        assertEquals(-1, function.indexOfX(1.5));
        assertEquals(-1, function.indexOfX(5.0));
    }

    @Test
    public void testIndexOfY() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfY(1.0));
        assertEquals(2, function.indexOfY(9.0));
        assertEquals(-1, function.indexOfY(5.0));
    }

    @Test
    public void testFloorIndexOfX() {
        double[] xValues = {1.0, 3.0, 5.0, 7.0};
        double[] yValues = {1.0, 9.0, 25.0, 49.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(0.5));
        assertEquals(0, function.floorIndexOfX(1.0));
        assertEquals(0, function.floorIndexOfX(2.0));
        assertEquals(1, function.floorIndexOfX(4.0));
        assertEquals(2, function.floorIndexOfX(6.0));
        assertEquals(3, function.floorIndexOfX(7.0));
        assertEquals(4, function.floorIndexOfX(8.0));
    }

    @Test
    public void testApplyExactMatch() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(1.0, function.apply(1.0), 0.0001);
        assertEquals(9.0, function.apply(3.0), 0.0001);
    }

    @Test
    public void testApplyInterpolation() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(6.5, function.apply(2.5), 0.0001);
    }

    @Test
    public void testApplyExtrapolationLeft() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(-2.0, function.apply(0.0), 0.0001);
    }

    @Test
    public void testApplyExtrapolationRight() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(23.0, function.apply(5.0), 0.0001);
    }

    @Test
    public void testSinglePointFunction() {
        MathFunction source = x -> x * 2;
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(source, 5.0, 5.0, 1);

        assertEquals(1, function.getCount());
        assertEquals(5.0, function.getX(0), 0.0001);
        assertEquals(10.0, function.getY(0), 0.0001);
        assertEquals(10.0, function.apply(3.0), 0.0001);
        assertEquals(10.0, function.apply(10.0), 0.0001);
    }

    @Test
    public void testInsertAtBeginning() {
        double[] xValues = {2.0, 3.0, 4.0};
        double[] yValues = {4.0, 9.0, 16.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(1.0, 1.0);

        assertEquals(4, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(1.0, function.getY(0), 0.0001);
        assertEquals(2.0, function.getX(1), 0.0001);
    }

    @Test
    public void testInsertAtEnd() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(4.0, 16.0);

        assertEquals(4, function.getCount());
        assertEquals(4.0, function.getX(3), 0.0001);
        assertEquals(16.0, function.getY(3), 0.0001);
        assertEquals(3.0, function.getX(2), 0.0001);
    }

    @Test
    public void testInsertInMiddle() {
        double[] xValues = {1.0, 3.0, 5.0};
        double[] yValues = {1.0, 9.0, 25.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 4.0);

        assertEquals(4, function.getCount());
        assertEquals(2.0, function.getX(1), 0.0001);
        assertEquals(4.0, function.getY(1), 0.0001);
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(3.0, function.getX(2), 0.0001);
    }

    @Test
    public void testInsertReplaceExisting() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 5.0);

        assertEquals(3, function.getCount()); // Количество не изменилось
        assertEquals(2.0, function.getX(1), 0.0001);
        assertEquals(5.0, function.getY(1), 0.0001); // Y изменился
    }

    @Test
    public void testInsertIntoEmptyList() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(new double[0], new double[0]);

        function.insert(1.0, 2.0);

        assertEquals(1, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(2.0, function.getY(0), 0.0001);
    }

    @Test
    public void testRemoveFirst() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(2, function.getCount());
        assertEquals(2.0, function.getX(0), 0.0001);
        assertEquals(3.0, function.getX(1), 0.0001);
    }

    @Test
    public void testRemoveLast() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(2);

        assertEquals(2, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(2.0, function.getX(1), 0.0001);
    }

    @Test
    public void testRemoveMiddle() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(1);

        assertEquals(2, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(3.0, function.getX(1), 0.0001);
    }

    @Test
    public void testRemoveSingleElement() {
        double[] xValues = {1.0};
        double[] yValues = {2.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(0, function.getCount());
        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(0));
    }

    @Test
    public void testRemoveAllElements() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.remove(0);
        function.remove(0);

        assertEquals(0, function.getCount());
    }

    @Test
    public void testGetNodeInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> function.getX(3));
    }

    @Test
    public void testComplexInsertAndRemove() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(new double[0], new double[0]);

        // Добавляем элементы
        function.insert(2.0, 4.0);
        function.insert(1.0, 1.0);
        function.insert(3.0, 9.0);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(2.0, function.getX(1), 0.0001);
        assertEquals(3.0, function.getX(2), 0.0001);

        // Удаляем средний элемент
        function.remove(1);

        assertEquals(2, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(3.0, function.getX(1), 0.0001);

        // Добавляем новый элемент в середину
        function.insert(2.0, 4.0);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0), 0.0001);
        assertEquals(2.0, function.getX(1), 0.0001);
        assertEquals(3.0, function.getX(2), 0.0001);
    }
}
