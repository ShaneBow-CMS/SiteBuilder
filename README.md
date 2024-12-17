# Site Builder

## Development

This is *Classic Java*, built with a batch file (`_build.bat`).

The directory structure exactly matches the package structure,
and the batch file sets the `classpath` accordingly.

### Compile and jar

* `cd` `~apps/src/com/shanebow/web/SiteBuilder`
* `./_build.bat`

### Run from command line

* `cd` `~apps/usr/shanebow/tools`
* `java -jar SiteBuilder.jar`

## Tabs

The tabs let you access various important directories for a project:

* __Source__ holds the directory tree of source files that are iterated over to build the target
* __Target__ holds the finished project which is generally uploaded to the web
* __Themes__ holds various themes that can be imported into source files
* __Libs__ holds code organized as components that can be imported into source files (via `~~some/lib/path`) and/or copied directly to the target directory tree (via a `.dir` file)
* __Apps__ is a configurable directory relative to the *Source* directory (note that you can use `..` to traverse up to/through the source's parent directory)
* __Assets__ is another configurable directory similar to *Apps*, except that it is relative to `public_html` when it exists

## Original Motivation

The following was written back in 2011, back in the days when web sites
were built as basically a set of static `html` pages sprinkled with some
Javascript or *Macromedia Flash* to provide some interactivity.

*Site Builder* does a lot of what *Dreamweaver* [did] — It helps you build web
 sites. But it has one feature that __I__ really need and love: the ability
 to switch from typing in English to Thai with the click of a button. This became
 mandatory for me when I launched [ThaiDrills](https://ThaiDrills.com):
 a site that teaches the Thai language.

Here's an example: __เขียนภาษาไทยในแบบง่ายและสนุก__ — it means *typing
 Thai is fun and easy*. Took about 30 seconds to do — Try *that* in
 *Dreamweaver* :)

Besides this, the interface is more to my liking as an HTML guy. There's no
 WYSIWYG here. And there's built in support for JavaScript and YUI.

Also I added some preprocessing for CSS files that let's me use variables for
 colors which get substituted in when the page is built.

A combo box at the bottom makes switching between sites a snap. Just navigate
 the directory tree and edit the "main content" of the selected page. The build
 process pulls in all the other pieces (like heading, side bar, and menus). Kind
 of like JSP, ASP, etc except that it's making static pages (albeit with ajax).

Each site gets a default *layout* which is really just an html file
 (sans the `<head>` and `<body>` tags) which can import other html
 fragments that I call *div* files.

So for example, if I created `menu.div` and `sidebar.div`,
 then the layout file could be set up as follows:

~~~ {.language-js .line-numbers}
<div class="yui3-g layout">
 <div class="yui3-u nav">
  ~menu.div
 </div>

 <div class="yui3-u main">
  ~top-menu.div
  ~content
 </div>
  ~footer.div
</div>
  ~scripts.div
~~~

You get the idea — the `~content` entry causes the current
 content page to get pulled in. You can create an arbitrary number of other
 layouts as well, but most sites will usually only have a couple — even a
 commerce site will have only a handful. And, it's easy to override the default
 layout for any page.

*Site Builder* is structured in a tab view with the following tabs:

* __Site Map__ This has a tree view of the site map being constructed and
   a table containing all of the pages in the site, and another listing all the
   site pages that are not yet in the site map. The whole thing works with drag
   and drop. In the tables you can specify page priority and change frequency for
   the search engines. There's also a button to edit the `robots.txt` file. The
   build process will create `sitemap.html`, `sitemap.xml`, and the `robots.txt` files.
* __Content__ A directory navigator (based at the site's source dir) and the
  files in the current dir on the left. The editor for the selected file on the
  right. Your usual buttons, and that all important "Thai" checkbox".
* __Templates__ This is where you edit those `.layout` files and `.div` files mentioned above.
* __Style__ Displays the content of the css directory and let's you edit `.css` files.
* __Script__ Displays the content of the js directory and let's you edit `.js` files.

There is full link check functionality: click the button to verify all links
 (including those in `<img>`, `<link>`, and `<script>` tags) —
 and identify orphan pages — both internal and external.

The upload button will ftp it all up to your domain.

This is one of the programs I use myself on a daily basis, so I've got it
 workin' pretty good...
