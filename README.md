# CMT107-coursework
## Overview

This coursework consists of two parts related to graphics and Image processing, respectively
This program has been successfully run in IntellJ and completes all requirements including Interactive Transformations, Modelling and Lighting, Texture mapping and Image Processing.

Please install the right package

from 
<pre>
https://jogamp.org/deployment/jogamp-current/archive/
</pre>

choose the 
<pre>
gluegen-rt,jar
jogl-all.jar
</pre>

install in the global library first and choose the module and add again.

## graphics
Mainly designed are Scube, SObject, SSphere, Steapot and STorus classes, as well as four related shaders: Gouraud.frag, Gouraud.vert, Texture.frag and Texture.vert, among which WelshDragon.jpg is to be used. The texture pictures are in the same directory as the main running program。 
### How to Use
VCCW01，VCCW02，VCCW03 It is the main running file of this part. You can run it by pressing the run key. If everything runs normally, a result window will pop up to display various required graphics.

## Image processing

This Java program performs image processing and corner detection using the Harris Corner Detection algorithm. It includes features such as Sobel filtering to compute image gradients, calculating Harris corner strength, applying a threshold to detect corners, and an improved version that rejects weaker corners in close proximity to stronger ones.


### Features
Image Processing: The program uses Sobel filters to compute the gradient of the image. It also includes options for applying sharpening and blurring filters.（may be not used in this project)

Harris Corner Detection: The Harris Corner Detection algorithm is implemented to identify corners in the image based on the computed corner strength.

Improved Corner Detection: An improved version is implemented to reject weaker corners in close proximity to stronger ones, enhancing the accuracy of corner detection.

Visualization: The program displays the original image, the result of standard corner detection, and the improved corner detection side by side for visual comparison.

Safeguard ：From the initial processing of the image (greying), to the calculation of the response value, to the final setting of the threshold or the use of T to reject lower intensity values are implemented in separate methods, which facilitates the maintenance and modification of the code.

### How to Use
Room.jpg should be in the same directory as the VCCW04 file, running the VCCW04 file directly will give you two differently processed results, but note that I have not used bright colours for the corners, i.e. grey dots similar to the background image, so please look carefully!

## License

This program is released under the MIT License.

Feel free to experiment with the code and adapt it to your needs. If you find any issues or have suggestions, please open an issue or contribute to the project.




