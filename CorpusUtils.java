import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.sun.xml.internal.bind.api.impl.NameConverter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fuga on 15/12/3.
 */
public class CorpusUtils {

    private String corpuspath;

    private static int documentcount = 1;

    final private float maintain_threshold = 0.5F;

    public CorpusUtils(String corpuspath) {
        this.corpuspath = corpuspath;
    }

    public boolean readAllFile() {
        boolean isfinished = false;
        File root = new File(corpuspath);
        if (root.exists()) {
            String output_path = "slice/";
            File output = new File(output_path);
            if (!output.exists()) {
                output.mkdir();
                if (readAllFile(root, output_path)) isfinished = true;
            }
            else return true;
        }
        return isfinished;
    }

    public boolean readAllFile(File parent, String outputpath) {
        try {
            if (parent.isFile()) {
                if (parent.getName().equals(".DS_Store")) {
                    return true;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(parent), "gb2312"));
                Map<String, Float> frequency = new HashMap<>();
                String line;
                List<Term> allterms = new ArrayList<>();
                while ((line=br.readLine())!=null) {
                    List<Term> terms = StandardTokenizer.segment(line);
                    allterms.addAll(terms);
                    for (Term t : terms) {
                        if (frequency.containsKey(t.toString())) {
                            frequency.put(t.toString(), frequency.get(t.toString())+1);
                        }
                        else {
                            frequency.put(t.toString(), 1F);
                        }
                    }
                }
                br.close();

                WordsRank wr = new WordsRank();
                Map<String, Float> rank = wr.wordsrank(allterms);

                BufferedWriter bw = new BufferedWriter(new FileWriter(outputpath + "/slice_" + documentcount++ + ".txt"));
                int i = 0;
                for (String str : rank.keySet()) {
                    bw.write(str + "\t" + frequency.get(str) + "\t" + rank.get(str));
                    bw.newLine();
                    i++;
                    if (i>rank.size()*maintain_threshold) break;
                }
                bw.close();
                System.out.println("文档" + documentcount + "分词处理完成");
                return true;
            }
            else {
                File[] filelist = parent.listFiles();
                for (File f : filelist) {
                    readAllFile(f, outputpath);
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


}