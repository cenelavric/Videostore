package tv.beenius.videostore.event;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;

/**
 * Notifies actor list facelet(s) about a actor list change via WebSocket push mechanism.
 * Notification is triggered by Java Event mechanism. 
 */
@RequestScoped
public class ActorChangeNotifier {

  @SuppressWarnings("cdi-ambiguous-dependency")
  @Inject 
  @Push (channel = "actorChannel")
  private PushContext actorChannel;
  
  /**
   * Sends an update notification to WebSocket as defined in facelet.
   * Method is triggered by Observer after event delivering transaction is completed. 
   * 
   * @param event Actor event.
   */
  public void onActorEvent(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) 
      @ActorEvent 
      final String event) {
    actorChannel.send("actorListNotification");
  }

}
