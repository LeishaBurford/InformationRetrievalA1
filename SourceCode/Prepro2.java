import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class Prepro2 {

	private List<String> stopwordsList;
	private List<List<String>> corpus;
	private List<String> vocabulary;

    public Prepro2() {
        stopwordsList = new ArrayList<>();
        corpus = new ArrayList<List<String>>();
        vocabulary = new ArrayList<>();
    }

    public List<String> getVocabulary(String stopWordsFile, String dataFile)
	{
		//Create stopwordsList
        try {
            Stemmer myStemmer = new Stemmer();
            FileReader file2 = new FileReader(stopWordsFile);
            BufferedReader reader2 = new BufferedReader(file2);
            String stopword = reader2.readLine();
            while (stopword != null) {
                char[] toStem = stopword.toCharArray();
                myStemmer.add(toStem, toStem.length);
                myStemmer.stem();
                String stemmed = myStemmer.toString();
                stopwordsList.add(stemmed);
                stopword = reader2.readLine();
            }
            reader2.close();
            //Create vocabulary from the documents words
            FileReader file = new FileReader(dataFile);
            BufferedReader reader = new BufferedReader(file);
            String line = reader.readLine();
            while (line != null) {
                line = line.replaceAll("http\\S+", "");
                // replace the tabs with spaces
                line = line.replaceAll("\t", " ");
                String[] tempTokens = line.split(" ");
                List<String> tempDoc = new ArrayList<String>();
                tempDoc.add(tempTokens[0].replaceAll("[^0-9]","")); //adding the docID


                //now filtering and stemming the words:
                for (int x = 1; x < tempTokens.length; x++) {
                    String tempValue = tempTokens[x].toLowerCase();
                    //removing non alphanumeric char
                    tempValue = tempValue.replaceAll("[^A-Za-z0-9]", "");
                    //checking if numeric char in the string: if yes, discarded
                    if (tempValue == tempValue.replaceAll("[^a-zA-Z]", "")) {
                        char[] toStem = tempValue.toCharArray();
                        myStemmer.add(toStem, toStem.length);
                        myStemmer.stem();
                        String stemmed = myStemmer.toString();

                        if (!stopwordsList.contains(stemmed) && stemmed != "") {
                            tempDoc.add(stemmed);//we store all the tokens contained in a doc (with duplicates, to get the freq afer)
                            if (!vocabulary.contains(stemmed)) {
                                vocabulary.add(stemmed);//no duplicate in vocab
                            }
                        }
                    }
                }
                corpus.add(tempDoc);
                line = reader.readLine();
            }
            vocabulary.remove("");
            Collections.sort(vocabulary, String.CASE_INSENSITIVE_ORDER);
            reader.close();
            return (vocabulary);
        }catch (Exception e) {
            System.out.println("Error reading in Prepro2.java");
            e.printStackTrace();
        }
        return  null;
	}
	public List<String> getStopWordList()
	{return(stopwordsList);
	}
	public List<List<String>> getCorpus()  {
		return(corpus);
	}

    public HashMap<String,Integer> getDocLength(){
        HashMap<String,Integer> docLength = new HashMap<>();
        // for each doc in the corpus, we add its ID mapping to its length in the docLength structure
        for(List<String> doc : corpus) {
            docLength.put(doc.get(0), doc.size());
        }
        return docLength;
    }
}

