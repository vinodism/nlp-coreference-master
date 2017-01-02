
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.simple.*;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

public class SentenceObj {
    String sentenceWithXML;
    String sentenceWithoutXML;
    List<NPObj> npList = new ArrayList<NPObj>();
    Sentence nlpParsedObj;
    int startPosIndex = 0;

    public SentenceObj(Sentence nlpParsedObj,int startPosIndex){

        this.nlpParsedObj = nlpParsedObj;
        this.sentenceWithXML = nlpParsedObj.toString();
        this.sentenceWithoutXML = this.sentenceWithXML.replaceAll("<[^>]+>", "");
        this.startPosIndex = startPosIndex;

        parse(this.sentenceWithoutXML);
    }


    public String removeExtraSpace(String npWithSpaces, String sent){
        //apply the brtue force approach to remove the space from sentence
        for(int n = 0; n < npWithSpaces.length() ; n++){
            if(npWithSpaces.charAt(n) == ' ') {
                StringBuilder sb = new StringBuilder(npWithSpaces);
                sb.deleteCharAt(n);
                String s = sb.toString();
                if(sent.indexOf(s) >= 0){
                    return s;
                }
            }
        }
        //or send the same npWithSpace as you have to no choice
        return npWithSpaces;
    }
    /**
     * will return all the noun phrases
     * todo: great variety of changes can be made in this function to get all the important
     * refer highlighted sections in the paper
     * @param parse
     * @return
     */
    private List<String> getNounPhrases(Tree parse, String sentenceWithoutXML) {
        List<String> result = new ArrayList<String>();
        //TregexPattern pattern = TregexPattern.compile("(NP < (NNP $++ NNP $++ NNP)) | (NP < (NNP $++ NNP )) | (@NNS) | (@NN) |(@NNPS) | (@PRP) | (@VBD)");
        //TregexPattern pattern = TregexPattern.compile("(@NP < @NNP & < @NNP) | (@NNP) | (@NNS) | (@NN) |(@NNPS) | (@PRP) | (@VBD)");
        //TregexPattern pattern = TregexPattern.compile("(@NP < @NNP & < @NNP) | (@NP < @NNP & < @NNP & < @NNP) | (@NNS) | (@NN) |(@NNPS) | (@PRP)");
        TregexPattern pattern = TregexPattern.compile(" (@NNS) | (@NN) |(@NNPS) | (@PRP) | (@NNP)");
        TregexMatcher matcher = pattern.matcher(parse);

        while (matcher.findNextMatchingNode()) {

            Tree match = matcher.getMatch();
            String sent = sentenceWithoutXML;
            String np = SentenceUtils.listToString(match.yield());

            // check if there are any extra space
            // if not then remove all the extra space
            if(sent.indexOf(np) < 0) {
                np = removeExtraSpace(np, sent);
            }

            // this will add the np by removing
            // all the unnecessary spaces
            if(!Settings.getInstance().printcsv) {
                System.out.println("np = " + np);
            }

            result.add(np);
        }
        return result;
    }

