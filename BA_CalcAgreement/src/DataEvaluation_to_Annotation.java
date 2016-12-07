import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.visualization.CoincidenceMatrixPrinter;
import de.tudarmstadt.ukp.dkpro.statistics.agreement.visualization.ContingencyMatrixPrinter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

import static de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy.countAnnotationsPerCategory;
import static de.tudarmstadt.ukp.dkpro.statistics.agreement.coding.CodingAnnotationStudy.countTotalAnnotationsPerCategory;

public class DataEvaluation_to_Annotation {
    public HashMap<String, String> intercorpAspect;
    public HashMap<String, List<String>> annotatedDynamics;
    public HashMap<String, List<String>> humansilver;


    public DataEvaluation_to_Annotation() {
        this.intercorpAspect = new HashMap<>();
        this.annotatedDynamics = new HashMap<>();
        this.humansilver = new HashMap<>();
    }

    //TODO:
    public void readIntercorpVerbAspect(String filename) {
        Path path = Paths.get(filename);
        try (BufferedReader bufferedReader = Files.newBufferedReader(path);
             CSVReader reader = new CSVReader(bufferedReader)) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String infinitiv = nextLine[0].trim();
                String token = nextLine[1].trim();
                String aspect = nextLine[2].trim();
                String aspectValue = "";
                if (aspect.equals("pf")) {
                    aspectValue = "telic";
                } else {
                    aspectValue = "atelic";
                }
                //System.out.println(verb + " " + aspectValue + "\n");
                this.intercorpAspect.put(token, aspectValue);
                //intercorpAspect.add(aspectValue);
            }
            System.out.println("telic " + this.intercorpAspect.values().stream().filter(a -> a.equals("telic")).count());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(this.intercorpAspect);
    }

    public void readAllAnnotatedDynamics(String filename, String silverStandard_human, String differences) {
        PrepareEvaluationAnnotations.emptyFile(silverStandard_human);
        PrepareEvaluationAnnotations.emptyFile(differences);
        Path path = Paths.get(filename);
        try (BufferedReader br = Files.newBufferedReader(path);
             CSVReader csv = new CSVReader(br)) {
            String[] line;
            while ((line = csv.readNext()) != null) {
                String verb = line[0].trim();
                String a1 = line[1].trim();
                String a2 = line[2].trim();
                String a3 = line[3].trim();
                List<String> annotations = new ArrayList(Arrays.asList(a1, a2, a3));
                List<String> silverAnnotations = new ArrayList<>(Arrays.asList(a1, a2, a3));
                if (!annotations.contains("dummy_t")) {
                    this.annotatedDynamics.put(verb, annotations);
                }
                if (checkLabelsSame(annotations)) {
                    //System.out.println(annotations);
                    this.humansilver.put(verb, silverAnnotations);
                    writeSilverStandard(silverStandard_human, verb, annotations);
                } else {
                    writeDifferences(differences, verb, annotations);
                }

                //if I also wanted to detect missing values
                //this.annotatedDynamics.put(verb, annotations);
            }
            System.out.println(this.humansilver);
            System.out.println(this.humansilver.values().stream().filter(a -> a.contains("telic")).count());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean checkLabelsSame(List<String> annotations) {
        Set labels = new HashSet(annotations);
        return labels.size() == 1 || (labels.size() == 2 && labels.contains("dummy_t"));
    }


    public void readSingleAnnotatedDynamics(String filename) {
        Path path = Paths.get(filename);
        try (BufferedReader br = Files.newBufferedReader(path);
             CSVReader csv = new CSVReader(br)) {
            String[] line;
            while ((line = csv.readNext()) != null) {
                String verb = line[0].trim();
                String a1 = line[1].trim();
                ArrayList<String> annotations = new ArrayList(Arrays.asList(a1));
                if (!annotations.contains("dummy_t")) {
                    this.annotatedDynamics.put(verb, annotations);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public CodingAnnotationStudy getAnnotationStudy() {
        Set<String> verbKeys = this.annotatedDynamics.keySet();
        int numberAnnotators = this.annotatedDynamics.values().iterator().next().size() + 1;
        System.out.println(numberAnnotators);
        CodingAnnotationStudy telicityStudy = new CodingAnnotationStudy(numberAnnotators);
        for (String key : verbKeys) {
            List<String> item = annotatedDynamics.get(key);
            item.add(this.intercorpAspect.get(key));
            telicityStudy.addItem(item.toArray());
        }
        return telicityStudy;
    }


    //emptied at call
    public void writeSilverStandard(String silverStandard_human, String verb, List<String> annotations) {
        Path p_1 = Paths.get(silverStandard_human);
        try (BufferedWriter bf_1 = Files.newBufferedWriter(p_1, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND, StandardOpenOption.WRITE);
             CSVWriter csv_1 = new CSVWriter(bf_1)) {
            List<String> temp = new ArrayList<>(Arrays.asList(verb));
            temp.addAll(annotations);
            String[] silver = temp.stream().toArray(String[]::new);
            csv_1.writeNext(silver);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //emptied at call
    public void writeDifferences(String differences, String verb, List<String> annotations) {
        Path p_2 = Paths.get(differences);
        try (BufferedWriter bf_2 = Files.newBufferedWriter(p_2, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND, StandardOpenOption.WRITE);
             CSVWriter csv_2 = new CSVWriter(bf_2)) {
            List<String> temp = new ArrayList<>(Arrays.asList(verb));
            temp.addAll(annotations);
            String[] difference = temp.stream().toArray(String[]::new);
            csv_2.writeNext(difference);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAllAgreementMeasures(CodingAnnotationStudy study) {
        System.out.println("Intercorp Telicity Agreement + VallexAnnotator");
        FleissKappaAgreement agreement = new FleissKappaAgreement(study);
        double fk = agreement.calculateAgreement();
        System.out.println(fk);
        double observed = agreement.calculateObservedAgreement();
        System.out.println(observed);
        double expected = agreement.calculateExpectedAgreement();
        System.out.println(expected);

        System.out.println("Telicity coincidence matrix: ");
        new CoincidenceMatrixPrinter()
                .print(System.out, study);
        System.out.println();
        //System.out.println("Contingency matrix");
        //new ContingencyMatrixPrinter().print(System.out, study);

        System.out.println("item count" + " " + study.getItemCount());
        System.out.println("telic: rater distribution " + Arrays.toString(countAnnotationsPerCategory(study).get("telic")));
        System.out.println("atelic: rater distribution " + Arrays.toString(countAnnotationsPerCategory(study).get("atelic")));
        //System.out.println("uncategorized dynamic items" + Arrays.toString(countAnnotationsPerCategory(study).get("dummy_t")));
        System.out.println("total telicity distribution " + countTotalAnnotationsPerCategory(study));
    }

    public void writeSilverVallex(String silver) {
        PrepareEvaluationAnnotations.emptyFile(silver);
        Set<String> verbs = this.humansilver.keySet();
        //System.out.println(this.humansilver);
        Path p = Paths.get(silver);
        int atelic = 0;
        try (BufferedWriter br = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND, StandardOpenOption.WRITE);
             CSVWriter writer = new CSVWriter(br)) {
            for (String verb : verbs) {
                String vallex = this.intercorpAspect.get(verb);
                List<String> annotators = this.humansilver.get(verb);
                //System.out.println(annotators);
                System.out.println("an " + annotators);
                annotators.add(vallex);
                //System.out.println("+v" +annotators);
                if (checkLabelsSame(annotators)) {
                    if (annotators.contains("atelic")) {
                        ++atelic;
                    }
                    ArrayList<String> temp = new ArrayList<>(Arrays.asList(verb));
                    temp.addAll(annotators);
                    String[] match = temp.stream().toArray(String[]::new);
                    writer.writeNext(match);
                }
            }
            System.out.println("atelic in humanVallex " + atelic);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        //ALL ANOTATORS
        String intercorpVerbsFile = "intercorpVerbAspect/verbKeyAspect.csv";
        DataEvaluation_to_Annotation gatheredAnnotation = new DataEvaluation_to_Annotation();
        gatheredAnnotation.readIntercorpVerbAspect(intercorpVerbsFile);
        String allAnnotations = "ComparisonEvaluationVerbs/evaluationAnnotationVerbs_Dynamic.csv";
        String silverStandard_human = "ComparisonEvaluationVerbs/silverStandard_human.csv";
        String differences = "ComparisonEvaluationVerbs/differences.csv";

        gatheredAnnotation.readAllAnnotatedDynamics(allAnnotations, silverStandard_human, differences);

        gatheredAnnotation.getAllAgreementMeasures(gatheredAnnotation.getAnnotationStudy());

        String silverStandard_valex = "ComparisonEvaluationVerbs/silverStandard_vallex.csv";
        gatheredAnnotation.writeSilverVallex(silverStandard_valex);

        // SINGLE ANNOTATOR + VALLEXINTERCORP
//        String intercorpVerbsFile = "intercorpVerbAspect/verbKeyAspect.csv";
//        DataEvaluation_to_Annotation gatheredAnnotation = new DataEvaluation_to_Annotation();
//        gatheredAnnotation.readIntercorpVerbAspect(intercorpVerbsFile);
//        String allAnnotations = "ComparisonEvaluationVerbs/annotator3.csv";
//        gatheredAnnotation.readSingleAnnotatedDynamics(allAnnotations);
//
//        gatheredAnnotation.getAllAgreementMeasures(gatheredAnnotation.getAnnotationStudy());


    }

}