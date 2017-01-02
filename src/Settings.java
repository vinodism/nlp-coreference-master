


public class Settings {
    private static Settings ourInstance = new Settings();

    public static Settings getInstance() {
        return ourInstance;
    }

    private Settings() {
    }

    Double radius               = 5.00;
    Double minValue             = -1000000.0;
    Double maxValue             = 1000000.0;
    Double weightWords          =  10.0;
    Double weightHeadnoun       =  1.0;
    Double weightPosition       =  5.0;
    Double weightPronoun        =  radius;
    Double weightArticle        =  radius;
    Double weightWordsSubstring =  minValue;
    Double weightAppositive     =  minValue;
    Double weightNumber         =  maxValue;
    Double weightProperName     =  maxValue;
    Double weightSemanticClass  =  maxValue;
    Double weightGender         =  maxValue;
    Double weightAnimacy        =  maxValue;

    //NP TYPES
    NPTYPE nptype = NPTYPE.BASE;


    boolean printcsv = false;

    //getters

    public Double getRadius() {
        return radius;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public Double getWeightWords() {
        return weightWords;
    }

    public Double getWeightHeadnoun() {
        return weightHeadnoun;
    }

    public Double getWeightPosition() {
        return weightPosition;
    }

    public Double getWeightPronoun() {
        return weightPronoun;
    }

    public Double getWeightArticle() {
        return weightArticle;
    }

    public Double getWeightWordsSubstring() {
        return weightWordsSubstring;
    }

    public Double getWeightAppositive() {
        return weightAppositive;
    }

    public Double getWeightNumber() {
        return weightNumber;
    }

    public Double getWeightProperName() {
        return weightProperName;
    }

    public Double getWeightSemanticClass() {
        return weightSemanticClass;
    }

    public Double getWeightGender() {
        return weightGender;
    }

    public Double getWeightAnimacy() {
        return weightAnimacy;
    }

    public NPTYPE getNptype() {
        return nptype;
    }
}
