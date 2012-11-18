/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.*;
import java.util.Arrays;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import semantic.yago.Store;

/**
 *
 * @author vaıo
 */
public class Semantic {

    /**
     * @param args the command line arguments
     */
    public final static String metin = "C++, high-level computer programming language. Developed by Bjarne Stroustrup of Bell Laboratories in the early 1980s, it is based on the traditional C language but with added object-oriented programming and other capabilities. C++, along with Java, has become popular for developing commercial software packages that incorporate multiple interrelated applications. C++ is considered one of the fastest languages and is very close to low-level languages, thus allowing complete control over memory allocation and management. This very feature and its many other capabilities also make it one of the most difficult languages to learn and handle on a large scale.";

    public static void main(String[] args) {
        try {
            Store store = new Store("http://88.249.20.70:8890/sparql");
            String result = store.execQuery("SELECT * WHERE {?x ?p ?y} LIMIT 10", "http://yago.org", Store.RESULT_FORMAT.PLAIN);
            
            StringReader reader = new StringReader(result);
            
            System.out.println(result);
            
            Model model = ModelFactory.createDefaultModel();
            model.read(reader, "http://yago.org", "N3");
            model.write(System.out);
                    
            
        } catch (Exception e) {
            System.out.println("Hata oluştu: \r\n" );
            e.printStackTrace();
        }

    }

    public static void parseMarse() throws FileNotFoundException {
        // TODO code application logic here
        //System.out.println(System.getProperty("user.dir"));
        InputStream modelParser = new FileInputStream("models/en-parser-chunking.bin");


        try {

            ParserModel model = new ParserModel(modelParser);
            Parser parser = ParserFactory.create(model);
            // 
            //Parse topParses[] = ParserTool.parseLine("Edmund Sharpe (1809–1877) was an English architect, architectural historian, railway engineer, and sanitary reformer. Sharpe's main focus was on churches, and he was a pioneer in the use of terracotta as a structural material in church building, designing what were known as \"pot\" churches. He also designed secular buildings, including domestic properties and schools, and worked on the development of railways in Northwest England, designing bridges and planning new lines. ", parser, 1);
            Parse topParses[] = ParserTool.parseLine(metin, parser, 1);


            Parse p = topParses[0];
            System.out.println(p.getHead().getChildren());



            topParses[0].show();
            Parse sentence = topParses[0].getChildren()[0];

            System.out.println(sentence.getChildCount());



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void tokenMoken() throws FileNotFoundException {
        // TODO code application logic here
        //System.out.println(System.getProperty("user.dir"));
        InputStream modelSent = new FileInputStream("models/en-sent.bin");
        InputStream modelToken = new FileInputStream("models/en-token.bin");
        InputStream modelPerson = new FileInputStream("models/en-ner-organization.bin");

        try {

            final SentenceModel sModel = new SentenceModel(modelSent);


            SentenceDetectorME sDetector = new SentenceDetectorME(sModel);
            String sentences[] = sDetector.sentDetect(metin);

            TokenizerModel tModel = new TokenizerModel(modelToken);
            Tokenizer tokenizer = new TokenizerME(tModel);



            TokenNameFinderModel tnModel = new TokenNameFinderModel(modelPerson);
            NameFinderME nameFinder = new NameFinderME(tnModel);

            for (String sentence : sentences) {
                String tokens[] = tokenizer.tokenize(sentence);
                Span nameSpans[] = nameFinder.find(tokens);
                System.out.println("Found entity: " + Arrays.toString(Span.spansToStrings(nameSpans, tokens)));
            }



        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
