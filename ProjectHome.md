This tool allows you to see the relationships between your tasks in a project visually.  You can point it at your Gradle script, and it will import any scripts used by that, then piece together a graph showing how those tasks depend on each other.

To run the program (it's Java `WebStart`), go to http://gradle-script-visualizer.googlecode.com/svn/trunk/webstart/index.html, full documentation (with example output) is  there so you can see exactly what it does before running it.

The UI is build using Intellij IDEA - you don't need this to build (or even have Gradle installed, as the Gradle wrapper is provided), but you'll need it if you want to change the UI.  Get the communitiy edition, it's free - http://www.jetbrains.com/idea/download/index.html .

Note - when you open the project in your IDE - import the Gradle script.  That's the best way to do it (although the IDEA and Eclipse plugins work), as it sets up the project better.