import java.util.*;


public class Distance {
    private static Distance ourInstance = new Distance();

    public static Distance getInstance() {
        return ourInstance;
    }

    List<Double> listWeight;
    Map<String,Double> weightMap;
    List<NPObj> listOfAllNPs = new ArrayList<NPObj>();
    Map posToNPMap = new HashMap();

    private Distance() {
        this.weightMap = new HashMap<String,Double>();
    }

    //set list of all nps
    public void setNPList(List<NPObj> listOfAllNPs){
        this.listOfAllNPs = listOfAllNPs;
        for(int p = 0 ; p < listOfAllNPs.size() ; p++){
            this.posToNPMap.put(listOfAllNPs.get(p).getPos(),listOfAllNPs.get(p));
        }
    }

    public void loadMap(Double radius){

        Double maxValue = Settings.getInstance().maxValue;
        Double minValue = Settings.getInstance().minValue;
        this.weightMap.put("words",Settings.getInstance().getWeightWords());
        this.weightMap.put("headnoun",Settings.getInstance().getWeightHeadnoun());
        this.weightMap.put("position",Settings.getInstance().getWeightPosition());
        this.weightMap.put("pronoun",Settings.getInstance().getWeightPronoun());
        this.weightMap.put("article",Settings.getInstance().getWeightArticle());
        this.weightMap.put("words-substring",Settings.getInstance().getWeightWordsSubstring());
        this.weightMap.put("appositive",Settings.getInstance().getWeightAppositive());
        this.weightMap.put("number",Settings.getInstance().getWeightNumber());
        this.weightMap.put("proper-name",Settings.getInstance().getWeightProperName());
        this.weightMap.put("semantic-class",Settings.getInstance().getWeightSemanticClass());
        this.weightMap.put("gender",Settings.getInstance().getWeightGender());
        this.weightMap.put("animacy",Settings.getInstance().getWeightAnimacy());
    }

