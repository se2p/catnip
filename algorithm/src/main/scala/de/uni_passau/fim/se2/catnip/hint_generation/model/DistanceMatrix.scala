package de.uni_passau.fim.se2.catnip.hint_generation.model

import breeze.optimize.linear.KuhnMunkres

/** Represents the matrix of node distances created by
  * [[DistanceMatrixBuilder#build]].
  *
  * @param elementsOuterIndex
  *   the nodes represented in the rows of the matrix.
  * @param elementsInnerIndex
  *   the nodes represented in the columns of the matrix.
  * @param matrix
  *   the matrix of distances as described above.
  */
final case class DistanceMatrix[A, B](
    elementsOuterIndex: IndexedSeq[A],
    elementsInnerIndex: IndexedSeq[B],
    fillerElementA: A,
    fillerElementB: B,
    matrix: Seq[Seq[Double]]
) {
  require(
    elementsOuterIndex.lengthCompare(elementsInnerIndex.size) == 0,
    "The number of nodes in rows and columns is not equal!"
  )
  require(
    matrix.forall(_.lengthCompare(matrix.length) == 0),
    "The matrix is not square!"
  )
  require(
    elementsOuterIndex.lengthCompare(matrix.length) == 0,
    "The number of elements in the index lists do not match matrix dimensions!"
  )

  /** Uses the weights stored in `matrix` to compute a best mapping between
    * elements.
    * @return
    *   a map of elements (outer -> inner) for which a mapping could be found
    *   together with total mapping costs.
    */
  def computePairingsKuhnMunkres(): MatchingResult[A, B] = {
    if (elementsOuterIndex.isEmpty) {
      MatchingResult(Map.empty, 0.0)
    } else {
      val (pairIndices, totalCosts) = KuhnMunkres.extractMatching(matrix)

      val pairs = pairIndices.indices
        .map(idx => (idx, pairIndices(idx)))
        // if no pairing could be found, idxInner = -1
        .filter { case (_, idxInner) => idxInner >= 0 }
        .map { case (idxOuter, idxInner) =>
          elementsOuterIndex(idxOuter) -> elementsInnerIndex(idxInner)
        }
        // remove all filler elements
        .filter { case (a, b) => a != fillerElementA && b != fillerElementB }
        .toMap

      MatchingResult(pairs, totalCosts)
    }
  }
}
