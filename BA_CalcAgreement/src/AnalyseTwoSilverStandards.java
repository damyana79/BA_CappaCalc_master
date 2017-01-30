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
import java.util.*;
import java.util.stream.Collectors;

public class AnalyseTwoSilverStandards {
    private Map<String, String> silver_human_only;
    private Map<String, String> silver_human_vallex;
    private Set<String> difference;

    private static HashMap<String, String> readSilvers(String silver) {
        HashMap<String, String> dict = new HashMap<>();
        Path p = Paths.get(silver);
        try (BufferedReader br = Files.newBufferedReader(p);
             CSVReader reader = new CSVReader(br);) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String verb = nextLine[0].trim();
                String label = nextLine[1].trim();
                dict.put(verb, label);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return dict;
    }

    private void compareSilvers(String human_only, String human_vallex) {
        this.silver_human_only = readSilvers(human_only);
        this.silver_human_vallex = readSilvers(human_vallex);
        Set<String> human_k = this.silver_human_only.keySet();
        Set<String> human_vallex_k = this.silver_human_vallex.keySet();
        this.difference = human_k.stream().filter(n -> !human_vallex_k.contains(n)).collect(Collectors.toSet());
        //System.out.println(this.difference.size());
    }

    private void outputDifference(String corpus, String verbs) {
        emptyFile(verbs);
        Path p = Paths.get(corpus);
        Path p_v = Paths.get(verbs);
        try (BufferedReader br = Files.newBufferedReader(p);
             CSVReader csvReader = new CSVReader(br);
             BufferedWriter bw = Files.newBufferedWriter(p_v, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                     StandardOpenOption.APPEND, StandardOpenOption.WRITE);
             CSVWriter csvWriter = new CSVWriter(bw)) {
            String[] firstline = new String[]{"Verb", "human only labels", "human vallex labels", "sentence"};
            csvWriter.writeNext(firstline);

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String verb_token = nextLine[1].trim();
                if (this.difference.contains(verb_token)) {
                    String label = nextLine[2].trim();
                    String sentence = nextLine[3].trim();

                    String[] output = new String[]{verb_token, this.silver_human_only.get(verb_token), label, sentence};
                    csvWriter.writeNext(output);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void emptyFile(String filename) {
        try {
            Files.deleteIfExists(Paths.get(filename));
        } catch (IOException e) {
            System.err.println("Cannot access " + filename);
            System.err.println(e);
            return;
        }
    }

    public static void main(String[] args) {
        String human_only = "ComparisonEvaluationVerbs/silverStandard_human.csv";
        String human_vallex = "ComparisonEvaluationVerbs/silverStandard_vallex.csv";
        String verbs = "ComparisonEvaluationVerbs/silverDifference.csv";
        String corpus = "ComparisonEvaluationVerbs/selected_1_copy.csv";

        AnalyseTwoSilverStandards asi = new AnalyseTwoSilverStandards();
        asi.compareSilvers(human_only, human_vallex);
        asi.outputDifference(corpus, verbs);
    }
}
