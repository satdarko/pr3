import java.util.concurrent.*;
import java.util.Random;
import java.util.Scanner;

public class MatrixMultiplication {
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

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        long startTime = System.nanoTime();
        long[][] result = forkJoinPool.invoke(new MatrixMultiplicationTask(matrixA, matrixB, 0, size, 0, size));
        long endTime = System.nanoTime();

        System.out.println("Результат добутку матриць:");
        printMatrix(result);

        long duration = (endTime - startTime) / 1_000_000;
        System.out.println("Час виконання: " + duration + " мс");
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

    public static void printMatrix(long[][] matrix) {
        for (long[] row : matrix) {
            for (long value : row) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
    }

    // Завдання для ForkJoinPool
    static class MatrixMultiplicationTask extends RecursiveTask<long[][]> {
        private final long[][] matrixA;
        private final long[][] matrixB;
        private final int rowStart;
        private final int rowEnd;
        private final int colStart;
        private final int colEnd;
        private final int threshold = 64;  // Поріг для розбиття задачі на менші підзадачі

        public MatrixMultiplicationTask(long[][] matrixA, long[][] matrixB, int rowStart, int rowEnd, int colStart, int colEnd) {
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.rowStart = rowStart;
            this.rowEnd = rowEnd;
            this.colStart = colStart;
            this.colEnd = colEnd;
        }

        @Override
        protected long[][] compute() {
            int size = matrixA.length;
            long[][] result = new long[size][size];

            // Якщо завдання маленьке, безпосередньо виконуємо обчислення
            if (rowEnd - rowStart <= threshold && colEnd - colStart <= threshold) {
                for (int i = rowStart; i < rowEnd; i++) {
                    for (int j = colStart; j < colEnd; j++) {
                        result[i][j] = 0;
                        for (int k = 0; k < size; k++) {
                            result[i][j] += matrixA[i][k] * matrixB[k][j];
                        }
                    }
                }
            } else {
                // Розділяємо задачу на менші підзадачі
                int midRow = (rowStart + rowEnd) / 2;
                int midCol = (colStart + colEnd) / 2;

                MatrixMultiplicationTask task1 = new MatrixMultiplicationTask(matrixA, matrixB, rowStart, midRow, colStart, midCol);
                MatrixMultiplicationTask task2 = new MatrixMultiplicationTask(matrixA, matrixB, midRow, rowEnd, colStart, midCol);
                MatrixMultiplicationTask task3 = new MatrixMultiplicationTask(matrixA, matrixB, rowStart, midRow, midCol, colEnd);
                MatrixMultiplicationTask task4 = new MatrixMultiplicationTask(matrixA, matrixB, midRow, rowEnd, midCol, colEnd);

                invokeAll(task1, task2, task3, task4);

                long[][] result1 = task1.join();
                long[][] result2 = task2.join();
                long[][] result3 = task3.join();
                long[][] result4 = task4.join();

                // Об'єднуємо результати
                for (int i = rowStart; i < rowEnd; i++) {
                    for (int j = colStart; j < colEnd; j++) {
                        result[i][j] = result1[i][j] + result2[i][j] + result3[i][j] + result4[i][j];
                    }
                }
            }
            return result;
        }
    }
}
