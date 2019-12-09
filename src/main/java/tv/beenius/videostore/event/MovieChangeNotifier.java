package tv.beenius.videostore.event;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;

/**
 * Notifies movie list facelet(s) about a movie list change via WebSocket push mechanism.
 * Notification is triggered by Java Event mechanism. 
 */
@RequestScoped
public class MovieChangeNotifier {

  @SuppressWarnings("cdi-ambiguous-dependency")
  @Inject 
  @Push (channel = "movieChannel")
  private PushContext movieChannel;
  
  /**
   * Sends an update notification to WebSocket as defined in facelet.
   * Method is triggered by Observer after event delivering transaction is completed.
   * 
   * @param event Movie event.
   */
  public void onMovieEvent(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) 
      @MovieEvent 
      final String event) {
    movieChannel.send("movieListNotification");
  }

}
