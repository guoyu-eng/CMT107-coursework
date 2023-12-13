package Objects;

public class SCube extends SObject {
    private float sideLength;

    public SCube(float sideLength) {
        super();
        this.sideLength = sideLength;
        update();
    }

    @Override
    protected void genData() {
        //  generate a simple cube centered at the origin
        float halfSide = sideLength / 2.0f;

        // Vertices
        float[] vertices = {
                -halfSide, halfSide, halfSide,   // triangle 1
                halfSide, halfSide, halfSide,
                halfSide, -halfSide, halfSide,

                -halfSide, halfSide, halfSide,   // triangle 2
                halfSide, -halfSide, halfSide,
                -halfSide, -halfSide, halfSide,

                // back face
                -halfSide, halfSide, -halfSide,   // triangle 1
                halfSide, -halfSide, -halfSide,
                halfSide, halfSide, -halfSide,

                -halfSide, halfSide, -halfSide,   // triangle 2
                -halfSide, -halfSide, -halfSide,
                halfSide, -halfSide, -halfSide,

                // left face
                -halfSide, halfSide, halfSide,   // triangle 1
                -halfSide, halfSide, -halfSide,
                -halfSide, -halfSide, halfSide,

                -halfSide, halfSide, -halfSide,   // triangle 2
                -halfSide, -halfSide, -halfSide,
                -halfSide, -halfSide, halfSide,

                // right face
                halfSide, halfSide, halfSide,   // triangle 1
                halfSide, -halfSide, halfSide,
                halfSide, halfSide, -halfSide,

                halfSide, halfSide, -halfSide,   // triangle 2
                halfSide, -halfSide, halfSide,
                halfSide, -halfSide, -halfSide,

                // top face
                halfSide, halfSide, halfSide,   // triangle 1
                halfSide, halfSide, -halfSide,
                -halfSide, halfSide, halfSide,

                -halfSide, halfSide, halfSide,   // triangle 2
                halfSide, halfSide, -halfSide,
                -halfSide, halfSide, -halfSide,

                // bottom face
                halfSide, -halfSide, halfSide,   // triangle 1
                -halfSide, -halfSide, halfSide,
                halfSide, -halfSide, -halfSide,

                -halfSide, -halfSide, halfSide,   // triangle 2
                -halfSide, -halfSide, -halfSide,
                halfSide, -halfSide, -halfSide
        };


        // Normals
        float[] normals = {
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,

                0, 0, 1,
                0, 0, 1,
                0, 0, 1,

                // back face
                0, 0, -1,
                0, 0, -1,
                0, 0, -1,

                0, 0, -1,
                0, 0, -1,
                0, 0, -1,

                // left face
                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,

                -1, 0, 0,
                -1, 0, 0,
                -1, 0, 0,

                // right face
                1, 0, 0,
                1, 0, 0,
                1, 0, 0,

                1, 0, 0,
                1, 0, 0,
                1, 0, 0,

                // top face
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,

                0, 1, 0,
                0, 1, 0,
                0, 1, 0,

                // bottom face
                0, -1, 0,
                0, -1, 0,
                0, -1, 0,

                0, -1, 0,
                0, -1, 0,
                0, -1, 0
        };


        // Indices for a simple cube
        int[] indices = {
                0, 1, 2, 2, 3, 0,
                4, 5, 6, 6, 7, 4,
                0, 4, 7, 7, 3, 0,
                1, 5, 6, 6, 2, 1,
                0, 1, 5, 5, 4, 0,
                2, 3, 7, 7, 6, 2
        };

        // Set the generated data
        this.vertices = vertices;
        this.normals = normals;
        this.textures = textures;
        this.indices = indices;
        this.numVertices = vertices.length / 3;
        this.numIndices = indices.length;
    }


    public void setSideLength(float sideLength) {
        this.sideLength = sideLength;
        updated = false;
    }

    public float getSideLength() {
        return sideLength;
    }
}


