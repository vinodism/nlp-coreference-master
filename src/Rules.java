/**
 * Created by vinodism on 11/5/16.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.simple.*;

public class Rules {


    List<String> nominativeList = Arrays.asList("they", "i", "he", "she", "we");
    List<String> accusativeList = Arrays.asList("me", "them", "him", "her", "us", "whom");
    List<String> possessiveList = Arrays.asList("my", "his", "our", "your", "your", "their", "mine", "yours", "his", "hers", "ours", "yours", "theirs");
    List<String> reflexiveList = Arrays.asList("yourself", "ourselves", "himself", "herself", "itself", "themselves", "myself");
    List<String> ambiguousList = Arrays.asList("you", "it");
    List<String> pluralPronoun = Arrays.asList("we", "they", "us", "them", "our", "ours");
    List<String> malesPronounList = Arrays.asList("he", "his", "himself", "him");
    List<String> femalesPronounList = Arrays.asList("she", "her", "herself", "hers");
    List<String> maleNamesList = new ArrayList<String>();
    List<String> femaleNamesList = new ArrayList<String>();
    List<String> animateList = new ArrayList<String>();
    List<String> inanimateList = new ArrayList<String>();

    private static Rules ourInstance = new Rules();

    public static Rules getInstance() {
        return ourInstance;
    }

    private Rules() {

        //files are downloaded from the following link
        //male and female http://www.cs.cmu.edu/Groups/AI/util/areas/nlp/corpora/names/
        try {

            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/data/male.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                maleNamesList.add(line.toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR: MALE FILE ERROR");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/data/female.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                femaleNamesList.add(line.toLowerCase());
            }
        } catch (Exception e) {
            System.out.println("ERROR: FEMALE FILE ERROR");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/data/animate.unigrams.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                animateList.add(line.toLowerCase());
            }
        } catch (Exception e) {
            System.out.println("ERROR: ANIMATE FILE ERROR");
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/data/inanimate.unigrams.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                inanimateList.add(line.toLowerCase());
            }
        } catch (Exception e) {
            System.out.println("ERROR: INANIMATE FILE ERROR");
        }
    }


    boolean matchPronounType(List<String> list, List<String> words) {
        for (String word : words) {
            word = word.toLowerCase();
            if (list.contains(word)) {
                return true;
            }
        }
        return false;
    }

    boolean isItemInList(List<String> list, String item) {
        item = item.toLowerCase();
        return list.contains(item);
    }

    /**
     * @param npObj
     * @return
     */
    List<String> ruleOneGetALlWords(NPObj npObj) {
        Sentence sent = new Sentence(npObj.strNP);
        return sent.words();
    }

    /**
     * @param words
     * @return
     */
    String ruleTwoGetPronounType(List<String> words) {
        String pronounType = "NONE";

        if (matchPronounType(nominativeList, words)) {
            pronounType = "NOM";
        } else if (matchPronounType(accusativeList, words)) {
            pronounType = "ACC";
        } else if (matchPronounType(possessiveList, words)) {
            pronounType = "POS";
        }
        //todo check this reflexive whether its required or not
        else if (matchPronounType(reflexiveList, words)) {
            pronounType = "REF";
        }
//		else if(matchPronounType(ambiguousList,words)){
//			pronounType = "AMB";
//		}

        return pronounType;
    }

    /**
     * @param allWords
     * @return
     */
    String ruleThreeGetArticle(List<String> allWords) {
        String article = "NONE";
        for (String word : allWords) {
            String trimLowerCaseWord = word.trim();
            if (trimLowerCaseWord.equals("a")) {
                article = "INDEF";
            } else if (trimLowerCaseWord.equals("an")) {
                article = "INDEF";
            } else if (trimLowerCaseWord.equals("the")) {
                article = "DEF";
            }
        }

        return article;
    }

    /**
     * Rule 4
     *
     * @param npObj
     * @return
     */
    String ruleFourGetAppositive(NPObj npObj) {
        return "NO";
    }

    /**
     * @param allWords
     * @return
     */
    ENUM_NUMBER_TYPE ruleFiveGetNumber(List<String> allWords) {
        ENUM_NUMBER_TYPE number = ENUM_NUMBER_TYPE.SINGULAR;

        int wordsSize = allWords.size();
        if (wordsSize > 0) {
            String lastWord = allWords.get(wordsSize - 1).toLowerCase();
            if (lastWord.endsWith("s")) {
                number = ENUM_NUMBER_TYPE.PLURAL;
                return number;
            }
        }

        //two way to check this case
        for (String pronoun : pluralPronoun) {
            if (allWords.contains(pronoun)) {
                number = ENUM_NUMBER_TYPE.PLURAL;
                break;
            }
        }

        return number;
    }

    /**
     * @param npObj
     * @return
     */
    boolean ruleSixGetProperNames(NPObj npObj) {
        Sentence sent = new Sentence(npObj.strNP);
        for (String tag : sent.nerTags()) {
            if (tag.equals("PERSON")) {
                return true;
            }
        }
        return false;
    }

    String ruleSevenHeadNoun(String sentence) {

        Sentence nlpSentence = new Sentence(sentence);
        int headIndex = nlpSentence.algorithms().headOfSpan(new Span(0,nlpSentence.length()));
        String head = nlpSentence.words().get(headIndex);

        if(head.length() > 0) {
            return head;
        }
        else{
            return "";
        }
//        Sentence sent = new Sentence("your text should go here");
//        sent.algorithms()..headOfSpan(new Span(0, 2));  // Should return 1
//
//        int wordsSize = words.size();
//        if (wordsSize > 0) {
//            return words.get(wordsSize - 1);
//        }
//        return "";
    }

    //todo imporove this feature
    //todo by applying pos tagger
    String ruleEightGender(List<String> words) {
        for (String word : words) {
            word = word.toLowerCase();
            if (isItemInList(malesPronounList, word)) {
                return "MASC";
            } else if (isItemInList(maleNamesList, word)) {
                return "MASC";
            } else if (isItemInList(femalesPronounList, word)) {
                return "FEM";
            } else if (isItemInList(femaleNamesList, word)) {
                return "FEM";
            } else if (word.equals("it")) {
                return "EITHER";
            }
        }

        return "NEUTER";
    }

    /**
     * Nin Animacy
     *
     * @param headNoun
     * @return
     */
    ENUM_ANIM_TYPE ruleNineAnimacy(String headNoun) {


        if(isItemInList(animateList, headNoun)){
            return ENUM_ANIM_TYPE.ANIMATE;
        }
        else if(isItemInList(inanimateList, headNoun)){
            return ENUM_ANIM_TYPE.INANIMATE;
        }


        return ENUM_ANIM_TYPE.UNKNOWN;
    }




    List<String> getNERtype(String phrase, String tag) {
        Sentence sent = new Sentence(phrase);
        List<String> tags = sent.nerTags();
        List<String> finaltag = new ArrayList();

        Pair<String,Dictionaries.Person> check  = new Pair<String,Dictionaries.Person> ("sunny", Dictionaries.Person.IT);

        boolean istagpresent = false;
        boolean issomeothertagpresent = false;
        for (int i = 0; i < tags.size(); i++) {
            //to check if valid tag is present
            if ((tags.get(i).equals(tag))) {
                istagpresent = true;
            }
            //to check if other tag is present
            if ((!(tags.get(i).equals(tag))) && (!(tags.get(i).equals("O")))) {
                issomeothertagpresent = true;
            }
        }
        //if other tag is there return null
        if (issomeothertagpresent) {
            return null;
        }
        //if all are 0s then return null
        if ((!istagpresent) && (!issomeothertagpresent)) {
            return null;
        }
        //in all other cases we go here.
        String finalname = "";
        for (int j = 0; j < tags.size(); j++) {
            if ((tags.get(j).equals(tag))||(sent.word(j).toLowerCase().equals("mr."))||(sent.word(j).toLowerCase().equals("mr"))
                    ||(sent.word(j).toLowerCase().equals("miss"))||(sent.word(j).toLowerCase().equals("mrs"))
                    ||(sent.word(j).toLowerCase().equals("mrs."))) {
                finalname = finalname + " " + sent.word(j);
                if (j == tags.size() - 1) {
                    finaltag.add(finalname);
                }
            } else {
                if (finalname.trim().length() > 0) {
                    finaltag.add(finalname);
                }
                finalname = "";
            }
        }
        return finaltag;
    }

    /**
     * Ten semantic class
     *
     * @param headNoun
     * @param isPerson
     * @param gender
     * @param animacy
     * @return
     */
    String ruleTenSemanticClass(String headNoun, boolean isPerson, String gender, ENUM_ANIM_TYPE animacy) {
//        if (isPerson) {
//            return "HUMAN";
//        } else if (animacy.equals("ANIM")) {
//            return "HUMAN";
//        }
//
//        Sentence sent = new Sentence(headNoun);
//        List<String> tags = sent.nerTags();
//        if (tags.size() > 0) {
//            if ((!tags.get(0).equals("O"))) {
//                return tags.get(0);
//            }
//        }
        return "OBJECT";
    }
