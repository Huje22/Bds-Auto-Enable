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
  
}
