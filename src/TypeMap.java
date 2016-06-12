import java.util.*;

public class TypeMap extends HashMap<Variable, Type> { 

// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.

  public void display () {
    System.out.print("{ ");
    String sep = "";
    for (Variable key : keySet() ) {
        System.out.print(sep + "<" + key + ", " + get(key) + ">");
        sep = ", ";
    }
    System.out.println(" }");
  }


}