    //reference
    //http://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    public <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<T>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<T>(set);
    }

    //reference
    //http://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    public <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

    public boolean isArticle(String word) {
        if( word.equals("a") || word.equals("the") || word.equals("an") ){
            return true;
        }
        else{
            return false;
        }
    }

    public double calculate(NPObj np1, NPObj np2){
        double distance = 0.0;

        this.listWeight = new ArrayList<Double>();

        FeaturesObj featureObj1 = np1.getFeaturesObj();
        FeaturesObj featureObj2 = np2.getFeaturesObj();

        //Apply Rule 1
        List<String> allWordsOne = featureObj1.getRuleOne_allWords();
        List<String> allWordsTwo = featureObj2.getRuleOne_allWords();
        List<String> longerWords = allWordsOne.size() < allWordsTwo.size() ? allWordsTwo: allWordsOne;
        List<String> shortWords  = allWordsOne.size() >= allWordsTwo.size() ? allWordsTwo: allWordsOne;

        int matchCount = 0;
       // if(featureObj1.getRuleTwo_PronounType().equals("NONE") && featureObj2.getRuleTwo_PronounType().equals("NONE")) {
            for (int sIndex = 0; sIndex < shortWords.size(); sIndex++) {
                for (int lIndex = 0; lIndex < longerWords.size(); lIndex++) {
                    String longerWord = longerWords.get(lIndex).trim().toLowerCase();
                    String shortWord = shortWords.get(sIndex).trim().toLowerCase();

                    //rule for considering complete names
                    if(longerWord.equals("mr.") || shortWord.equals("mr.")
                    || longerWord.equals("mrs.") || shortWord.equals("mrs.")
                    || longerWord.equals("ms.") || shortWord.equals("ms.")
                    || longerWord.equals(",") || shortWord.equals(",")
                    || longerWord.equals(";") || shortWord.equals(";")
                    || longerWord.equals("'") || shortWord.equals("'")
                    || longerWord.equals("\"") || shortWord.equals("\"")
                    || longerWord.equals(" ") || shortWord.equals(" ")
                    || longerWord.equals("") || shortWord.equals("")
                    || longerWord.equals("'s") || shortWord.equals("'s")){

                        continue;
                    }

                    if (longerWord.equals(shortWord) && !isArticle(longerWord) && !isArticle(shortWord)) {
                        matchCount++;
                    }
                }
            }
        //}
        Double weight1 = this.weightMap.get("words");
        if(matchCount >= 1){
            weight1 = 0.0;
        }
        //Double weight1 = (double)((longerWords.size() - matchCount)/(double)longerWords.size()) * this.weightMap.get("words");
        //system.out.println("\nWeight 1 - Words");
        //system.out.println(weight1);

        //Apply Rule 2
        Double incompatibityFunction2 = 0.0;
        if(featureObj1.getRuleSeven_headNoun().equals(featureObj2.getRuleSeven_headNoun())){
            incompatibityFunction2 = 1.0;
        }
        Double weight2 = (incompatibityFunction2 * weightMap.get("headnoun"));
        //system.out.println("\nWeight 2 - Head Noun");
        //system.out.println(weight2);


        //Apply Rule 3
        Double maxDifference = (double)np1.getMaxDifference();
        Double posDifference = (double)Math.abs(featureObj1.getRuleEleven_position() - featureObj2.getRuleEleven_position());
        Double weight3 = ((posDifference/maxDifference) *  this.weightMap.get("position"));
        //system.out.println("\nWeight 3 - Position");
        //system.out.println(weight3);


        //Apply Rule 4
        Double incompatibityFunction4 = 0.0;
        if((!featureObj1.getRuleTwo_PronounType().equals("NONE")) && (featureObj2.getRuleTwo_PronounType().equals("NONE"))){
            incompatibityFunction4 = 1.0;
        }

        Double weight4 = (incompatibityFunction4 * this.weightMap.get("pronoun"));
        //system.out.println("\nWeight 4 - Pronoun");
        //system.out.println(weight4);


        //todo Apply Rule 5
        //todo modified rule
//        Double incompatibityFunction5 = 0.0;
//        if(featureObj2.getRuleThree_article().equals("INDEF") && featureObj2.getRuleFour_appositive().equals("NO")){
//            incompatibityFunction5 = 1.0;
//        }
//        Double weight5 = (incompatibityFunction5 * this.weightMap.get("article"));
        ////system.out.println("\nWeight 5 - Article");
        ////system.out.println(weight5);
        //this.listWeight.add(weight5);

        //Apply Rule 6
//        Double incompatibityFunction6 = 0.0;
//        if(np2.getStrNP().contains(np1.getStrNP())){
//            incompatibityFunction6 = 1.0;
//        }
//        Double weight6 = (incompatibityFunction6 * this.weightMap.get("words-substring"));
        //system.out.println("\nWeight 6 - Words Substring");
        //system.out.println(weight6);


        //todo Apply Rule 7
        //APPOSITIVE
        //Double incompatibityFunction7 = 0.0;
        //String appositive = featureObj1
//        Double weight7 = 0.0;
//        if(np1.isEndingWithComma()){
//            if((np2.getPos() - np1.getPos() == 2)){
//                if(np1.getFeaturesObj().getRuleThree_article() != "NONE"
//                    ||np2.getFeaturesObj().getRuleThree_article() != "NONE") {
//
//                    weight7 = Settings.getInstance().minValue;
//                }
//            }
//        }

        //Apply Rule 8
        //NUMBER - Singular or Plural
        Double incompatibityFunction8 = 0.0;
        if(!featureObj1.getRuleFive_number().equals(featureObj2.getRuleFive_number())){
            incompatibityFunction8 = 1.0;
        }
        Double weight8 = (incompatibityFunction8 * this.weightMap.get("number"));
        //system.out.println("\nWeight 8 - Number");
        //system.out.println(weight8);


        //Apply Rule 9
        //PROPER-NAME
        //todo check if mismatch on every word is correct
        Double incompatibityFunction9 = 0.0;
        if(featureObj1.isRuleSix_properName() && featureObj2.isRuleSix_properName() && !(np1.getStrNP().equals(np2.getStrNP()))){
            incompatibityFunction9 = 1.0;
        }
        Double weight9 = (incompatibityFunction9 * this.weightMap.get("proper-name"));
        //system.out.println("\nWeight 9 - Proper Name");
        //system.out.println(weight9);


        //Apply Rule 10
        //SEMANTIC CLASS
        Double incompatibityFunction10 = 0.0;
        if(featureObj1.getRuleTen_semanticClass().equals(featureObj2.getRuleTen_semanticClass())){
            incompatibityFunction10 = 1.0;
        }
        Double weight10 = (incompatibityFunction10 * this.weightMap.get("semantic-class"));
        //system.out.println("\nWeight 10 - Semantic Class");
        //system.out.println(weight10);


        //Apply Rule 11
        //GENDER
        //todo doubtful check calculations
        Double incompatibityFunction11 = 0.0;
        if( !featureObj1.getRuleEight_gender().equals(featureObj2.getRuleEight_gender()) ){
            incompatibityFunction11 = 1.0;
        }
        else if(featureObj1.getRuleEight_gender().equals("EITHER")){
            if(!(featureObj2.getRuleEight_gender().equals("MASC") || featureObj2.getRuleEight_gender().equals("FEM"))){
                incompatibityFunction11 = 1.0;
            }
        }
        else if (featureObj2.getRuleEight_gender().equals("EITHER")){
            if(!(featureObj1.getRuleEight_gender().equals("MASC") || featureObj1.getRuleEight_gender().equals("FEM"))){
                incompatibityFunction11 = 1.0;
            }
        }
        Double weight11 = (incompatibityFunction11 * this.weightMap.get("gender"));
        //system.out.println("\nWeight 11 - Gender");
        //system.out.println(weight11);


        //Apply Rule 12
        //ANIMACY
        Double incompatibityFunction12 = 0.0;
        if(!featureObj1.getRuleNine_animacy().equals(featureObj2.getRuleNine_animacy())){
            incompatibityFunction12 = 1.0;
        }
        Double weight12 = (incompatibityFunction12 * this.weightMap.get("animacy"));

        //system.out.println("\nWeight 12 - Animacy");
        //system.out.println(weight12);

        //system.out.println("------------");

        //System.out.println(" words match count = " + Math.round(weight1) + " : \t" + np1.getStrNP() + ",\t" + np2.getStrNP());
        this.listWeight.add(weight1);  //words
//        this.listWeight.add(weight2);  //headnoun
//        this.listWeight.add(weight3);  //position
//        this.listWeight.add(weight4);  //pronoun
//        this.listWeight.add(weight6);  //words substring
//        this.listWeight.add(weight7);  //appositive
//        this.listWeight.add(weight8);  //number
//        this.listWeight.add(weight9);  //proper name
//        this.listWeight.add(weight10); //semantic class
//        this.listWeight.add(weight11); //gender
//        this.listWeight.add(weight12); //animacy

        for(double weight: this.listWeight){
            //system.out.println(weight);
            distance += weight;
        }
        //System.out.println("DISTANCE" + (distance));
        return (distance);
        //return 10000;
    }


    public void setRadius(Double radius) {
        loadMap(radius);
    }
}
