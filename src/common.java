
import java.util.*;

//File needed to be changed
enum PronounType {
    I(Gender.EITHER, Type.SUBJECTIVE),
    ME(Gender.EITHER, Type.OBJECTIVE),
    MYSELF(Gender.EITHER, Type.REFLEXIVE),
    MINE(Gender.EITHER, Type.POSESSIVE_PRONOUN),
    MY(Gender.EITHER, Type.POSESSIVE_DETERMINER),
    WE(Gender.EITHER, Type.SUBJECTIVE),
    US(Gender.EITHER, Type.OBJECTIVE),
    OURSELF(Gender.EITHER, Type.REFLEXIVE),
    OURSELVES(Gender.EITHER, Type.REFLEXIVE),
    OURS(Gender.EITHER, Type.POSESSIVE_PRONOUN),
    OUR(Gender.EITHER, Type.POSESSIVE_DETERMINER),

    YOU(Gender.EITHER, Type.OBJECTIVE),
    YOURSELF(Gender.EITHER, Type.REFLEXIVE),
    YOURS(Gender.EITHER, Type.POSESSIVE_PRONOUN),
    YOUR(Gender.EITHER, Type.POSESSIVE_DETERMINER),
    YOURSELVES(Gender.EITHER, Type.REFLEXIVE),

    HE(Gender.MALE, Type.SUBJECTIVE),
    HIM(Gender.MALE, Type.OBJECTIVE),
    HIMSELF(Gender.MALE, Type.REFLEXIVE),
    HIS(Gender.MALE, Type.POSESSIVE_PRONOUN),
    SHE(Gender.FEMALE, Type.SUBJECTIVE),
    HER(Gender.FEMALE, Type.OBJECTIVE),
    HERSELF(Gender.FEMALE, Type.REFLEXIVE),
    HERS(Gender.FEMALE, Type.POSESSIVE_PRONOUN),
    IT(Gender.NEUTRAL, Type.SUBJECTIVE),
    ITSELF(Gender.NEUTRAL, Type.REFLEXIVE),
    ITS(Gender.NEUTRAL, Type.POSESSIVE_PRONOUN),
    THEY(Gender.EITHER, Type.OBJECTIVE),
    THEM(Gender.EITHER, Type.OBJECTIVE),
    THEMSELVES(Gender.EITHER, Type.REFLEXIVE),
    THEIRSELVES(Gender.EITHER, Type.REFLEXIVE),
    THEIRS(Gender.EITHER, Type.POSESSIVE_PRONOUN),
    THEIR(Gender.EITHER, Type.POSESSIVE_DETERMINER);

    private static final PronounType[] cachedValues = values();
    public static enum Type {SUBJECTIVE, OBJECTIVE, REFLEXIVE, POSESSIVE_PRONOUN, POSESSIVE_DETERMINER,NONE}
    public static enum Gender {MALE, FEMALE, NEUTRAL, EITHER}

    public final Type type;
    public final Gender gender;
    private static Set<String> pronounSet = new HashSet<String>();
    private PronounType(Gender gender, Type type) {
        this.gender = gender;
        this.type = type;
    }
    private static String[] pronounList = new String[]{
            "i",
            "all",
            "he",
            "her",
            "hers",
            "herself",
            "him",
            "himself",
            "his",
            "hisself",
            "it",
            "its",
            "itself",
            "me",
            "mine",
            "my",
            "myself",
            "one",
            "one's",
            "oneself",
            "our",
            "ours",
            "ourself",
            "ourselves",
            "she",
            "thee",
            "their",
            "theirs",
            "theirselves",
            "them",
            "themself",
            "themselves",
            "they",
            "thine",
            "thou",
            "thy",
            "thyself",
            "us",
            "we",
            "y'all",
            "y'all's",
            "y'all's selves",
            "ye",
            "you",
            "you all",
            "your",
            "yours",
            "yourself",
            "yourselves",
            "youse",
    };
    public static boolean isThisPronnoun(String cand){
        return pronounSet.contains(cand.toLowerCase());
    }
    public PronounType whichpronoun(String word){
        for(PronounType p : values()){
            if(p.name().equalsIgnoreCase(word)){ return p; }
        }
        return null;
    }
    static {
        for(String pronoun : pronounList){
            if(pronounSet.contains(pronoun)){ throw new IllegalStateException("Duplicate pronoun: " + pronoun); }
            pronounSet.add(pronoun);
        }
    }

}