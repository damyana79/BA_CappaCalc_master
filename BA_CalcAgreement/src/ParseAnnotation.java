import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class ParseAnnotation {
    /**
     * @param documentName
     * @param annotationFiles
     * @return output: AL[HM <spanId:AnnotationObject>] per document; input
     * provided durch iterieren über groupdocs(String folderpath)
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
     * @param folderpath
     * @return output: all files in HashMap:(full)filename:[corresponding
     * annotated files]
     */
    //TODO: ?input: nicht folderpath, sonsern liste aus files, die schon vorkombiniert wurden in einer anderen methode
    public static Map<String, List<String>> groupDocs(String folderpath) {
        Map<String, List<String>> groupDoc = new HashMap<String, List<String>>();
        //TODO: den teil des codes, das mir alle files gibt in einer anderen methode auslagern;
        // die kombiantionen/diese methode hier so lange aufrufen, bis die kombinationsliste leer ist
        File folder = new File(folderpath);
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
    public static void lookupLabels(List<List<Annotation>> disagreementProDoc,
                                    String rawDocPathName, String outputFilePathName) {
        // Set<String> docName = disagreementList.get(0).keySet();

        String text;
        try {
            text = new String(Files.readAllBytes(Paths.get(rawDocPathName)),
                    "UTF-8");
        } catch (IOException e) {
            System.err.println("Error reading file " + rawDocPathName);
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

    public static boolean telicityDifference(List<Annotation> difference) {
        List<String> temp = difference.stream().map(annotation -> annotation == null ? null : annotation.telicity)
                .collect(Collectors.toList());
        return temp.contains("telic") && temp.contains("atelic");
    }

    //TODO: remove hard coding
    public static String getRawTextname(String documentName,
                                        String xmlFolderName, String projectName, String textFolderName) {

        //System.out.println(documentName + " " + xmlFolderName + " " + textFolderName);
        String rawName = documentName.replace(xmlFolderName + "\\"
                + projectName, "");
        int underscore = rawName.lastIndexOf("_");
        rawName = rawName.substring(0, underscore);

        String rawDocPathName = textFolderName + "\\" + rawName + ".txt";
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

    public static void main(String[] args) throws DocumentException,
            IOException {
        //String projectName = "NewSEAspectTelicity_";
        //String textFolderName ="raw_textfiles";
        String projectName = "Evaluation_AspectTelicity_";
        String textFolderName = "Evaluation_raw_texts";

        //ENTER INPUT AND OUTPUT FILENAMES
        //TODO: hardcoded is ugly
//        String outputDocPathName = "outputDifferences_all\\all3_alltexts.txt";
//        String outputDocPathName_telicity = "outputDifferences_telicity\\all3_alltexts_telicity.txt";
//        String xmlAnnotFolder = "all_texts_all_annotators";

        String outputDocPathName = "evaluation_outputDifferences/test1.txt";
        String outputDocPathName_telicity = "evaluation_outputDifferences_telicity/test2.txt";
        String xmlAnnotFolder = "Evaluation_AT_all";

        //CONDUCT STUDY
        Map<String, List<String>> allDocs = groupDocs(xmlAnnotFolder);
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


        // OUTPUT DIFFERENCES IN ANNOTATION
        // empty output file
        emptyFile(outputDocPathName);
        emptyFile(outputDocPathName_telicity);

        for (List<List<Annotation>> difference : agreement.getDifferences()) {
            if (difference.isEmpty())
                continue;
            String document = difference.get(0).get(0).document;
            String rawDocPathName = getRawTextname(document, xmlAnnotFolder, projectName,
                    textFolderName);
            lookupLabels(difference, rawDocPathName, outputDocPathName);

            List<List<Annotation>> differencesFiltered = difference.stream().filter(ParseAnnotation::telicityDifference)
                    .collect(Collectors.toList());

            lookupLabels(differencesFiltered, rawDocPathName, outputDocPathName_telicity);
        }

    }
}
