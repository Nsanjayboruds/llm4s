package org.llm4s.samples.rag

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 * Tests for HybridSearchComparisonExample
 *
 * Note: These tests verify the example structure without requiring
 * network access or external services. Actual RAG functionality is
 * tested in the core module.
 */
class HybridSearchComparisonExampleSpec extends AnyFlatSpec with Matchers {

  behavior of "HybridSearchComparisonExample"

  it should "have sample documents defined" in {
    val docs = HybridSearchComparisonExample.sampleDocuments
    docs should not be empty
    docs.size should be >= 3
  }

  it should "have valid document IDs" in {
    val docs = HybridSearchComparisonExample.sampleDocuments
    docs.foreach { case (id, content) =>
      id should not be empty
      content should not be empty
      id should startWith("doc-")
    }
  }

  it should "truncate long content correctly" in {
    val longText = "a" * 100
    val truncated = HybridSearchComparisonExample.truncateContent(longText, 50)
    truncated.length should be <= 53 // 50 + "..."
    truncated should endWith("...")
  }

  it should "not truncate short content" in {
    val shortText = "short"
    val truncated = HybridSearchComparisonExample.truncateContent(shortText, 50)
    truncated shouldBe shortText
  }

  it should "handle whitespace in content truncation" in {
    val text = "word1   word2\n\nword3"
    val truncated = HybridSearchComparisonExample.truncateContent(text, 100)
    truncated should not include regex("\\s{2,}")
  }
}

    rag match {
      case Some(r) =>
        sampleDocs.foreach { case (docId, content) =>
          val result = r.ingestText(content, docId)
          result should be a Symbol("right")
          result.map(_ >= 1) should be(Right(true))
        }

      case None => fail("RAG not initialized")
    }
  }

  it should "perform semantic search successfully" in {
    rag match {
      case Some(r) =>
        // Ingest a document
        r.ingestText("Large language models are transformers trained on text data.", "llm-doc")

        // Search for related concept
        val results = r.query("transformer neural networks")
        results should be a Symbol("right")
        results.map(_.nonEmpty) should be(Right(true))

      case None => fail("RAG not initialized")
    }
  }

  it should "return results with scores" in {
    rag match {
      case Some(r) =>
        r.ingestText("Cloud computing provides on-demand computing resources.", "cloud-doc")

        val results = r.query("cloud infrastructure")
        results match {
          case Right(records) =>
            records.nonEmpty should be(true)
            records.foreach { record =>
              record.score should be >= 0.0
              record.score should be <= 1.0
            }
          case Left(_) => fail("Search failed")
        }

      case None => fail("RAG not initialized")
    }
  }

  it should "handle empty queries gracefully" in {
    rag match {
      case Some(r) =>
        r.ingestText("Test document content.", "test-doc")
        val results = r.query("")

        // Empty query should either return empty results or error gracefully
        results match {
          case Right(records) => records.isEmpty should be(true)
          case Left(_)        => // Acceptable to return error for empty query
        }

      case None => fail("RAG not initialized")
    }
  }

  it should "handle special characters in queries" in {
    rag match {
      case Some(r) =>
        r.ingestText("Special characters: @#$% and &*().", "special-doc")

        val query = "Special characters and symbols"
        val results = r.query(query)

        results should be a Symbol("right")

      case None => fail("RAG not initialized")
    }
  }

  it should "ingest multiple documents and retrieve them" in {
    rag match {
      case Some(r) =>
        val docs = Seq(
          ("doc-a", "Artificial intelligence is transforming industries."),
          ("doc-b", "Machine learning is a subset of artificial intelligence."),
          ("doc-c", "Deep learning uses neural networks with multiple layers.")
        )

        docs.foreach { case (docId, content) =>
          val result = r.ingestText(content, docId)
          result should be a Symbol("right")
        }

        val results = r.query("machine learning and artificial intelligence")
        results match {
          case Right(records) =>
            records.nonEmpty should be(true)
            records.size should be <= docs.size

          case Left(_) => fail("Search failed")
        }

      case None => fail("RAG not initialized")
    }
  }

  it should "handle very long documents" in {
    rag match {
      case Some(r) =>
        val longDoc = ("Lorem ipsum dolor sit amet " * 100).take(5000)
        val result = r.ingestText(longDoc, "long-doc")

        result should be a Symbol("right")

        val searchResults = r.query("lorem ipsum")
        searchResults should be a Symbol("right")

      case None => fail("RAG not initialized")
    }
  }

  it should "maintain search relevance with topK limit" in {
    rag match {
      case Some(r) =>
        // Ingest documents
        val docs = Seq(
          ("query-1", "The quick brown fox jumps over the lazy dog."),
          ("query-2", "A fast red fox leaps over a sleepy canine."),
          ("query-3", "The swift brown fox hurdles over the sluggish dog."),
          ("query-4", "Unrelated content about cats and mice."),
          ("query-5", "More unrelated content about birds and trees.")
        )

        docs.foreach { case (docId, content) =>
          r.ingestText(content, docId)
        }

        val results = r.query("quick fox jumps")
        results match {
          case Right(records) =>
            // More relevant documents should appear first
            records.nonEmpty should be(true)
            if (records.size >= 2) {
              records.head.score should be >= records(1).score

            }

          case Left(_) => fail("Search failed")
        }

      case None => fail("RAG not initialized")
    }
  }

  it should "handle metadata correctly" in {
    rag match {
      case Some(r) =>
        val metadata = Map(
          "source" -> "test-source",
          "category" -> "test-category",
          "version" -> "1.0"
        )

        r.ingestText("Document with metadata.", "metadata-doc", metadata)

        val results = r.query("metadata document")
        results match {
          case Right(records) =>
            records.nonEmpty should be(true)
            // Metadata should be preserved
            records.foreach { record =>
              record.metadata should not be empty
            }

          case Left(_) => fail("Search failed")
        }

      case None => fail("RAG not initialized")
    }
  }

  it should "deduplicate identical documents" in {
    rag match {
      case Some(r) =>
        val docContent = "Identical content for all documents."

        // Ingest same content with different IDs
        r.ingestText(docContent, "dup-1")
        r.ingestText(docContent, "dup-2")
        r.ingestText(docContent, "dup-3")

        val results = r.query("identical content")
        results match {
          case Right(records) =>
            // Should return results but possibly deduplicated
            records.nonEmpty should be(true)

          case Left(_) => fail("Search failed")
        }

      case None => fail("RAG not initialized")
    }
  }
}
