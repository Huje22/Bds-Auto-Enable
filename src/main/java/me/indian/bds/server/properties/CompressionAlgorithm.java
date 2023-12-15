package me.indian.bds.server.properties;

public enum CompressionAlgorithm {

  ZLIB("zlib"),
  SNAPPY("snappy");

  private final String algorithmName;

  CompressionAlgorithm(final String algorithmName) {
    this.algorithmName = algorithmName;
  }

  public String getAlgorithmName() {
    return this.algorithmName;
  }
}