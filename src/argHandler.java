import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

/**
 * Handles user arguments from the main() thread.
 * This should be used to parse user arguments and be accessed to retrieve the options they specified. You should also make sure that the first thing you do in main() is to check this for doExit()
 *
 * @author TheFuzzyFish
 */
public class argHandler {
    private boolean doExit;
    private String version;
    private boolean doLog;
    private String logPath;
    private boolean doScript;
    private String scriptPath;
    private String interfaceName;
    private String macList;
    private boolean doBuildMacList;
    private int dbm;

    /**
     * Constructs a new argHandler based on the argument array from main().
     *
     * @param args the arguments passed in from the command line through main()
     */
    public argHandler(String[] args) {
        doExit = true; // Flag is flipped per the arguments for whether or not main() should immediately exit based on context
        version = getAsset("version.txt"); // Not quite sure why I decided to do this, but I'm going to make argHandler in charge of the version number throughout the rest of the program
        this.dbm = -50;

        /* If there were no arguments supplied, print the help file */
        if (args.length == 0) {
            printAsset("help.txt");
            return;
        }

        /* Loop through the arguments and detect valid flags */
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                default:
                    System.out.println("Unrecognized flag \"" + args[i] + "\".");
                case "-h":
                case "-H":
                case "--help":
                    printAsset("help.txt");
                    break;
                case "-v":
                case "-V":
                case "--version":
                    System.out.println("AirGuard version " + version);
                    printAsset("legal.txt");
                    break;
                case "-i":
                case "--interface":
                    i++; // Move forward 1 argument
                    this.doExit = false;
                    this.interfaceName = args[i];
                    break;
                case "-l":
                case "--list":
                    i++; // Move forward 1 argument
                    /* Check if path is valid */
                    try {
                        Paths.get(args[i]);
                    } catch (InvalidPathException e) {
                        System.out.println("It appears that \"" + args[i] + "\" isn't a valid filesystem path. Please provide a path to a MAC address list.");
                        return;
                    }

                    this.doExit = false;
                    this.macList = args[i];
                    break;
                case "-b":
                case "--build":
                    this.doBuildMacList = true;
                    this.doExit = false;
                    break;
                case "-d":
                case "--dbm":
                    i++; // Move forward 1 argument
                    this.dbm = Integer.parseInt(args[i]);
                    break;
                case "--script":
                    i++; // Move forward 1 argument
                    /* Check if path is valid */
                    try {
                        if (!(new File(args[i]).exists())) {
                            throw new Exception();
                        }
                    } catch (Exception e) {
                        System.out.println("It appears that \"" + args[i] + "\" isn't a valid filesystem path. Please provide a script we can execute upon detection of untrusted device.");
                        this.doExit = true;
                        return;
                    }

                    this.doExit = false;
                    this.doScript = true;
                    this.scriptPath = new File(args[i]).getAbsolutePath();
                    break;
                case "--log":
                    i++; // Move forward 1 argument
                    /* Check if path is valid */
                    try {
                        Paths.get(args[i]);
                    } catch (Exception e) {
                        System.out.println("It appears that \"" + args[i] + "\" isn't a valid filesystem path. Please provide a path where we can write your log file.");
                        this.doExit = true;
                        return;
                    }

                    this.doExit = false;
                    this.doLog = true;
                    this.logPath = new File(args[i]).getAbsolutePath();
                    break;
            }
        }
    }

    /**
     * Determines from the given arguments whether or not the program should exit. Always check this first, and exit main() if required
     *
     * @return whether or not main() should immediately exit
     */
    public boolean doExit() {
        if (this.doBuildMacList && this.macList == null) {
            System.out.println("If you want to build a new trusted MAC list, you have to specify it with --list");
            return true;
        }
        if (this.getInterfaceName() == null) {
            System.out.println("You must specify the name of the promiscuous interface to monitor!");
            return true;
        }
        if (this.getMacList() == null) {
            System.out.println("You must specify the path to the list of known MAC addresses");
            return true;
        }

        return doExit;
    }

    /**
     * Returns the contents of version.txt
     *
     * @return the version number
     */
    public String getVersion() {
        return version;
    }

    /**
     * Prints out a file from inside the compiled JAR
     *
     * @param assetName the name of the file you wish to print
     */
    public void printAsset(String assetName) {
        InputStream strm = getClass().getResourceAsStream(assetName); // Open the file as a stream

        try (BufferedReader br = new BufferedReader(new InputStreamReader(strm))) { // Open the stream as a BufferedReader
            String line;

            while ((line = br.readLine()) != null) { // Print each line in the BufferedReader
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Hmmm this should never happen... It seems your JAR file may be corrupted. Try contacting the author");
        }
    }

    /**
     * Returns the first line in the specified file from the JAR
     * This method is most notably used to get the version number from /version.txt in the assets dir
     *
     * @param assetName the name of the file you wish to get the first line from
     * @return the first line of the file specified
     */
    public String getAsset(String assetName) {
        InputStream strm = getClass().getResourceAsStream(assetName); // Open the file as a stream

        try (BufferedReader br = new BufferedReader(new InputStreamReader(strm))) { // Open the stream as a BufferedReader
            return br.readLine(); // Returns the first line
        } catch (IOException e) {
            System.out.println("Hmmm this should never happen... It seems your JAR file may be corrupted. Try contacting the author");
            return "JAR file corruption detected. Dev made an oopsie";
        }
    }

    /**
     * Returns the log path if specified
     * @return the path to a log file
     */
    public String getLogPath() {
        return this.logPath;
    }

    /**
     * Returns the script path if specified
     * @return the script to execute on a matched MAC detection
     */
    public String getScriptPath() {
        return this.scriptPath;
    }

    /**
     * Returns whether or not you should be logging matches
     * @return whether or not to log matches
     */
    public boolean doLog() {
        return this.doLog;
    }

    /**
     * Returns whether or not you should be running scripts on matches
     * @return whether or not to run a script
     */
    public boolean doScript() {
        return this.doScript;
    }

    /**
     * Returns the interface name specified by the user
     * @return the interface name
     */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * Returns the path to the MAC address list file
     * @return the path to the MAC list file
     */
    public String getMacList() {
        return this.macList;
    }

    /**
     * Return whether or not a new MAC address list should be built
     * @return whether or not a new MAC list should be built
     */
    public boolean doBuildMacList() {
        return this.doBuildMacList;
    }

    public int getDbm() {
        return this.dbm;
    }
}
