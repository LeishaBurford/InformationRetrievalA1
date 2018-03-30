import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.*;

public class RetrieveRank {

    private InvertedIndex invertedIndex;
    private List<Query> queries;
    private HashMap<String, Integer> documentLengths;

    public RetrieveRank(InvertedIndex invertedIndex, List<Query> queries, HashMap<String, Integer> documentLengths) {
        this.invertedIndex = invertedIndex;
        this.queries = queries;
        this.documentLengths = documentLengths;
    }

    // returns the queries with their scores
    // query -> [docID, score]
    public Map<Query, Map<String, Double>> getRankedResults() {
        // for each query, compute its relevant documents and cosine scores
        LinkedHashMap<Query, Map<String, Double>> results = new LinkedHashMap<>();
        for (Query query : queries) {
            results.put(query, getScore(query));
        }
        return results;
    }

    // look at each term in query, calculate its frequency with respect to the entire query
    // returns a hashmap of term -> term frequency, for given query
    public HashMap<String,Double> getQueryTermFrequencies(Query query) {
        //get the frequency of each word in the query
        HashMap<String,Double> queryTermFrequencies = new HashMap<>();
        for(String term : query.getQueryTerms()) {
            // check if this term is new
            if(queryTermFrequencies.containsKey(term)) {
                // term has already been added, update term frequency
                queryTermFrequencies.put(term,queryTermFrequencies.get(term) + 1);
            }else {
                // new term, add to hashmap
                queryTermFrequencies.put(term, 1d);
            }
        }
        return queryTermFrequencies;
    }

    // method calculates the tf-idf of each term in a given query
    // the weights are then normalized
    // return hashmap of term -> tf-idf for each term in query
    public HashMap<String,Double> getNormalizedQueryWeights(Query query) {
        // get the tf of each word in the query
        HashMap<String, Double> queryWeights = getQueryTermFrequencies(query);
        // for each term in the query, compute the tf-idf
        for(String term : query.getQueryTerms()){
            // get the idf of the term from the inverted index
            double idf = invertedIndex.getTermIDF(term);
            // get the tf of this term
            double tf = queryWeights.get(term);
            // for each word compute W_iq=(0.5+0.5*tf_word_in_query)*idf_word_in_corpus
            double weight = ( 0.5 + 0.5 * tf) * idf;
            // add (word,w_iq) in the queryWeights
            queryWeights.put(term, weight);
        }

        // now normalizing the weights
        double sum = (double) 0;

        // sum the squares of each of the terms
        for(String term : queryWeights.keySet()){
            sum += Math.pow(queryWeights.get(term), 2);
        }

        // take the square root of the above sum
        double normalizingFactor = Math.sqrt(sum);

        // divide each term by the normalizing factor
        for(String term :queryWeights.keySet()) {
            queryWeights.put(term, queryWeights.get(term) / normalizingFactor);
        }
        return queryWeights;
    }

    // this is the score computing function function
    // computes the cosine score of each document for the specific query
    // Returns hashmap of docID -> cosineScore for the query
    private Map<String,Double> getScore(Query query) {
        //score(query,doc)=sum_(i=words) weight(i,query)*weight(i,doc)
        HashMap<String,Double> scores = new HashMap<>();
        HashMap<String,Double> queryWeights = getNormalizedQueryWeights(query);

        //for each term of the query
        for(String term : query.getQueryTerms()) {
            //get weight (term, query)
            double w_q = queryWeights.get(term);
            // get the list of documents that contain this term with their normalized weights
            HashMap<String, Double> termWeights = invertedIndex.getNormalizedWeight(term);
            // check if this term was in our vocabulary
            if(termWeights == null)
                continue;
            //for each doc containing this term
            for(String docID : termWeights.keySet()) {
                // get weight(term,doc)
                // we need the normalized tf-idf for this term, docID
                double w_d = termWeights.get(docID);

                // Create or update the score of the doc weightq*weightdoc
                if(scores.containsKey(docID)) {
                    scores.put(docID, scores.get(docID) + (w_q * w_d));
                } else {
                    scores.put(docID, w_q * w_d);
                }
            }
        }

        // now we sort the documents by score
        // put in list using entry, so we can access it in compare()
        LinkedList<Map.Entry<String, Double>> docsAndScores = new LinkedList(scores.entrySet());

        // sorting the list by term frequency
        Collections.sort(docsAndScores, new Comparator<Map.Entry<String, Double>>() {

            @Override
            public int compare(Map.Entry<String, Double> t1, Map.Entry<String, Double> t2) {
                return t2.getValue().compareTo(t1.getValue());
            }
        });

        // Storing the list into LinkedHashMap to preserve the order of insertion.
        Map<String, Double> sortedScores = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : docsAndScores) {
            sortedScores.put(entry.getKey(), entry.getValue());
        }

        return sortedScores;
    }
}
