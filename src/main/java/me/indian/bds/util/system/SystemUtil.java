package me.indian.bds.util.system;

public class SystemUtil {

    private SystemUtil(){}

    public static SystemOS getSystem() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return SystemOS.WINDOWS;
        } else if (os.contains("nix") || os.contains("nux")) {
            return SystemOS.LINUX;
//        } else if (os.contains("mac")) {
//            return SystemOS.MAC;
        } else {
            return SystemOS.UNSUPPORTED;
        }
    }

    public static String getFullyOsName() {
        return System.getProperty("os.name");
    }

  public static void clearSystemCache(){
switch(getSystem()){
    case LINUX ->{
//sync && echo 3 > /proc/sys/vm/drop_caches
    }
    case WINDOWS -> {
// echo 1 > %SystemRoot%\System32\\DriverStore\EnforcementCache\EnforcementClientAggregateCache.dat
    }
  }

      public static long getRamUsageByPid(final long pid)  throws IOException , UnSupportedSystemException {
            switch (SystemOS.getSystem()) {
              case WINDOWS -> getMemoryUsageWindows(pid);
                case LINUX -> getMemoryUsageLinux(pid);
                case UNSUPORTED -> throw new UnSupportedSystemException("Nie można pozyskać ilość ram z nie wspieranego systemu");
                
            }
        } 
      }

      private static long getMemoryUsageWindows(final long pid) throws IOException {
        final Process process = Runtime.getRuntime().exec("tasklist /NH /FI \"PID eq " + pid + "\"");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".exe")) {
                    final String[] tokens = line.split("\\s+");
                    if (tokens.length > 4) {
                        final String memoryStr = tokens[4].replaceAll("\\D", "");
                        return Long.parseLong(memoryStr);
                    }
                }
            }
        }
        return -1;
    }

    private static long getMemoryUsageLinux(final long pid) throws IOException {
        final Process process = Runtime.getRuntime().exec("ps -p " + pid + " -o rss=");
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            final String line = reader.readLine();
            return line != null ? Long.parseLong(line) : -1;
        }
    }
}
