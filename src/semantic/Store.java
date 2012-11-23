/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.tdb.TDBFactory;
import java.util.HashMap;

/**
 * Bir TDB Store'a baglanarak Sparql sorgulari calistirmayi saglayan siniftir.
 * author Bahadir
 */
public class Store {
    
    public Dataset dataset;
    private HashMap<String, String> prefixMap;
    private int flags;
    private String prefixString = ""; // bu hic kullanilmamali, cunku dirty olabilir.. bunun yerine getPrefixString kullanilmali
    private boolean dirtyPrefixString;
    private String storePath;

    
    public static final class FLAGS {

        /**
         * Store ekrana log basar
         */
        public static final int DEBUG = 1;
        /**
         * Ekrana daha fazla detaylı bilgi bas. DEBUG set edilmemisse bir fonksyionu olmaz.
         */
        public static final int VERBOSE = 2;
        /**
         * Baslangicta standart prefixler alinsin
         */
        public static final int SET_DEFAULT_PREFIXES = 4;
        /**
         * Obje olusur olusmaz baglanti kurulur
         */
        public static final int AUTO_CONNECT = 8;
    }
    
    /**
     * Bir store olusturur ancak baslangicta hic bir ayari set etmez.
     * @param storePath TDB dosyalarinin bulundugu dizin
     */
    public Store(String storePath) {
        this(storePath, 0);
    }
    
    /**
     * 
     * @param storePath TDB dosyalarinin bulundugu dizin
     * @param flags Bayraklar
     */
    public Store(String storePath, int flags) {
        this.flags |= flags;
        this.storePath = storePath;
        prefixMap = new HashMap<>();

        if (isFlagSet(FLAGS.SET_DEFAULT_PREFIXES)) {
            addDefaultPrefixes();
        }
        if (isFlagSet(FLAGS.AUTO_CONNECT)) {
            connect();
        }

    }

    public final void connect() {
        dataset = TDBFactory.createDataset(storePath);
        console("Connected to store at: " + storePath);
    }

    public void beginRead() {
        end();
        dataset.begin(ReadWrite.READ);
    }

    public void end() {
        dataset.end();
    }

    /**
     * Sadece SELECT queryleri için çalışır. 
     *
     * @param sparql
     * @return Sorguda bir hata olusursa null döner.
     */
    public ResultSet query(String sparql) {
        ResultSet rs;
        try {
            console("Executing query: \r\n" + sparql, "Executing query: \r\n" + getPrefixString() + sparql);
            long startTime = System.currentTimeMillis();
            boolean wasInTrans = dataset.isInTransaction();
            if (!wasInTrans) {
                dataset.begin(ReadWrite.READ);
            }
            QueryExecution query = QueryExecutionFactory.create(getPrefixString() + sparql, dataset);
            rs = query.execSelect();
            if (!wasInTrans) {
                dataset.end();
            }
            long time = System.currentTimeMillis() - startTime;

            console("Query executed in : " + time + " miliseconds.\r\n");
            return rs;

        } catch (QueryParseException e) {
            console("Sorgu hatası oluştu: \r\n" + e.getLocalizedMessage());
            return null;
        }


    }
    /**
     * Bir select sorgusu calistirir ve sonucu ResultSet olarak dondururken ekranada basar.
     * @param sparql
     * @return 
     */
    public ResultSet queryAndPrint(String sparql) {
        ResultSet rs = query(sparql);
        if (rs != null) {
            ResultSetFormatter.out(System.out, rs);
        }
        return rs;
    }

    private boolean isFlagSet(int flags) {
        return (this.flags & flags) == flags;
    }

    public final void addDefaultPrefixes() {

        addPrefix("dbp", "http://dbpedia.org/ontology/");
        addPrefix("owl", "http://www.w3.org/2002/07/owl#");
        addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        addPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        addPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

        console("Default prefixes added: \r\n" + getPrefixString());

    }

    /**
     * Her sorguda kullanilacak olan prefixleri ekler. base icin prefix null
     * secilmelidir.
     *
     * @param prefix
     * @param URI
     * @return
     */
    public boolean addPrefix(String prefix, String URI) {
        dirtyPrefixString = true;
        return prefixMap.put(prefix == null ? "@base" : prefix, URI) != null;

    }
    /**
     * Verilen bir prefix'i kaldirir.
     * @param prefix
     * @return 
     */
    public boolean removePrefix(String prefix) {
        return dirtyPrefixString = (prefixMap.remove(prefix) != null);
    }

    public void clearPrefixes() {
        prefixMap.clear();
        dirtyPrefixString = true;
    }

    private void constructPrefixString() {
        prefixString = "";
        for (String key : prefixMap.keySet()) {
            if (key.equals("@base")) {
                prefixString = "base <" + prefixMap.get(key) + ">\r\n" + prefixString;
            } else {
                prefixString += "prefix " + key + ": <" + prefixMap.get(key) + "> \r\n";
            }

        }
        prefixString += "\r\n";

        dirtyPrefixString = false;
        console("Prefix strings constructed");

    }

    public String getPrefixString() {
        if (dirtyPrefixString) {
            constructPrefixString();
        }
        return prefixString;
    }

    /**
     * Verilen string'in icinden onceden eklenmis olan prefix urilerini temizler.
     * Boylece daha temiz bir goruntu elde edilir. 
     * Orn: http://yago-knowledge.org/resource/Albert_Einstein yerine Albert_Einstein
     * @param string
     * @return 
     */
    public String removePrefixURIFrom(String string) {
  
        for (String uri : prefixMap.values()) {
            string = string.replace(uri, ""); 
        }
        
        return string;
 
    }
    private void console(String comment) {
        console(comment, "");
    }

    private void console(String comment, String verbose) {
        if (isFlagSet(FLAGS.DEBUG)) {
            System.out.println("STORE> " + (isFlagSet(FLAGS.VERBOSE) && !verbose.isEmpty() ? verbose : comment));
        }
    }
}
