import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LabSeven {
    private static final int NUM_THREADS = 8;

    public static void main(String[] args) {
        String[] filenames = {"input1.txt", "input2.txt", "input3.txt"};

        try {
            for (int i = 0; i < filenames.length; i++) {
                long startTime = System.currentTimeMillis();
                String commonestWord = commonestWordFromSingleFile(filenames[i]);
                long endTime = System.currentTimeMillis();
                System.out.println("Найчастіше зустрічається слово. файл № " + (i + 1) + ": " + commonestWord);
                System.out.println("Час виконання. файл № " + (i + 1) + ": " + (endTime - startTime) + " мс");
                System.out.println();
            }

            long startTime = System.currentTimeMillis();
            String commonestWordAllFiles = commonestWordFromAllFiles(filenames);
            long endTime = System.currentTimeMillis();
            System.out.println("Найчастіше зустрічається слово у всіх файлах: " + commonestWordAllFiles);
            System.out.println("Час виконання: " + (endTime - startTime) + " мс");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String commonestWordFromSingleFile(String filename) throws IOException, InterruptedException {
        Map<String, Integer> wordCountMap = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    word = word.toLowerCase();
                    synchronized (wordCountMap) {
                        int count = wordCountMap.getOrDefault(word, 0);
                        wordCountMap.put(word, count + 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        String commonestWord = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                commonestWord = entry.getKey();
            }
        }

        return commonestWord;
    }

    public static String commonestWordFromAllFiles(String[] filenames) throws IOException, InterruptedException {
        Map<String, Integer> wordCountMap = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (String filename : filenames) {
            executor.execute(() -> {
                try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] words = line.split("\\s+");
                        for (String word : words) {
                            word = word.toLowerCase();
                            synchronized (wordCountMap) {
                                int count = wordCountMap.getOrDefault(word, 0);
                                wordCountMap.put(word, count + 1);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        String commonestWord = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                commonestWord = entry.getKey();
            }
        }

        return commonestWord;
    }
}
