import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class SplitEvaluationAnnotators {


    //TODO: viel besser möglich
    public static void getSingleAnnotators(String input, String file1, String file2, String file3) {
        Path path = Paths.get(input);
        ArrayList<String[]> annotator1 = new ArrayList<>();
        ArrayList<String[]> annotator2 = new ArrayList<>();
        ArrayList<String[]> annotator3 = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path);
             CSVReader csvReader = new CSVReader(br)) {
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String verb = nextLine[0].trim();
                String aspect1 = nextLine[1].trim();
                String aspect2 = nextLine[2].trim();
                String aspect3 = nextLine[3].trim();
                String telicity1 = nextLine[4].trim();
                String telicity2 = nextLine[5].trim();
                String telicity3 = nextLine[6].trim();

                if (aspect1.equals("dynamic")) {
                    annotator1.add(new String[]{verb, telicity1});
                }
                if (aspect2.equals("dynamic")) {
                    annotator2.add(new String[]{verb, telicity2});
                }
                if (aspect3.equals("dynamic")) {
                    annotator3.add(new String[]{verb, telicity3});
                }
            }
            writeSingleAnnotators(file1, annotator1);
            writeSingleAnnotators(file2,annotator2);
            writeSingleAnnotators(file3, annotator3);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void writeSingleAnnotators(String input, ArrayList<String[]> annotations) {
        Path path = Paths.get(input);
        try (BufferedWriter buf = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND, StandardOpenOption.WRITE);
             CSVWriter writer = new CSVWriter(buf)) {
            for (String[] annotation: annotations){
                writer.writeNext(annotation);
            }

        } catch (IOException e) {
            System.err.println("Error writing file " + path);
            // System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public static void  main(String[] args){
        String fileAll = "EvaluationVerbs/evaluationAnnotationVerbs.csv";
        String annotator1 = "EvaluationVerbs/annotator1.csv";
        String annotator2 = "EvaluationVerbs/annotator2.csv";
        String annotator3 = "EvaluationVerbs/annotator3.csv";

        getSingleAnnotators(fileAll, annotator1, annotator2, annotator3);

    }

}
