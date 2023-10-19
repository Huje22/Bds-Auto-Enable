package me.indian.bds.manager.server;

public class ServerStats{

private final long totalUpTime;

  public ServerStats(final long totalUpTime)(
   this.totalUpTime = totalUpTime;
}

public void addOnlineTime(final long time){
this.totalUpTime += time;
}
}
