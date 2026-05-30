package blockchainServer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class readJSON {

    public readJSON() {
        // JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("userlogs.json")) {
            // Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray blockList = (JSONArray) obj;
            System.out.println(blockList);

            blockList.forEach(blockobject -> parseLogObject((JSONObject) blockobject));

            if (!readblockreq.blockchain.isEmpty())
                readblockreq.previousHash = readblockreq.blockchain.get(readblockreq.blockchain.size() - 1).hash;

            readblockreq.verifyChain();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private static void parseLogObject(JSONObject blockobject) {

        JSONObject blockdetails = (JSONObject) blockobject.get("block");

        int aid = Integer.parseInt(blockdetails.get("aid").toString());
        String problem = (String) blockdetails.get("problem");
        String test = (String) blockdetails.get("test");
        String treport = (String) blockdetails.get("treport");
        String report = (String) blockdetails.get("report");
        String prev = (String) blockdetails.get("previoushash");
        long time = (long) blockdetails.get("timestamp");
        String hash = (String) blockdetails.get("hash");

        int nonce = 0;
        Object nonceObj = blockdetails.get("nonce");
        if (nonceObj != null)
            nonce = Integer.parseInt(nonceObj.toString());

        Block b = new Block();

        b.aid = aid;
        b.problem = problem;
        b.test = test;
        b.treport = treport;
        b.report = report;
        b.timeStamp = time;
        b.previousHash = prev;
        b.nonce = nonce;
        b.hash = hash;

        readblockreq.blockchain.add(b);

    }
}
