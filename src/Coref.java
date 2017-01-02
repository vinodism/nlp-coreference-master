import java.io.*;
import org.w3c.dom.Document;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Coref {
    public static void main(String[] args) throws IOException, Exception {
        //this will read the parameters given in
        //in the command line

        String listFile = "scorer/listfiletst.txt";
        String resDir = "scorer/responses/";

        //use . to get current directory
        File fin = new File(listFile);
        FileInputStream fis = new FileInputStream(fin);

        //Construct BufferedReader from InputStreamReader
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        //open the response file
        PrintWriter writer = new PrintWriter("scorer/responselist.txt", "UTF-8");

        String line = null;
        while ((line = br.readLine()) != null)
        {
            //todo rmeove
            if(line.equals("EOF")){
                break;
            }

            String crfName = line;
            FileObj fobj = new FileObj(crfName);

            String responseFileName = new File(crfName).getName();
            int pos = responseFileName.lastIndexOf(".");
            if (pos > 0) {
                responseFileName =responseFileName.substring(0, pos) + ".response";
                saveOutput(fobj.getDocument(),  resDir + responseFileName);
                writer.println("responses/" + responseFileName);
            }
        }

        writer.close();
        br.close();
    }

    public static void saveOutput(Document doc, String filename) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File(filename));
            Source input = new DOMSource(doc);

            transformer.transform(input, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}