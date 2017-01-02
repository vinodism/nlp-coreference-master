

import java.util.*;
import java.io.File;
import com.google.common.io.Files;
import java.nio.charset.Charset;
import java.lang.String;
import java.util.List;

import edu.stanford.nlp.coref.data.Dictionaries;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import javax.xml.parsers.*;
import java.io.StringReader;
import edu.stanford.nlp.simple.*;

public class FileObj {

    List<SentenceObj> sentencesObjList  = new ArrayList<SentenceObj>();
    List<NPObj> npObjList               = new ArrayList<NPObj>();
    List<String> allSentenceExceptCoref = new ArrayList<String>();
    String sentenceWithXML;
    Document document;
    int pos = 0;
    /**
     *
     * @param fileName
     * @throws Exception
     */
    public FileObj(String fileName) throws Exception{

        // read all the map
        generateMapOfNPToID(fileName);

        // read all the sentences of the file
        // and store in the sentences data structure
        // parse();

        //read all the sentence from the map
        //create the list of NPObjs
        populateNPList();

//        //todo remove this function
//        for(NPObj npObj: this.npObjList){
//            npObj.setMaxDifference(this.npObjList.size());
//            npObj.loadFeatures();
//        }
        ClusteringAlgorithm clusteringAlgorithmObj = new ClusteringAlgorithm();
        clusteringAlgorithmObj.loadData(this.npObjList);
        List<List<NPObj>> setOfCluster = clusteringAlgorithmObj.run(Settings.getInstance().getRadius());

        this.document = new PrintXML().print(setOfCluster,this.sentenceWithXML);
        //this.document = new PrintXML().printWithNewAlgo(npObjList,this.sentenceWithXML);

        UtilitySingleton.getInstance().clearMap();
    }

