package de.uni_passau.fim.se2.catnip.hint_generation.model

import de.uni_passau.fim.se2.catnip.hint_generation.model.distance.Distance
import scala.collection.mutable

/** Stores distances between nodes and can construct a distance matrix on
  * demand.
  */
class DistanceMatrixBuilder[A, B](
    val notFoundPenalty: Double = DistanceMatrixBuilder.defaultPenalty,
    val fillerA: A,
    val fillerB: B
) {
  private val valuesOuter = mutable.HashMap.empty[A, Int]
  private val valuesInner = mutable.HashMap.empty[B, Int]

  private val distances   = mutable.Buffer.empty[Distance[A, B]]
  private var maxDistance = -1.0

  /** Add a new node pair with a distance in between.
    * @param distance
    *   the distance between two nodes that should be stored.
    */
  def +=(distance: Distance[A, B]): Unit = {
    if (!valuesOuter.contains(distance.from)) {
      valuesOuter.put(distance.from, valuesOuter.size)
    }
    if (!valuesInner.contains(distance.to)) {
      valuesInner.put(distance.to, valuesInner.size)
    }

    distances += distance
    maxDistance = math.max(maxDistance, distance.distance)
  }

  /** Constructs a matrix of distances between nodes.
    *
    * The diagonal is always zero, as it stores the distance of a node to
    * itself. The default value for unknown distances is the maximal found
    * distance plus the penalty.
    *
    * @return
    *   the list of stored nodes and their distances as specified above.
    */
  def build(): DistanceMatrix[A, B] = {
    val size   = math.max(valuesOuter.size, valuesInner.size)
    val matrix = Array.fill(size, size)(maxDistance + notFoundPenalty)

    distances
      .map(dist =>
        (valuesOuter(dist.from), valuesInner(dist.to), dist.distance)
      )
      .foreach { case (i, j, distance) => matrix(i)(j) = distance }

    def mapKeysToSortedList[T](m: mutable.HashMap[T, Int]): IndexedSeq[T] = {
      m.toList.sortBy(_._2).map(_._1).toIndexedSeq
    }

    val valuesOuterList = mapKeysToSortedList(valuesOuter).appendedAll(
      (valuesOuter.size until valuesInner.size).map(_ => fillerA)
    )
    val valuesInnerList = mapKeysToSortedList(valuesInner).appendedAll(
      (valuesInner.size until valuesOuter.size).map(_ => fillerB)
    )

    DistanceMatrix(
      valuesOuterList,
      valuesInnerList,
      fillerA,
      fillerB,
      matrix.map(_.toSeq).toSeq
    )
  }
}

object DistanceMatrixBuilder {
  private val defaultPenalty = 10.0

  def apply[A, B](
      distances: Iterable[Distance[A, B]],
      fillerA: A,
      fillerB: B
  ): DistanceMatrixBuilder[A, B] = {
    apply(10.0, distances, fillerA, fillerB)
  }

  def apply[A, B](
      penalty: Double,
      distances: Iterable[Distance[A, B]],
      fillerA: A,
      fillerB: B
  ): DistanceMatrixBuilder[A, B] = {
    val matrixBuilder =
      new DistanceMatrixBuilder[A, B](penalty, fillerA, fillerB)
    distances.foreach(matrixBuilder += _)
    matrixBuilder
  }
}
