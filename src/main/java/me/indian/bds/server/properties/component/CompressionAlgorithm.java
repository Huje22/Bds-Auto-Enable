package me.indian.bds.server.properties.component;

public enum CompressionAlgorithm {

    ZLIB("zlib"),
    SNAPPY("snappy");

    private final String algorithmName;

    CompressionAlgorithm(final String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public static CompressionAlgorithm getByName(final String algorithmName) throws NullPointerException {
        return switch (algorithmName.toLowerCase()) {
            case "zlib" -> CompressionAlgorithm.ZLIB;
            case "snappy" -> CompressionAlgorithm.SNAPPY;
            default -> throw new NullPointerException();
        };
    }

    public String getAlgorithmName() {
        return this.algorithmName;
    }
}