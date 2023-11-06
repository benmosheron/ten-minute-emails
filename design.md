Initial thought was to use a SQL database, with a separate process removing out of date records.

General thoughts on the code: I'm not an evangelist for the type level stack, or tagless final style, but have been learning
it recently and thought this would be the most fun whilst fulfilling the requirements.


Going for simplicity, so will replace DB with an in memory map.

Could use ConcurrentHashMap from java, but I've been learning the typelevel stack recently and wanted to have some fun, so using Ref for thread safety.

If you try to add an email to a missing key, it will just ignore the email. This is a bit of a shortcut

Inbox uses a vector for fast appending.

I didn't implement pagination, a non-trivial implementation would involve splitting the inboxes for each key into multiple pages.


# Thoughts on the ID Generator

The requirements are not very restrictive:
* A user just needs to obtain an email address
* Two users should not generate the same address
* No restriction on user calling the API multiple times. The simplest implementation will always generate a new address (no need to incorporate the user info into the email)
* No restriction on the generated emails being ordered / sortable
* Bonus 1: human-readable - we can do easily by randomly picking from a list of words
* Bonus 2: scalable to distributed generation - Is it enough to ensure that all instances use a different RNG seed?

For my purposes, a simple PRNG is enough. For human readability, we can choose from a list of words. 
We should ensure the cardinality is high enough that collisions will be rare.

I have copied a list of 100 three-letter words from a website.
We can use four of these for a cardinality of 100 million.

# Adding a Basic HTTP Server

The spec doesn't mention an HTTP server, but it feels a bit sad to not add one, so I will add a minimal http4s server.
The server is not the focus of the project, so I won't spend much effort on it, and will omit:
* error codes
* security
* documentation
* tests
* logging
* JSON

The API will accept the following HTTP calls on localhost:8080

* `POST /email` - create a new temporary email
* `POST /email/<temporary email prefix>` - add a new email to the inbox of this temporary email
  * request body: data for the email to be added 
* `GET /email/<temporary email prefix>` - get all emails for this temporary email

Data is accepted/returned in plain text.

