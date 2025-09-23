import myfirstpackage.MySecondClass;

class MyFirstClass {
    public static void main(String[] s) {
        // Создание и инициализация объекта "o" типа MySecondClass
        MySecondClass o = new MySecondClass(5, 3);
        System.out.println(o.multiply());
        
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                o.setFirstNumber(i);
                o.setSecondNumber(j);
                System.out.print(o.multiply());
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}