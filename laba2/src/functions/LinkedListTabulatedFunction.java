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
    protected int count;
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
        count ++;
    }
    private Node getNode(int index) { //метод для получения узла по индексу
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
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
    public LinkedListTabulatedFunction(double [] xValues, double [] yValues) { //конструктор из массивов
        for (int i = 0; i < xValues.length; i++) {
            addNode(xValues[i], yValues[i]);
        }
    }
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) { // конструктор: дискретизация
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
    @Override
    public int getCount() {
        return count;
    }
    @Override
    public double leftBound() {
        return head.x;
    }

    @Override
    public double rightBound() {
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
            if (Double.compare(current.x, x) == 0) {
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
            if (Double.compare(current.y, y) == 0) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }
    @Override
    protected double extrapolateLeft(double x) { //экстраполяция слева (x<leftBound)
        if (count == 1) {
            return getY(0);
        }
        return interpolate(x, getX(0), getX(1), getY(0), getY(1));
    }
    @Override
    protected double extrapolateRight(double x) { //экстраполяция справа (x>rightBound)
        if (count == 1) {
            return getY(0);
        }
        return interpolate(x, getX(count - 2), getX(count - 1), getY(count - 2), getY(count - 1));
    }
    @Override
    protected double interpolate(double x, int floorIndex) { //интерполяция (x между двух точек)
        if (count == 1) {
            return getY(0);
        }
        return interpolate(x, getX(floorIndex), getX(floorIndex+1), getY(floorIndex), getY(floorIndex+1));
    }
    @Override
    protected int floorIndexOfX(double x) {
        if (x < getX(0)) return 0;
        if (x > getX(count - 1))return count;
        int left = 0;
        int right = count - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (getX(mid) == x) {
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
    public void insert(double x, double y) {
        // если список пустой, просто добавляем узел
        if (head == null) {
            addNode(x, y);
            return;
        }
        // проверяем, существует ли уже узел с таким x
        for (int i = 0; i < count; i++) {
            Node currentNode = getNode(i);
            if (Math.abs(currentNode.x - x) < 1e-10) {
                // если нашли узел с таким x, обновляем y и выходим
                currentNode.y = y;
                return;
            }
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
        if (count == 1) {           //если в списке один узел
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
