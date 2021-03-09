package kafka.server

import kafka.utils.CoreUtils.inLock
import org.apache.kafka.common.utils.Time

import java.util.PriorityQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import scala.util.control.Breaks.{break, breakable}

class FetcherEventBus(time: Time) {
  private val eventLock = new ReentrantLock()
  private val newEventCondition = eventLock.newCondition()

  private val queue = new PriorityQueue[QueuedFetcherEvent]
  private val scheduler = new SimpleScheduler[DelayedFetcherEvent]
  @volatile private var shutdownInitialized = false

  def size() = {
    queue.size()
  }

  /**
   * close should be called in a thread different from the one calling getNextEvent()
   */
  def close(): Unit = {
    shutdownInitialized = true
    inLock(eventLock) {
      newEventCondition.signalAll()
    }
  }

  def put(event: FetcherEvent): QueuedFetcherEvent = {
    inLock(eventLock) {
      val queuedEvent = new QueuedFetcherEvent(event, time.milliseconds())
      queue.add(queuedEvent)
      newEventCondition.signalAll()
      queuedEvent
    }
  }

  def schedule(delayedEvent: DelayedFetcherEvent) = {
    inLock(eventLock) {
      scheduler.schedule(delayedEvent)
      newEventCondition.signalAll()
    }
  }

  /**
   * There are 3 cases when the getNextEvent() method is called
   * 1. There is at least one delayed event that has become current. If so, we return the delayed event with the earliest
   * due time.
   * 2. There is at least one event in the queue. If so, we return the event with the highest priority from the queue.
   * 3. There are neither delayed events that have become current, nor queued events. We block until the earliest delayed
   * event becomes current. A special case is that there are no delayed events at all, under which we would block
   * indefinitely until being explicitly waken up by a new delayed or queued event.
   *
   * @return Either a QueuedFetcherEvent or a DelayedFetcherEvent that has become current
   */
  def getNextEvent(): Either[QueuedFetcherEvent, DelayedFetcherEvent] = {
    inLock(eventLock) {
      var result : Either[QueuedFetcherEvent, DelayedFetcherEvent] = null

      breakable {
        while (!shutdownInitialized) {
          val (delayedFetcherEvent, delayMs) = scheduler.peek()

//          println(s"delayed ${delayedFetcherEvent.nonEmpty}, queue:${!queue.isEmpty}")
          if (delayedFetcherEvent.nonEmpty) {
            // remove the item from the scheduler
            scheduler.poll()
            result = Right(delayedFetcherEvent.get)
            break
          } else if (!queue.isEmpty) {
            result = Left(queue.poll())
            break
          } else {
//            println(s"sleeping for ${delayMs}")
            newEventCondition.await(delayMs, TimeUnit.MILLISECONDS)
          }
        }
      }

      result
    }
  }

}