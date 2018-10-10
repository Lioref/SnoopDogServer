# SnoopDogServer
The project is an implementation of a server meant to be used by dog owners who's dogs have a tendency towards barking when left alone. <br/>

The project is still in progress and is being written for learning purposes of multi-threaded programming, socket programming and data line programming in java, and of course, to be used for my dog :) <br/>

## The server does the following things:
1. Records short intervals of the room where the computer is, analyzing the noise in the room. <br/>
2. If the sound is above a certain level (a field in the sound library), it sends a "Bark!" message to all clients.<br/>
3. The server can play a recorded message upon request "play command" from the client. The recording should either calm the dog or tell it to be queit. An example of a recording by me can be seen in the res file. <br/>
4. If a client requests "send audio", the server sends the client the sound interval clips, so that the user can listen in and confirm the sound is indeed barking before playing a command for the dog. <br/>
<br/>

The server currently supports sending audio to only a single user, and as mentioned above, still in progress. 

