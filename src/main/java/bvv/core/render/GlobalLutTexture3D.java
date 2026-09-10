package bvv.core.render;

import bvv.core.backend.GpuContext;
import bvv.core.backend.Texture3D;
import static bvv.core.backend.Texture.InternalFormat.RGBA8UI;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class GlobalLutTexture3D implements Texture3D
{
	private final int[] maxsize = new int[ 3 ];
	
	public final List<Integer> zOffsetsPerVolume;
	private final List<int []> lutsSizes;
	private final List<ByteBuffer> uploadData;
	
	public GlobalLutTexture3D()
	{
		zOffsetsPerVolume = new ArrayList<>();
		uploadData = new ArrayList<>();
		lutsSizes = new ArrayList<>();
	}

	public void init( final GpuContext context )
	{
		context.delete( this );
		zOffsetsPerVolume.clear();
		uploadData.clear();
		lutsSizes.clear();
		maxsize[0] = 0;
		maxsize[1] = 0;
		maxsize[2] = 0;
	}


	public void addVolumeLUT(final int[] sizeLut, final ByteBuffer data)
	{
		final int zOffset = this.maxsize[2];
		zOffsetsPerVolume.add( zOffset );
		uploadData.add( data );
		lutsSizes.add( new int[] {sizeLut[0], sizeLut[1], sizeLut[2]} );
		maxsize[2] += sizeLut[2];
		maxsize[0] = Math.max(  sizeLut[0], maxsize[0]);
		maxsize[1] = Math.max(  sizeLut[1], maxsize[1]);

		
	}
	public void upload(final GpuContext context)
	{
		for(int i = 0; i < uploadData.size(); i++)
		{
			final int [] lutSize = lutsSizes.get( i );
		context.texSubImage3D(
				this,
				0,         
				0,         
				zOffsetsPerVolume.get( i ),   
				lutSize[ 0 ],
				lutSize[ 1 ],
				lutSize[ 2 ],
				uploadData.get( i )
			);
		}
	}
//	public void uploadVolumeLut( final GpuContext context, final int volumeSlot, final LookupTextureARGB lut )
//	{
//		final int zOffset = volumeSlot * lutDepth;
//		final int[] size = lut.getSize();
//
//		context.texSubImage3D(
//			this,
//			0,          // x offset
//			0,          // y offset
//			zOffset,    // z offset (slot index * lutDepth)
//			size[ 0 ],
//			size[ 1 ],
//			size[ 2 ],
//			lut.getData()
//		);
//	}
	public Vector3f getSize3f()
	{
		return new Vector3f( maxsize[ 0 ], maxsize[ 1 ], maxsize[ 2 ] );
	}

	// --- Texture3D Implementation ---

	@Override
	public InternalFormat texInternalFormat()
	{
		return RGBA8UI;
	}

	@Override
	public int texWidth()
	{
		return maxsize[0];
	}

	@Override
	public int texHeight()
	{
		return maxsize[1];
	}

	@Override
	public int texDepth()
	{
		return maxsize[2];
	}

	@Override
	public MinFilter texMinFilter()
	{
		return MinFilter.NEAREST;
	}

	@Override
	public MagFilter texMagFilter()
	{
		return MagFilter.NEAREST;
	}

	@Override
	public Wrap texWrap()
	{
		return Wrap.CLAMP_TO_EDGE;
	}
}