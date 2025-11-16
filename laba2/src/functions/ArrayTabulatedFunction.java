package functions;

import java.util.Arrays;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable {
    private double[] xArray;
    private double[] yArray;
    private int count;

    public ArrayTabulatedFunction(double[] xArray, double[] yArray) {
        if (xArray.length != yArray.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        if (xArray.length < 2) {
            throw new IllegalArgumentException("At least 2 points required");
        }
        this.count = xArray.length;
        this.xArray = Arrays.copyOf(xArray, count);
        this.yArray = Arrays.copyOf(yArray, count);
    }

    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("At least 2 points required");
        }
        this.count = count;
        this.xArray = new double[count];
        this.yArray = new double[count];
        if (xFrom > xTo) {
            //меняем местами начальную точку и конечную
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }
        if (xFrom == xTo) {
            //заполняем все х одним значением
            Arrays.fill(xArray, xFrom);
            double yArrays = source.apply(xFrom);
            //заполняем все у одним значением
            Arrays.fill(yArray, yArrays);
        } else {
            //иначе выполняем дискретизацию
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                xArray[i] = xFrom + i * step;
                yArray[i] = source.apply(xArray[i]);
            }
        }
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        return xArray[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        return yArray[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        yArray[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(getX(i) - x) < 1e-12) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (Math.abs(getY(i) - y) < 1e-12) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public double leftBound() {
        return getX(0);
    }

    @Override
    public double rightBound() {
        return getX(count - 1);
    }

    @Override
    protected int floorIndexOfX(double x) { //поиск левой границы для интервала х
        if (x < getX(0)) {
            throw new IllegalArgumentException("x is less than left bound: " + x);
        }
        if (x > getX(count - 1)) return count;
        //бинарный поиск интервала
        int left = 0;
        int right = count - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2; //находим средний индекс
            if (Math.abs(getX(mid) - x) < 1e-12) {
                return mid;
            } else if (getX(mid) < x){
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return right;
    }

    @Override
    protected double extrapolateLeft(double x) {
        // Убрана проверка count == 1, так как теперь гарантируется count >= 2
        return interpolate(x, getX(0), getX(1), getY(0), getY(1));
    }

    @Override
    protected double extrapolateRight(double x) {
        // Убрана проверка count == 1, так как теперь гарантируется count >= 2
        return interpolate(x, getX(count - 2), getX(count - 1), getY(count - 2), getY(count - 1));
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        // Убрана проверка count == 1, так как теперь гарантируется count >= 2
        if (floorIndex < 0 || floorIndex >= count - 1) {
            throw new IllegalArgumentException("Invalid floor index: " + floorIndex);
        }
        return interpolate(x, getX(floorIndex), getX(floorIndex+1), getY(floorIndex), getY(floorIndex+1));
    }

    @Override
    public void insert(double x, double y) {
        for (int i = 0; i < count; i++) { //поиск х
            if (Math.abs (xArray[i] - x) < 1e-12) {
                yArray[i] = y; //замена значения
                return;
            }
        }
        int insertIndex = 0; //поиск позиции для вставки
        while (insertIndex < count && xArray[insertIndex] < x) {
            insertIndex++;
        }
        double[] newXValues = new double[count + 1]; //создание увеличенных массивов
        double[] newYValues = new double[count + 1];

        System.arraycopy(xArray, 0, newXValues, 0, insertIndex); //копирование эл-тов до позиции вставки
        System.arraycopy(yArray, 0, newYValues, 0, insertIndex);

        newXValues[insertIndex] = x; //втавка нового элемента
        newYValues[insertIndex] = y;

        System.arraycopy(xArray, insertIndex, newXValues, insertIndex + 1, count - insertIndex); //копирование эл-тов после поиции вставки
        System.arraycopy(yArray, insertIndex, newYValues, insertIndex + 1, count - insertIndex);

        xArray = newXValues; //замена старых массивов
        yArray = newYValues;
        count++;
    }

    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        if (count == 2) {
            throw new IllegalStateException("Cannot remove element - minimum 2 points required");
        }

        //новые массивы уменьшенного размера
        double[] newXArray = new double[count - 1];
        double[] newYArray = new double[count - 1];

        //копирует левую часть до удаляемого элемента
        System.arraycopy(xArray, 0, newXArray, 0, index);
        System.arraycopy(yArray, 0, newYArray, 0, index);

        //копирует правую часть до удаляемого элемента
        System.arraycopy(xArray, index + 1, newXArray, index, count - index - 1);
        System.arraycopy(yArray, index + 1, newYArray, index, count - index - 1);
        this.xArray = newXArray; //заменяет старые массивы на новые
        this.yArray = newYArray;
        this.count--;
    }
}