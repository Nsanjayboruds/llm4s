# Running the Hybrid Search Comparison Example

## Quick Reference

### Prerequisites

- JDK 21+ 
- Scala 3.7.1
- SBT 1.10.6+
- Git

### Basic Setup

```bash
# Clone the repository
git clone https://github.com/llm4s/llm4s.git
cd llm4s

# Install pre-commit hooks
./hooks/install.sh

# Set up your environment (optional - for real embeddings)
export OPENAI_API_KEY=sk-your-key-here
```

## Running the Example

### Option 1: No API Keys (Mock Embeddings)

This runs the example with mock embeddings - perfect for testing:

```bash
cd /home/nishant-borude/Documents/llm/llm4s
sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
```

**What happens:**
- Loads 8 sample documents about LLMs and RAG
- Creates in-memory embeddings (no API calls)
- Runs 4 different search queries
- Displays results with scores and timing

**Duration:** ~2-5 seconds

### Option 2: With Real OpenAI Embeddings

For better semantic search results:

```bash
export OPENAI_API_KEY=sk-proj-your-key-here
sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
```

**What's different:**
- Uses OpenAI's text-embedding-3-small
- Better semantic understanding of documents
- Slightly slower (~3-10 seconds)
- More accurate relevance ranking

### Option 3: With Answer Generation

To also generate LLM-powered answers:

```bash
export OPENAI_API_KEY=sk-proj-your-key-here
export LLM_MODEL=openai/gpt-4o
sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
```

## Expected Output

### Header and Initialization
```
================================================================================
Hybrid Search Comparison Example
================================================================================

Ingesting sample documents...
  Ingesting 8 documents...
  Ingestion complete: 8 successful, 0 failed
```

### Search Results Section

```
Running search comparisons...
────────────────────────────────────────────────────────────────────────────────

Query: What is RAG and how does it work?
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Results (234 ms, 8 records found):

  1. [Score: 0.9312] Retrieval Augmented Generation (RAG) is a technique that 
     combines language models with information retrieval. RAG systems retrieve 
     relevant documents from a knowledge base and use them to augment the 
     model's context, enabling more accurate and up-to-date responses.
     Source: llm-concepts

  2. [Score: 0.8756] Hybrid search combines vector search and keyword search to 
     leverage their complementary strengths. Vector search finds semantically 
     similar documents, while keyword search finds exact matches. Reciprocal 
     Rank Fusion intelligently merges results from both approaches.
     Source: llm-concepts

  3. [Score: 0.7834] Vector embeddings transform text into numerical 
     representations that capture semantic meaning. These embeddings enable 
     semantic search by computing similarity between vectors in a...
     Source: llm-concepts
  ... and 5 more results

Metrics:
  Latency: 234 ms
  Results: 8
  Avg Score: 0.7834

Query: Explain vector embeddings and semantic search
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Results (189 ms, 8 records found):

  1. [Score: 0.9456] Vector embeddings transform text into numerical 
     representations that capture semantic meaning. These embeddings enable 
     semantic search by computing similarity between vectors in a...
     Source: llm-concepts

  2. [Score: 0.8123] Language models like GPT-4 and Claude are foundation models
     trained on vast amounts of text. They excel at understanding and 
     generating human-like text. However, their training data has a cutoff 
     date, making RAG essential for accessing current information.
     Source: llm-concepts

  ... and 6 more results

Metrics:
  Latency: 189 ms
  Results: 8
  Avg Score: 0.8012

[... similar output for remaining 2 queries ...]

================================================================================
Comparison Complete!
================================================================================
```

## Verifying the Output

### Key Things to Check

1. **All 8 documents ingested successfully**
   ```
   ✓ Ingestion complete: 8 successful, 0 failed
   ```

2. **All 4 queries returned results**
   ```
   ✓ Query 1: Results (234 ms, 8 records found)
   ✓ Query 2: Results (189 ms, 8 records found)
   ✓ Query 3: Results (156 ms, 8 records found)
   ✓ Query 4: Results (201 ms, 8 records found)
   ```

3. **Score ranges are valid (0.0-1.0)**
   ```
   ✓ Score: 0.9312  ✓ Score: 0.8756  ✓ Score: 0.7834
   ```

4. **Latency is reasonable**
   ```
   ✓ In-memory: 150-250 ms per query
   ✓ With OpenAI: 1000-3000 ms per query
   ```

5. **Results are semantically relevant**
   ```
   ✓ "RAG" query returns RAG-related documents first
   ✓ "embeddings" query returns embedding documents first
   ✓ "BM25" query returns keyword search documents first
   ```

## Running the Tests

### Run All Tests

```bash
cd /home/nishant-borude/Documents/llm/llm4s
sbt "samples/test" -- -k HybridSearchComparison
```

### Run Specific Test

