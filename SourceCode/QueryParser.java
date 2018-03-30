import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryParser {

    public static ArrayList<Query> getQueries(String fileName, List<String> stopWords) {

        try {
            ArrayList<Query> queries = new ArrayList<>();
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();

            while (line != null) {
                // determine if this line contains relevant information
                // this isn't the best way to do this, but I don't want to require external libraries

                if (line.contains("num")) {
                    String[] elements = line.split(" ");
                    String id = elements[2];

                    // next line has the query
                    line = bufferedReader.readLine();
                    String query = line.substring(8, line.length() - 9).trim();
                    // now preprocess the query
                    List<String> queryTerms = new ArrayList<String>();
                    query = query.replaceAll("http\\S+", "");
                    String[] tokens = query.split(" ");
                    Stemmer myStemm = new Stemmer();
                    for (String word : tokens) {
                        word = word.replaceAll("[^A-Za-z0-9]", "");
                        if(word == word.replaceAll("[^a-zA-Z]", "")) {
                            char[] toStem = word.toCharArray();
                            myStemm.add(toStem,toStem.length);
                            myStemm.stem();
                            String stemmed = myStemm.toString();
                            if(!stopWords.contains(stemmed) && stemmed!="") {
                                queryTerms.add(stemmed);
                            }
                        }
                    }

                    // queryTerms now contain all the relevant terms of the query

                    Query newQuery = new Query(id, queryTerms);
                    queries.add(newQuery);
                }
                line = bufferedReader.readLine();

            }
            bufferedReader.close();
            return queries;
        }catch (Exception e) {
            System.out.println("Error reading query file");
            e.printStackTrace();
        }
        return null;
    }




}
