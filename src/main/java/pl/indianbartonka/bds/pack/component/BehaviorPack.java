package pl.indianbartonka.bds.pack.component;

public class BehaviorPack {

    private String name;
    private String pack_id;
    private String subpack;
    private int[] version;

    public BehaviorPack(final String name, final String pack_id, final String subpack, final int[] version) {
        this.name = name;
        this.pack_id = pack_id;
        this.subpack = subpack;
        this.version = version;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPackId() {
        return this.pack_id;
    }

    public void setPackId(final String pack_id) {
        this.pack_id = pack_id;
    }

    public String getSubpack() {
        return this.subpack;
    }

    public void setSubpack(final String subpack) {
        this.subpack = subpack;
    }

    public int[] getVersion() {
        return this.version;
    }

    public void setVersion(final int[] version) {
        this.version = version;
    }
}
