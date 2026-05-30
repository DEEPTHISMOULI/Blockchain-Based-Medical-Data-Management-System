package blockchainServer;

import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Lenovo
 */
public class readblockreq extends Thread {
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 5;
    public static String previousHash = "0";

    readblockreq() {
        super();
        start();
    }

    public void run() {
        try {
            ServerSocket ss = new ServerSocket(3000);

            while (true) {
                Socket soc = ss.accept();
                ObjectOutputStream oos = new ObjectOutputStream(soc.getOutputStream());
                ObjectInputStream oin = new ObjectInputStream(soc.getInputStream());

                String req = (String) oin.readObject();

                if (req.equals("ADDBLOCK")) {
                    int aid = (Integer) oin.readObject();
                    String problem = (String) oin.readObject();
                    String test = (String) oin.readObject();
                    String treport = (String) oin.readObject();
                    String report = (String) oin.readObject();

                    blockcserver.jTextArea1.append("Doctor report recieved for Appointment " + aid + "\n");

                    Block b = new Block(aid, problem, test, treport, report, previousHash);
                    blockchain.add(b);
                    previousHash = b.hash;
                    blockcserver.jTextArea1.append("Block Successfully added!\n");
                    blockchain.get(blockchain.size() - 1).mineBlock(difficulty);

                    oos.writeObject("SUCCESS");
                } else if (req.equals("GETTESTREPORT")) {
                    int aid = (Integer) oin.readObject();
                    blockcserver.jTextArea1.append("Searching details for Appointment " + aid + "!\n");

                    Block b = getLatestBlock(aid);
                    if (b != null) {
                        oos.writeObject("FOUND");
                        oos.writeObject(b.test);
                        oos.writeObject(b.treport);
                        oos.writeObject(b.report);
                    } else
                        oos.writeObject("NOTFOUND");
                } else if (req.equals("GETMYREPORT")) {
                    int aid = (Integer) oin.readObject();
                    blockcserver.jTextArea1.append("Searching details for Appointment " + aid + "!\n");

                    Block b = getLatestBlock(aid);
                    if (b != null) {
                        oos.writeObject("FOUND");
                        oos.writeObject(b.problem);
                        oos.writeObject(b.test);
                        oos.writeObject(b.treport);
                        oos.writeObject(b.report);
                    } else
                        oos.writeObject("NOTFOUND");
                } else if (req.equals("GETTEST")) {
                    int aid = (Integer) oin.readObject();
                    blockcserver.jTextArea1.append("Searching details for Appointment " + aid + "!\n");

                    Block b = getLatestBlock(aid);
                    if (b != null) {
                        oos.writeObject("FOUND");
                        oos.writeObject(b.test);
                        oos.writeObject(b.treport);
                        blockcserver.jTextArea1.append("Test details sent to lab !\n");
                    } else
                        oos.writeObject("NOTFOUND");
                } else if (req.equals("UPDATETESTREPORT")) {
                    int aid = (Integer) oin.readObject();
                    String treport = (String) oin.readObject();
                    blockcserver.jTextArea1.append("updating test report " + aid + " !\n");
                    String reply = updatetestreport(aid, treport);
                    oos.writeObject(reply);
                } else if (req.equals("UPDATEDOCTORREPORT")) {
                    int aid = (Integer) oin.readObject();
                    String report = (String) oin.readObject();
                    blockcserver.jTextArea1.append("updating Doctor report for " + aid + " !\n");
                    String reply = updatedoctorreport(aid, report);
                    oos.writeObject(reply);
                }

                oos.close();
                oin.close();
                soc.close();

            }

        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    Block getLatestBlock(int aid) {
        Block latest = null;
        for (int i = 0; i < blockchain.size(); i++) {
            Block b = blockchain.get(i);
            if (b.aid == aid)
                latest = b;
        }
        return latest;
    }

    String updatetestreport(int aid, String treport) {
        String reply = "NOTFOUND";
        try {
            Block latest = getLatestBlock(aid);
            if (latest != null) {
                Block b = new Block(aid, latest.problem, latest.test, treport, latest.report, previousHash);
                blockchain.add(b);
                previousHash = b.hash;
                blockcserver.jTextArea1.append("Appending updated test report as a new block for " + aid + "\n");
                blockchain.get(blockchain.size() - 1).mineBlock(difficulty);
                reply = "SUCCESS";
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return reply;
    }

    String updatedoctorreport(int aid, String report) {
        String reply = "NOTFOUND";
        try {
            Block latest = getLatestBlock(aid);
            if (latest != null) {
                Block b = new Block(aid, latest.problem, latest.test, latest.treport, report, previousHash);
                blockchain.add(b);
                previousHash = b.hash;
                blockcserver.jTextArea1.append("Appending updated doctor report as a new block for " + aid + "\n");
                blockchain.get(blockchain.size() - 1).mineBlock(difficulty);
                reply = "SUCCESS";
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return reply;
    }

    public static void verifyChain() {
        try {
            int n = blockchain.size();
            String target = new String(new char[difficulty]).replace('\0', '0');
            for (int i = 0; i < n; i++) {
                Block b = blockchain.get(i);

                if (!b.calculateHash().equals(b.hash)) {
                    log("Verification FAILED at block " + i + " (aid " + b.aid + "): content altered - hash mismatch.");
                    return;
                }

                if (b.hash.length() < difficulty || !b.hash.substring(0, difficulty).equals(target)) {
                    log("Verification FAILED at block " + i + " (aid " + b.aid + "): Proof-of-Work prefix invalid.");
                    return;
                }

                if (i > 0 && !b.previousHash.equals(blockchain.get(i - 1).hash)) {
                    log("Verification FAILED at block " + i + " (aid " + b.aid + "): broken hash link.");
                    return;
                }

                if (i > 0 && b.timeStamp < blockchain.get(i - 1).timeStamp) {
                    log("Verification FAILED at block " + i + " (aid " + b.aid
                            + "): timestamp not monotonic - possible back-dating.");
                    return;
                }
            }
            log("Chain verification PASSED: " + n + " block(s) intact.");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    static void log(String msg) {
        System.out.println(msg);
        try {
            if (blockcserver.jTextArea1 != null)
                blockcserver.jTextArea1.append(msg + "\n");
        } catch (Exception e) {
        }
    }

    void writelogs() {
        try {
            if (blockchain.size() > 0) {
                JSONArray blockList = new JSONArray();

                for (int i = 0; i < blockchain.size(); i++) {
                    Block b = (Block) blockchain.get(i);
                    JSONObject blockdetails = new JSONObject();

                    blockdetails.put("aid", b.aid);
                    blockdetails.put("problem", b.problem);
                    blockdetails.put("test", b.test);
                    blockdetails.put("treport", b.treport);
                    blockdetails.put("report", b.report);
                    blockdetails.put("previoushash", b.previousHash);
                    blockdetails.put("timestamp", b.timeStamp);
                    blockdetails.put("nonce", b.nonce); // FIX: persist nonce for full PoW re-verification
                    blockdetails.put("hash", b.hash);

                    JSONObject blockObject = new JSONObject();
                    blockObject.put("block", blockdetails);

                    blockList.add(blockObject);
                }

                FileWriter file = new FileWriter("userlogs.json");
                file.write(blockList.toJSONString());
                file.flush();
                file.close();

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
