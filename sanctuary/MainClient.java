/**
 *
 * @author Thunder
 */
package sanctuary;

import sanctuary.client.ApplicationController;

public class MainClient {

    public static void main(String[] args) {
        if (args.length == 0) {
            ApplicationController controller = new ApplicationController();
            controller.start();
        } else {
            System.out.println("You don't need arguments !!");
        }
    }
}
