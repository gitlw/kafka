package integration.kafka.server

import kafka.cluster.BrokerEndPoint
import kafka.server.{AddPartitions, AsyncFetcherLagStats, AsyncFetcherStats, FetcherEvent, FetcherEventBus, FetcherEventManager, FetcherEventProcessor, GetPartitionCount, RemovePartitions, TruncateAndFetch}
import org.apache.kafka.common.utils.Time
import org.easymock.EasyMock.{anyObject, createMock, expect, replay, verify}
import org.junit.Assert.assertEquals
import org.junit.Test

class FetcherEventManagerTest {

  @Test
  def testInitialState(): Unit = {
    val time = Time.SYSTEM
    val fetcherEventBus: FetcherEventBus = createMock(classOf[FetcherEventBus])
    expect(fetcherEventBus.put(TruncateAndFetch)).andReturn(null)
    replay(fetcherEventBus)

    val processor : FetcherEventProcessor = createMock(classOf[FetcherEventProcessor])
    val fetcherEventManager = new FetcherEventManager("thread-1", fetcherEventBus, processor, time)

    fetcherEventManager.start()
    fetcherEventManager.close()

    verify(fetcherEventBus)
  }

  @Test
  def testEventExecution(): Unit = {
    val time = Time.SYSTEM
    val fetcherEventBus = new FetcherEventBus(time)

    @volatile var addPartitiosProcessed = 0
    @volatile var removePartitionsProcessed = 0
    @volatile var getPartitionsProcessed = 0
    @volatile var truncateAndFetchProcessed = 0
    val processor : FetcherEventProcessor = new FetcherEventProcessor {
      override def process(event: FetcherEvent): Unit = {
        event match {
          case AddPartitions(initialFetchStates, future) =>
            addPartitiosProcessed += 1
            future.complete(null)
          case RemovePartitions(topicPartitions, future) =>
            removePartitionsProcessed += 1
            future.complete(null)
          case GetPartitionCount(future) =>
            getPartitionsProcessed += 1
            future.complete(1)
          case TruncateAndFetch =>
            truncateAndFetchProcessed += 1
        }

      }

      override def fetcherStats: AsyncFetcherStats = ???

      override def fetcherLagStats: AsyncFetcherLagStats = ???

      override def sourceBroker: BrokerEndPoint = ???

      override def close(): Unit = {}
    }

    val fetcherEventManager = new FetcherEventManager("thread-1", fetcherEventBus, processor, time)
    val addPartitionsFuture = fetcherEventManager.addPartitions(Map.empty)
    val removePartitionsFuture = fetcherEventManager.removePartitions(Set.empty)
    val getPartitionCountFuture = fetcherEventManager.getPartitionsCount()

    fetcherEventManager.start()
    addPartitionsFuture.get()
    removePartitionsFuture.get()
    getPartitionCountFuture.get()

    assertEquals(1, addPartitiosProcessed)
    assertEquals(1, removePartitionsProcessed)
    assertEquals(1, getPartitionsProcessed)
    fetcherEventManager.close()


  }

}

