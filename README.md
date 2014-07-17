OpenFlappyBird
==============

An open source clone of a famous flappy bird game for Android using the amazing [AndEngine][1]

![Logo](http://i.imgur.com/UO84Emn.png)

History
-------

When the original Flappy Bird game was removed from Google Play (and other well known app marketplaces), I, along with [thousands][2] of other unimaginative developers rushed to fill that void with our own crappy rip off clones. I must point out though that I merely saw this as an ideal excuse to learn how to use [AndEngine][1] rather than flood Google Play with [malware][3]. Incidently my clone (along with almost every other) was [removed][4] by Google rather swiftly! 

Anyway, in my pursuit of creating the perfect Flappy Bird clone I could find numerous fragmented tutorials, snippets and documentation but I found it frustratingly difficult to find actual examples of real, finished games that were open source - working code is after all the very best form of documentation. 

So, here is my slightly-rough-around-the-edges attempt to reproduce one of the most annoying yet popular games ever to hit mobile devices - I hope it can help some others to get to grips with game development on Android.

Instructions
--------
First check out the source of [AndEngine][5], add it to your Eclipse workspace as an Android library project. Check out the source for OpenFlappyBird and add it to your workspace - hopefully the reference to AndEngine should be ok if you have them both in the same directory. If not just remove the broken reference and add it again.

TODO
--------
  - Never did manage to finish the scoreboard stuff
  - Gradle-a-fy all teh things!
  - Maybe tidy things up, this was slapped together in 2 about evenings!

License
-------

                DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                    Version 2, December 2004

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>

 Everyone is permitted to copy and distribute verbatim or modified
 copies of this license document, and changing it is allowed as long
 as the name is changed.

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. You just DO WHAT THE FUCK YOU WANT TO.


[1]: http://www.andengine.org/
[2]: https://play.google.com/store/search?q=flappy%20bird
[3]: http://uk.pcmag.com/news/33567/nearly-80-percent-of-flappy-bird-clones-contained
[4]: http://i.imgur.com/bJSoYYI.png
[5]: https://github.com/nicolasgramlich/AndEngine
