import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class macList {
    private String listFileName;
    private ArrayList<String> authorizedList;
    private HashMap<String, Long> lastNotified;

    public macList() {
        this.authorizedList = new ArrayList<String>();

        this.lastNotified = new HashMap<String, Long>();
    }

    public void init(String filename) throws Exception {
        this.listFileName = filename;

        File listFile = new File(filename);
        if (!listFile.exists()) {
            new FileWriter(listFile).write("");
        }
        Scanner fileScanner = new Scanner(listFile);

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();

            if (!this.doesMatch(line)) {
                this.authorizedList.add(line);
            }
        }

        fileScanner.close();
    }

    public void addMAC(String MAC) throws Exception {
        if (this.doesMatch(MAC)) {
            return;
        }

        this.authorizedList.add(MAC);

        if (listFileName != null) {
            FileWriter write = new FileWriter(new File(listFileName), true);
            write.write(MAC + "\n");
            write.close();
        }
    }

    public boolean doesMatch(String MAC) {
        for (int i = 0; i < this.authorizedList.size(); i++) {
            if (this.authorizedList.get(i).equals(MAC)) {
                return true;
            }
        }

        return false;
    }

    public void markNotified(String MAC) {
        lastNotified.put(MAC, Instant.now().getEpochSecond());
    }

    public boolean doNotify(String MAC) {
        if (lastNotified.get(MAC) != null) {
            return Instant.now().getEpochSecond() - lastNotified.get(MAC) >= 60;
        } else {
            return true;
        }
    }
}
