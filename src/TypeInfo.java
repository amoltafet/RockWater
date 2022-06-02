/*
 * File: TypeInfo.java
 * Date: Spring 2022
 * Auth: S. Bowers
 * Desc: Helper class for holding user-defined (record) type and
 *       function signature information generated from the static
 *       analyzer and used by the (intermediate) code generator.
 */

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class TypeInfo {

  // a type has a name and a set of component name-type pairs
  private Map<String,Map<String,String>> types = new HashMap<>();

  /**
   * Returns the current set of type names being stored.
   */
  public Set<String> types() {
    return types.keySet();
  }

  /**
   * Returns the component names for the give type in the order they
   * were added.
   * @param type the type whose component names are returned
   */
  public Set<String> components(String type) {
    return types.get(type).keySet();
  }

  /**
   * Adds a type. If the name of the type already exists, do nothing.
   * @param type the name of the new type being added
   */
  public void add(String type) {
    // linked hash map maintains order of components (useful for function types)
    if (!types.containsKey(type))
      types.put(type, new LinkedHashMap<>());
  }

  /**
   * Adds a component to the given type. Components are returned in the
   * order they are added.
   * @param type the type name that the component is being added to
   * @param componentName the name of the component to add
   * @param componentType the type of the component to add
   */
  public void add(String type, String componentName, String componentType) {
    types.get(type).put(componentName, componentType);
  }

  /**s
   * Returns the component type for the given component.
   * @param type the name of the type that contains the component
   * @param componentName the name of the type component
   * @return the type of the given component
   */
  public String get(String type, String componentName) {
    if (types.containsKey(type))
      return types.get(type).get(componentName);
    return null;
  }
      
}
