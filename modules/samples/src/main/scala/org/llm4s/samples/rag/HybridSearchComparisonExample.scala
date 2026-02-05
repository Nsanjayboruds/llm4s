package org.llm4s.samples.rag

import org.llm4s.chunking.ChunkerFactory
import org.llm4s.config.Llm4sConfig
import org.llm4s.llmconnect.config.{ EmbeddingModelConfig, EmbeddingProviderConfig }
import org.llm4s.rag.{ RAG, RAGConfig }
import org.llm4s.types.Result
import org.slf4j.LoggerFactory

/**
 * Hybrid Search Comparison Example
 *
 * Demonstrates different RAG search strategies with a small in-memory corpus.
 * This example requires an embedding provider (OpenAI, Anthropic, etc.) to be configured.
 *
 * == Quick Start ==
 *
 * {{{
 * # Configure your LLM provider
 * export LLM_MODEL=openai/gpt-4o
 * export OPENAI_API_KEY=sk-...
 *
 * # Run the example
 * sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
 * }}}
 *
 * == What This Example Shows ==
 *
 * - Building a RAG pipeline with proper configuration
 * - Ingesting documents with error handling
 * - Running queries and displaying results
 * - Proper resource cleanup with bracket pattern
 *
 * @see [[org.llm4s.rag.RAG]] for RAG pipeline details
 */
object HybridSearchComparisonExample {
  private val logger = LoggerFactory.getLogger(getClass)

  // Sample RAG corpus about LLMs and retrieval (public for testing)
  val sampleDocuments = Seq(
    (
      "doc-1",
      """Retrieval Augmented Generation (RAG) is a technique that combines
        |language models with information retrieval. RAG systems retrieve relevant
        |documents from a knowledge base and use them to augment the model's context,
        |enabling more accurate and up-to-date responses.""".stripMargin
    ),
    (
      "doc-2",
      """Vector embeddings transform text into numerical representations that
        |capture semantic meaning. These embeddings enable semantic search by
        |computing similarity between vectors in a high-dimensional space.""".stripMargin
    ),
    (
      "doc-3",
      """BM25 is a probabilistic relevance framework for full-text search.
        |Unlike vector embeddings, BM25 ranks documents based on term frequency
        |and inverse document frequency.""".stripMargin
    ),
    (
      "doc-4",
      """Hybrid search combines vector search and keyword search to leverage
        |their complementary strengths. Vector search finds semantically similar
        |documents, while keyword search finds exact matches.""".stripMargin
    )
  )

  def main(args: Array[String]): Unit = {
    logger.info("=" * 80)
    logger.info("Hybrid Search Comparison Example")
    logger.info("=" * 80)

    val result = runComparison()

    result match {
      case Right(_) =>
        logger.info("=" * 80)
        logger.info("Comparison Complete!")
        logger.info("=" * 80)

      case Left(error) =>
        logger.error("Error: {}", error.formatted)
        System.exit(1)
    }
  }

  /**
   * Main comparison logic with proper resource management.
   * Uses bracket pattern to ensure RAG is closed even on failure.
   */
  def runComparison(): Result[Unit] = {
    for {
      // Get embedding config from environment
      embeddingProviderConfig <- Llm4sConfig.embeddingProvider()
      
      // Build RAG pipeline
      _ <- bracket(buildRAG(embeddingProviderConfig)) { rag =>
        for {
          _ <- {
            logger.info("Ingesting sample documents...")
            ingestDocuments(rag)
          }
          _ <- {
            logger.info("\nRunning queries...")
            runQueries(rag)
          }
        } yield ()
      }
    } yield ()
  }

  /**
   * Bracket pattern for resource management.
   * Ensures resource is closed even if operation fails.
   */
  private def bracket[A <: AutoCloseable, B](
    acquire: Result[A]
  )(use: A => Result[B]): Result[B] = {
    acquire.flatMap { resource =>
      val result = use(resource)
      resource.close()
      result match {
        case Left(err) =>
          logger.warn("Operation failed, resource cleaned up: {}", err.formatted)
          Left(err)
        case r => r
      }
    }
  }

  /**
   * Build RAG pipeline with embedding provider config.
   */
  private def buildRAG(embeddingProviderConfig: EmbeddingProviderConfig): Result[RAG] = {
    val embeddingModelConfig = EmbeddingModelConfig.fromProviderConfig(embeddingProviderConfig)
    
    RAG.fromEmbeddingConfig(
      embeddingModelConfig = embeddingModelConfig,
      embeddingProviderConfig = embeddingProviderConfig,
      config = RAGConfig(
        chunkingConfig = ChunkerFactory.Config(
          strategy = ChunkerFactory.Strategy.Sentence,
          maxTokens = 300,
          overlapTokens = 50
        )
      )
    )
  }

  /**
   * Ingest documents with proper error handling.
   * Returns error if any ingestion fails.
   */
  private def ingestDocuments(rag: RAG): Result[Unit] = {
    sampleDocuments.foldLeft(Right(()): Result[Unit]) { case (acc, (docId, content)) =>
      acc.flatMap { _ =>
        rag.ingestText(content, docId, Map("source" -> "llm-concepts")).flatMap { chunkCount =>
          logger.info("  Ingested {}: {} chunks", docId, chunkCount: Integer)
          Right(())
        }
      }
    }
  }

  /**
   * Run queries with proper error handling.
   */
  private def runQueries(rag: RAG): Result[Unit] = {
    val queries = Seq(
      "What is RAG and how does it work?",
      "Explain vector embeddings"
    )

    queries.foldLeft(Right(()): Result[Unit]) { case (acc, query) =>
      acc.flatMap { _ =>
        logger.info("\nQuery: {}", query)
        executeQuery(rag, query)
      }
    }
  }

  /**
   * Execute a single query and display results.
   */
  private def executeQuery(rag: RAG, query: String): Result[Unit] = {
    val startTime = System.currentTimeMillis()

    rag.query(query).map { results =>
      val duration = System.currentTimeMillis() - startTime
      
      logger.info("Results ({} ms, {} found):", duration: java.lang.Long, results.size: Integer)
      
      results.take(3).zipWithIndex.foreach { case (result, idx) =>
        val truncated = truncateContent(result.content, 80)
        logger.info("  {}. [Score: {:.3f}] {}", 
          (idx + 1): Integer, 
          result.score: java.lang.Double, 
          truncated)
      }
    }
  }

  /**
   * Truncate text for display (public for testing).
   */
  def truncateContent(text: String, maxLen: Int): String = {
    val cleaned = text.replaceAll("\\s+", " ").trim
    if (cleaned.length > maxLen) cleaned.substring(0, maxLen) + "..."
    else cleaned
  }
}
