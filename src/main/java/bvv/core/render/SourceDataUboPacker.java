package bvv.core.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.joml.Vector3f;

public class SourceDataUboPacker
{
    /**
     * Stride per SourceData struct in std140 layout:
     * - mat4 im  : 16 floats                     = 64 bytes
     * - vec3 min : 3 floats (+ 4 bytes padding)  = 16 bytes
     * - vec3 max : 3 floats (+ 4 bytes padding)  = 16 bytes
     * Total      : 96 bytes (multiple of 16)
     */
    public static final int STRUCT_SIZE_BYTES = 96;

    private final float[] matrixBuffer = new float[16];

    /**
     * Packs a list of SourceData instances into a target direct ByteBuffer.
     * Reuses the targetBuffer to prevent Garbage Collection overhead per frame.
     *
     * @param sources      List of SourceData objects
     * @param targetBuffer Direct ByteBuffer (capacity >= sources.size() * 96)
     */
    public void pack( List< SourceData > sources, ByteBuffer targetBuffer )
    {
        targetBuffer.clear();

        for ( SourceData src : sources )
        {
            // 1. mat4 im (16 floats column-major = 64 bytes)
            src.getIm().get( matrixBuffer );
            for ( int i = 0; i < 16; i++ )
            {
                targetBuffer.putFloat( matrixBuffer[i] );
            }

            // 2. vec3 sourcemin (12 bytes) + 4 bytes padding -> 16 bytes total
            Vector3f min = src.getSourcemin();
            targetBuffer.putFloat( min.x );
            targetBuffer.putFloat( min.y );
            targetBuffer.putFloat( min.z );
            targetBuffer.putFloat( 0.0f ); // Pad offset 76..79

            // 3. vec3 sourcemax (12 bytes) + 4 bytes padding -> 16 bytes total
            Vector3f max = src.getSourcemax();
            targetBuffer.putFloat( max.x );
            targetBuffer.putFloat( max.y );
            targetBuffer.putFloat( max.z );
            targetBuffer.putFloat( 0.0f ); // Pad offset 92..95
        }

        targetBuffer.flip();
    }

    /**
     * Allocates a native-ordered direct ByteBuffer for a given maximum source capacity.
     */
    public static ByteBuffer allocateBuffer( int maxSources )
    {
        return ByteBuffer.allocateDirect( maxSources * STRUCT_SIZE_BYTES )
                         .order( ByteOrder.nativeOrder() );
    }
}