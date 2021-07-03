import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

/***
 * @IterationalSolver - выполняет задачу: нахождение решения СЛАУ методом Гаусса-Зеделя
 */
public class IterationalSolver {

    private double[][] matrix;
    private int rows, columns;
    private double accuracy;

    private double[] rowSums;
    private Replacement replacement;

    private final int CONTROL_ITERATIONS = 10;
    private final int CONTROL_THRESHOLD = 4;

    /***
     * @Reranger - выполняет задачу перестановки
     * indexes - массив строк которые учавствуют в перестановке
     */
    private static class Replacement {
        int[] indexes;
        int resultKey;

        Replacement(int[] i, int r) {
            indexes = i;
            resultKey = r;
        }
    }

    public void readFromFile(String inputPath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(inputPath));
        scanner.useLocale(Locale.ENGLISH);

        rows = scanner.nextInt();
        columns = rows + 1;
        accuracy = scanner.nextDouble();
        matrix = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++)
                matrix[i][j] = scanner.nextDouble();
        }
        scanner.close();
        calculateRowSums();
        replacement = new Replacement(createIndexesArray(), -1);
    }

    private void calculateRowSums() {
        rowSums = new double[rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns -1; j++) { //не учитываем последний элемент матрицы
                rowSums[i] += Math.abs(matrix[i][j]);
            }
        }
    }

    public void printMatrix() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns - 1; j++)
                System.out.printf("%16.6e", matrix[i][j]);
            System.out.printf("   |  %13.6e", matrix[i][columns - 1]);
            System.out.println();
        }
        System.out.println();
    }

    /**
     * @analysis - метод вызывает проверку выполнения достаточного условия сходимости
     * @return возвращает значения в main для определения: удастся ли найти решения итерационным методом?
     */
    public int analysis() {
        if (isDiagonalZero())
            return dusCheck() ? 0 : 1;
        else
            return findReplacement().resultKey;
    }

    /**
     * @isDiagonalZero - проверяем элементы главной диагонали матрицы на наличие в них нулей
     * @return возвращает true если находит, и false если нет нулей
     */
    public boolean isDiagonalZero() {
        for (int i = 0; i < rows; i++)
            if (isZero(matrix[replacement.indexes[i]][i]))
                return false;
        return true;
    }

    /**
     * @isZero - проверка числа на ноль с учетом точности
     * accuracy - точность
     * @return true если число равно 0, false если не равно
     */
    private boolean isZero(double a) {
        return Math.abs(a) < accuracy;
    }

    /**
     * @duscCheck - Проверка достаточного условия сходимости
     * 1) значения по модулю на главной диагонали должны быть не меньше суммы остальных значений по модулю
     * 2) должно быть хотя бы одно значение, которое строго больше этой суммы
     * @return
     */
    public boolean dusCheck() {
        boolean higherThanSum = false;
        Replacement r = replacement;

        for (int i = 0; i < rows; i++) {
            if (Math.abs(matrix[r.indexes[i]][i]) < rowSums[r.indexes[i]] - Math.abs(matrix[r.indexes[i]][i]))
                return false;
            if (Math.abs(matrix[r.indexes[i]][i]) > rowSums[r.indexes[i]] - Math.abs(matrix[r.indexes[i]][i]))
                higherThanSum = true;
        }
        return higherThanSum;
    }

    /**
     * @findReplacement - Поиск подходящей перестановки
     * подходящая перестановка 'r' - перестановка с выполненным ДУС
     * @return если не находит, возвращает последнюю перестановку без нулей на диагонали
     */
    public Replacement findReplacement() {
        int[] notNullPermutation = createIndexesArray();
        replace(0, notNullPermutation);
        if (replacement.resultKey == 1) replacement.indexes = notNullPermutation;
        setMatrix(replacement);
        return replacement;
    }

    /**
     * @replace - Перестановка строк
     */
    void replace(int i, int[] notNullReplacement) {
        if (replacement.resultKey == 0) return;
        if (i == replacement.indexes.length - 1) {
            if (dusCheck())
                replacement.resultKey = 0;
            else if (isDiagonalZero()) {
                System.arraycopy(replacement.indexes, 0, notNullReplacement, 0, rows);
                replacement.resultKey = 1;
            }
        } else {
            for (int j = i; j < replacement.indexes.length; j++) {
                swapElementsInArray(replacement.indexes, i, j);
                    replace(i + 1, notNullReplacement);
                if (replacement.resultKey == 0) return;
                swapElementsInArray(replacement.indexes, i, j);
            }
        }
    }

    private void swapElementsInArray(int[] tmp, int a, int b) {
        int t = tmp[b];
        tmp[b] = tmp[a];
        tmp[a] = t;
    }

    private int indexOf(int a, int[] array) {
        for (int i = 0; i < array.length; i++)
            if (a == array[i]) return i;
        return -1;
    }

    private int[] createIndexesArray() {
        int[] a = new int[rows];
        for (int i = 0; i < rows; i++) {
            a[i] = i;
        }
        return a;
    }

    /**
     * @setMatrix - Ставит строки матрицы в соотвествие с перестановкой
     */
    private void setMatrix(Replacement replacement) {
        double[][] tmp = new double[rows][];

        for (int i = 0; i < rows; i++)
            tmp[i] = matrix[replacement.indexes[i]];
        matrix = tmp;
    }

    // Решение без контроля
    public double[] uncontrolledSolve() {
        return uncontrolledSolve(new double[rows]);
    }
    private double[] uncontrolledSolve(double[] accuracy) {
        double maxDifference;

        do maxDifference = nextAccuracy(accuracy);
        while (maxDifference > this.accuracy);

        return accuracy;
    }

    /**
     * @controlledSolve - нахождение решения СЛАУ с контролем
     * максимальная разность между приближениями должна уменьшаться
     * current - массив с текущими приближениями
     * @return - возвращаем количество последних уменьшающихся разностей - 'lastDecreaseDifferences'
     */
    public double[] controlledSolve() {
        double[] current = new double[rows];
        double prevDiff, currDiff = nextAccuracy(current);
        int lastDecreaseDifferences = 0;

        for (int i = 0; i < CONTROL_ITERATIONS; i++) {
            prevDiff = currDiff;
            currDiff = nextAccuracy(current);
            lastDecreaseDifferences = currDiff < prevDiff ? lastDecreaseDifferences + 1 : 0;
            if (currDiff < accuracy) return current;
        }
        return lastDecreaseDifferences >= CONTROL_THRESHOLD ? uncontrolledSolve(current) : null;
    }
    
    /**
     * @nextAccuracy - Вычисление следующего приблежения
     * @return 'maxDifference' - максимальную разность между следующей и предыдущей итерации
     */
    private double nextAccuracy(double[] accuracy) {
        double maxDifference = 0;

        for (int k = 0; k < rows; k++) {

            double sum = matrix[k][columns - 1];
            for (int s = 0; s < k; s++)
                sum -= matrix[k][s] * accuracy[s];
            for (int s = k + 1; s < rows; s++)
                sum -= matrix[k][s] * accuracy[s];

            double prev = accuracy[k];
            accuracy[k] = sum / matrix[k][k];
            double diff = Math.abs(accuracy[k] - prev);

            if (diff > maxDifference)
                maxDifference = diff;
        }
        //System.out.println(maxDifference);
        return maxDifference;

    }
}