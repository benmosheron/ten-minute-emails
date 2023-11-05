Initial thought was to use a SQL database, with a separate process removing out of date records.

General thoughts on the code: I'm not an evangelist for the type level stack, or tagless final style, but have been learning
it recently and thought this would be the most fun whilst fulfilling the requirements.


Going for simplicity, so will replace DB with an in memory map.

Could use ConcurrentHashMap from java, but I've been learning the typelevel stack recently and wanted to have some fun, so using Ref for thread safety.

If you try to add an email to a missing key, it will just ignore the email. This is a bit of a shortcut

Inbox uses a vector for fast appending.

I didn't implement pagination, a non-trivial implementation would involve splitting the inboxes for each key into multiple pages.