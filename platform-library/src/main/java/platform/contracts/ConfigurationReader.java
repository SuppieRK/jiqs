package platform.contracts;

/**
 * Defines common expectation for the functionality which reads properties from configuration.
 *
 * <p>The exact details about how the configuration is read are irrelevant.
 */
public interface ConfigurationReader {
  /**
   * Get a config for a path and a given class.
   *
   * @param path path to get the config for
   * @param clazz class to get the class for
   * @param <T> type of class to get
   * @return the configuration as a specified type
   */
  <T> T read(String path, Class<T> clazz);
}