    public static List<String> breakAtConjunction(String nounPhrase){
        List<String> wordList=new ArrayList<String>();
        String[] splits = nounPhrase.split("\\band\\b|\\bor\\b|\\bfor\\b|\\bnor\\b|\\bbut\\b|\\byet\\b|\\bso\\b|\\bafter\\b|\\balthough\\b|\\bas\\b" +
                "\\bas though\\b|\\bbecause\\b|\\bbefore\\b|\\beven\\b|\\bif\\b|\\bsince\\b|\\bso that\\b|\\bthan\\b|\\bthat\\b|\\bthough\\b|\\btill\\b|\\bunless\\b|\\buntil\\b" +
                "\\bwhen\\b|\\bwhenever\\b|\\bwhere\\b|\\bwhile\\b|\\blest\\b|\\beither\\b|\\bneither\\b|\\bwhether\\b"+
                "\\bAnd\\b|\\bOr\\b|\\bFor\\b|\\bNor\\b|\\bBut\\b|\\bYet\\b|\\bSo\\b|\\bAfter\\b|\\bAlthough\\b|\\bAs\\b" +
                "\\bAs Though\\b|\\bBecause\\b|\\bBefore\\b|\\bEven\\b|\\bIf\\b|\\bSince\\b|\\bSo That\\b|\\bThan\\b|\\bThat\\b|\\bThough\\b|\\bTill\\b|\\bUnless\\b|\\bUntil\\b" +
                "\\bWhen\\b|\\bWhenever\\b|\\bWhere\\b|\\bWhile\\b|\\bLest\\b|\\bEither\\b|\\bNeither\\b|\\bWhether\\b"+
                "\\bAND\\b|\\bOR\\b|\\bFOR\\b|\\bNOR\\b|\\bBUT\\b|\\bYET\\b|\\bSO\\b|\\bAFTER\\b|\\bALTHOUGH\\b|\\bAS\\b" +
                "\\bAS THOUGH\\b|\\bBECAUSE\\b|\\bBEFORE\\b|\\bEVEN\\b|\\bIF\\b|\\bSINCE\\b|\\bSO THAT\\b|\\bTHAN\\b|\\bTHAT\\b|\\bTHOUGH\\b|\\bTILL\\b|\\bUNLESS\\b|\\bUNTIL\\b" +
                "\\bWHEN\\b|\\bWHENEVER\\b|\\bWHERE\\b|\\bWHILE\\b|\\bLEST\\b|\\bEITHER\\b|\\bNEITHER\\b|\\bWHETHER\\b");

        for(int i=0;i<splits.length;i++) {
            if (splits[i].trim().length() > 0){
                wordList.add(splits[i].trim());
            }
        }
        return wordList;
    }

    /**
     * call the function to parse the sentence
     * to fetch all the noun phrases
     * @param sentenceWithoutXML
     * @return
     */
    void parse(String sentenceWithoutXML){
        String tokens[] =sentenceWithoutXML.split(",");
        int commaCount = StringUtils.countMatches(sentenceWithoutXML, ",");

        UtilitySingleton utilitySingletonObj = UtilitySingleton.getInstance();
        String commaID = "";

        this.startPosIndex++;

        //take action if there are any tokens
        if(tokens.length > 0) {

            for (int t = 0; t < tokens.length; t++) {

                String sentence = tokens[t].trim();
                if(sentence.length() > 0) {

                    List<String> finalner = Rules.getInstance().getNERtype(sentence, "PERSON");
                    if (finalner != null && finalner.size() > 0) {

                        for (int i = 0; i < finalner.size(); i++) {

                            String ID = utilitySingletonObj.getGenerateNewKey();

                            utilitySingletonObj.addListPersonID(ID);
                            utilitySingletonObj.updateMap(ID, finalner.get(i), this.startPosIndex, true);
                            this.startPosIndex++;
                        }
                    }
                    else {

                        Sentence sent = new Sentence(sentence);

                        //parse result of all the sentence and
                        //add to the nplist of object
//                        List<String> results = sent.algorithms().keyphrases();//getNounPhrases(sent.parse(), sentence);
//                        for (String np : results) {
//
//                            //for (String npConj : breakAtConjunction(np)) {
//
//                                String ID = utilitySingletonObj.getGenerateNewKey();
//                                utilitySingletonObj.updateMap(ID, np, this.startPosIndex, false);
//                                this.startPosIndex++;
//                            //}
//                        }

                        //parse result of all the sentence and
                        //add to the nplist of object
                        List<String> results = getNounPhrases(sent.parse(), sentence);
                        for (String np : results) {

                            for (String npConj : breakAtConjunction(np)) {

                                String ID = utilitySingletonObj.getGenerateNewKey();
                                utilitySingletonObj.updateMap(ID, npConj, this.startPosIndex, false);
                                this.startPosIndex++;
                            }
                        }

                        if (results.size() == 0) {
                            commaCount = 0;
                        }
                    }
                }

                if(commaCount > 0) {

                    commaID = utilitySingletonObj.getInstance().getGenerateNewKey();
                    utilitySingletonObj.getInstance().updateMap(commaID, ",", this.startPosIndex, false);
                    this.startPosIndex++;

                    commaCount--;
                }
            }
        }
        while(commaCount > 0) {

            commaID = utilitySingletonObj.getInstance().getGenerateNewKey();
            utilitySingletonObj.getInstance().updateMap(commaID, ",", this.startPosIndex, false);
            this.startPosIndex++;

            commaCount--;
        }
    }
}
