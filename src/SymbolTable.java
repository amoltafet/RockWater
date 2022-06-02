/*
 * File: SymbolTable.java
 * Date: Spring 2022
 * Name: S. Bowers
 * Desc: SymbolTable implementation for HW-5
 */

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;


public class SymbolTable {

  // the table is a list of environments, where an environment is a
  // mapping (bindings) from strings to objects
  List<Map<String,String>> environments = new ArrayList<>();


  // adds an environment to the table (which acts as the "current"
  // environment)
  public void pushEnvironment() {
    HashMap<String,String> newEnvironment = new HashMap<>();
    environments.add(newEnvironment);
  }

  // removes the last pushed environment
  public void popEnvironment() {
    if (size() > 0)
      environments.remove(environments.size() - 1);
  }

  // add binding to current environment
  public void add(String name, String info) {
    if (size() > 0)
      environments.get(size() - 1).put(name, info);
  }

  // returns first binding for name from the table
  public String get(String name) {
    for (int i = size() - 1; i >= 0; --i) {
      Map<String,String> env = environments.get(i);
      if (env.containsKey(name))
        return env.get(name);
    }
    return null;
  }

  // true if given name is in any environments
  public boolean nameExists(String name) {
    for (int i = size() - 1; i >= 0; --i) 
      if (environments.get(i).containsKey(name))
        return true;
    return false;
  }

  // true if the given name is in the most recently pushed environment
  public boolean nameExistsInCurrEnv(String name) {
    if (size() > 0)
      return environments.get(size() - 1).containsKey(name);
    return false;
  }

  // returns number of environments
  public int size() {
    return environments.size();
  }

  // output the table for testing
  @Override
  public String toString() {
    String s = "";
    String r = "";
    for (int i = 0; i < size(); ++i) {
      Map<String,String> env = environments.get(i);
      s += r + i + ":\n" + r + env + "\n";
      r += " ";
    }
    return s;
  }
  
}
