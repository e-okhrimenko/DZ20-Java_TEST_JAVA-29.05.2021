import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class Test {
    private final String path;
    private int quantity = 0;
    private final ArrayList<Task> task = new ArrayList<>();
    int errorAns = 0;

    public Test(String path) {
        this.path = path;
    }

    private void start() {
        int realQuantity = 0;

        List<Integer> userAnswers;
        try (Scanner scanner = new Scanner(System.in)) {
            parseQuestions();
            while (quantity != realQuantity++) {
                setQuestion(realQuantity);
                userAnswers = getUserAnswers(realQuantity, scanner);
                errorAns = analyzerUserAnswer(realQuantity, errorAns, userAnswers);
            }
        }
        System.out.println(testResult(errorAns));
    }

    private List<Integer> getUserAnswers(int realQuantity, Scanner scanner) {
        List<Integer> userKeys;
        while (true) {
            System.out.print("\nУкажите правельные варианты ответов разделяя запятой (1,2,3,...): ");
            String stringIn = scanner.nextLine().trim().replaceAll(" ", "");
            String[] tempKeys = stringIn.split(",");
            userKeys = new ArrayList<>();
            try {
                if (tempKeys.length == 0) {
                    throw new NumberFormatException();
                }
                for (String tempKey : tempKeys) {
                    userKeys.add(Integer.parseInt(tempKey));
                    if (userKeys.get(userKeys.size() - 1) > task.get(realQuantity - 1).answers.size() || userKeys.get(userKeys.size() - 1) < 1) {
                        throw new NumberFormatException();
                    }
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ввели не верно. Введите правильно!");
            }
        }
        return userKeys;
    }

    private void setQuestion(int realQuantity) {
        System.out.println("\nВопрос " + realQuantity + " из " + quantity
                + ":\n" + task.get(realQuantity - 1).question
                + "\nВарианты ответа:");
        for (int i = 1; i < task.get(realQuantity - 1).answers.size() + 1; i++) {
            System.out.print(i + ") " + task.get(realQuantity - 1).answers.get(i - 1));
        }
    }

    private int analyzerUserAnswer(int realQuantity, int errorAns, List<Integer> userKeys) {
        Collections.sort(userKeys);
        Collections.sort(task.get(realQuantity - 1).key);
        if (task.get(realQuantity - 1).key.equals(userKeys)) {
            System.out.println("Ответ верный!");
        } else {
            System.out.println("Ответ НЕ верный. Верный ответ: " + task.get(realQuantity - 1).key.toString() + ":");
            for (int i = 0; i < task.get(realQuantity - 1).key.size(); i++) {
                System.out.print("- " + task.get(realQuantity - 1).answers.get((task.get(realQuantity - 1)).key.get(i) - 1));
            }
            errorAns++;
            System.out.println("Из пройденных " + realQuantity + " вопросов, не верных ответов " + errorAns + ". ");
        }
        return errorAns;
    }

    private String testResult(int errorAns) {
        switch (errorAns) {
            case 0:
                return "\nПоздравляю! Вы успешно прошли тест, все ответы верны.";
            case -1:
                return "\nФайл теста не найден. Проверте путь и наличие файла в корневой директории программы.";
            default:
                return "\nУвы, Вы не прошли тест. \nВы дали " + errorAns + " не верных ответов, из " + quantity + " вопросов.";
        }
    }

    private String charsetsConverter(String inString) {
        return new String(inString.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private void parseQuestions() {
        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            String line;
            String question = null;
            List<Integer> key;
            ArrayList<String> answers;

            while ((line = file.readLine()) != null) {
                if (line.contains("@Description")) {
                    System.out.println("\n" + charsetsConverter(file.readLine()));
                    continue;
                }
                if (line.contains("@Question")) {
                    quantity++;
                    question = charsetsConverter(file.readLine());
                    continue;
                }
                if (line.contains("@Options")) {
                    key = parseKey(line);
                    answers = parseAnswers(file);
                    task.add(new Task(question, key, answers));
                }
            }
        } catch (IOException e) {
            errorAns = -1;
        }
    }

    @NotNull
    private List<Integer> parseKey(String line) {
        List<Integer> key;
        key = new ArrayList<>();
        final String substring = line.substring((line.indexOf("@Key") + 5));
        if (line.contains("@OnlyOne")) {
            key.add(Integer.parseInt(substring));
        }
        if (line.contains("@Multiple")) {
            parseMultiple(key, substring);
        }
        return key;
    }

    private void parseMultiple(List<Integer> key, String substring) {
        String[] tempKeys = substring.split(",");
        for (String tempKey : tempKeys) {
            key.add(Integer.parseInt(tempKey));
        }
    }

    @NotNull
    private ArrayList<String> parseAnswers(RandomAccessFile file) throws IOException {
        ArrayList<String> answers;
        String line;
        answers = new ArrayList<>();
        boolean bool = true;
        while (bool) {
            if (!(line = file.readLine()).equals("")) {
                if (file.getFilePointer() >= file.length()) {
                    answers.add(charsetsConverter(line) + "\n");
                    break;
                }
                answers.add(charsetsConverter(line) + "\n");
            } else {
                bool = false;
            }
        }
        return answers;
    }

    private static class Task {
        private final String question;
        private final List<Integer> key;
        private final ArrayList<String> answers;

        private Task(String question, List<Integer> key, ArrayList<String> answers) {
            this.question = question;
            this.key = key;
            this.answers = answers;
        }
    }

    public static void main(String[] args) {
        Test test = new Test("./java1.test");
        test.start();
    }
}