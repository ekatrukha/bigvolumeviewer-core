package bvv.core.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SourceData
{
    private final Matrix4f im;
    private final Vector3f sourcemin;
    private final Vector3f sourcemax;

    /**
     * Constructs a SourceData instance with direct object references.
     */
    public SourceData( Matrix4f im, Vector3f sourcemin, Vector3f sourcemax )
    {
        this.im = ( im != null ) ? new Matrix4f( im ) : new Matrix4f();
        this.sourcemin = ( sourcemin != null ) ? new Vector3f( sourcemin ) : new Vector3f();
        this.sourcemax = ( sourcemax != null ) ? new Vector3f( sourcemax ) : new Vector3f();
    }
 // --- Getters and Setters ---


    public Matrix4f getIm()
    {
        return im;
    }

    public Vector3f getSourcemin()
    {
        return sourcemin;
    }

    public Vector3f getSourcemax()
    {
        return sourcemax;
    }

    /**
     * Returns a 16-element float array of the 4x4 transform matrix 
     * in column-major order expected by OpenGL std140 buffers.
     */
    public float[] getImMatrixColumnMajor()
    {
        float[] dest = new float[ 16 ];
        im.get( dest );
        return dest;
    }
}
