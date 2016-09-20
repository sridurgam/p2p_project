Souce code sets up a peer-to-peer network for file downloading which resembles some features of Bit-torrent. There are two pieces of software – peer and file owner(server). The file owner breaks the file into chunks of 100KB, each stored as a
separate file. Peers connect to the file owner to download some chunks after which they maintain two threads of control for their upload and download neighbours to enable the exchange of chunks. A cyclic connection is established for the file distribution and the network remains active until all the peers get a copy of the entire file. The project is implemented in Java using Java Socket Programming APIs. 
