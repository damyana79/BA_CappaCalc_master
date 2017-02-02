import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.opencsv.CSVWriter;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class ParseAnnotation {

    String projectName;
    String xmlFolderName;
    String textDocPathName;


    public ParseAnnotation(String projectName, String xmlAnnotationsFolder, String rawDocPathName) {
        this.projectName = projectName;
        this.xmlFolderName = xmlAnnotationsFolder;
        this.textDocPathName = rawDocPathName;
    }


    /**
     * @param documentName
     * @param annotationFiles
     * @return output: AL[HM <spanId:AnnotationObject>] per document; input
     * provided through iteration over groupdocs(String folderpath)
     * @throws IOException
     * @throws DocumentException
     */
    public static ArrayList<HashMap<Integer, Annotation>> getParsedAnnotations(
            String documentName, List<String> annotationFiles)
            throws IOException, DocumentException {
        ArrayList<HashMap<Integer, Annotation>> parsedAnnotations = new ArrayList<HashMap<Integer, Annotation>>();
        for (String filename : annotationFiles) {
            parsedAnnotations.add(parseFile(filename));
        }
        return parsedAnnotations;
    }

    /**
     * @param filename
     * @return output: for each annotated file HashMap <id, Annotation(attributes I need)>
     * @throws DocumentException
     * @throws IOException
     */
    public static HashMap<Integer, Annotation> parseFile(String filename)
            throws DocumentException, IOException {
        HashMap<Integer, Annotation> annotationTable = new HashMap<Integer, Annotation>();

        File inputFile = new File(filename);
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputFile);

        List<Node> nodes = document
                .selectNodes("/document/annotations/annotation");

        for (Node node : nodes) {
            int start = Integer.parseInt(node.selectSingleNode("start")
                    .getText());
            int end = Integer.parseInt(node.selectSingleNode("end").getText());
            String targetType = node.selectSingleNode("targetType").getText();

            List<Node> andere = node.selectNodes("labels");
            String aspClass = null;
            String telicity = null;
            for (Node deeper : andere) {
                String name = deeper.valueOf("@labelSetName");
                String value = deeper.selectSingleNode("label").getText();
                if (name.equals("Lexical Aspectual Class")) {
                    aspClass = value;
                } else if (name.equals("Telicity")) {
                    telicity = value;
                } else
                    throw new IOException("illegal label name " + name);
            }
            Annotation item = new Annotation(start, end, filename, targetType,
                    aspClass, telicity);
            annotationTable.put(start, item);
        }
        return annotationTable;
    }

    /**
     * //@param folderpath
     *
     * @return output: all files in HashMap:(full)filename:[corresponding
     * annotated files]
     */
    //TODO: ?input: nicht folderpath, sonsern liste aus files, die schon vorkombiniert wurden in einer anderen methode
    //public static Map<String, List<String>> groupDocs(String folderpath)
    public Map<String, List<String>> groupDocs() {
        Map<String, List<String>> groupDoc = new HashMap<String, List<String>>();
        //TODO: den teil des codes, das mir alle files gibt in einer anderen methode auslagern;
        // die kombiantionen/diese methode hier so lange aufrufen, bis die kombinationsliste leer ist
        File folder = new File(this.xmlFolderName);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            System.err.println("This directory doesn't exist");
        }
        if (listOfFiles.length == 0) {
            System.err.println("This directory is empty.");
        }
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String wholeName = file.getPath();
                // System.out.println(wholeName);
                int underscore = wholeName.lastIndexOf("_");
                String filename = wholeName.substring(0, underscore);
                // String annotator = wholeName.substring(underscore + 1);
                // System.out.println(filename + " " + annotator);

                if (!groupDoc.containsKey(filename)) {
                    groupDoc.put(filename, new ArrayList<String>());
                }
                groupDoc.get(filename).add(wholeName);
            }
        }
        // System.out.println(groupDoc);
        return groupDoc;
    }

    public static String getSentence(String text, int begin, int end) {
        while (begin > 0 && text.charAt(begin) != '.') {
            begin--;
        }
        if (text.charAt(begin) == '.')
            ++begin;
        if (text.charAt(begin) == ' ')
            ++begin;
        while (end < text.length() && text.charAt(end) != '.') {
            end++;
        }
        return text.substring(begin, end + 1).trim();
    }

    /**
     * @param disagreementProDoc
     * @return collects different annotations in HashMap <Integer,ArrayList<Annotation>> disagreementProDoc
     */
    public void lookupLabels(List<List<Annotation>> disagreementProDoc, String rawDocPathname, String outputFilePathName) {
        // Set<String> docName = disagreementList.get(0).keySet();

        String text;
        try {
            text = new String(Files.readAllBytes(Paths.get(rawDocPathname)),
                    "UTF-8");
        } catch (IOException e) {
            System.err.println("Error reading file " + rawDocPathname);
            return;
        }

        for (List<Annotation> annotations : disagreementProDoc) {
            // String docName = annotations.get(0).document;
            int begin = annotations.get(0).begin;
            int end = annotations.get(0).end;
            String differentWord = text.substring(begin, end);
            String sentence = getSentence(text, begin, end);
            // System.out.println(differentWord);

            Path path = Paths.get(outputFilePathName);
            try (BufferedWriter writer = Files.newBufferedWriter(path,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {

                // writer.append(rawDocPathName);
                writer.newLine();
                writer.append(differentWord + "\n");
                // writer.newLine();
                writer.append(sentence + "\n");
                // writer.newLine();
                for (Annotation annotation : annotations) {
                    writer.append("" + annotation);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Error writing file " + path);
                // System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void lookupEvaluationVerbs(List<List<Annotation>> annotationList, String rawDocPathname, String outputFilePathName, String outputDynamic) {
        String text;
        try {
            text = new String(Files.readAllBytes(Paths.get(rawDocPathname)),
                    "UTF-8");
        } catch (IOException e) {
            System.err.println("Error reading file " + rawDocPathname);
            return;
        }
        for (List<Annotation> annotations : annotationList) {
            // String docName = annotations.get(0).document;
            int begin = annotations.get(0).begin;
            int end = annotations.get(0).end;
            String verb = text.substring(begin, end);

            Path path = Paths.get(outputFilePathName);
            Path path_2 = Paths.get(outputDynamic);
            try (BufferedWriter bw = Files.newBufferedWriter(path,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                 BufferedWriter bw_2 = Files.newBufferedWriter(path_2,
                         StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                         StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                 CSVWriter writer = new CSVWriter(bw);
                 CSVWriter writer_2 = new CSVWriter(bw_2)) {


                List<String> aspectualClassLabels = new ArrayList<>();
                //aspectualClassLabels.add(verb);
                List<String> telicityLabels = new ArrayList<>();

                List<String> dynamicOnly = new ArrayList<>();

                for (Annotation annotation : annotations) {
                    if (annotation.aspClass == null) {
                        annotation.aspClass = "dummy_a";
                    }
                    aspectualClassLabels.add(annotation.aspClass);

                    if ("stative".equals(annotation.aspClass)) {
                        telicityLabels.add("X_atelic");
                    } else {
                        if (annotation.telicity == null) {
                            annotation.telicity = "dummy_t";
                        }
                        telicityLabels.add(annotation.telicity);
                    }
                }
                if (checkOnlyDynamic(aspectualClassLabels)) {
                    List<String> temporal = new ArrayList<>(Arrays.asList(verb));
                    temporal.addAll(telicityLabels);
                    String[] dynamic = temporal.stream().toArray(String[]::new);
                    writer_2.writeNext(dynamic);
                }
                List<String> temp = new ArrayList<>(Arrays.asList(verb));
                temp.addAll(aspectualClassLabels);
                List<String> newList = Stream.concat(temp.stream(), telicityLabels.stream()).collect(Collectors.toList());
                String[] verbdata = newList.stream().toArray(String[]::new);
                writer.writeNext(verbdata);

            } catch (IOException e) {
                System.err.println("Error writing file " + path);
                // System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //changed schecker.size ans checker.contains to accomodate "dummy_a";
    public boolean checkOnlyDynamic(List<String> aspectList) {
        HashSet<String> checker = new HashSet<>(aspectList);
        return (checker.size() >= 1 && !checker.contains("stative"));
    }


    public static boolean telicityDifference(List<Annotation> difference) {
        List<String> temp = difference.stream().map(annotation -> annotation == null ? null : annotation.telicity)
                .collect(Collectors.toList());
        return temp.contains("telic") && temp.contains("atelic");
    }

    //TODO: remove hard coding
    public String getRawTextname(String documentName) {

        //System.out.println(documentName + " " + xmlFolderName + " " + textFolderName);
        String rawName = documentName.replace(this.xmlFolderName + "\\"
                + this.projectName, "");
        int underscore = rawName.lastIndexOf("_");
        rawName = rawName.substring(0, underscore);

        String rawDocPathName = this.textDocPathName + "\\" + rawName + ".txt";
        // System.out.println("doc " + rawDocPathName);
        return rawDocPathName;
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

    /**
     * OUTPUT DIFFERENCES IN ANNOTATION
     *
     * @param agreement
     * @param outputDocPathName
     * @param outputDocPathName_telicity
     */
    public void writeDifferences(AgreementForAnnotations agreement, String outputDocPathName, String outputDocPathName_telicity) {
        //empty output file
        emptyFile(outputDocPathName);
        emptyFile(outputDocPathName_telicity);

        for (List<List<Annotation>> difference : agreement.getDifferences()) {
            if (difference.isEmpty())
                continue;
            String document = difference.get(0).get(0).document;
            String rawDocPathName = this.getRawTextname(document);
            this.lookupLabels(difference, rawDocPathName, outputDocPathName);

            List<List<Annotation>> differencesFiltered = difference.stream().filter(ParseAnnotation::telicityDifference)
                    .collect(Collectors.toList());

            this.lookupLabels(differencesFiltered, rawDocPathName, outputDocPathName_telicity);
        }
    }

    /**
     * Output same annotations for telicity
     *
     * @param agreement
     * @param outputDocPathName
     * @param outputDocPathName_telicity
     */
    public void writeSameAnnotations(AgreementForAnnotations agreement, String outputDocPathName, String outputDocPathName_telicity) {
        //empty output file
        emptyFile(outputDocPathName);
        emptyFile(outputDocPathName_telicity);

        for (List<List<Annotation>> gold : agreement.getSameAnnotations()) {
            if (gold.isEmpty())
                continue;
            String document = gold.get(0).get(0).document;
            String rawDocPathName = this.getRawTextname(document);
            this.lookupLabels(gold, rawDocPathName, outputDocPathName);

            List<List<Annotation>> differencesFiltered = gold.stream().filter(ParseAnnotation::telicityDifference)
                    .collect(Collectors.toList());

            this.lookupLabels(differencesFiltered, rawDocPathName, outputDocPathName_telicity);
        }
    }

    public void writeEvalVerbs(AgreementForAnnotations agreement, String outputDocPathName, String outputDynamic) {
        //empty output file
        emptyFile(outputDocPathName);
        emptyFile(outputDynamic);

        for (List<List<Annotation>> allLabels : agreement.getAllAnnotations()) {
            if (allLabels.isEmpty())
                continue;
            String document = allLabels.get(0).get(0).document;
            String rawDocPathName = this.getRawTextname(document);
            this.lookupEvaluationVerbs(allLabels, rawDocPathName, outputDocPathName, outputDynamic);
        }
    }


    public static void main(String[] args) throws DocumentException,
            IOException {
        //TODO: to run the code on the other dataset, switch the commented and the uncommented filenames
        //#1a
//        String projectName = "NewSEAspectTelicity_";
//        String textFolderNameRaw = "raw_textfiles";

        //#1b
        String projectName = "Evaluation_AspectTelicity_";
        String textFolderNameRaw = "Evaluation_raw_texts";

        //ENTER INPUT FILENAMES
        //#2a
        //String xmlAnnotFolder = "annotations_alltexts_Melissa_ich";

        //#2b
        //String xmlAnnotFolder = "Evaluation_AT_Melissa_ich";
        String xmlAnnotFolder = "Evaluation_AT_all";

        ParseAnnotation annotationParser = new ParseAnnotation(projectName, xmlAnnotFolder, textFolderNameRaw);


        //CONDUCT STUDY
        Map<String, List<String>> allDocs = annotationParser.groupDocs();
        Set<String> filenames = allDocs.keySet();

        if (filenames.size() == 0) {
            System.out.println("No documents found.");
            return;
        }
        // TODO: is there a better way?
        int numberOfAnnotators = allDocs.values().iterator().next().size();
        AgreementForAnnotations agreement = new AgreementForAnnotations(
                numberOfAnnotators);

        for (String doc : filenames) {
            ArrayList<HashMap<Integer, Annotation>> parsedAnnotations = getParsedAnnotations(
                    doc, allDocs.get(doc));
            agreement.addDocument(doc, parsedAnnotations);
        }
        System.out.println(agreement);

        agreement.printCoincidenceMatrix();


        // OUTPUT DIFFERENCES
        //String outputDocPathName = "outputDifferences_all\\all3_alltexts.txt";
        //String outputDocPathName_telicity = "outputDifferences_telicity\\all3_alltexts_telicity.txt";
        //String outputDocPathName = "evaluation_outputDifferences/test1.txt";
        //String outputDocPathName_telicity = "evaluation_outputDifferences_telicity/test2.txt";

        //annotationParser.writeDifferences(agreement, outputDocPathName, outputDocPathName_telicity);

        // OUTPUT GOLD
        //TODO: warum nur 1 document?
//        String outputDocPathName_G = "ComparisonEvaluationVerbs/test1.txt";
//        String outputDocPathName_telicity_G = "ComparisonEvaluationVerbs/test2.txt";
//        annotationParser.writeSameAnnotations(agreement, outputDocPathName_G, outputDocPathName_telicity_G);


        // EVALUATION COLLECTION VERBS
        String verbsPath = "ComparisonEvaluationVerbs/evaluationAnnotationVerbs_2.csv";
        String dynamicPath = "ComparisonEvaluationVerbs/evaluationAnnotationVerbs_Dynamic_2.csv";
        annotationParser.writeEvalVerbs(agreement, verbsPath, dynamicPath);


    }

}