//	//features extracted from the parsing the corpus.
//	void ExtractFeatures(NPObj nPhrases) {
//		{
//			Features featureObj = nPhrases.fObj;
//			featureObj.setPosition(nPhrases.fPos);
//			//if(Utilities.getInstance().ifPronoun(nPhrases.strNP){
//			//featureObj.setPronounType(PronounType.);
//			//}
//			String[] words = nPhrases.strNP.split("\\s");
//			featureObj.setHeadNoun(words[words.length - 1]);
//			featureObj.setWords(nPhrases.strNP);
//			for (int i = 0; i < words.length; i++) {
//				if ((words[i].toLowerCase().equals("a")) || (words[i].toLowerCase().equals("an"))) {
//					featureObj.setArticle("Indefinite");
//				} else if (words[i].toLowerCase().equals("the")) {
//					featureObj.setArticle("Indefinite");
//				}
//			}
//		}
//	}
//	//checks if two NP are appositives or not.
/*	private boolean isAppositive(NPObj a, NPObj b) {
        Features afeatureObj = a.fObj;
		Features bfeatureObj = b.fObj;
		{
					if (afeatureObj.getIsProperNoun() && (!bfeatureObj.getIsProperNoun())) {
						return true;
					} else if (!afeatureObj.getIsProperNoun() && (bfeatureObj.getIsProperNoun())) {
						return true;
					} else {
						return false;
					}
				} else {
					if (Utilities.getInstance().isInteger(a.SentenceArray[index_a + 1]))
						return true;
				}

			}
		}
		return false;
	}*/

    Dictionaries.Gender identifyGender(List<String> words){
        for(String word : words){
            word = word.toLowerCase();
            if(((word.trim().equals("mr"))||(word.trim().equals("mr.")))||(word.trim().equals("sir"))||(word.trim().equals("mister"))){
                return Dictionaries.Gender.MALE;
            }
            if(((word.trim().equals("miss"))||(word.trim().equals("ms.")))||(word.trim().equals("mrs"))||(word.trim().equals("mrs."))||word.trim().equals("madam")){

                return Dictionaries.Gender.FEMALE;
            }
            if(isItemInList(malesPronounList,word)){
                return Dictionaries.Gender.MALE;
            }
            if(isItemInList(femalesPronounList,word)){
                return Dictionaries.Gender.FEMALE;
            }
            if(isItemInList(maleNamesList,word)){
                return Dictionaries.Gender.MALE;
            }
            if(isItemInList(femaleNamesList,word)){
                return Dictionaries.Gender.FEMALE;
            }
            if(word.equals("it")){
                return Dictionaries.Gender.NEUTRAL;
            }
            if(word.length() > 2 && word.substring(word.length()-2,word.length()-1).equals("yn")){
                return Dictionaries.Gender.FEMALE;
            }
            if(word.length() > 2 && word.substring(word.length()-2,word.length()-1).equals("ch")) {
                return Dictionaries.Gender.MALE;
            }
            if(word.length() > 1 && word.charAt(word.length()-1)=='a'){
                return Dictionaries.Gender.FEMALE;
            }
            if(word.length() > 1 && word.charAt(word.length()-1)=='k'){
                return Dictionaries.Gender.MALE;
            }
            if(word.length() > 1 && word.charAt(word.length()-1)=='p'){
                return Dictionaries.Gender.MALE;
            }
            if(word.length() > 1 && word.charAt(word.length()-1)=='v'){
                return Dictionaries.Gender.MALE;
            }
            if(word.length() > 1 && word.charAt(word.length()-1)=='v'){
                return Dictionaries.Gender.MALE;
            }
            if(word.length() > 1 && word.charAt(word.length()-1)=='e'){
                return Dictionaries.Gender.FEMALE;
            }
            if(word.length() > 1 && word.charAt(word.length()-1)=='i'){
                return Dictionaries.Gender.FEMALE;
            }

        }
        //if nothing found we can check with following pronouns
        return Dictionaries.Gender.UNKNOWN;
    }


    List<String> getAbbreviation(String nounPhrase){

//        Sentence np = new Sentence(nounPhrase);
//        List<String> words = np.words();
//
//        //remove any article
//        if(words.size() > 0){
//            if((words.get(0).toLowerCase().equals("a"))
//                    ||(words.get(0).toLowerCase().equals("an"))
//                    ||(words.get(0).toLowerCase().equals("the"))
//                    ||(words.get(0).toLowerCase().equals("this"))){
//
//                words.remove(0);
//            }
//        }
//
//        String withDots;
//        String withoutDots;
//        for(String word: words){
//            if(word.length() > 0 && Character.isUpperCase(word.charAt(0))){
//                withDots += word.charAt(0)
//            }
//            else{
//                break;
//            }
//        }


        List<String> allabbs= new ArrayList<String>();
        String[] npbreak=nounPhrase.split("\\s+");
        int startindex=0;
        if(npbreak[0].length() > 0
                && (npbreak[0].toLowerCase().equals("a"))
                    ||(npbreak[0].toLowerCase().equals("an"))
                    ||(npbreak[0].toLowerCase().equals("the"))
                    ||(npbreak[0].toLowerCase().equals("this"))){
            startindex++;
        }

        if((npbreak.length-startindex)>1){
            String finalabb="";
            String finalabbinc="";
            for(int i=startindex;i<npbreak.length;i++) {
                if(npbreak[i].length() > 0) {
                    if (Character.isUpperCase(npbreak[i].charAt(0))) {
                        finalabb = finalabb.trim() + npbreak[i].charAt(0);
                        finalabbinc = finalabbinc.trim() + npbreak[i].charAt(0);
                    } else {
                        if ((npbreak[i].toLowerCase().equals("in")) || (npbreak[i].toLowerCase().equals("of"))) {
                            finalabbinc = finalabbinc.trim() + Character.toUpperCase(npbreak[i].charAt(0));
                        } else {
                            return allabbs;
                        }
                    }
                }
            }

            if(0 == finalabb.length() || 0 == finalabbinc.length()){
                return allabbs;
            }

            String finalabbwithdots=Character.toString(finalabb.charAt(0));
            String finalabbincwithdots=Character.toString(finalabbinc.charAt(0));
            for(int k=1;k<finalabb.length();k++){
                finalabbwithdots=finalabbwithdots+"."+finalabb.charAt(k);
            }
            finalabbwithdots=finalabbwithdots+".";

            for(int z=1;z<finalabbinc.length();z++){
                finalabbincwithdots=finalabbincwithdots+"."+finalabbinc.charAt(z);
            }
            finalabbincwithdots=finalabbincwithdots+".";
            allabbs.add(finalabb.trim());
            allabbs.add(finalabbwithdots.trim());
            if(finalabb.length()!=finalabbinc.length()){
                allabbs.add(finalabbinc.trim());
                allabbs.add(finalabbincwithdots.trim());
            }
        }
        return allabbs;
    }


}