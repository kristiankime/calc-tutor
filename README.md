# calc-tutor

This project is the code for my PhD thesis in [Professor Hickey's lab at Brandeis](http://www.cs.brandeis.edu/~tim/)

This project is a play application designed to be deployed on Heroku that is being used in my education research. 


## Tools and Support
The following sections detail tools and various other helpful things that were used in building the app.

### [Git](http://git-scm.com/)
This project is using Git as a version control system. If you are new to Git you can find an [install guide and crash course here](http://git-scm.com/book/en/Getting-Started-Git-Basics). 

While git can be [used locally](http://tiredblogger.wordpress.com/2009/11/09/creating-local-git-repositories-yeah-its-that-simple/), or you can host it yourself with something like [gitorious](http://gitorious.org/) this project will use [GitHub](https://github.com/). For information on how to get started with Github [check here](https://help.github.com/articles/set-up-git).

### [Play](http://www.playframework.com/)

Play is a Java or Scala based Web Framework, I've chosen to work in Scala for this project. Play requires a JDK be installed on your system. So if you don't already have one you'll need to get one. The project is currently using play 2.2.0. Details about how to install play (and a JDK) are on the [play install page](http://www.playframework.com/documentation/2.2.x/Installing).

Play has a lot to it. Documentation and tutorials can be found on the play web site. There is a [To Do List Example App here](http://www.playframework.com/documentation/2.2.x/ScalaTodoList). But if all you want to do is run this project you'll just need to get Play installed.

### [Eclipse](http://www.eclipse.org/)

Eclipse is a IDE which has support for Scala an even play projects. Initial installation is simple just download from the [download page](http://www.eclipse.org/downloads/) and put it where you want it. The project is tested under eclipse version 4.2 (Juno). 

Eclipse has many plugins that may be useful but the primary one for this project is the [Scala one](http://scala-ide.org/download/current.html). This project is currently tested using the scala ide from this download site http://download.scala-ide.org/sdk/e38/scala210/stable/site. The plugin recommends increasing the JVM heap size, instructions for which can be found [here](http://wiki.eclipse.org/FAQ_How_do_I_increase_the_heap_size_available_to_Eclipse%3F).

Another helpful plugin is EGit which can be found by searching in the eclipse market place.

### [Heroku](https://www.heroku.com/)

Heroku is a web hosting company that greatly simplifies deployment, maintenance and upgrading of your web application. Heroku has it a toolbelt you need to install but once in place it's pretty easy to use. Fortunately Play works nicely with heroku and they have instructions on installation etc [here](http://www.playframework.com/documentation/2.1.3/ProductionHeroku).

* http://www.playframework.com/documentation/2.5.x/ProductionHeroku * 

### [Slick](http://slick.lightbend.com/) 

 
### [H2](http://www.h2database.com/) 
Using H2 with play 

* http://www.playframework.com/documentation/2.2.x/ScalaDatabase
* http://www.playframework.com/documentation/2.2.x/Developing-with-the-H2-Database
* http://www.playframework.com/documentation/2.1.1/ProductionConfiguration

### [PostgreSQL](http://www.postgresql.org/)

* https://devcenter.heroku.com/articles/heroku-postgresql#local-setup
* http://postgresapp.com/

### [Play-Pac4j](https://github.com/pac4j/play-pac4j)
Pac4j is a general java authentication and authorization library that has a version for play.
By default the system makes it a little hard to allow users to pick which authentication method they like to use so I built a [sample app](https://github.com/kristiankime/play-pac4j-slick/) that routes the user to a login choice page if they are not authenticated.  

### [sbt-less](https://github.com/sbt/sbt-less)
To enable auto compilation of LESS I used the play [sbt-less](https://github.com/sbt/sbt-less) plugin

### [Purecss](http://purecss.io/)
Naming conventions: http://smacss.com/
Based on: http://necolas.github.io/normalize.css/
Skin Builder: http://yui.github.io/skinbuilder/index.html?opt=calctutor,045FE7,FFFFFF,1,1,10,1.5&h=0,-30,60&n=0,-30,75&l=0,-30,80&b=0,-30,90&mode=pure

https://css-tricks.com/snippets/css/a-guide-to-flexbox/

https://css-tricks.com/snippets/css/complete-guide-grid/
https://gridbyexample.com/examples/