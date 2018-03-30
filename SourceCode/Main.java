import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Main {

    public static void main(String[] args) {

        // preprocess the files
        Prepro2 preprocessor = new Prepro2();
        System.out.println("Preprocessing the data...");
        List<String> vocabulary = preprocessor.getVocabulary("../StopWords.txt", "../Trec_microblog11.txt");
        System.out.println("Vocabulary size: " + vocabulary.size());

        // output the vocabulary to a file for reference
        Path p = Paths.get("../Results/step1_vocabulary.txt");
        try {
            OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING));
            for(String word : vocabulary) {
                word += "\n";
                byte data[] = word.getBytes();
                out.write(data, 0, data.length);
            }
        } catch (IOException x) {
            System.err.println(x);
        }

        // corpus is list of [documentID, [array of document tokens]]
        List<List<String>> corpus = preprocessor.getCorpus();

        List<String> stopWords = preprocessor.getStopWordList();
        // get the size of each of the documents
        HashMap<String, Integer> documentLengths = preprocessor.getDocLength();

        // build the inverted index
        //create the data structure for inverted index (filling words and empty arrays)
        System.out.println("Creating the inverted index...");
        InvertedIndex invertedIndex = new InvertedIndex(vocabulary);

        System.out.println("Filling the inverted index...");
        // fill the postings lists of the inverted index
        for (int d = 0; d < corpus.size(); d++) {
            //separating tokens
            List<String> doc = corpus.get(d);

            //getting the doc ID
            String docId = doc.get(0); //to be stored now (deleted right after)
            docId = docId.replaceAll("[^0-9]",""); //removing noisy characters in the docID

            for (int i = 1; i < doc.size();i++) {
                String token = doc.get(i);
                // add the document containing this token to the inverted index
                invertedIndex.addDocument(token,docId);
            }
        }

        // take this line out if testing on full data set
        // System.out.println(invertedIndex);

        // output the index to a file for reference
        p = Paths.get("../Results/step2_inverted_index.txt");
        try {
            OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING));
            String result = "Word\tdocFreq --> [ DocId, termFreq ]\n";
            result += "-----------------------------------------------\n";
            byte data[] = result.getBytes();
            out.write(data, 0, data.length);

            for(String word : vocabulary) {
                HashMap<String, Integer> list = invertedIndex.getPostingsList(word);
                result = word + " " + list.size() + " --> ";
                for(String s : list.keySet()) {
                    result += " [" + s + ", " + list.get(s) + "] ";
                }
                result += "\n";
                data= result.getBytes();
                out.write(data, 0, data.length);
            }
        } catch (IOException x) {
            System.err.println(x);
        }

        System.out.println("Normalizing the invertedIndex");
        // creating index with normalized weights
        invertedIndex.fillNormalizedWeightIndex();


        System.out.println("Parsing the queries...");
        // parse queries, queries is list of all queries, preprocessed
        ArrayList<Query> queries = QueryParser.getQueries("../topics_MB1-49.txt", stopWords);

        System.out.println("Creating rank object...");
        // determine ranking of the queries
        RetrieveRank rank = new RetrieveRank(invertedIndex, queries, documentLengths);

        System.out.println("Computing scores");
        // this is where most of the work happens
        // weights of inverted index are normalized
        // query weights are normalized
        // cosine score is computed
        // results contains query -> [docID, score]
        Map<Query, Map<String, Double>> results = rank.getRankedResults();

        // output the query and doc scores to a file for reference
        p = Paths.get("../Results/step3_retrieval_and_ranking.txt");
        try {
            OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(p, CREATE, TRUNCATE_EXISTING));
            
            String result = "";
            for(Query query : results.keySet()) {
                result = query.getId() + " --> ";
                Map<String, Double> matchedDocs = results.get(query);
                for(String docID : matchedDocs.keySet()) {
                    result += " [" + docID + ", " + matchedDocs.get(docID) + "] ";
                }
                result += "\n";
                byte data[] = result.getBytes();
                out.write(data, 0, data.length);
            }
        } catch (IOException x) {
            System.err.println(x);
        }


        // display the results
        DecimalFormat df3 = new DecimalFormat("#.###");
        String lineToPrint = String.format("%-8s %-2s %-17s %-4s %-5s %-5s", "Topic_ID", "Q0", "DocID", "Rank", "Score", "Tag" );
        // System.out.println(lineToPrint);

        int tagValue = 0;
        for (Query query : results.keySet()) {
            tagValue++;
            // for each document containing words from this query
            // we only want the top 1000 documents
            int count = 1;
            for(String docID: results.get(query).keySet()) {

                if (count >= 1000) {
                    break;
                }
                String topic_id = query.getId();
                String score = df3.format(results.get(query).get(docID));
                String tag = "myRun" + tagValue;
                lineToPrint = topic_id + " Q0 " + docID + " " + count + " " + score + " " + tag;
                // lineToPrint = String.format("%-5s %-2s %-17s %-3d %-4s %-5s",
                //         topic_id , "Q0", docID, count, score, tag );
                // System.out.println(lineToPrint);
                count++;


                //preparing to store the line in a file
                lineToPrint += "\n";
                byte data[] = lineToPrint.getBytes();
                p = Paths.get("../Results/Trec_microblog11-results.txt");

                try (OutputStream out = new BufferedOutputStream(
                        Files.newOutputStream(p, CREATE, APPEND))) {
                    out.write(data,0, data.length);
                } catch (IOException x) {
                    System.err.println(x);
                }
            }
        }
    }
}
