import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.Scanner;

public class FileSearchTask extends RecursiveTask<Integer> {
    private final File directory;
    private final String keyword;

    public FileSearchTask(File directory, String keyword) {
        this.directory = directory;
        this.keyword = keyword;
    }

    @Override
    protected Integer compute() {
        int count = 0;
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Рекурсивно запускаємо нову задачу для вкладеної директорії
                    FileSearchTask task = new FileSearchTask(file, keyword);
                    task.fork();
                    count += task.join(); // Додаємо результат підзадачі
                } else if (file.getName().contains(keyword)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Отримуємо вхідні дані від користувача
        System.out.print("Введіть шлях до директорії: ");
        String directoryPath = scanner.nextLine();
        System.out.print("Введіть літеру або слово для пошуку в назвах файлів: ");
        String keyword = scanner.nextLine();

        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Неправильний шлях до директорії.");
            return;
        }

        // Запускаємо вимірювання часу
        long startTime = System.nanoTime();

        // Використовуємо ForkJoinPool для запуску задачі
        ForkJoinPool pool = new ForkJoinPool();
        FileSearchTask task = new FileSearchTask(directory, keyword);

        int result = pool.invoke(task);

        // Завершуємо вимірювання часу
        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000; // переводимо наносекунди в мілісекунди

        System.out.println("Кількість файлів, що містять '" + keyword + "' у назві: " + result);
        System.out.println("Час виконання: " + durationInMillis + " мс");
    }
}
