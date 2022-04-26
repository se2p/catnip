package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

/** Contains a list of [[HintPostprocessor]] s that can be applied in that order
  * to a [[HintGenerationResult]].
  */
class PostProcessingDriver {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val processors = ListBuffer[HintPostprocessor]()

  def add(p: HintPostprocessor): Unit = {
    processors.addOne(p)
  }

  def add(ps: List[HintPostprocessor]): Unit = {
    processors.addAll(ps)
  }

  /** Applies all post-processors to `hints` in the order they were added.
    * @param hints
    *   the original list of hints.
    * @return
    *   a new list of hints with all postprocessing applied.
    */
  def process(hints: HintGenerationResult): HintGenerationResult = {
    processors.foldLeft(hints) { case (h, p) =>
      val beforeSize = h.hints.size
      val processed  = p.process(h)
      val sizeDiff   = beforeSize - processed.hints.size
      val verb = if (sizeDiff < 0) {
        "added"
      } else {
        "removed"
      }
      logger.info(s"${p.name} $verb ${sizeDiff.abs} hints.")

      processed
    }
  }

  /*
  /** Finds all classes in the JAR that implement the [[HintPostprocessor]]
   * interface.
   * @param jar
   *   some JAR file.
   * @return
   *   nothing, or a failure message of loading the JAR failed.
   */
  def add(jar: File): Unit = {
    val jarFile        = new JarFile(jar)
    val entries        = jarFile.entries()
    val urls           = List(new URL(s"jar:file:${jar.getCanonicalPath}!/"))
    val urlClassLoader = URLClassLoader.newInstance(urls.toArray)

    while (entries.hasMoreElements) {
      val entry = entries.nextElement()
      if (entry.getName.endsWith(".class")) {
        val className = entry.getName
          .substring(0, entry.getName.length - 6)
          .replace("/", ".")
        val c = urlClassLoader.loadClass(className)
        if (
          c.getInterfaces
            .exists(interface => interface.getName == "HintPostprocessor")
        ) {
          processors.addOne(
            c.getConstructors.head
              .newInstance()
              .asInstanceOf[HintPostprocessor]
          )
        }
      }
    }

    jarFile.close()
  }
   */
}
