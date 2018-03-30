import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Query {

    // a query is an object with an id and a list of terms.

    private String id;
    private List<String> queryTerms;

    public Query(String id, List<String> queryTerms) {
        this.id = id;
        this.queryTerms = queryTerms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public List<String> getQueryTerms() {
        return queryTerms;
    }

    public void setQueryTerms(List<String> queryTerms) {
        this.queryTerms = queryTerms;
    }

    @Override
    public String toString() {
        String result = id + " [ ";
        for (String term : queryTerms) {
            result += term + " ";
        }
        return result + " ]";
    }


}
