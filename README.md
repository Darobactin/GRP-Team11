# GRP-Team11
Welcome to our repository!

### Acknowledgements

Project BeautyCamera is built based on existing project [xufuji456](https://github.com/xufuji456)/**BeautyCamera** available at https://github.com/xufuji456/BeautyCamera

Project MagicCameraNew is built based on existing project [wuhaoyu1990](https://github.com/wuhaoyu1990)/**MagicCamera** available at https://github.com/wuhaoyu1990/MagicCamera

<h3>Assignments</h3>

<h4>For all:</h4>

In the next semester we will make use of this repository to complete this project efficiently. All codes will be submitted here. Please create your own branch and post merge requirements if needed. If you don't have the permission to pull files, please contact me and I will add you as the collaborator.

The new framework is `MagicCameraNew` and I hope everyone can read the code in advance.

<h4>For Edward's subgroup:</h4>
1. **Decoration library**

   Add new masks and widgets on `MagicCameraNew/app/src/main/assets/filter`.

2. **Take pictures review preview and short video**

   Implement all features on `CameraActivity` and `EditActivity`.

   You can modify copy relevant codes from wuhaoyu1990's old eclipse project.

<h4>For Jason's subgroup:</h4>

1. **Face and landmark detection algorithm research**

   Please write a function or class containing a function that takes a Bitmap image as its parameter and returns the positions of faces.

2. **Beautify the face algorithm research and search pictures function**

   Now we use openGL to construct filters. You need to learn how to use openGL adequately.

   Fortunately there are several filters that can be followed as examples. A filter contains four parts, that is, 

   filter class at `MagicCameraNew/library/src/main/java/com/seu/magicfilter/filter/advanced`

   texture image at `MagicCameraNew/app/src/main/assets/filter`

   vertex shader and fragment shader at `MagicCameraNew/library/src/main/res/raw`