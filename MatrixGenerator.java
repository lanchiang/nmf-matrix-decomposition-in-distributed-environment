import java.io.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Fuga on 15/12/3.
 */
public class MatrixGenerator {

    private String readin_path;
    private Map<String, Integer> word_index;

    public MatrixGenerator(String readin_path) {
        this.readin_path = readin_path;
    }

    public void wordIndex() throws IOException {
        word_index = new TreeMap<>();
        File root = new File(readin_path);
        if (!root.exists()) return;
        File[] files = root.listFiles();

        int index = 0;
        int filecount = 0;
        for (File file: files) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine())!=null) {
                String[] info = line.split("\t");
                if (!word_index.containsKey(info[0])) {
                    word_index.put(info[0], ++index);
                }

            }
            System.out.println(filecount++);
            br.close();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter("word_index.txt"));
        for (String word : word_index.keySet()) {
            bw.write(word + "\t" + word_index.get(word));
            bw.newLine();
        }
        bw.close();
    }

    public void makeMatrixCSV(String output_path) {
        try {
            File root = new File(readin_path);
            if (!root.exists()) return;
            File[] files = root.listFiles();

            int filecount = 0;
            BufferedWriter bw = new BufferedWriter(new FileWriter(output_path));
            for (File file: files) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String docnum = file.getName().substring(0, file.getName().length()-4).split("_")[1];
                String line;
                while ((line=br.readLine())!=null) {
                    String[] info = line.split("\t");
                    try {
                        Float.parseFloat(info[2]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    bw.write(docnum + "," + word_index.get(info[0]) + "," + info[2]);
                    bw.newLine();
                }
                System.out.println(filecount++);
                br.close();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
