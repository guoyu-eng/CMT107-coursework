/**
 * VC02 - Simple OpenGL Application
 *
 * This program demonstrates a basic OpenGL application using JOGL (Java Bindings for OpenGL).
 * It creates an OpenGL window, sets up a simple scene with a textured cube
 *
 * Dependencies:
 * - JOGL library (com.jogamp.opengl)
 *
 * Components:
 * - GLWindow: The OpenGL window for rendering.
 * - GLEventListener: Handles OpenGL events and rendering.
 * - ShaderProg: Basic shader program for rendering.
 * - Transform: Utility class for handling transformations.
 * - Vec4: Basic class for representing 4D vectors.
 * - SCube: Simple cube object data.
 *
 * Author: Guoyu
 */
import java.nio.FloatBuffer;
import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import static com.jogamp.opengl.GL3.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import Basic.ShaderProg;
import Basic.Transform;
import Basic.Vec4;
import Objects.SCube;
import javax.imageio.ImageIO;

public class VCCW03 {

    private GLWindow window; //Define a window
    final FPSAnimator animator=new FPSAnimator(60, true);
    final Renderer renderer = new Renderer();

    public VCCW03() {
        // Get OpenGL version 3 profile, and
        // enable the canvas use version 3
        GLProfile glp = GLProfile.get(GLProfile.GL3);
        GLCapabilities caps = new GLCapabilities(glp);
        window = GLWindow.create(caps);

        //Set the canvas to listen GLEvents from renderer
        window.addGLEventListener(renderer);
        //Set the canvas to listen mouse events from renderer
        window.addMouseListener(renderer);
        //window.addMouseMotionListener(renderer);

        // Animator act on canvas
        animator.add(window);

        window.setTitle("Coursework 3");
        window.setSize(500,500);
        window.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        window.setVisible(true);

        animator.start();

    }

    public static void main(String[] args) {
        new VCCW03();
    }

    class Renderer implements GLEventListener, MouseListener {

        // Define a Transformation instance
        // Transformation matrix is initialised as Identity;
        private Transform T = new Transform();

        //VAOs and VBOs parameters
        private int idPoint=0, numVAOs = 1;
        private int idBuffer=0, numVBOs = 1;
        private int[] VAOs = new int[numVAOs];
        private int[] VBOs = new int[numVBOs];

        //Model parameters
        private int numVertices = 36;
        private int vPosition;
        private int vColor;

        //Transformation parameters
        private int ModelView;
        private int Projection;
        private float scale = 0.5f;
        private float tx = 0;
        private float ty = 0;
        private float rx = 0;
        private float ry = 0;

        //Mouse position
        private int xMouse = 0;
        private int yMouse = 0;



        private int program;
        private SCube cube;

        //VAOs and VBOs parameters

        private int idElement=0, numEBOs = 1;


        private int[] EBOs = new int[numEBOs];

        //Model parameters
        private int numElements = 36;



        private int NormalTransform;

        ByteBuffer texImg;

        private int texWidth, texHeight;
        private int texName[] = new int[1];


        @Override
        public void display(GLAutoDrawable drawable) {
            // Get the GL pipeline object this
            GL3 gl = drawable.getGL().getGL3();

            // Enable 2D texturing
            gl.glEnable(GL_TEXTURE_2D);

            // Clear color and depth buffers
            gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Set point size and line width
            gl.glPointSize(5);
            gl.glLineWidth(5);

            // Initialize transformation matrix
            T.initialize();
            T.rotateZ(30.0f);
            T.rotateX(15.0f);
            T.rotateY(30.0f);

            //Key control interaction
            T.scale(scale, scale, scale);
            T.translate(tx, ty, 0);


            //Locate camera
            T.lookAt(0, 0, 0, 0, 0, -1, 0, 1, 0);  	//Default

            // Send model_view to shader. Here true for transpose
            //means converting the row-major matrix to column major one,
            //which is required for pre-multiplication matrix
            gl.glUniformMatrix4fv( ModelView, 1, true, T.getTransformv(), 0 );
            gl.glUniformMatrix4fv( NormalTransform, 1, true, T.getInvTransformTv(), 0 );

            gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL); //default

            gl.glDrawArrays(GL_TRIANGLES, 0, numVertices);
            gl.glDisable(GL_TEXTURE_2D);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
            System.exit(0);
        }

