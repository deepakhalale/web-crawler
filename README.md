# Web Crawler
Crawl all pages for a given domain and output a simple site map. 

# How do I build web crawler?

    mvn package 

# Unit tests

    mvn test

# How do I run web crawler

    java -jar ./target/web-crawler-1.0.jar --starting.url=<domain url>

# Limitations
* Fails when the domain is slow to respond and retries are exhausted.

# Improvements
* There are Thread.sleep in the codebase whose durations need be made configurable.
* Test coverage could be improved
* Can introduce BDD style code coverage, may be using cucumber

