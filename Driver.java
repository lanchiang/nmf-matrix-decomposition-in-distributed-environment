import Jama.Matrix;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.summary.TextRankKeyword;
import org.apache.hadoop.mapred.JobConf;

import java.io.IOException;
import java.util.List;

/**
 * Created by Fuga on 15/12/2.
 */
public class Driver {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
////        CorpusUtils cu = new CorpusUtils("/Users/Fuga/Documents/workspace/NMFDecomposition/SogouC.reduced");
////        cu.readAllFile();
////        MatrixGenerator mg = new MatrixGenerator("/Users/Fuga/Documents/workspace/NMFDecomposition/slice");
////        mg.wordIndex();
////        mg.makeMatrixCSV("matrix.csv");
//        MatrixUtils mu = new MatrixUtils();
////        mu.readFile("matrix.csv");
//        mu.readFile("m1.csv");
//        Matrix m = mu.getX();
//        NMFDecomposition nmf = new NMFDecomposition(m, 5);
////        NMFDecomposition nmf = new NMFDecomposition(7);
//        nmf.factorizeNMF_Di();
//        Matrix w = nmf.getW();
//        Matrix h = nmf.getH();
//        MatrixUtils.print(w);
//        MatrixUtils.print(h);
////        MatrixUtils.print(m);
//        MatrixUtils.print(w.times(h));
//        System.exit(0);
        matrix_reconstruct("/nmf/w.csv", "/nmf/h.csv");
    }

    public static void matrix_reconstruct(String matrix1_path, String matrix2_path) {
        try {
            JobConfiguration jconf = new JobConfiguration(ParameterLoad.loadparatmer());
            jconf.setInput1(matrix1_path);
            jconf.setInput2(matrix2_path);
            jconf.setOutput("/nmf");
            jconf.setOutputname("reconstruct.csv");
            MatrixMultiply.run(jconf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
