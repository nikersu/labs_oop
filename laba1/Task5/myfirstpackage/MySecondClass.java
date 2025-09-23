package myfirstpackage;

public class MySecondClass {
    private int firstNumber;
    private int secondNumber;
    
    public MySecondClass(int firstNumber, int secondNumber) {
        this.firstNumber = firstNumber;
        this.secondNumber = secondNumber;
    }
    
    public int getFirstNumber() {
        return firstNumber;
    }
    
    public int getSecondNumber() {
        return secondNumber;
    }
    
    public void setFirstNumber(int firstNumber) {
        this.firstNumber = firstNumber;
    }
    
    public void setSecondNumber(int secondNumber) {
        this.secondNumber = secondNumber;
    }
    
    public int multiply() {
        return firstNumber * secondNumber;
    }
}