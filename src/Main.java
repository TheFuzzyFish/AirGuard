import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
        argHandler argHandler = new argHandler(args);
        if (argHandler.doExit()) return;

        if (!sanity()) {
            System.out.println("Sanity checks failed. Please be sure that both tshark and iwconfig are installed and the running user has access to them");
            return;
        }

        Process watchdog = Runtime.getRuntime().exec(new String[]{"tshark", "-Y", "wlan_radio.signal_dbm >= " + argHandler.getDbm(), "-I", "-n", "-l", "-i", argHandler.getInterfaceName()});
        if (watchdog.isAlive()) {
            System.out.println("Started wireless listener");
        } else {
            System.out.println("Error starting wireless listener. Do you have tshark installed? Is your wireless interface working?");
        }

        channelHopper hopper = new channelHopper(argHandler.getInterfaceName());
        hopper.start();
        if (!hopper.isKilled()) {
            System.out.println("Started channel hopping 2.4 GHz");
        } else {
            System.out.println("Error channel hopping. Can this user execute iwconfig?");
        }

        BufferedReader read = new BufferedReader(new InputStreamReader(watchdog.getInputStream()));

        macList macList = new macList();
        macList.init(argHandler.getMacList());

        FileWriter logWriter = null;
        if (argHandler.getLogPath() != null) {
            logWriter = new FileWriter(new File(argHandler.getLogPath()), true);
        }

        while (watchdog.isAlive()) {
        	String line = read.readLine();
            Matcher matcher = Pattern.compile("..:..:..:..:..:..").matcher(line);
            ArrayList<String> matches = new ArrayList<>();

            while (matcher.find()) {
                matches.add(matcher.group());
            }

            for (int i = 0; i < matches.size(); i++) {
                if (!matches.get(i).toLowerCase().equals("ff:ff:ff:ff:ff:ff") && !macList.doesMatch(matches.get(i))) {
                    String output = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss").format(LocalDateTime.now());
                    if (argHandler.doBuildMacList()) {
                        output = output.concat("\tAdding unknown device " + matches.get(i) + " to trusted MAC list.");
                        try {
                            macList.addMAC(matches.get(i));
                        } catch (Exception e) {
                            System.out.println("Error writing to MAC address list!");
                        }
                    } else {
                        output = output.concat("\tDetected unknown device " + matches.get(i));
                    }

                    if (macList.doNotify(matches.get(i))) {
                        if (argHandler.doLog()) {
                            try {
                                logWriter.write(output + "\n");
                                logWriter.flush();
                            } catch (IOException e) {
                                System.out.println("Error writing to log!");
                            }
                        }

                        if (argHandler.doScript() && !argHandler.doBuildMacList()) {
                            try {
                                Runtime.getRuntime().exec(new String[]{argHandler.getScriptPath(), matches.get(i)});
                            } catch (IOException e) {
                                System.out.println("Error running script!");
                            }
                        }

                        macList.markNotified(matches.get(i));
                        System.out.println(output);
                    }

                    break;
                }
            }
        }

        System.out.println("Tshark exited unexpectedly, closing...");
    }

    public static boolean sanity() {
        try {
            boolean tsharkOkay = false;
            boolean iwconfigOkay = false;

            Process tshark = Runtime.getRuntime().exec(new String[]{"tshark", "-v"});
            if (tshark.waitFor(2, TimeUnit.SECONDS)) {
                tsharkOkay = tshark.exitValue() == 0;
            } else {
                System.out.println("Tshark call timed out... Has the system crashed?");
                return false;
            }

            Process iwconfig = Runtime.getRuntime().exec(new String[]{"iwconfig", "-v"});
            if (iwconfig.waitFor(2, TimeUnit.SECONDS)) {
                iwconfigOkay = iwconfig.exitValue() == 0;
            } else {
                System.out.println("iwconfig call timed out... Has the system crashed?");
                return false;
            }

            return tsharkOkay && iwconfigOkay;
        } catch (Exception e) {
            return false;
        }
    }
}
