Sample Program - Filer
======================
This program was not intended for commercial use.  The Filer program is a simple example
of retrieving a file from multiple nodes (or peers).

How it works
============
Filer shows an example of broadcasting a request for a file by name.  Nodes on the network
respond if they have the file along with the size and date.  The originator of the request
collects the responses and creates a list of nodes from whom it will retrieve the file.  Filer
then sends a request for a chunk of the file from each node on the list.  The nodes then
respond by sending the chunk requested back to the caller.  The originator of the request
then reassembles the file in its proper order.

Why we wrote this sample
========================
Why is it that when we attempt to watch a video from the web, even if we have a T1, the video
stops and re-buffers?  Why is it that when I attempt to download a large file from the web, on
my 384K DSL line, I only receive it at 128K?  The answer: server bottlenecks.  The servers are
simply overloaded with requests.  The cure: distributed servers with intelligent software.

Even unintelligent distributed networks will become overloaded with server bottlenecks without
sophisticated software.  DSL and high-speed cable providers are rolling out Internet access with
greater download speeds than upload (e.g. 128K upload, 1.5M download).  As these installations
become more prevelant, it makes much more sense to intelligently distribute software in smaller
chunks from multiple servers.  A client with 1.5M download could receive a file much quicker
in chunks from 10 providers with 128K upload speeds if the chunks are sent simultaneously.

This is why we wrote Filer.

Improvements
============
There can be many.  This sample was written in about 1 1/2 days.  It needs much work and to
become much more intelligent.  CRC's would be a good idea.  More intelligence could definitely
be built into the download list derivation.  File intelligence (size, date, etc.) also needs
some work.

Have fun, the basics are here...