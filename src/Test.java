import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class Test {
    private final String PATH;
    private int quantity = 0;
    private String testName;
    private final ArrayList<Task> TASK = new ArrayList<>();

    public Test(String path) {
        this.PATH = path;
    }

    private void start() {
        int realQuantity = 0;
        int errorAns = 0;
        List<Integer> userAnswers;
        try (Scanner scanner = new Scanner(System.in)) {

            parseQuestions();

            System.out.println("\n" + testName);
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
                    if (userKeys.get(userKeys.size() - 1) > TASK.get(realQuantity - 1).ANSWERS.size() || userKeys.get(userKeys.size() - 1) < 1) {
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
                + ":\n" + TASK.get(realQuantity - 1).QUESTION
                + "\nВарианты ответа:");
        for (int i = 1; i < TASK.get(realQuantity - 1).ANSWERS.size() + 1; i++) {
            System.out.print(i + ") " + TASK.get(realQuantity - 1).ANSWERS.get(i - 1));
        }
    }

    private int analyzerUserAnswer(int realQuantity, int errorAns, List<Integer> userKeys) {
        Collections.sort(userKeys);
        Collections.sort(TASK.get(realQuantity - 1).KEY);
        if (TASK.get(realQuantity - 1).KEY.equals(userKeys)) {
            System.out.println("Ответ верный!");
        } else {
            System.out.println("Ответ НЕ верный. Верный ответ: " + TASK.get(realQuantity - 1).KEY.toString() + ":");
            for (int i = 0; i < TASK.get(realQuantity - 1).KEY.size(); i++) {
                System.out.print("- " + TASK.get(realQuantity - 1).ANSWERS.get((TASK.get(realQuantity - 1)).KEY.get(i) - 1));
            }
            errorAns++;
            System.out.println("Из пройденных " + realQuantity + " вопросов, не верных ответов " + errorAns + ". ");
        }
        return errorAns;
    }

    private String testResult(int errorAns) {
        if (errorAns == 0) {
            return "\nПоздравляю! Вы успешно прошли тест, все ответы верны.";
        }
        return "\nУвы, Вы не прошли тест. \nВы дали " + errorAns + " не верных ответов, из " + quantity + " вопросов.";
    }

    private String charsetsConverter(String inString) {
        return new String(inString.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private void parseQuestions() {
        try (RandomAccessFile file = new RandomAccessFile(PATH, "r")) {
            String line;
            String question = null;
            List<Integer> key;
            ArrayList<String> answers;

            while ((line = file.readLine()) != null) {
                if (line.contains("@Description")) {
                    this.testName = charsetsConverter(file.readLine());
                    continue;
                }
                if (line.contains("@Question")) {
                    quantity++;
                    question = charsetsConverter(file.readLine());
                    continue;
                }
                if (line.contains("@Options")) {
                    key = new ArrayList<>();
                    final String substring = line.substring((line.indexOf("@Key") + 5));
                    if (line.contains("@OnlyOne")) {
                        key.add(Integer.parseInt(substring));
                    }
                    if (line.contains("@Multiple")) {
                        String[] tempKeys = substring.split(",");
                        for (String tempKey : tempKeys) {
                            key.add(Integer.parseInt(tempKey));
                        }
                    }
                    answers = new ArrayList<>();
                    boolean bool = true;
                    while (bool) {
                        if (!(line = file.readLine()).equals("")) {
                            if (file.getFilePointer() >= file.length()) {
                                answers.add(charsetsConverter(line) + "\n"); //charsetsConverter(line);  answers.add(line + "\n");
                                break;
                            }
                            answers.add(charsetsConverter(line) + "\n"); //answers.add(line + "\n");
                        } else {
                            bool = false;
                        }
                    }
                    TASK.add(new Task(question, key, answers));
                }
            }
        } catch (IOException e) {
            System.out.println("Файл теста не найден. Проверте путь и наличие файла в корневой директории программы.");
        }
    }

    private static class Task {
        private final String QUESTION;
        private final List<Integer> KEY;
        private final ArrayList<String> ANSWERS;

        private Task(String question, List<Integer> key, ArrayList<String> answers) {
            this.QUESTION = question;
            this.KEY = key;
            this.ANSWERS = answers;
        }
    }

    public static void main(String[] args) {
        Test test = new Test("./java1.test");
        test.start();
    }
}