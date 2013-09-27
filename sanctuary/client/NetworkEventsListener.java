/**
 *
 * @author Thunder
 */
package sanctuary.client;

public interface NetworkEventsListener {

    public void loginCompleted();

    public void abortCompleted();

    public void updateCompleted(boolean successful);
}
