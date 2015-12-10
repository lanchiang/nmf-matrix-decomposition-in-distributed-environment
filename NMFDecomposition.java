import Jama.Matrix;

import java.io.IOException;

/**
 * Created by Fuga on 15/12/2.
 */
public class NMFDecomposition {

    // X = W*R*H

    Matrix X;
    Matrix W;
    Matrix H;
    int n;
    int m;
    int r; // dimension of R
    int maxIterations;
    double tolerance;

    public static double EPS = 1e-9;

    public NMFDecomposition(int r) {
        this.r = r;

        setupParameters(100, 0.01);
    }

    public NMFDecomposition(Matrix x, int r) {
        this.X = x;
        this.r = r;
        this.n = X.getRowDimension();
        this.m = X.getColumnDimension();

        setupParameters(100, 0.01);
    }

    public NMFDecomposition(Matrix X, int r, int maxIterations) {
        this(X, r);
        this.maxIterations = maxIterations;
    }

    public NMFDecomposition(Matrix X, int r, int maxIterations, double tolerance) {
        this(X, r, maxIterations);
        this.tolerance = tolerance;
    }

    private void setupParameters(int maxIterations, double tolerance) {

        W = new Matrix(n, r);
        H = new Matrix(r, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < r; j++) {
                W.set(i, j, Math.random());
            }
        }
        MatrixUtils mu = new MatrixUtils();
        mu.matrixToDisc(W, "w.csv");
        mu.matrixToDisc(W.transpose(), "wt.csv");

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < m; j++) {
                H.set(i, j, Math.random());
            }
        }
        mu.matrixToDisc(H, "h.csv");
        mu.matrixToDisc(H.transpose(), "ht.csv");

        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    void factorizeNMF_Di() throws IOException, InterruptedException, ClassNotFoundException {
        int iterations = 0;

        JobConfiguration jconf = new JobConfiguration(ParameterLoad.loadparatmer());
        FSOperation fso = new FSOperation();
        while (iterations < maxIterations) {
            // Calculate W(transpose)*W
            jconf.setInput1("/nmf/wt.csv");
            jconf.setInput2("/nmf/w.csv");
            jconf.setOutput("/nmf/output");
            jconf.setOutputname("wtw.csv");
            MatrixMultiply.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS")+"/nmf/output/wtw.csv", jconf.getPath().get("HDFS")+"/nmf/wtw.csv");
            System.out.println("Iteration:" + iterations + ",Calculate W(T)*W");

            // Update Matrix H
            // Schritt eins, WT*X und WTW*H berechnen
            jconf.setInput1("/nmf/wt.csv");
            jconf.setInput2("/nmf/x.csv");
            jconf.setOutputname("wtx.csv");
            MatrixMultiply.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/wtx.csv", jconf.getPath().get("HDFS") + "/nmf/wtx.csv");
            System.out.println("Iteration:" + iterations + ",Calculate W(T)*X");
            jconf.setInput1("/nmf/wtw.csv");
            jconf.setInput2("/nmf/h.csv");
            jconf.setOutputname("wtwh.csv");
            MatrixMultiply.run(jconf);
                fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/wtwh.csv", jconf.getPath().get("HDFS") + "/nmf/wtwh.csv");
            System.out.println("Iteration:" + iterations + ",Calculate W(T)W*H");

            // Schritt zwei, calculate factor
            jconf.setInput1("/nmf/wtx.csv");
            jconf.setInput2("/nmf/wtwh.csv");
            jconf.setOutputname("factor.csv");
            MatrixDivisionByElement.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/factor.csv", jconf.getPath().get("HDFS") + "/nmf/factor.csv");
            System.out.println("Iteration:" + iterations + ",Calculate W(T)*X / W(T)W*H by element");

            // Schritt drei, update matrix H
            jconf.setInput1("/nmf/h.csv");
            jconf.setInput2("/nmf/factor.csv");
            jconf.setOutputname("h.csv");
            MatrixMultiplyByElement.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/h.csv", jconf.getPath().get("HDFS") + "/nmf/tmp/h.csv");
            // update matrix HT
            jconf.setOutputname("ht.csv");
            MatrixMultiplyByElementTranspose.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/ht.csv", jconf.getPath().get("HDFS") + "/nmf/ht.csv");
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/tmp/h.csv", jconf.getPath().get("HDFS") + "/nmf/h.csv");

            // Calculate H*H(transpose)
            jconf.setInput1("/nmf/h.csv");
            jconf.setInput2("/nmf/ht.csv");
//            jconf.setOutput("/nmf/output");
            jconf.setOutputname("hht.csv");
            MatrixMultiply.run(jconf);
                fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/hht.csv", jconf.getPath().get("HDFS") + "/nmf/hht.csv");
            System.out.println("Iteration:"+iterations+",Calculate H*H(T)");

            // Update matrix W
            // Schritt eins, X*HT und W*HHT berechnen
            jconf.setInput1("/nmf/x.csv");
            jconf.setInput2("/nmf/ht.csv");
            jconf.setOutputname("xht.csv");
            MatrixMultiply.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/xht.csv", jconf.getPath().get("HDFS") + "/nmf/xht.csv");
            System.out.println("Iteration:" + iterations + ",Calculate X*H(T)");

            jconf.setInput1("/nmf/w.csv");
            jconf.setInput2("/nmf/hht.csv");
            jconf.setOutputname("whht.csv");
            MatrixMultiply.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/whht.csv", jconf.getPath().get("HDFS") + "/nmf/whht.csv");
            System.out.println("Iteration:" + iterations + ",Calculate W*HH(T)");

            // Schritt zwei, calculate factor
            jconf.setInput1("/nmf/xht.csv");
            jconf.setInput2("/nmf/whht.csv");
            jconf.setOutputname("factor.csv");
            MatrixDivisionByElement.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/factor.csv", jconf.getPath().get("HDFS") + "/nmf/factor.csv");
            System.out.println("Iteration:" + iterations + ",Calculate X*H(T) / W*HH(T) by element");

            // Schritt drei, update matrix W
            jconf.setInput1("/nmf/w.csv");
            jconf.setInput2("/nmf/factor.csv");
            jconf.setOutputname("w.csv");
            MatrixMultiplyByElement.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/w.csv", jconf.getPath().get("HDFS") + "/nmf/tmp/w.csv");
            // update matrix WT
            jconf.setOutputname("wt.csv");
            MatrixMultiplyByElementTranspose.run(jconf);
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/output/wt.csv", jconf.getPath().get("HDFS") + "/nmf/wt.csv");
            fso.moveFile(jconf.getPath().get("HDFS") + "/nmf/tmp/w.csv", jconf.getPath().get("HDFS") + "/nmf/w.csv");

            // Check for convergence
            if (euclideanDistanceSquare(X, W.times(H)) < tolerance) {
                break;
            }
            iterations++;
        }
    }

    void factorizeNMF() throws IllegalArgumentException {
        int iterations = 0;

        while (iterations < maxIterations) {
            // Calculate W(transpose) * W;
            Matrix WTW = new Matrix(r, r);
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < r; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < n; k++) {
                        sum += W.get(k, i) * W.get(k, j);
                    }
                    WTW.set(i, j, sum);
                }
            }
            System.out.println("Iteration:"+iterations+",Calculate W(T)*W");

            // Update matrix H
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < m; j++) {
                    double numerator = 0.0;
                    for (int k = 0; k < n; k++) {
                        numerator += W.get(k, i) * X.get(k, j); //WT*X
                    }

                    double denominator = 0.0;
                    for (int k = 0; k < r; k++) {
                        denominator += WTW.get(i, k) * H.get(k, j); // WTW*H
                    }

                    if (denominator != 0.0) {
                        H.set(i, j, H.get(i, j) * numerator / denominator);
                    }
                    else {
                        H.set(i, j, X.get(i, j));
                    }
                }
            }
            System.out.println("Iteration:"+iterations+",Update matrix H");

            //Calculate H * H(transpose)
            Matrix HHT = new Matrix(r, r);
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < r; j++) {
                    double sum = 0.0;
                    for (int k = 0; k < m; k++) {
                        sum += H.get(i, k) * H.get(j, k);
                    }
                    HHT.set(i, j, sum);
                }
            }
            System.out.println("Iteration:"+iterations+",Calculate H*H(T)");

            // Update matrix W
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < r; j++) {
                    double numerator = 0.0;
                    for (int k = 0; k < m; k++) {
                        numerator += X.get(i, k) * H.get(j, k);
                    }

                    double denominator = 0.0;
                    for (int k = 0; k < r; k++) {
                        denominator += W.get(i, k) * HHT.get(k, j);
                    }

                    if (denominator != 0.0) {
                        W.set(i, j, W.get(i, j) * numerator / denominator);
                    }
                    else {
                        W.set(i, j, 0.0);
                    }
                }
            }
            System.out.println("Iteration:"+iterations+",Update matrix W");

            // Check for convergence
            if (euclideanDistanceSquare(X, W.times(H)) < tolerance) {
                break;
            }
            iterations++;
        }
    }

    double euclideanDistanceSquare(Matrix A, Matrix B) throws IllegalArgumentException {
        double distance = 0.0;
        int p = A.getRowDimension();
        int q = A.getColumnDimension();

        if (p != B.getRowDimension() || q != B.getColumnDimension()) {
            throw new IllegalArgumentException("Matrix dimensions must agree");
        }

        for (int i = 0; i < p; i++) {
            for (int j = 0; j < q; j++) {
                distance += Math.pow((A.get(i, j) - B.get(i, j)), 2.0);
            }
        }

        return distance;
    }

    int getr() {
        return r;
    }

    int getn() {
        return n;
    }

    int getm() {
        return m;
    }

    Matrix getW() {
        return W;
    }

    Matrix getH() {
        return H;
    }
}