```bash
# Test document ingestion
sbt "samples/test" -- -k "should ingest sample documents"

# Test semantic search
sbt "samples/test" -- -k "should perform semantic search"

# Test result scoring
sbt "samples/test" -- -k "should return results with scores"
```

### Test Output Example

```
[info] HybridSearchComparisonExampleSpec:
[info]
[info] HybridSearchComparisonExample
[info]   - should ingest sample documents without errors
[info]   - should perform semantic search successfully
[info]   - should return results with scores
[info]   - should handle empty queries gracefully
[info]   - should handle special characters in queries
[info]   - should ingest multiple documents and retrieve them
[info]   - should handle very long documents
[info]   - should maintain search relevance with topK limit
[info]   - should handle metadata correctly
[info]   - should deduplicate identical documents
[info]
[info] Run completed in 3.245 seconds
[info] Tests: passed 10
```

## Troubleshooting

### Issue: "sbt: command not found"

**Solution:** Install SBT
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install sbt

# macOS
brew install sbt

# Or download from https://www.scala-sbt.org/download.html
```

### Issue: "Cannot resolve symbol OpenAI"

**Solution:** Make sure you're in the right directory and build the project first
```bash
cd /home/nishant-borude/Documents/llm/llm4s
sbt core/compile
sbt samples/compile
```

### Issue: "Compilation errors about Result type"

**Solution:** Ensure using Scala 3.7.1
```bash
sbt scalaVersion
# Should output: 3.7.1
```

### Issue: Example runs but shows no results

**Solution:** Check document ingestion completed:
```
  Ingestion complete: 8 successful, 0 failed
```

If you see failures, the vector store might not be initialized properly. Try with mock embeddings first:
```bash
unset OPENAI_API_KEY  # Disable real embeddings
sbt "samples/runMain org.llm4s.samples.rag.HybridSearchComparisonExample"
```

## Next Steps

### 1. Explore the Code

Read through the implementation:
```bash
# Main example
cat modules/samples/src/main/scala/org/llm4s/samples/rag/HybridSearchComparisonExample.scala

# Tests
cat modules/samples/src/test/scala/org/llm4s/samples/rag/HybridSearchComparisonExampleSpec.scala
```

### 2. Modify and Experiment

Try making changes:

**Add more documents:**
```scala
sampleDocuments :+ (
  "doc-9",
  "Your new document content here..."
)
```

**Change search queries:**
```scala
val queries = Seq(
  "Your custom query 1",
  "Your custom query 2"
)
```

**Adjust chunking:**
```scala
.withChunking(
  ChunkerFactory.Strategy.Sentence,
  500,   // Increase chunk size
  100    // Increase overlap
)
```

### 3. Run Tests After Changes

```bash
sbt "samples/test"
```

### 4. Format Code

Before committing:
```bash
sbt scalafmtAll
```

## Performance Tips

### For Faster Development Loop
```bash
# Don't compile the whole project
sbt "samples/run-main org.llm4s.samples.rag.HybridSearchComparisonExample"
```

### For Better Search Quality
```bash
export OPENAI_API_KEY=sk-...           # Use real embeddings
export COHERE_API_KEY=...              # Add reranking
# Both significantly improve quality but take longer
```

### For Batch Testing
```bash
# Run all RAG examples
sbt "samples/test" -- -k "RAG.*Example"

# Run with verbose output
sbt "samples/test" -- -v
```

## File Locations

```
Project Root: /home/nishant-borude/Documents/llm/llm4s

Main Example:
  modules/samples/src/main/scala/org/llm4s/samples/rag/
  └── HybridSearchComparisonExample.scala

Tests:
  modules/samples/src/test/scala/org/llm4s/samples/rag/
  └── HybridSearchComparisonExampleSpec.scala

Documentation:
  ├── CONTRIBUTION_ISSUE.md (Issue template)
  ├── IMPLEMENTATION_SUMMARY.md (This doc)
  └── README.md (Project overview)

Related Examples:
  modules/samples/src/main/scala/org/llm4s/samples/rag/
  ├── RAGBuilderExample.scala
  ├── DocumentQAExample.scala
  ├── RAGASEvaluationExample.scala
  └── DocumentLoaderExample.scala
```

## Getting Help

### Documentation
- **RAG Guide:** https://llm4s.github.io/guide/rag
- **Vector Stores:** https://llm4s.github.io/guide/vector-store
- **Examples:** https://llm4s.github.io/examples/

### Community
- **Discord:** https://discord.gg/4uvTPn6qww
- **GitHub Issues:** https://github.com/llm4s/llm4s/issues
- **Email:** kannupriyakalra@gmail.com

### Related Examples
- `RAGBuilderExample.scala` - RAG configuration patterns
- `DocumentQAExample.scala` - Q&A workflows
- `RAGASEvaluationExample.scala` - Evaluation metrics

---

**Status:** Ready to Run ✅
**Last Updated:** February 4, 2026
