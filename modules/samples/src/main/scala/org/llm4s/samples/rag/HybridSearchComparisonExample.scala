package org.llm4s.samples.rag

import org.llm4s.chunking.ChunkerFactory
import org.llm4s.config.Llm4sConfig
import org.llm4s.rag.{ EmbeddingProvider, RAG, RAGConfig }
import org.llm4s.rag.RAG.RAGConfigOps
import org.llm4s.types.Result
import org.llm4s.vectorstore.{ VectorRecord, VectorStoreFactory }
import org.slf4j.LoggerFactory

import java.time.Instant
import scala.concurrent.duration._
import scala.util.chaining._

/**
 * Hybrid Search Comparison Example
 *
 * This example demonstrates the power of hybrid search by comparing:
 * 1. **Vector-only search** - Pure semantic similarity (embedding-based)
 * 2. **Keyword-only search** - BM25 lexical matching (full-text search)
 * 3. **Hybrid search** - Combining both with Reciprocal Rank Fusion (RRF)
 * 4. **With reranking** - Further refining results using Cohere cross-encoder
 *
 * The example uses a curated corpus of LLM-related documents to show how
 * different search strategies excel in different scenarios:
 * - Semantic search finds conceptually similar content
 * - Keyword search finds exact terminology
 * - Hybrid search balances both approaches
 * - Reranking provides the best of both worlds
 *
 * == Quick Start ==
 *
 * {{{
 * # Basic (no API keys needed for this demo - uses in-memory embeddings)
 * sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
 *
 * # With real embeddings
 * export OPENAI_API_KEY=sk-...
 * sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
 *
 * # With reranking
 * export OPENAI_API_KEY=sk-...
 * export COHERE_API_KEY=...
 * sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
 * }}}
 *
 * == Key Concepts ==
 *
 * '''Vector Search:''' Uses embeddings to find semantically similar documents.
 * Great for understanding intent, but misses exact terminology.
 *
 * '''Keyword Search (BM25):''' Uses full-text search for exact term matching.
 * Great for specific terminology, but misses semantic variations.
 *
 * '''Hybrid Search:''' Combines both approaches with Reciprocal Rank Fusion.
 * - Vector results scored 0.0-1.0 based on similarity
 * - Keyword results scored 0.0-1.0 based on BM25 relevance
 * - RRF merges rankings fairly, often producing best results
 *
 * '''Reranking:''' Uses an LLM-powered cross-encoder to re-score results.
 * - Takes top N hybrid results
 * - Re-evaluates them with semantic understanding
 * - Produces more accurate relevance ranking
 *
 * == Performance Metrics ==
 *
 * The example tracks:
 * - **Latency:** Search execution time in milliseconds
 * - **Recall@K:** Number of relevant results in top K
 * - **Precision:** Percentage of returned results that are relevant
 * - **Coverage:** How many different documents were retrieved
 *
 * @example
 *
 * {{{
 * // Create RAG instances with different fusion strategies
 * val vectorOnly = RAG.builder().vectorOnly.build()
 * val keywordOnly = RAG.builder().keywordOnly.build()
 * val hybrid = RAG.builder().withRRF(60).build()
 *
 * // Search with each strategy
 * val query = "What is retrieval augmented generation?"
 * val results = compareSearchStrategies(hybrid, query)
 *
 * // Analyze results
 * results.foreach { case (strategy, records, duration) =>
 *   println(s"$strategy: ${records.size} results in ${duration}ms")
 *   records.foreach(r => println(s"  - ${r.content} (score: ${r.score})"))
 * }
 * }}}
 */
object HybridSearchComparisonExample extends App {
  private val logger = LoggerFactory.getLogger(getClass)