        @Override
        public void init(GLAutoDrawable drawable) {
            GL3 gl = drawable.getGL().getGL3();

            try {
                System.out.println("Generating texture from image...");
                // Read the texture image from a file
                texImg = readImage("WelshDragon.jpg");

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            // Generate a texture object
            gl.glGenTextures(1, texName, 0);
            // Bind the texture object
            gl.glBindTexture(GL_TEXTURE_2D, texName[0]);
            // Transfer image data to the texture object
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight,
                    0, GL_BGR, GL_UNSIGNED_BYTE, texImg);

            // Set texture parameters
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

            // Bind the texture unit to the texture sampler in the shader
            gl.glUniform1i(gl.glGetUniformLocation(program, "tex"), 0);
            // Activate the texture unit
            gl.glActiveTexture(GL_TEXTURE0);
            // Bind the texture object
            gl.glBindTexture(GL_TEXTURE_2D, texName[0]);

            float[] texCoord = {
                    // front face
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,

                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,

                    // back face
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f,

                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,

                    // left face
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,

                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,

                    // right face
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,

                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    0.0f, 0.0f,

                    // top face
                    1.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,

                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,

                    // bottom face
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,

                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f
            };

            //same color on each side
            float [] colorArray = {
                    0, 0, 0,  //Front color
                    0, 0, 0,
                    0, 0, 0,
                    0, 0, 0,  //Front color
                    0, 0, 0,
                    0, 0, 0,
                    0, 1, 0,  //Back color
                    0, 1, 0,
                    0, 1, 0,
                    0, 1, 0,
                    0, 1, 0,
                    0, 1, 0,
                    1, 0, 0,  //Front color
                    1, 0, 0,
                    1, 0, 0,
                    1, 0, 0,
                    1, 0, 0,
                    1, 0, 0,
                    1, 1, 0,  //Right color
                    1, 1, 0,
                    1, 1, 0,
                    1, 1, 0,
                    1, 1, 0,
                    1, 1, 0,
                    1, 0, 1,  //Top color
                    1, 0, 1,
                    1, 0, 1,
                    1, 0, 1,
                    1, 0, 1,
                    1, 0, 1,
                    0, 1, 1,  //Bottom color
                    0, 1, 1,
                    0, 1, 1,
                    0, 1, 1,
                    0, 1, 1,
                    0, 1, 1,

            };

            gl.glEnable(GL_DEPTH_TEST);
            int[] VAOs = new int[numVAOs];
            gl.glGenVertexArrays(numVAOs,VAOs,0);
            gl.glBindVertexArray(VAOs[idPoint]);


            SCube cube = new SCube(1.0f);
            float [] vertexArray = cube.getVertices();
            float [] normalArray = cube.getNormals();
            int [] vertexIndexs1 =cube.getIndices();

            numElements = cube.getNumIndices();
            // get the Buffer of ， vertex coordinate， normal coordinate ,vectors coordinate and color coordinate
            FloatBuffer normals = FloatBuffer.wrap(normalArray);
            FloatBuffer textures = FloatBuffer.wrap(texCoord);
            FloatBuffer vertices = FloatBuffer.wrap(vertexArray);
            FloatBuffer colors = FloatBuffer.wrap(colorArray);

            gl.glGenBuffers(numVBOs, VBOs,0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, VBOs[idBuffer]);

            //calculate the size of the element in Buffer
            long vertexSize = vertexArray.length*(Float.SIZE/8);
            long colorSize = colorArray.length*(Float.SIZE/8);
            long normalSize = normalArray.length*(Float.SIZE/8);
            long texSize = texCoord.length*(Float.SIZE/8);
            gl.glBufferData(GL_ARRAY_BUFFER, vertexSize + colorSize + texSize+normalSize+texSize,
                    null, GL_STATIC_DRAW);

            // Load the real data separately.
            gl.glBufferSubData( GL_ARRAY_BUFFER, 0, vertexSize, vertices );
            gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize, normalSize, normals );
            gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize+normalSize, texSize, textures );
            gl.glBufferSubData( GL_ARRAY_BUFFER, vertexSize+normalSize+texSize, colorSize, colors );

            IntBuffer elements = IntBuffer.wrap(vertexIndexs1);


            gl.glGenBuffers(numEBOs, EBOs,0);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBOs[idElement]);

            long indexSize = vertexIndexs1.length*(Integer.SIZE/8);
            gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexSize,
                    elements, GL_STATIC_DRAW); // pay attention to *Float.SIZE/8


            ShaderProg shaderproc = new ShaderProg(gl, "Texture.vert", "Texture.frag");
            int program = shaderproc.getProgram();
            gl.glUseProgram(program);


            // Initialize the vertex position attribute in the vertex shader
            vPosition = gl.glGetAttribLocation( program, "vPosition" );
            gl.glEnableVertexAttribArray(vPosition);
            gl.glVertexAttribPointer(vPosition, 3, GL_FLOAT, false, 0, 0L);

            vColor = gl.glGetAttribLocation( program, "vColor" );
            gl.glEnableVertexAttribArray(vColor);
            gl.glVertexAttribPointer(vColor, 3, GL_FLOAT, false, 0, vertexSize);

            int vTexCoord = gl.glGetAttribLocation(program, "vTexCoord");
            gl.glEnableVertexAttribArray(vTexCoord);
            gl.glVertexAttribPointer(vTexCoord, 2, GL_FLOAT, false, 0, vertexSize+normalSize);


            int vNormal = gl.glGetAttribLocation( program, "vNormal" );
            gl.glEnableVertexAttribArray(vNormal);
            gl.glVertexAttribPointer(vNormal, 3, GL_FLOAT, false, 0, vertexSize);



            //Get connected with the ModelView matrix in the vertex shader
            ModelView = gl.glGetUniformLocation(program, "ModelView");
            Projection = gl.glGetUniformLocation(program, "Projection");
            NormalTransform = gl.glGetUniformLocation(program, "NormalTransform");



            // Initialize shader lighting parameters
            float[] lightPosition = {5.0f, -3.0f, 15.0f, 0.0f};
            Vec4 lightAmbient = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            Vec4 lightDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            Vec4 lightSpecular = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);

            Vec4 materialAmbient = new Vec4(0.229412f, 0.223529f, 0.107451f, 1.0f);
            Vec4 materialDiffuse = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
            Vec4 materialSpecular = new Vec4(0.97654f, 0.97654f, 0.77654f, 1.0f);
            float  materialShininess = 100f;

            Vec4 ambientProduct = lightAmbient.times(materialAmbient);
            float[] ambient = ambientProduct.getVector();
            Vec4 diffuseProduct = lightDiffuse.times(materialDiffuse);
            float[] diffuse = diffuseProduct.getVector();
            Vec4 specularProduct = lightSpecular.times(materialSpecular);
            float[] specular = specularProduct.getVector();

            gl.glUniform4fv( gl.glGetUniformLocation(program, "AmbientProduct"),
                    1, ambient,0 );
            gl.glUniform4fv( gl.glGetUniformLocation(program, "DiffuseProduct"),
                    1, diffuse, 0 );
            gl.glUniform4fv( gl.glGetUniformLocation(program, "SpecularProduct"),
                    1, specular, 0 );

            gl.glUniform4fv( gl.glGetUniformLocation(program, "LightPosition"),
                    1, lightPosition, 0 );

            gl.glUniform1f( gl.glGetUniformLocation(program, "Shininess"),
                    materialShininess );

            gl.glUseProgram(program);
            gl.glBindTexture(GL_TEXTURE_2D, texName[0]);


            gl.glDrawElements(GL_TRIANGLES, numElements, GL_UNSIGNED_INT, 0);
            gl.glUniform1i( gl.glGetUniformLocation(program, "tex1"), 0 );


            // Debug output to check texture information
            System.out.println("Texture ID: " + texName[0]);
            System.out.println("Texture Width: " + texWidth + ", Height: " + texHeight);
            // Check for OpenGL errors
            // This is necessary. Otherwise, the The color on back face may display
            gl.glEnable(GL_DEPTH_TEST);
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int w,
                            int h) {

            GL3 gl = drawable.getGL().getGL3();
            gl.glViewport(x, y, w, h);

            T.initialize();
            if(h<1){h=1;}
            if(w<1){w=1;}
            float a = (float) w/ h;


            if (w < h) {
                T.ortho(-1, 1, -1/a, 1/a, -1, 1);
            }
            else{
                T.ortho(-1*a, 1*a, -1, 1, -1, 1);
            }


            // Convert right-hand to left-hand coordinate system
            T.reverseZ();
            gl.glUniformMatrix4fv( Projection, 1, true, T.getTransformv(), 0 );

        }
        @Override
        public void mouseClicked(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mouseEntered(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mousePressed(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mouseReleased(com.jogamp.newt.event.MouseEvent mouseEvent) {

        }

        @Override
        public void mouseMoved(com.jogamp.newt.event.MouseEvent mouseEvent) {
            xMouse = mouseEvent.getX();
            yMouse = mouseEvent.getY();


        }

        @Override
        public void mouseDragged(com.jogamp.newt.event.MouseEvent mouseEvent) {
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();

            //left button down, move the object
            if((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) != 0){
                // Fill in code here, so that the object moves
                // in the same direction as the mouse motion.
                int dx = x - xMouse;
                int dy = y - yMouse;
                rx += (y-yMouse);
                // update
                xMouse = x;
                yMouse = y;

            }

            //right button down, rotate the object
            if((mouseEvent.getModifiers() & InputEvent.BUTTON3_MASK) != 0){
                // the rotation angle around the y axis
                ry += (x-xMouse);

                // Add code here to calculate the rotation angle around the x axis
                rx += (y-yMouse);

                xMouse = x;
                yMouse = y;
            }

            //middle button down, scale the object
            if((mouseEvent.getModifiers() & InputEvent.BUTTON2_MASK) != 0){
                //Add code here so that the object will scale down (shrink)
                // when the mouse moves up (y increases),
                // and it will scale up (expand) when the mouse moves down.
                scale *= Math.pow(1.1, (y-yMouse) * 0.5);

                xMouse = x;
                yMouse = y;


            }

        }
        private ByteBuffer readImage(String filename) throws IOException {
            try {
                ByteBuffer imgbuf;
                BufferedImage img = ImageIO.read(new FileInputStream(filename));

                texWidth = img.getWidth();
                texHeight = img.getHeight();

                // Debug output to check image information
                System.out.println("Loaded image: Width=" + texWidth + ", Height=" + texHeight);

                DataBufferByte datbuf = (DataBufferByte) img.getData().getDataBuffer();
                imgbuf = ByteBuffer.wrap(datbuf.getData());
                return imgbuf;
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }

        @Override
        public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent mouseEvent) {


        }
    }
}
