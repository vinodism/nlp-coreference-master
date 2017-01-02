import java.util.HashSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Set;
public class Utilities {

    /**
     * Created by vinodism on 11/5/16.
     */
    /*

    public boolean ifPronoun(String word){
        return false;
    }
*/
    private static Utilities ourInstance = new Utilities();
    public static Utilities getInstance() {
        return ourInstance;
    }

    private Utilities() {
    }
    public  boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
