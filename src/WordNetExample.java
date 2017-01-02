import java.io.IOException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.net.URL;
import edu.mit.jwi.*;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ILexFile;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class WordNetExample {
    public static void main(String[] args) throws IOException {

        //construct URL to WordNet Dictionary directory on the computer
        String wordNetDirectory = "src/WNDb";
        String path = wordNetDirectory + File.separator + "dict";
        URL url = new URL("file", null, path);

        //construct the Dictionary object and open it
        IDictionary dict = new Dictionary(url);
        dict.open();

        // look up first sense of the word "dog "
        IIndexWord idxWord = dict.getIndexWord ("spokesman", POS.NOUN );
        IWordID wordID = idxWord.getWordIDs().get(0) ;
        IWord word = dict.getWord (wordID);
        System.out.println("Id = " + wordID);
        System.out.println("word.getSenseKey() = " + word.getSenseKey().getHeadWord());
        System.out.println(" Lemma = " + word.getLemma());
        System.out.println(" Gloss = " + word.getSynset().getGloss());
    }
}
