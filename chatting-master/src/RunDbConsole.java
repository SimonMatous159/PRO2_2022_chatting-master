import org.apache.derby.tools.ij;

import java.io.IOException;

public class RunDbConsole {
    public static void main (String[] args) { //Zapne derby tools a potom v konzoli můžu psát příkazy
        try {
            // connect 'jdbc:derby:ChatClientDb_skB;create=true'; //spustim a do console vlozim tento prikaz
            // pro pipojeni k DB; 3. spustim toto a vlozim tento prikaz => (select * from ChatMessages;)
            ij.main(args); // provoláme derbytools
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
