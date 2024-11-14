import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.Random;
import java.util.Scanner;
import java.util.List;

public class MatrixMultiplication2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введіть розмір матриць (наприклад, 3 для 3x3):");
        int size = scanner.nextInt();

        System.out.println("Введіть мінімальне значення елементів матриці:");
        int minValue = scanner.nextInt();

        System.out.println("Введіть максимальне значення елементів матриці:");
        int maxValue = scanner.nextInt();

        long[][] matrixA = generateMatrix(size, minValue, maxValue);
        long[][] matrixB = generateMatrix(size, minValue, maxValue);

        System.out.println("Матриця A:");
        printMatrix(matrixA);

        System.out.println("Матриця B:");
        printMatrix(matrixB);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        long startTime = System.nanoTime();
        long[][] result = multiplyMatricesWithWorkDealing(matrixA, matrixB, executor);
        long endTime = System.nanoTime();

        System.out.println("Результат добутку матриць:");
        printMatrix(result);

        long duration = (endTime - startTime) / 1_000_000;
        System.out.println("Час виконання: " + duration + " мс");

        executor.shutdown();
    }

    public static long[][] generateMatrix(int size, int minValue, int maxValue) {
        long[][] matrix = new long[size][size];
        Random random = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = minValue + (long)(random.nextDouble() * (maxValue - minValue + 1));
            }
        }
        return matrix;
    }

    public static long[][] multiplyMatricesWithWorkDealing(long[][] matrixA, long[][] matrixB, ExecutorService executor) {
        int size = matrixA.length;
        long[][] result = new long[size][size];

        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int finalI = i;
            futures.add(executor.submit(() -> {
                for (int j = 0; j < size; j++) {
                    result[finalI][j] = 0;
                    for (int k = 0; k < size; k++) {
                        result[finalI][j] += matrixA[finalI][k] * matrixB[k][j];
                    }
                }
                return null;
            }));
        }

        // Чекаємо завершення всіх задач
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void printMatrix(long[][] matrix) {
        for (long[] row : matrix) {
            for (long value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
    }
}
