package Objects;
public class STorus extends SObject{
    private float innerRadius;
    private float outerRadius;
    private int sides;
    private int rings;
    private int slices;
    private int stacks;

    public STorus(float innerRadius, float outerRadius, int sides, int rings) {
        super();
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.sides = sides;
        this.rings = rings;
        update();
    }

    @Override
    protected void genData() {
        int i, j, k;

        // Calculate the angles for generating vertices
        double deltaRing = 2 * PI / rings;
        double deltaSide = 2 * PI / sides;

        // Generate vertices coordinates, normal values, and texture coordinates
        numVertices = (sides + 1) * (rings + 1);
        vertices = new float[numVertices * 3];
        normals = new float[numVertices * 3];
        textures = new float[numVertices * 2];

        k = 0;
        for (i = 0; i <= rings; i++) {
            double phi = i * deltaRing;
            for (j = 0; j <= sides; j++) {
                double theta = j * deltaSide;

                double x = (outerRadius + innerRadius * cos(theta)) * cos(phi);
                double y = (outerRadius + innerRadius * cos(theta)) * sin(phi);
                double z = innerRadius * sin(theta);

                vertices[3 * k] = (float) x;
                vertices[3 * k + 1] = (float) y;
                vertices[3 * k + 2] = (float) z;

                // Calculate normal vector
                double nx = cos(theta) * cos(phi);
                double ny = cos(theta) * sin(phi);
                double nz = sin(theta);

                normals[3 * k] = (float) nx;
                normals[3 * k + 1] = (float) ny;
                normals[3 * k + 2] = (float) nz;

                // Calculate texture coordinates
                textures[2 * k] = (float) j / sides;
                textures[2 * k + 1] = (float) i / rings;

                k++;
            }
        }

        // Generate indices for the triangular mesh
        numIndices = sides * rings * 6;
        indices = new int[numIndices];

        k = 0;
        for (i = 0; i < rings; i++) {
            for (j = 0; j < sides; j++) {
                int current = i * (sides + 1) + j;
                int next = current + sides + 1;

                // Create two triangles for each quad
                indices[k++] = current;
                indices[k++] = next;
                indices[k++] = current + 1;

                indices[k++] = current + 1;
                indices[k++] = next;
                indices[k++] = next + 1;
            }
        }
    }





public void setRadius(float radius){
        this.innerRadius = radius;
        updated = false;
    }

    public void setSlices(int slices){
        this.slices = slices;
        updated = false;
    }

    public void setStacks(int stacks){
        this.stacks = stacks;
        updated = false;
    }

    public float getRadius(){
        return innerRadius;
    }

    public int getSlices(){
        return slices;
    }

    public int getStacks(){
        return stacks;
    }

}



