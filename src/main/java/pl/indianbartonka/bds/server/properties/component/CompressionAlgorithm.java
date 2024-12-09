package pl.indianbartonka.bds.server.properties.component;

public enum CompressionAlgorithm {

    ZLIB("zlib"),
    SNAPPY("snappy");

    private final String algorithmName;

    CompressionAlgorithm(final String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public static CompressionAlgorithm getByName(final String algorithmName) {
        return switch (algorithmName.toLowerCase()) {
            case "zlib" -> ZLIB;
            case "snappy" -> SNAPPY;
            default -> throw new IllegalArgumentException("Unknown compression algorithm: " + algorithmName);
        };
    }

    public String getAlgorithmName() {
        return this.algorithmName;
    }
}