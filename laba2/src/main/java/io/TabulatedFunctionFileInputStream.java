package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;

import java.io.*;

public class TabulatedFunctionFileInputStream {
    public static void main(String[] args) {
        // Чтение функции из файла
        try (FileInputStream fileInputStream = new FileInputStream("input/binary function.bin");
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            ArrayTabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction function = FunctionsIO.readTabulatedFunction(bufferedInputStream, arrayFactory);
            System.out.println(function.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Чтение функции из консоли
        System.out.println("Введите размер и значения функции");
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(System.in);
            
            LinkedListTabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
            TabulatedFunction consoleFunction = FunctionsIO.readTabulatedFunction(bufferedInputStream, linkedListFactory);
            
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = differentialOperator.derive(consoleFunction);
            System.out.println(derivative.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

