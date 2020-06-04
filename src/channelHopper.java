public class channelHopper extends Thread {
    private String interfaceName;
    private boolean kill;

    public channelHopper(String interfaceName) {
        this.interfaceName = interfaceName;
        this.kill = false;
    }

    public void run() {
        while (!kill) {
            for (int channel = 0; channel <= 11; channel++) {
                if (kill) {
                    break;
                }

                try {
                    Process p = Runtime.getRuntime().exec("iwconfig " + this.interfaceName + " channel " + channel);
                    p.waitFor();
                    Thread.sleep(250);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.kill = true;
                    break;
                }
            }
        }
    }

    public void kill() {
        this.kill = true;
    }

    public boolean isKilled() {
        return this.kill;
    }
}