    /**
     * this will populate the np list
     */
    void populateNPList(){

        Map<Integer,Integer> localTempMap = new HashMap<Integer, Integer>();
        // Getting a Set of Key-value pairs
        Set entrySet = UtilitySingleton.getInstance().getMap().entrySet();

        // Obtaining an iterator for the entry set
        Iterator it = entrySet.iterator();

        // Get the sorted list of all pos
        List<Integer>  listOfAllCurrentPos = new ArrayList<Integer>();
        while(it.hasNext()){
            Map.Entry me = (Map.Entry)it.next();
            listOfAllCurrentPos.add(((NPObj) me.getValue()).getPos());
        }
        Collections.sort(listOfAllCurrentPos);

        // Iterate through all the pos
        Integer localPos = 1;
        for(Integer currentPos: listOfAllCurrentPos){
            localTempMap.put(currentPos,localPos++);
        }

        //now update the object with new pos and
        //add to the np object list
        it = entrySet.iterator();
        while(it.hasNext()){
            Map.Entry me = (Map.Entry)it.next();
            NPObj npObj = ((NPObj) me.getValue());
            int currentPos = npObj.getPos();
            int newPos = localTempMap.get(currentPos);
            npObj.setPos(newPos);

            //this will add all the np object in the list
            if(Settings.getInstance().getNptype().equals(NPTYPE.ELLEN)) {
                if (Integer.parseInt(npObj.getID()) > 0) {
                    this.npObjList.add(npObj);
                }
            }
            else{
                this.npObjList.add(npObj);
            }
        }

        //sort all the np objs
        Collections.sort(this.npObjList, new Comparator<NPObj>() {
            public int compare(NPObj np1, NPObj np2) {
                return np1.getPos() - np2.getPos();
            }
        });

        ///////////////////////////////////  load all the features  //////////////////////
        for(NPObj npObj: this.npObjList){
            npObj.setMaxDifference(this.npObjList.size());
            npObj.loadFeatures();

            List<String> words = npObj.getFeaturesObj().getRuleOne_allWords();
            if(npObj.isPerson) {
                npObj.setGender(Rules.getInstance().identifyGender(words));
            }
            else{
                npObj.setGender(Dictionaries.Gender.UNKNOWN);
            }

            npObj.setAnim_type(npObj.getFeaturesObj().getRuleNine_animacy());
        }

        ///////////////////////////////////////// abbreviation /////////////////////////////////////////////////
        for(int n = 0; n < this.npObjList.size(); n++){
            NPObj npObjN = this.npObjList.get(n);

            for(int s = n+1; s < this.npObjList.size(); s++){
                NPObj npObjS = this.npObjList.get(s);

                String npS = npObjS.getStrNP().replaceAll("\\ba\\b","");
                npS = npS.replaceAll("\\ban\\b","");
                npS = npS.replaceAll("\\bthe\\b","");
                npS = npS.replaceAll("\\bA\\b","");
                npS = npS.replaceAll("\\bAN\\b","");
                npS = npS.replaceAll("\\bTHE\\b","");
                npS = npS.replaceAll("\\bAn\\b","");
                npS = npS.replaceAll("\\bThe\\b","");
                npS = npS.trim().toUpperCase();

                boolean flag = false;
                for(String abbrvN : npObjN.abbreviationList){
                    if(npS.equals(abbrvN)){
                        if(npObjN.getREF().equals("")) {
                            npObjN.setREF(npObjS.getID());
                        }
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    break;
                }
            }
        }

        ////////////////////////////////////////////exact same match//////////////////////////////////////
        for(int n = this.npObjList.size()-1; n >= 0 ; n--){
            NPObj npN = this.npObjList.get(n);

            for(int s = n-1; s >= 0 ;s--){
                NPObj npS = this.npObjList.get(s);

                if(npS.getStrNP().equals(npN.getStrNP())){
                    if(npN.getREF().equals("")) {
                        npN.setREF(npS.getID());
                    }
                    break;
                }
            }
        }


        //////////////////////////////////// comma logic goes over here ////////////////////////////////////////

        //this will look for the comma for next element
        for(int n = 0; n < this.npObjList.size() - 1; n++){
            NPObj np1 = this.npObjList.get(n);
            NPObj np2 = this.npObjList.get(n+1);

            String strNP1 = np1.getStrNP().trim();
            String strNP2 = np2.getStrNP().trim();

            if(!strNP1.equals(",") && strNP1.length() > 0){
                char lastCharacterNP1 = strNP1.charAt(strNP1.length()-1);
                if(lastCharacterNP1 == ','){
                    np1.setEndingWithComma(true);
                }
                else if(strNP2.equals(",")){
                    np1.setEndingWithComma(true);
                }
            }
        }

        // this will check for the
        //apply coref identification
        //this will look for the comma for next element
        for(int n = 0; n < this.npObjList.size() - 3; n++){
            NPObj np1 = this.npObjList.get(n);
            NPObj np2 = this.npObjList.get(n+1);
            NPObj np3 = this.npObjList.get(n+2);

            if(true == np1.isEndingWithComma()
                && ( Integer.parseInt(np1.getID()) > 0
                    || Integer.parseInt(np3.getID()) > 0)){

                if(np1.isEndingWithComma && np1.isPerson){
                    np3.setREF(np1.getID());
                }
                else {
                    List<String> allWords = np3.getFeaturesObj().getRuleOne_allWords();
                    for (String word : allWords) {
                        if (isArticle(word)) {
                            if(np3.getREF().equals("")) {
                                np3.setREF(np1.getID());
                            }
                            break;
                        }
                    }
                }
            }
        }



        ///////////////////////////////////Pronoun Resolution///////////////////////////////////////////////////
        //////////////////////  he his himself him she her herself hers ////////////////////////////////////////

        for(int n = this.npObjList.size()-1; n >= 0 ; n--){
            NPObj nplast = this.npObjList.get(n);

            if(nplast.getStrNP().equals("he")
                || nplast.getStrNP().equals("his")
                || nplast.getStrNP().equals("himself")
                || nplast.getStrNP().equals("him")
                || nplast.getStrNP().equals("she")
                || nplast.getStrNP().equals("her")
                || nplast.getStrNP().equals("herself")
                || nplast.getStrNP().equals("hers")){

                Dictionaries.Gender npLastGender
                        = Rules.getInstance().identifyGender(nplast.getFeaturesObj()
                                        .getRuleOne_allWords());

                for(int fromN = n-1; fromN >= 0 ;fromN--){
                    NPObj npFromN = this.npObjList.get(fromN);

                    Dictionaries.Gender npFromNGender
                            = Dictionaries.Gender.UNKNOWN;

                    if(npFromN.getStrNP().equals("he")
                            || npFromN.getStrNP().equals("his")
                            || npFromN.getStrNP().equals("himself")
                            || npFromN.getStrNP().equals("him")
                            || npFromN.getStrNP().equals("she")
                            || npFromN.getStrNP().equals("her")
                            || npFromN.getStrNP().equals("herself")
                            || npFromN.getStrNP().equals("hers")){

                        npFromNGender = Rules.getInstance().identifyGender(npFromN.getFeaturesObj()
                                                        .getRuleOne_allWords());
                    }

                    //if(npFromN.getStrNP().equals(nplast.getStrNP())){
                    if(npFromNGender == npLastGender && npFromNGender != Dictionaries.Gender.UNKNOWN){
                        nplast.setREF(npFromN.getID());
                        break;
                    }
                    else if(npFromN.isPerson){
                        if(npFromN.getGender() == npLastGender){
                            if(nplast.getREF().equals("")) {
                                nplast.setREF(npFromN.getID());
                            }
                            break;
                        }
                        else if(npFromN.getGender() == Dictionaries.Gender.UNKNOWN){
                            if(nplast.getREF().equals("")) {
                                nplast.setREF(npFromN.getID());
                            }
                            break;
                        }
                    }
//                    else if(npFromN.getAnim_type() == ENUM_ANIM_TYPE.ANIMATE){
//                        if(nplast.getREF().equals("")) {
//                            nplast.setREF(npFromN.getID());
//                        }
//                    }
                }
            }
        }


        ///////////////////////////////////Pronoun Resolution///////////////////////////////////////////
        // they them
        ////////////////////////////////////////////////////////////////////////////////////////////////
        for(int n = this.npObjList.size()-1; n >= 0 ; n--){

            NPObj nplast = this.npObjList.get(n);
            if(nplast.getStrNP().equals("them") || nplast.getStrNP().equals("they") || nplast.getStrNP().equals("their")){

                for(int fromN = n-1; fromN >= 0 ;fromN--){

                    NPObj npFromN = this.npObjList.get(fromN);
                    if(npFromN.getStrNP().equals("them")
                            || npFromN.getStrNP().equals("they")
                            || npFromN.getStrNP().equals("their")
                            || ( ENUM_NUMBER_TYPE.PLURAL == npFromN.getFeaturesObj().getRuleFive_number()
                                    && ENUM_ANIM_TYPE.ANIMATE == npFromN.getAnim_type()
                                    && ENUM_ANIM_TYPE.INANIMATE == npFromN.getAnim_type())){

                        if(nplast.getREF().equals("")) {
                            nplast.setREF(npFromN.getID());
                        }
                        break;
                    }
                }
            }
        }


        ///////////////////////////////////Pronoun Resolution///////////////////////////////////////////
        // we our ours
        ////////////////////////////////////////////////////////////////////////////////////////////////
        for(int n = this.npObjList.size()-1; n >= 0 ; n--){

            NPObj nplast = this.npObjList.get(n);
            if( nplast.getStrNP().equals("we")
                    || nplast.getStrNP().equals("our")
                    || nplast.getStrNP().equals("ours") ){

                for(int fromN = n-1; fromN >= 0 ;fromN--){

                    NPObj npFromN = this.npObjList.get(fromN);
                    if(npFromN.getStrNP().equals("we")
                            || npFromN.getStrNP().equals("our")
                            || npFromN.getStrNP().equals("ours")
                            || ( ENUM_NUMBER_TYPE.PLURAL == npFromN.getFeaturesObj().getRuleFive_number()
                                    && ENUM_ANIM_TYPE.ANIMATE == npFromN.getAnim_type())){

                        if(nplast.getREF().equals("")) {
                            nplast.setREF(npFromN.getID());
                        }
                        break;
                    }
                }
            }
        }


        ///////////////////////////////////Pronoun Resolution///////////////////////////////////////////
        // it its
        ////////////////////////////////////////////////////////////////////////////////////////////////
        for(int n = this.npObjList.size()-1; n >= 0 ; n--){

            NPObj nplast = this.npObjList.get(n);
            if( nplast.getStrNP().equals("it")
                    || nplast.getStrNP().equals("its")){

                for(int fromN = n-1; fromN >= 0 ;fromN--){

                    NPObj npFromN = this.npObjList.get(fromN);
                    if(npFromN.getStrNP().equals("it")
                            || npFromN.getStrNP().equals("its")
                            || ENUM_ANIM_TYPE.INANIMATE == npFromN.getAnim_type()){

                        if(nplast.getREF().equals("")) {
                            nplast.setREF(npFromN.getID());
                        }
                        break;
                    }
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //this will print the csv files
        for(int n = 0; n < this.npObjList.size() ; n++){
            NPObj np1 = this.npObjList.get(n);

            System.out.print(np1.getPos());
            System.out.print(";" + np1.getID());
            System.out.print(";" + np1.getStrNP().replaceAll("\\n",""));
            System.out.print("; " + np1.isEndingWithComma());
            System.out.print("; " + np1.getREF());
            System.out.print("; " + np1.isPerson());
            System.out.print("; " + np1.getGender());
            System.out.print("; " + np1.getAnim_type());
            System.out.println("");
        }

    }

    public Document getDocument(){
        return this.document;
    }

    public boolean isArticle(String word) {
        if( word.equals("a") || word.equals("the") || word.equals("an") ){
            return true;
        }
        else{
            return false;
        }
    }

    void generateMapOfNPToID(String fileName)  throws Exception{

        // read some text from the file..
        File inputFile = new File(fileName);

        System.out.println(inputFile);
        this.sentenceWithXML = Files.toString(inputFile, Charset.forName("UTF-8"));
        //this.sentenceWithXML = new Scanner(new File(fileName)).useDelimiter("\\Z").next();
//         this.sentenceWithXML = this.sentenceWithXML.replaceAll(" ", " ");
//         this.sentenceWithXML = this.sentenceWithXML.replaceAll("-", " ");
//         this.sentenceWithXML = this.sentenceWithXML.replaceAll("'s", " ");
//         this.sentenceWithXML = this.sentenceWithXML.replaceAll("'S", " ");
//         this.sentenceWithXML = this.sentenceWithXML.replaceAll("(\\r|\\n|\\r\\n)+", " ");
//         this.sentenceWithXML = this.sentenceWithXML.replaceAll("[ ]{2,}", " ");


        //this will modify the files
        //this is the temporary work around solution to read
        //sentence which are not marked with
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("<TXT>","<TXT><SENT>");
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("</TXT>","</SENT></TXT>");
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("<COREF","</SENT><COREF");
        this.sentenceWithXML = this.sentenceWithXML.replaceAll("</COREF>","</COREF><SENT>");

        try {
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(this.sentenceWithXML));
            Document doc = builder.parse(is);

            NodeList sentNodeList = doc.getElementsByTagName("SENT");
            NodeList corefNodeList = doc.getElementsByTagName("COREF");
            Rules ruleObj = Rules.getInstance();

            int sentIndex = 0;
            int corefIndex = 0;


            while(sentIndex < sentNodeList.getLength() || corefIndex < corefNodeList.getLength()) {

                if(sentIndex < sentNodeList.getLength()) {
                    Node sentNode = sentNodeList.item(sentIndex);
                    if (sentNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) sentNode;
                        String textContent = eElement.getTextContent();
                        textContent = textContent.replaceAll("\u00A0", " ");
                        parse(textContent);
                    }
                    pos += 50;
                    sentIndex++;
                }

                if(corefIndex < corefNodeList.getLength()) {
                    Node corefNode = corefNodeList.item(corefIndex);
                    if (corefNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) corefNode;
                        String textContent = eElement.getTextContent();
                        String ID = eElement.getAttribute("ID");

                        if(ruleObj.getNERtype(textContent,"PERSON") != null){
                            UtilitySingleton.getInstance().updateMap(ID, textContent, pos, true);
                        }
                        else{
                            UtilitySingleton.getInstance().updateMap(ID, textContent, pos, false);
                        }
                    }
                    pos += 50;
                    corefIndex++;
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * this function would be responsible
     * for the parsing of file into sentences
     */
    void parse(String sent){

        //for(String sent : this.allSentenceExceptCoref ) {
            //System.out.println(sent);
            edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(sent);
            // will iterate over all the sentences in the text
            for (Sentence nlpSentence : doc.sentences()) {
                SentenceObj sentenceObj = new SentenceObj(nlpSentence,pos);
                this.sentencesObjList.add(sentenceObj);

                pos += 50;
            }
        //}
    }
}
