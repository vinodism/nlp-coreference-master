import java.util.*;


public class ClusteringAlgorithm {
//    private static ClusteringAlgorithm ourInstance = new ClusteringAlgorithm();
//
//    public static ClusteringAlgorithm getInstance() {
//        return ourInstance;
//    }
    List<List<NPObj>>  listOfListOfNPObjs;

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

    public ClusteringAlgorithm() {
        this.listOfListOfNPObjs = new ArrayList<List<NPObj>>();
    }

    List<NPObj> globalListNPObjs;

    public void loadData(List<NPObj> listNPObjs){

        this.globalListNPObjs = listNPObjs;

        Collections.sort(listNPObjs, new Comparator<NPObj>() {
            public int compare(NPObj np1, NPObj np2) {
                return  np1.getPos() - np2.getPos();
            }
        });

        Distance distanceObj = Distance.getInstance();
        distanceObj.setNPList(listNPObjs);

        for(NPObj npObj: listNPObjs){

//            if( npObj.getREF().equals("") ) {
                List<NPObj> listOfNPObj = new ArrayList<NPObj>();
                listOfNPObj.add(npObj);
                this.listOfListOfNPObjs.add(listOfNPObj);
//            }
        }
    }


    public boolean allNPCompatible(List<NPObj> Ci,List<NPObj> Cj){
        Distance distanceObj = Distance.getInstance();
        Double maxValue = Settings.getInstance().getMaxValue();
        for(NPObj NPa: Cj){
            for(NPObj NPb: Ci){
                if(distanceObj.calculate(NPa,NPb) >= 5){
                    return false;
                }
            }
        }
        return true;
    }
    public List<List<NPObj>>  run(double radius){
        Distance distanceObj = Distance.getInstance();
        distanceObj.setRadius(radius);

        int size = this.listOfListOfNPObjs.size();
        for(int j = size-1 ; j >= 0; j--){
            for(int i = j - 1; i >= 0; i--){

                List<NPObj> Cj = this.listOfListOfNPObjs.get(j);
                NPObj NPj = Cj.get(0);

                List<NPObj> Ci = this.listOfListOfNPObjs.get(i);
                NPObj NPi = Ci.get(0);

                Double distance = distanceObj.calculate(NPi,NPj);
                if(distance < radius && allNPCompatible(Cj, Ci)){
                    Cj = union(Ci,Cj);
                    this.listOfListOfNPObjs.set(j,Cj);
                }
            }
        }


        if(!Settings.getInstance().printcsv) {
            //clear
            int index = 0;
            for (List<NPObj> listNPObjs : this.listOfListOfNPObjs) {
                index++;
                if (listNPObjs.size() > 1) {
                    for (NPObj npObj : listNPObjs) {
                        System.out.println(npObj.getStrNP() + " \t\t: " + npObj.getID());
                    }
                    System.out.println("---------------------------------------------------------------------");
                }

            }
        }


//        // add all the element which are defined for clustering
//        for(NPObj npObj: this.globalListNPObjs){
//
//            if( !npObj.getREF().equals("") ) {
//                List<NPObj> listOfNPObj = new ArrayList<NPObj>();
//                listOfNPObj.add(npObj);
//                this.listOfListOfNPObjs.add(listOfNPObj);
//            }
//        }

        return this.listOfListOfNPObjs;
    }
}
