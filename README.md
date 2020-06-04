# AirGuard
AirGuard is a real-world intrustion detection system based around the detection of Wi-Fi packets from mobile devices.

You can use this program to build up a list of all trusted device MAC addresses, and it can alert you (or run a script) if there's a device detected within a certain dBm signal strength.
AirGuard is very reliable out in urban areas where there isn't a whole lot of 2.4 GHz congestion, but you may want to consider severely limiting the dBm sensitivity if you live in an area where devices come and go all the time.

# Installation
You can either clone this repository and compile the code yourself (not recommended), or you can just download the latest .jar file (recommended) from [the releases page.](https://github.com/TheFuzzyFish/AirGuard/releases)

AirGuard's backend relies on [tshark](https://www.wireshark.org/docs/man-pages/tshark.html) for packet capture and [iwconfig](https://linux.die.net/man/8/iwconfig) for channel hopping, so make sure that these programs are installed and accessible from the command line.

When you pass the wireless interface to AirGuard, be sure that it's set to [monitor mode](https://en.wikipedia.org/wiki/Promiscuous_mode) so that it can see <i>all nearby devices</i>.

# Usage
<pre>
Usage:
    java -jar AirGuard.jar --interface [interface] --list [trusted_mac_list[ [options]

Examples:
    java -jar AirGuard.jar -i wlo1mon -l /var/knownMACs.txt
    java -jar AirGuard.jar -i wlo1mon -l /var/knownMACS.txt --script /usr/local/share/alertTheAdmin.py
    java -jar AirGuard.jar -i wlo1mon -l /var/knownMACS.txt --log /var/log/airguard.log --script /usr/local/share/scripts/alertTheAdmin.py
    java -jar AirGuard.jar -i wlo1mon -l /var/knownMACS.txt --dbm -30 -b

Options:
    -h, --help          Displays this handy dandy help file!
    -v, --version       Displays program version info
    -i, --interface []  The promiscuous interface to monitor (required)
    -l, --list []       The list of trusted device addresses to ignore (required)
    -b, --build         Switches the program into "builder" mode where it adds all nearby devices to a new
                        trusted MAC list, specified by --list
    -d, --dbm []        The signal strength threshold that devices must be within to be detected (default -60 dBm is usually fine)
    --log []            The file path to log alien devices
    --script []         The script to run when an alien device is detected. The unrecognized MAC is passed as an argument to the script
</pre>

<b>To start, be sure that you have [tshark](https://www.wireshark.org/docs/man-pages/tshark.html) and [iwconfig](https://linux.die.net/man/8/iwconfig) installed</b>. Once you're sure of that, and you're running under a user that has access to both of those (which may have to be root), set up a wireless interface under monitor mode.
To do that, you should really only need 3 commands on most Linux systems (assuming your interface is called <b>wlan0</b>):
<pre>
ifconfig <b>wlan0</b> down
iwconfig <b>wlan0</b> mode monitor
ifconfig <b>wlan0</b> up
</pre>

<h3>Building your device list</h3>
Now that you're all set up, you're going to want to build a list of all nearby devices so that AirGuard knows when a device doesn't belong. To do that, all you have to do is pass the interface name with --interface, the new list filename with --list, and the --build flag. It will begin adding every device that it sees.

You may want to consider severely narrowing the range with --dbm -30 or even narrower during this process to that you don't pick up any unintended signals.

<h3>Off to the races!</h3>
Now you can specify the list and wireless interface, and AirGuard will automatically begin printing unrecognized devices to the console. Additionally you can --log those to a file, or run a --script (whose argument will be the offending MAC address) whenever an unrecognized device is seen.

If a device is seen multiple times, alerts (this includes script executions and logging) will only be run if 60 seconds have passed since the last alert

# Scenarios
I built AirGuard because I live in an urban area. Not many people visit, and it's pretty radio quiet. AirGuard is meant to be a real-world intrusion detection system to be run on a small computer with a monitor-capable Wi-Fi dongle. If there is an intruder of any kind, they'll likely be carrying a Wi-Fi-capable mobile device, which will constantly be sending out probes and other noisy messages we can hear. AirGuard will see that traffic and, upon noting that it's not part of our trusted device list, take action. I have it hooked up to a script that looks up the OUI prefix and message my Discord alert server via [webhooks](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks).

At the end of the day, I get realtime alerts if there are any new/unwanted devices nearby to betray the presence of a new/unwanted guest, which can be very valuable.