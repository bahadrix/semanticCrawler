/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic.yago;

import java.util.LinkedList;
import java.util.List;
import me.bahadir.http.Client;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * author vaıo
 */
public class Store {
    public static final class RESULT_FORMAT {
        public static final String AUTO = "auto";
        public static final String HTML = "text/html";
        public static final String SPREADSHEET = "application/vnd.ms-excel";
        public static final String XML = "application/sparql-results+xml";
        public static final String JSON = "application/sparql-results+json";
        public static final String JAVASCRIPT = "application/javascript";
        public static final String PLAIN = "text/plain";
        public static final String RDF_XML ="application/rdf+xml";
        public static final String CSV = "text/csv";
        public static final String TSV = "Text/tab-separated-values";
    }        
    
    private Client client;
    private String sparqlURL;
    
    public Store(String sparqlURL) {
// baha ev:http://88.249.20.70:8890
        //"http://88.249.20.70:8890/sparql"
        this.client = new Client();
        this.sparqlURL = sparqlURL;
        
        
    
    }
    
    public String execQuery(String queryString) throws Exception {
        return execQuery(queryString, "");
    }
    
    public String execQuery(String queryString, String defaultGraphURI) throws Exception {
        return execQuery(queryString, defaultGraphURI, RESULT_FORMAT.RDF_XML);
    }
    
    public String execQuery(String queryString, String defaultGraphURI, String resultFormat) throws Exception{
        
        List<NameValuePair> params = new LinkedList<>();
        
        params.add(new BasicNameValuePair("default-graph-uri", defaultGraphURI));
        params.add(new BasicNameValuePair("query", queryString));
        params.add(new BasicNameValuePair("format", resultFormat));
        params.add(new BasicNameValuePair("timeout", "0"));
        params.add(new BasicNameValuePair("debug", "on"));
        
        String res = client.request(sparqlURL, params);
        
        if (res.startsWith("Virtuoso 37000 Error")) {
            throw new Exception(res);
        }
        
        return res;
        
    }
    
    
}
