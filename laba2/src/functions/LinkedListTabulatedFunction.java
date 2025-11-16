package functions;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable {
    private static class Node { //вспомогательный класс
        public double x;
        public double y;
        public Node next;
        public Node prev;
        public Node(double x, double y) { //конструктор
            this.x = x;
            this.y = y;
        }
    }

    private Node head;
    private int count;

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        if (xValues.length < 2) {
            throw new IllegalArgumentException("At least 2 points required");
        }
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2) {
            throw new IllegalArgumentException("At least 2 points required");
        }
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if (xFrom == xTo) {
            double y = source.apply(xFrom);
            for (int i = 0; i < count; i++) {
                addNode(xFrom, y);
            }
        }
        else {
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                double x = xFrom + i * step;
                double y = source.apply(x);
                addNode(x, y);
            }
        }
    }

    private void addNode(double x, double y) { //метод для добавления узла в конец списка
        Node newNode = new Node(x, y);
        if (head == null) { //если список пустой
            head = newNode;
            head.next = head;
            head.prev = head;
        }
        else {
            Node last = head.prev;
            last.next = newNode;
            newNode.prev = last;
            newNode.next = head;
            head.prev = newNode;
        }
        count++;
    }

    private Node getNode(int index) { //метод для получения узла по индексу
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        Node current;
        if (index <= count / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            current = head.prev;
            for (int i = count - 1; i > index; i--) {
                current = current.prev;
            }
        }
        return current;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double leftBound() {
        if (head == null) {
            throw new IllegalStateException("Function is empty");
        }
        return head.x;
    }

    @Override
    public double rightBound() {
        if (head == null) {
            throw new IllegalStateException("Function is empty");
        }
        return head.prev.x;
    }

    @Override
    public double getX(int index) {
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.x - x) < 1e-12) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (Math.abs(current.y - y) < 1e-12) {
                return i;
            }
            current = current.next;
        }
        return -1;
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
        Node leftNode = getNode(floorIndex);
        Node rightNode = leftNode.next;
        return interpolate(x, leftNode.x, rightNode.x, leftNode.y, rightNode.y);
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (x < getX(0)) {
            throw new IllegalArgumentException("x is less than left bound: " + x);
        }
        if (x > getX(count - 1)) {
            return count;
        }

        // Линейный поиск для связного списка
        Node current = head;
        for (int i = 0; i < count - 1; i++) {
            // Если x находится между current.x и current.next.x, или равен current.x
            if (x >= current.x && x < current.next.x) {
                return i;
            }
            current = current.next;
        }

        // Если x равен последнему элементу или больше всех (но мы уже проверили > getX(count-1))
        return count - 1;
    }

    @Override
    public void insert(double x, double y) {
        // Проверяем, существует ли уже узел с таким x
        int existingIndex = indexOfX(x);
        if (existingIndex != -1) {
            setY(existingIndex, y);
            return;
        }

        // если список пустой, просто добавляем узел
        if (head == null) {
            addNode(x, y);
            return;
        }

        // создаем новый узел
        Node newNode = new Node(x, y);

        // если новый узел должен быть в начале списка
        if (x < head.x) {
            Node last = head.prev;
            // устанавливаем связи для нового узла
            newNode.next = head;
            newNode.prev = last;
            // обновляем связи соседних узлов
            head.prev = newNode;
            last.next = newNode;
            // обновляем голову списка
            head = newNode;
            count++;
            return;
        }

        // если новый узел должен быть в конце списка
        if (x > head.prev.x) {
            Node last = head.prev;
            // устанавливаем связи для нового узла
            newNode.next = head;
            newNode.prev = last;
            // обновляем связи соседних узлов
            last.next = newNode;
            head.prev = newNode;
            count++;
            return;
        }

        // поиск места для вставки в середину списка
        Node current = head;
        for (int i = 0; i < count; i++) {
            if (current.x < x && x < current.next.x) {
                // нашли место для вставки между current и current.next
                newNode.next = current.next;
                newNode.prev = current;
                // обновляем связи соседних узлов
                current.next.prev = newNode;
                current.next = newNode;
                count++;
                return;
            }
            current = current.next;
        }
    }

    @Override
    public void remove(int index) { //метод для удаления узлов
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index out of bounds: " + index);
        }
        if (count == 2) {
            throw new IllegalStateException("Cannot remove element - minimum 2 points required");
        }

        if (count == 1) { //если в списке один узел
            head = null;
            count = 0;
            return;
        }

        Node nodeToRemove = getNode(index);
        if (nodeToRemove == head) { //если удаляем голову
            head = head.next;
        }

        nodeToRemove.prev.next = nodeToRemove.next; //переписываем узлы
        nodeToRemove.next.prev = nodeToRemove.prev;
        count--;

        if (count == 0) { //если список стал пустым
            head = null;
        }
    }
}