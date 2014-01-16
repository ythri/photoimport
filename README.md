PhotoImport
===========

The PhotoImport tool is used to copy images from your digital camera onto your computer. It will automatically sort and organize your photos into a directory structure based on a configurable storage scheme, and possibly rename the files during that process.

A good organization of your image files is an essential part of most workflows. While you could also keep all your files in a single folder (or in the folder structure that your camera creates) and organize them purely using programs with integrated database like Aperture or Lightroom, this will lock you in to that program: Whenever you want to do something with a specific file, you will need to use that program. A much better way is to keep your images organized from the very start: By defining a storage scheme and consistently using it. For example, you could create subdirectories for each year, month and event. Then, when you want to look at photos from your trip to Paris in spring 2013, you will know where to search: In the folder 2013, subdirectory March or April, subdirectory named something like Trip_to_Paris.

Of course, such a storage scheme is only useful if applied consistently. Furthermore, importing all photos from your camera by hand, creating all needed subdirectories and renaming the files, is both tedious and error-prone. This is where tools like PhotoImport come into play: Once put your storage scheme into a configuration file, PhotoImport guarantees consistency and the whole import process can be executed with only a single command.

That being said, PhotoImport is a highly flexible tool that can support a large variety of different storage schemes. Besides that, it offers offers a set of other convenient features, like automatic backup during each import, automatic protection and verification of copied files, partial imports and more.


PhotoImport vs. DIM
-------------------

This tool is heavily inspired by the excellent [Digital Image Mover (Dim)](http://www.alanlight.com/dim/Dim.htm) by Alan Light. However, there are a few differences:

1.	PhotoImport is a command line tool and has no GUI (so far). Using a GUI, the import process can be controlled and adjusted to the current needs; however, for a fully automatic import process, the command line is much faster.

2.	PhotoImport is a lot more flexible than DIM: you can define your own variables besides those used in DIM like Client, Event and Job; you can define arbitrary targets (not just the main storage and a backup) and configure each target individually; you can specify subfolders for different extensions, e.g., to put raw files into a subfolder RAW/ relative to your jpgs; and much more.

3.	PhotoImport is easily configurable using an intuitive config files based on the JSON format instead of XML. Of course, this is mainly because in DIM you usually change the configuration in the GUI and do not directly edit the XML; the PhotoImport configuration must be edited manually.

4.	The photos that should be copied can be restricted to certain dates or numbers. This is especially important if you do not delete your SD card after every successful import, but leave old photos on there; then, you can make sure that the next import only copies newer photos.

5.	PhotoImport is missing a few features of DIM, like the Geolocation features introduced in the most recent version DIM 5.


Libraries
---------

PhotoImport depends on the following libraries:
-	JCommander to parse command line arguments
-	metadata-extractor to read exif tags from the image files
-	jackson to parse the configuration file