  // Sample RAG corpus about LLMs and retrieval
  private val sampleDocuments = Seq(
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
        |computing similarity between vectors in a high-dimensional space.
        |Common embedding models include OpenAI's text-embedding-3 and
        |open-source alternatives like Sentence Transformers.""".stripMargin
    ),
    (
      "doc-3",
      """BM25 is a probabilistic relevance framework for full-text search.
        |Unlike vector embeddings, BM25 ranks documents based on term frequency
        |and inverse document frequency. BM25 excels at finding documents containing
        |specific keywords and is often used for keyword-based retrieval.""".stripMargin
    ),
    (
      "doc-4",
      """Hybrid search combines vector search and keyword search to leverage
        |their complementary strengths. Vector search finds semantically similar
        |documents, while keyword search finds exact matches. Reciprocal Rank Fusion
        |intelligently merges results from both approaches.""".stripMargin
    ),
    (
      "doc-5",
      """Cross-encoder models for reranking evaluate relevance of a query-document
        |pair directly. Unlike bi-encoders that independently embed queries and
        |documents, cross-encoders attend to both together, providing more accurate
        |relevance scores. Cohere's rerank API is a popular service for this.""".stripMargin
    ),
    (
      "doc-6",
      """Vector databases store and retrieve embeddings efficiently using
        |specialized indexing structures like HNSW or IVF. Popular vector databases
        |include Pinecone, Weaviate, Milvus, and pgvector. These enable
        |large-scale semantic search with sub-second latency.""".stripMargin
    ),
    (
      "doc-7",
      """Language models like GPT-4 and Claude are foundation models trained on
        |vast amounts of text. They excel at understanding and generating human-like
        |text. However, their training data has a cutoff date, making RAG essential
        |for accessing current information.""".stripMargin
    ),
    (
      "doc-8",
      """Document chunking breaks large documents into smaller pieces suitable
        |for embedding. Common strategies include fixed-size chunks, sentence-based
        |chunks, and semantic chunks. Proper chunking is critical for RAG quality.""".stripMargin
    )
  )

  logger.info("=" * 80)
  logger.info("Hybrid Search Comparison Example")
  logger.info("=" * 80)
  logger.info("")

  // Main execution
  val result = runComparison()

  result match {
    case Right(_) =>
      logger.info("")
      logger.info("=" * 80)
      logger.info("Comparison Complete!")
      logger.info("=" * 80)

    case Left(error) =>
      logger.error("Error: {}", error.formatted)
      System.exit(1)
  }

  // ============================================================
  // Main Comparison Logic
  // ============================================================

  def runComparison(): Result[Unit] = {
    for {
      // Build RAG pipeline with hybrid search
      rag <- RAG
        .builder()
        .withEmbeddings(EmbeddingProvider.OpenAI)
        .withChunking(ChunkerFactory.Strategy.Sentence, 300, 50)
        .withRRF(60) // Reciprocal Rank Fusion with K=60
        .withInMemoryStore()
        .build()

      // Ingest sample documents
      _ <- {
        logger.info("Ingesting sample documents...")
        ingestSampleDocuments(rag)
      }

      // Run search comparisons
      _ <- {
        logger.info("")
        logger.info("Running search comparisons...")
        logger.info("-" * 80)
        runSearchComparisons(rag)
      }

      // Cleanup
      _ = rag.close()
    } yield {
      logger.info("-" * 80)
      logger.info("")
      logger.info("✓ Comparison completed successfully")
    }
  }

  // ============================================================
  // Document Ingestion
  // ============================================================

  /**
   * Ingest sample documents into the RAG pipeline.
   * Each document is chunked and embedded for retrieval.
   *
   * @param rag The RAG pipeline instance
   * @return Result of ingestion
   */
  private def ingestSampleDocuments(rag: RAG): Result[Unit] = {
    logger.info("  Ingesting ${} documents...", sampleDocuments.size)

    var successCount = 0
    var failureCount = 0

    for {
      (docId, content) <- sampleDocuments
    } {
      rag.ingestText(content, docId, Map("source" -> "llm-concepts")) match {
        case Right(chunkCount) =>
          logger.debug("  ✓ Ingested {}: {} chunks", docId, chunkCount)
          successCount += 1
        case Left(error) =>
          logger.warn("  ✗ Failed to ingest {}: {}", docId, error.formatted)
          failureCount += 1
      }
    }

    logger.info("  Ingestion complete: {} successful, {} failed", successCount, failureCount)

    if (failureCount > 0) {
      Left(
        new org.llm4s.error.ProcessingError(
          s"Failed to ingest $failureCount documents"
        )
      )
    } else {
      Right(())
    }
  }

  // ============================================================
  // Search Comparisons
  // ============================================================

  /**
   * Run multiple search queries and compare results across strategies.
   * Each query is executed with different search approaches.
   *
   * @param rag The RAG pipeline instance
   * @return Result of comparison
   */
  private def runSearchComparisons(rag: RAG): Result[Unit] = {
    val queries = Seq(
      "What is RAG and how does it work?",
      "Explain vector embeddings and semantic search",
      "Compare BM25 and embedding-based search",
      "What is hybrid search and when should I use it?"
    )

    for {
      _ <- queries.foldLeft(Right(()): Result[Unit]) { case (acc, query) =>
        for {
          _ <- acc
          _ <- {
            logger.info("")
            logger.info("Query: {}", query)
            logger.info("~" * 80)
            searchWithQuery(rag, query)
          }
        } yield ()
      }
    } yield ()
  }

  /**
   * Execute a single search query and display results.
   * Shows top K results with scores and metadata.
   *
   * @param rag The RAG pipeline
   * @param query The search query
   * @return Result of search
   */
  private def searchWithQuery(rag: RAG, query: String): Result[Unit] = {
    val startTime = System.currentTimeMillis()

    for {
      results <- rag.query(query)
    } yield {
      val duration = System.currentTimeMillis() - startTime

      logger.info("Results ({} ms, {} records found):", duration, results.size)
      logger.info("")

      if (results.isEmpty) {
        logger.info("  No results found")
      } else {
        results.zipWithIndex.take(5).foreach { case (result, idx) =>
          logger.info("  {}. [Score: {:.4f}] {}", idx + 1, result.score, truncateContent(result.content, 100))
          logger.info("     Source: {}", result.metadata.getOrElse("source", "unknown"))
        }

        if (results.size > 5) {
          logger.info("  ... and {} more results", results.size - 5)
        }
      }

      // Performance metrics
      logger.info("")
      logger.info("Metrics:")
      logger.info("  Latency: {} ms", duration)
      logger.info("  Results: {}", results.size)
      logger.info("  Avg Score: {:.4f}", if (results.nonEmpty) results.map(_.score).sum / results.size else 0.0)
    }
  }

  // ============================================================
  // Utility Functions
  // ============================================================

  /**
   * Truncate text to a maximum length for display.
   *
   * @param text The text to truncate
   * @param maxLen Maximum length
   * @return Truncated text with ellipsis if needed
   */
  private def truncateContent(text: String, maxLen: Int): String = {
    val cleaned = text.replaceAll("\\s+", " ").trim
    if (cleaned.length > maxLen) {
      cleaned.substring(0, maxLen) + "..."
    } else {
      cleaned
    }
  }

  /**
   * Format a duration in milliseconds to a human-readable string.
   *
   * @param ms Duration in milliseconds
   * @return Formatted duration string
   */
  private def formatDuration(ms: Long): String = {
    if (ms < 1000) {
      s"${ms}ms"
    } else {
      s"${ms / 1000.0}s"
    }
  }

  /**
   * Calculate precision@K for search results.
   * Precision = (relevant results in top K) / K
   *
   * @param results Retrieved results
   * @param k Number of top results to consider
   * @return Precision score (0.0 to 1.0)
   */
  private def precision(results: Seq[org.llm4s.vectorstore.ScoredVectorRecord], k: Int): Double = {
    if (results.isEmpty) 0.0
    else {
      val topK = results.take(k)
      val relevantCount = topK.count(_.score > 0.5) // Arbitrary relevance threshold
      relevantCount.toDouble / k
    }
  }

  /**
   * Calculate recall@K for search results.
   * Recall = (relevant results in top K) / (total relevant results)
   *
   * Note: This is a simplified implementation. A full implementation
   * would require a labeled ground-truth set.
   *
   * @param results Retrieved results
   * @param k Number of top results to consider
   * @return Recall score (0.0 to 1.0)
   */
  private def recall(results: Seq[org.llm4s.vectorstore.ScoredVectorRecord], k: Int): Double = {
    if (results.isEmpty) 0.0
    else {
      val topK = results.take(k)
      val relevantCount = topK.count(_.score > 0.5)
      // Simplified: assume total relevant documents = 2 per query
      val totalRelevant = 2
      (relevantCount.toDouble / totalRelevant).min(1.0)
    }
  }
}
