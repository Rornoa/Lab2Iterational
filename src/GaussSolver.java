import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GaussSolver {

    private double[][] matrix;
    private int rows, columns;
    private double accuracy;

    public static void main(String[] args) {
        GaussSolver gaussSolver = new GaussSolver();
        try {
            gaussSolver.readFile("/home/nik/IdeaProjects/ComputingMath/Lab2Iterational/src/input");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        gaussSolver.printMatrix(gaussSolver.matrix);
        Answer answer = gaussSolver.decideAnswer();

        if (answer.STATUS == Answer.Status.ONE) {
            answer.printRoots();
        } else {
            answer.printStatus();
        }
    }

    public void readFile(String theInputFilePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(theInputFilePath));
        rows = scanner.nextInt();
        columns = rows + 1;
        accuracy = scanner.nextDouble();
        matrix = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = scanner.nextDouble();
            }
        }
        scanner.close();
    }

    public void printMatrix(double[][] mat) {
        System.out.println("\n");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++)
                System.out.printf("%16.6e", mat[i][j]);

            System.out.println();
        }
    }

    /***
     * @Answer - Вложенный класс содержит корни системы алгебраический уравнений.
     */
    public static class Answer {

        public enum Status {ONE, NONE, INFINITY, INVERTIBLE}

        public final double[] ROOTS;
        public final Status STATUS;

        private Answer(double[] ROOTS, Status STATUS) {
            this.ROOTS = ROOTS;
            this.STATUS = STATUS;
        }

        public void printRoots() {
            System.out.println("\tРешение:");
            for (int i = 0; i < ROOTS.length; i++)
                System.out.printf("\tx%d = %13.6e\n", i, ROOTS[i]);
        }

        public void printStatus() {
            switch (STATUS) {
                case NONE:
                    System.out.println("\tСЛАУ не имеет решений");
                    break;
                case INFINITY:
                    System.out.println("\tСЛАУ имеет бесконечно много решений");
                    break;
                case ONE:
                    System.out.println("\tСЛАУ имеет единственное решение");
                    break;
                case INVERTIBLE:
                    System.out.println("\tСЛАУ является вырожденной");
            }
        }
    }

    public Answer decideAnswer() {
        double[][] tmp = cloneMatrix();
        boolean successfullyTriangular = tryMakeTriangular(tmp);
        printMatrix(tmp);
        if (!successfullyTriangular)
            return new Answer(null, Answer.Status.INVERTIBLE);
        // Коэфф. при последнем неизвестном
        if (isZero(tmp[rows - 1][columns - 2])) {
            // Свободный член
            if (isZero(tmp[rows - 1][columns - 1]))
                return new Answer(null, Answer.Status.INFINITY);
            else
                return new Answer(null, Answer.Status.NONE);
        }
        return new Answer(calculateRoots(tmp), Answer.Status.ONE);
    }

    private double[][] cloneMatrix() {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < columns; j++)
                result[i][j] = matrix[i][j];
        return result;
    }

    /***
     * @tryMakeTriangle - Проверка можно ли сделать матрицу треугольной
     * @return - возвращает true и вызывает метод @recalculateRow, если можно привести к треугольному виду. false если нет
     */
    private boolean tryMakeTriangular(double[][] matrix) {
        for (int k = 0; k < rows - 1; k++) {
            if (isZero(matrix[k][k])) {
                int notZeroLine = findValidRow(matrix, k);
                if (notZeroLine == rows) return false;
                swapRows(matrix, k, notZeroLine);
            }
            for (int i = k + 1; i < rows; i++)
                recalculateRow(matrix, k, i);
        }
        return true;
    }

    /**
     * @param current - индекс элемента главной диагонали
     * @return возвращает номер элемента который не равен нул
     * @findValidRow
     */
    private int findValidRow(double[][] tmp, int current) {
        int i = current + 1;
        while (i < rows && isZero(tmp[i][current])) i++; //Проходимся от следующего элемента от главной диагонли
        return i; // Идем по строкам вниз и возвращаем номер элемента если не равен нулю
    }

    private void swapRows(double[][] matrix, int firstRow, int secondRow) {
        double[] temporary = matrix[secondRow];
        matrix[secondRow] = matrix[firstRow];
        matrix[firstRow] = temporary;
    }

    /***
     * @recalculateRow - переставляем строки местами
     */
    private void recalculateRow(double[][] matrix, int origin, int current) {
        double f = matrix[current][origin] / matrix[origin][origin];
        matrix[current][origin] = 0;
        for (int j = origin + 1; j < columns; j++) {
            double n = matrix[current][j] - f * matrix[origin][j];
            matrix[current][j] = isZero(n) ? 0 : n;
        }
    }

    private boolean isZero(double element) {
        return Math.abs(element) < accuracy;
    }

    /**
     * @param triangleMatrix принимает матрицу в треугольном виде
     * @return возвращает матрицу ответов для неизвестных системы
     * @calculateRoots - обратный ход метода Гаусса
     * @value sum - это произведение самого последнего элемента "матрицы ответов" на коэффициент при неизвестной
     *
     * Метод состоит из двух частей
     * 1.) Проходим снизу вверх по матрице
     * 2.) Смещаемся на предыдущий элемент на главной диагонали
     * 3.) Запоминаем сумму произведений корней
     */
    private double[] calculateRoots(double[][] triangleMatrix) {
        double[] roots = new double[rows];
        for (int k = rows - 1; k >= 0; k--) {
            double sum = triangleMatrix[k][columns - 1];
            for (int s = columns - 2; s > k; s--)
                sum -= triangleMatrix[k][s] * roots[s];
            roots[k] = sum / triangleMatrix[k][k];
        }
        return roots;
    }
}


