#SAUBible

[Southern Adventist University Computing Dept.](https://www.southern.edu/cs/Pages/default.aspx) -- Class project for W13-CPTR209

---

SAUBible is an Android 4+ application that allows a user to access the KJV bible with basic features such as jump-to reference, bookmarking, searching, a Home screen with a daily verse, and basic settings adjustment.

The following describes the project source code structure and what various components are for.

## /Resources

Folder for miscellaneous scripts and resources for the project.

File | Description
--- | ---
createDailyVerseSQLiteDB.rb | ruby script to generate daily verse sqlfile for assets
iconset.psd | photoshop file for icons in drawer menu, can be rasterized for multiple resolutions
[Files to Note]

## /SAUBible

### /src
This folder contains the Java source code. Currently there are three packages.

Package | Description
--- | ---
edu.southern | Contains the Java classes associated with the activities and fragments.
edu.southern.resources | Contains classes that interface and ease accessing the database engine
edu.southern.data | Contains classes that involve internal data files and database (currently loads SQLite database from assets folder)

### /assets
Contains all the resources necessary for the Bible engine to work. Daily Verse database (generated from ruby script in `/resources`) should be placed here.

### /libs
Compiled bible engine (written in C) files are placed here, with compiled versions for different architectures.

### /jni
C Bible Engine source. Changes will not compile currently (search Android NDK recompiling for getting started with compiling C code)

### /res
Here there be monsters! Here is the xml files for all aspects of views, images, menus, dimension, string, and other resources. It has grown quite a bit but has some order (see below). Android resource folders has qualifiers such as `drawable-land` or `values-sw400dp` which with the application will use to search for the most relevant resource. For example, a 10in tablet will pull values from `values-sw720dp` before looking down to the other values folder. See the [resource docs](http://developer.android.com/guide/topics/resources/providing-resources.html#AlternativeResources) for more details.

Resource | Description
--- | ---
drawable | Contains the images and xml drawable files for views and styles to reference
layout | Contains the fragment and activity views that the Java source pull from
values | Contains string, dimension files, styles, and colors resources

---

## Appendix A. Approximate File Structure

* src
	* edu.southern
	* edu.southern.resources
* gen
	* com.slidingmenu.lib
	* edu.southern
* assets
	* CINDEX.KJV
	* CONCORD.KJV
	* KJVLEX.DAT
	* KJVLEX.LZW
	* KJVLEX.NDX
	* LZWTABLE.KJV
	* SRCHSTRONGS.DAT
	* SRCHSTRONGS.NDX
	* STATS.KJV
	* STRONGS.DAT
	* STRONGS.NDX
	* VERSE.KJV
	* VINDEX.KJV
	* WORDS.KJV
	* WORDSNDX.KJV
	* XREF.DAT
	* XREF.NDX
* bin
	* dexedLibs
	* res
	* AndroidManifest.xml
	* classes.dex
	* jarlist.cache
	* R.txt
	* resources.ap_
	* SAUBible.apk
* jni
	* Android.mk
	* CBibleENgine.h
	* CEMARGIN.CPP
	* CEMARGIN.H
	* CeStrongs.cpp
	* CeStrongs.h
	* edu_southern_CBibleEngine.h
	* Jceversetbl.h
	* JCMargin.cpp
	* JCMargin.h
	* JCStrongs.cpp
	* JCStrongs.h
	* JCVerse.cpp
	* JCVerse.h
* libs
	* armeabi
	* armeabi-v7a
	* x86
	* android-support-v4.jar
* obj
	* local
* res
	* drawable
	* drawable-hdpi
	* drawable-ldpi
	* drawable-mdpi
	* drawable-xhdpi
	* layout
	* menu
	* values
	* values-sw600dp
	* values-sw600dp-land
	* values-v11
	* values-v14
* AndroidManifest.xml
* proguard-project.text
* project.properties
