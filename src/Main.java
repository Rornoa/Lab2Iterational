import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {

        IterationalSolver iterationalSolver = new IterationalSolver();
        try {
            iterationalSolver.readFromFile("/home/nik/IdeaProjects/ComputingMath/Lab2Iterational/src/input");
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        iterationalSolver.printMatrix();
        int resultCode = iterationalSolver.analysis();
        iterationalSolver.printMatrix();

        if (resultCode == -1) {
            printFail();
        } else {
            printSolution(resultCode == 0? iterationalSolver.uncontrolledSolve() : iterationalSolver.controlledSolve());
        }

    }

    static void printFail() { System.out.println("\tНе удалось решить итерационным методом"); }

    static void printSolution(double[] s) {
        if (s == null)
            printFail();
        else {
            System.out.println("\tРешение:");
            for (int i = 0; i < s.length; i++)
                System.out.printf("\tx%d = %13.6e\n", i, s[i]);
        }
    }
}
