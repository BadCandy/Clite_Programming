import java.util.*;

public class State extends HashMap<Variable, Value> { 
    // Defines the set of variables and their associated values 
    // that are active during interpretation
    
    public State( ) { }
    
    public State(Variable key, Value val) {
        put(key, val);
    }
    
    public State onion(Variable key, Value val) {
        put(key, val);
        return this;
    }
    
    public State onion (State t) {
        for (Variable key : t.keySet( ))
            put(key, t.get(key));
        return this;
    }

    public void display( ) {
        System.out.print("{ ");
        String sep = "";
        for (Variable key : keySet( )) {
            System.out.print(sep + "<" + key + ", " + get(key) + ">");
            sep = ", ";
        }
        System.out.println(" }");
    }

}
