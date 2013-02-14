Sample Program - Quoter
=======================
This program was not intended for commercial use.  The Quoter program is a simple example
distributing information (in this case quotes) across an Adaptive network.

How it works
============
A node can request from the network (Adaptinet network) a list of symbols in which it would
like to receive updates.  This request is broadcast to the network.  Nodes respond if they
are currently receiving a symbol or symbols and if they can provide these symbols to the
requesting node.  The requesting Quoter chooses the best provider (based on their load and
response time) or providers of the quotes requested.  The requesting Quoter then subscribes
to the quote(s) from the best provider(s).

If no-one can provide a quote or quotes, the requester simply retrieves the quote(s) from
Yahoo and can now become a provider for the entire network (for those symbols).  Quoter
also monitors its provider list to make sure he is providing timely quotes.  If not, it
will unsubscribe from the provider and broadcast for a new provider.

Advantage
=========
With a few nodes retrieving Quotes, you could provide to thousands of subscribers.  It's just
a sample.  Have fun...