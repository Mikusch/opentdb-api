# OpenTDB API
A simple wrapper for the [OpenTDB API](https://opentdb.com/) by [PixelTail Games](https://www.pixeltailgames.com/).

## Usage
```java
// Create a new OpenTDB object and wait until it has received a token
OpenTDB api = OpenTDB.newOpenTDB().awaitToken();

// Send a request for 10 questions to the API
CompletableFuture<List<Question<?, ?>>> future = api.fetchQuestionsAsync(10);

// Await the requests's completion and print the received questions
future.thenAccept(questions -> questions.forEach(System.out::println));
```
