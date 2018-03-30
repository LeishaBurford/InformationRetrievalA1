import java.util.*;

public class InvertedIndex {

    private LinkedHashMap<String, HashMap<String, Integer>> invertedIndex;
    private int documentCount;
    private ArrayList<String> docIDs;
    private LinkedHashMap<String, HashMap<String, Double>> normalizedWeightIndex;

    // constructor will add all the Strings in list to the invertedIndex
    public InvertedIndex(List<String> data) {
        invertedIndex = new LinkedHashMap<>();
        documentCount = 0;
        docIDs = new ArrayList<>();
        for (String s : data) {
            // set up the empty map of documents that have this word
            HashMap<String, Integer> temp = new HashMap<>();
            // add the word and its empty list of documents to the index
            invertedIndex.put(s, temp);
        }
        normalizedWeightIndex = new LinkedHashMap<>();
        fillNormalizedWeightIndex();
    }

    // use this method to add a document with the specified word
    // if this document is already in the list for this word
    // then the termFrequency is updated,
    // otherwise, a new entry is made with termFrequency = 1
    public void addDocument(String word, String documentID) {
        // confirm that word is in the invertedIndex
        if(!invertedIndex.containsKey(word)) return;

        HashMap<String, Integer> list = invertedIndex.get(word);
        // check if this document is already here
        if (list.containsKey(documentID)) {
            // this document is already here, so update the termFrequency
            list.put(documentID, list.get(documentID) + 1);
        } else {
            // this is a new document for this word, add it to the list
            list.put(documentID, 1);
            // also update the document count
            // check if this document is anywhere in the invertedIndex
            // if not, update documentCount
            if(!docIDs.contains(documentID)) {
                docIDs.add(documentID);
                documentCount++;
            }

        }
    }

    // this method returns the idf for the specified term
    // Note: this shouldn't be called until all documents have been entered, as it is
    // dependant upon the number of documents
    // specifically, calculates idf = log_2( # of docs / documentFreq )
    public double getTermIDF(String word) {
        double docFreq = (double) getDocumentFrequency(word);
        if(docFreq == 0) {
            return 0;
        }else {
            return Math.log((double) documentCount / docFreq) / Math.log(2);
        }
    }

    // calculates and returns the w_ij = tf_ij * idf_i of the given word and document
    public double getTermDocumentWeight(String word, String documentID) {
        double idf = getTermIDF(word);
        double tf = invertedIndex.get(word).get(documentID);
        return idf * tf;
    }
    // returns the document frequency for a given word
    public int getDocumentFrequency(String word) {
        if (invertedIndex.get(word) == null) {
            return 0;
        }
        return invertedIndex.get(word).size();
    }

    // returns the term frequency for a given word and document
    public int getTermFrequency(String word, String documentID) {
        return invertedIndex.get(word).get(documentID);
    }


    // add a word to the inverted index
    public void addWord(String word) {
        // set up the empty map of documents that have this word
        HashMap<String, Integer> temp = new HashMap<>();

        // add the word and its empty list of documents to the index
        invertedIndex.put(word, temp);
    }

    public void fillNormalizedWeightIndex() {
        HashMap<String, Double> factorsPerDoc = new HashMap<>();
        //Scanning the inverted index and computing the normalizing factors for each doc
        //For each word, scanning its posting list
        for (String word : invertedIndex.keySet()){
            for (String docID : invertedIndex.get(word).keySet()){
                double weight = getTermDocumentWeight(word, docID);
                if (factorsPerDoc.containsKey(docID)) {
                    // this document is already here, so update the factor: + weight^2
                    factorsPerDoc.put(docID, factorsPerDoc.get(docID) + Math.pow(weight, (double) 2));
                } else {
                    // this is a new document for this word, create its normalizing factor
                    factorsPerDoc.put(docID, Math.pow(weight, (double) 2));
                }
            }
            // normalizedWeightIndex.put(word, factorsPerDoc);
        }
        //filling normalizedWeightIndex: copying the inverted index and
        // dividing each weight by its corresponding factor
        for (String word : invertedIndex.keySet()){
            HashMap<String, Double> temp = new HashMap<>();
            // add the word and its empty list of documents to the index
            normalizedWeightIndex.put(word, temp);

            for (String docID : getPostingsList(word).keySet()){
                double factor = Math.sqrt(factorsPerDoc.get(docID));
                normalizedWeightIndex.get(word).put(docID, getTermDocumentWeight(word, docID) / factor);

            }
        }
    }

    //get the normalized weights for a given word by going to the NormalizedWeightIndex
    public HashMap<String, Double> getNormalizedWeight(String word) {
        return normalizedWeightIndex.get(word);
    }


    public HashMap<String, Integer> getPostingsList(String word) {
        return invertedIndex.get(word);
    }

    @Override
    public String toString() {
        String result = "Word\tdocFreq --> [ DocId, termFreq ]\n";
        result += "-----------------------------------------------\n";
        for(String word : invertedIndex.keySet()) {
            HashMap<String, Integer> list = invertedIndex.get(word);
            result += word + " " + list.size() + " --> ";
            for(String s : list.keySet()) {
                result += " [" + s + ", " + list.get(s) + "] ";
            }
            result += "\n";
        }
        return result;
    }
}
