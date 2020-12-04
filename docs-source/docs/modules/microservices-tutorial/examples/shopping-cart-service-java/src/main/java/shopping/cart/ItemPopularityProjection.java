// tag::projection[]
package shopping.cart;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.Offset;
import akka.projection.ProjectionBehavior;
import akka.projection.ProjectionId;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.ExactlyOnceProjection;
import akka.projection.javadsl.SourceProvider;
import akka.projection.jdbc.javadsl.JdbcProjection;
import java.util.Optional;
import javax.persistence.EntityManagerFactory;

public final class ItemPopularityProjection {

  private ItemPopularityProjection() {}

  // tag::howto-read-side-without-role[]
  public static void init(
      ActorSystem<?> system,
      EntityManagerFactory entityManagerFactory,
      ItemPopularityRepository repository) {
    // FIXME remove
    JdbcProjection.createOffsetTableIfNotExists(
        () -> new HibernateJdbcSession(entityManagerFactory.createEntityManager()), system);

    ShardedDaemonProcess.get(system)
        .init( // <1>
            ProjectionBehavior.Command.class,
            "ItemPopularityProjection",
            ShoppingCart.TAGS.size(),
            index ->
                ProjectionBehavior.create(
                    createProjectionFor(system, entityManagerFactory, repository, index)),
            ShardedDaemonProcessSettings.create(system),
            Optional.of(ProjectionBehavior.stopMessage()));
  }
  // end::howto-read-side-without-role[]

  private static ExactlyOnceProjection<Offset, EventEnvelope<ShoppingCart.Event>>
      createProjectionFor(
          ActorSystem<?> system,
          EntityManagerFactory entityManagerFactory,
          ItemPopularityRepository repository,
          int index) {
    String tag = ShoppingCart.TAGS.get(index); // <2>

    SourceProvider<Offset, EventEnvelope<ShoppingCart.Event>> sourceProvider = // <3>
        EventSourcedProvider.eventsByTag(
            system,
            CassandraReadJournal.Identifier(), // <4>
            tag);

    return JdbcProjection.exactlyOnce( // <5>
        ProjectionId.of("ItemPopularityProjection", tag),
        sourceProvider,
        () -> new HibernateJdbcSession(entityManagerFactory.createEntityManager()),
        () -> new ItemPopularityProjectionHandler(tag, repository), // <6>
        system);
  }
}
// end::projection[]
