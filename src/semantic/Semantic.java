/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.io.*;
import opennlp.tools.parser.Parse;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 *
 * @author vaıo
 */
public class Semantic {

    /**
     * @param args the command line arguments
     */
    
    public static InputStream modelParser;
    public static Store store;

    public static void main(String[] args) throws IOException {
        
        // Parser icin model dosyasi. Su an ana rutinde kullanilmiyor.
        

        // Yagor Core buradan indirilebilir: http://www.mpi-inf.mpg.de/yago-naga/yago/downloads_old.html
        store = new Store("C:/HOME/yago2core_jena",
                Store.FLAGS.AUTO_CONNECT
                // | Store.FLAGS.DEBUG // Sorgulari gormek icin bunu uncomment etmeli
                | Store.FLAGS.VERBOSE
                | Store.FLAGS.SET_DEFAULT_PREFIXES);
        store.addPrefix(null, "http://yago-knowledge.org/resource/");

        ngramYago(
                "C++, high-level computer programming language. Developed by Bjarne Stroustrup of Bell Laboratories in the early 1980s, it is based on the traditional C language but with added object-oriented programming and other capabilities. C++, along with Java, has become popular for developing commercial software packages that incorporate multiple interrelated applications. C++ is considered one of the fastest languages and is very close to low-level languages, thus allowing complete control over memory allocation and management. This very feature and its many other capabilities also make it one of the most difficult languages to learn and handle on a large scale."
                ,2);
        

        //store.queryAndPrint("SELECT * WHERE {?a <hasPreferredMeaning> \"Albert Einstein\". ?a a ?b}");

    }

    /**
     * Verilen metnin ilk cumlesini ngramlara ayirarak yago'da arar.
     * @param metin
     * @param ngramSize
     * @throws FileNotFoundException 
     */
    public static void ngramYago(String metin, int ngramSize) throws FileNotFoundException {
        /**
         * Model dosyalari
         */
        InputStream modelSent = new FileInputStream("models/en-sent.bin");
        InputStream modelToken = new FileInputStream("models/en-token.bin");
 

        try {
            // Cumle modeli
            final SentenceModel sModel = new SentenceModel(modelSent);


            SentenceDetectorME sDetector = new SentenceDetectorME(sModel);
            String sentences[] = sDetector.sentDetect(metin);
            
            // Tokenlara bolucu
            TokenizerME tokenizer = new TokenizerME(new TokenizerModel(modelToken));

            String tokens[] = tokenizer.tokenize(sentences[1]);
            
            // Bu tokenlardan ngramlar olustur
            NGramSet ngSET = NGramSet.createNGramSet(tokens, ngramSize);

            System.out.println("Bakiom..");
            store.beginRead(); // Store'u transaction'a hazirla.
            
            /**
             * Her ngram icin yago aramasini yapan dongu.
             * ngSET.ngrams 'da kullanılabilir.
             */
            for (NGramSet.NGram ngram : ngSET.getWithSize(ngramSize)) {
                System.out.print(ngram.toString() + ": ");
                
                /**
                 * Bu sorguda elemanlarin yago uzerindeki attribute'larina bakiyoruz.
                 */
                ResultSet rs = store.query(
                        "SELECT distinct ?p WHERE {?a <hasPreferredMeaning> \"" + ngram.toString() + "\". ?a ?p ?la} LIMIT 200");
                while (rs.hasNext()) {
                    QuerySolution row = rs.nextSolution();
                    System.out.print(" " + store.removePrefixURIFrom(row.get("p").toString()) + ", ");
                }
                
                System.out.println();

            }
            store.end();


        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    /**
     * Parse'i preorder dolas, 2 kelimeden olusan noun phraseleri goster.
     * Su an aktif olarak kullanilmiyor, sadece OpenNLP'nin parseri ile 
     * denenen islemleri gostermek icin birakilmistir.
     * @param p
     */
    public static void traverseAndGet(Parse p) throws FileNotFoundException {
        modelParser = new FileInputStream("models/en-parser-chunking.bin");
        
        preOrder(p, new Visit<Parse>() {
            /**
             * Her ziyarette yapilacak isler
             */
            @Override
            public void doVisit(Parse item) {
                if (item.getChildCount() == 0) {
                    Parse ata = item.getParent().getParent();
                    if (ata.getChildCount() == 2 && ata.getType().equals("NP")) {
                        System.out.println("\r\n (" + ata.getType() + ")" + ata.toString());
                    }
                }
            }
        });
    }

    /**
     * Parser'da preorder gezer.
     * @param parse
     * @param visit 
     */
    public static void preOrder(Parse parse, Visit<Parse> visit) {

        visit.doVisit(parse);

        for (Parse p : parse.getChildren()) {
            preOrder(p, visit);
        }

    }
}
