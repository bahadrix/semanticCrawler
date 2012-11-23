/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package semantic;

import java.util.LinkedList;
import java.util.List;

/**
 * NGramlardan olusan genel sinif.
 * @author Bahadir
 */
public class NGramSet {
    /**
     * NGram sinifi. 
     * Icinde tokenlar bulunur.
     */
    public class NGram {

        /**
         * Token'lar
         */
        List<String> items;

        /**
         * Bos bir Ngram olusturur.
         */
        public NGram() {
            this.items = new LinkedList<>();
        }
        
        /**
         * Bir string listesinden NGram objesi olusturur.
         * @param ngram 
         */
        public NGram(List<String> ngram) {
            this.items = new LinkedList<>(ngram);
        }

        ;
        
        @Override
        public String toString() {
            return toString(" ");
        }

        public String toString(String delimiter) {
            String r = "";
            for (int i = 0; i < items.size(); i++) {
                r += items.get(i);
                if (i < items.size() - 1) {
                    r += delimiter;
                }

            }

            return r;
        }

        public int getLength() {
            return items.size();
        }
    }
    
    /**
     * Setteki NGram listesi
     */
    public List<NGram> ngrams;

    public NGramSet() {
        ngrams = new LinkedList<>();
    }

    /**
     * Bir string listesinden ngram olusturarak bunu set'e ekler.
     * @param stringList 
     */
    public void addItemSet(List<String> stringList) {
        ngrams.add(new NGram(stringList));
    }

    /**
     * Sadece belli sayıda öğe içeren NGramlari iceren listeyi dondurur.
     *
     * @param size
     * @return
     */
    public List<NGram> getWithSize(int size) {
        List<NGram> r = new LinkedList<>();

        for (NGram n : ngrams) {
            if (n.getLength() == size) {
                r.add(n);
            }
        }

        return r;

    }

    /**
     * NGramSet'teki butun ngramları ekrana basilabilecek formatta dondurur.
     * @param ngset
     * @return 
     */
    public static String NGrams2String(NGramSet ngset) {
        return NGrams2String(ngset.ngrams);

    }

    /**
     * Ekrana basilabilir formatta butun ngramlari dondurur.
     * @param ngramList ngram listesi.
     * @return 
     */
    public static String NGrams2String(List<NGram> ngramList) {

        String r = "";
        for (NGram n : ngramList) {
            r += n.toString() + "\r\n";

        }
        return r;

    }

    /**
     * Verilen tokenlardan ngramSet'i oluşturur.
     * Örn: Selahattin Abi, hemen buraya gel! için 3lü ngram istersek
     * [Selahattin] [Abi]
     * [Hemen] [buraya] [gel]
     * seklinde doner. Virgül olduğu için ilk ngrami 2li almistir.
     * 
     * Illa da 3lu isteniyorsa NGramSet.getWithSize(3) yapilarak sadece ucluler alinir.
     * Bu durumda sadece [hemen] [buraya] [gel] doner. 
     * Cunku virgul ile bolunmus kisimlar ayri ayri ele alinir.
     * 
     * @param tokens OpenNLP tokenizer.tokenize()'dan alinir genelde
     * @param n Ngram'in boyutu
     * @return 
     */
    public static NGramSet createNGramSet(String tokens[], int n) {

        NGramSet ngSET = new NGramSet();

        List<String> bag = new LinkedList<>();
        int k = 0;

        for (int i = 0; i < tokens.length; i++) {
            k++;
            String token = tokens[i];
            if (!isPunctuation(token)) {

                bag.add(token);
                if (k == n) {
                    i = i - (n - 1);
                    k = 0;
                    ngSET.addItemSet(bag);
                    bag.clear();
                }

            } else {
                k = 0;
                ngSET.addItemSet(bag);
                bag.clear();

            }

        }

        return ngSET;



    }
    /**
     * Verilen token bir noktalama isareti mi?
     * @param token
     * @return 
     */
    public static boolean isPunctuation(String token) {
        return token.matches("(?m)[\\p{P}]");
    }
}
