package bvv.debug;

import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import bdv.viewer.ConverterSetups;
import bdv.viewer.SourceAndConverter;
import bvv.vistools.Bvv;
import bvv.vistools.BvvFunctions;
import bvv.vistools.BvvOptions;


public class DebugVolumeNumberRaw
{
	public static void main( final String[] args )
	{
		int nVolumeEdge = 10;

		final Bvv bvv = BvvFunctions.show(BvvOptions.options().frameTitle( "Test max number of volumes" ));
		int nMaxVolumesToTry = 30;
		int[] bestGrid = findOptimalGridDimensions(nMaxVolumesToTry);
		int nx = bestGrid[0];
        int ny = bestGrid[1];
        double coeff = 1.5;
        double spacingX = nVolumeEdge * coeff;
        double spacingY = nVolumeEdge * coeff;
        double spacingZ = nVolumeEdge * coeff;
		for (int i = 0; i < nMaxVolumesToTry; i++) {
            int gridX = i % nx;
            int gridY = (i / nx) % ny;
            int gridZ = i / (nx * ny);

            double px = gridX * spacingX;
            double py = gridY * spacingY;
            double pz = gridZ * spacingZ;
            final AffineTransform3D t = new AffineTransform3D();
            t.translate( px, py, pz );
            String sTitle = Integer.toString( gridX ) + " " + Integer.toString( gridY ) + " " + Integer.toString( gridZ );
            final Img< ? > rai = makeCachedCellImg(new UnsignedByteType(), nVolumeEdge, 128, 255 );
            BvvFunctions.show( rai, sTitle, Bvv.options().addTo( bvv ).sourceTransform( t ));       
        }
		//assign random color
		final List< SourceAndConverter< ? > > sacList = bvv.getBvvHandle().getViewerPanel().state().getSources();
		final ConverterSetups convS = bvv.getBvvHandle().getConverterSetups();
		int sN = 0;
		for(final SourceAndConverter< ? > sac : sacList)
		{

			float hue = (float) sN / nMaxVolumesToTry;
		    int rgb = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
			convS.getConverterSetup( sac ).setColor( new ARGBType(rgb) );
		    sN++;
		}
	}
	
	public static int[] findOptimalGridDimensions(int n) {
        int s = (int) Math.ceil(Math.cbrt(n));
        
        int[] bestGrid = new int[]{1, 1, n};
        int minVolumeDiff = Integer.MAX_VALUE;
        int minShapeDiff = Integer.MAX_VALUE;

        // Search dimensions around the cube root boundary
        for (int x = 1; x <= s + 1; x++) {
            for (int y = x; y <= s + 1; y++) {
                int z = (int) Math.ceil((double) n / (x * y));
                if (z < y) continue; // Keep x <= y <= z

                int volume = x * y * z;
                int volDiff = volume - n;
                int shapeDiff = (y - x) + (z - y) + (z - x);

                // Prefer minimum empty space, then most cubic aspect ratio
                if (volDiff < minVolumeDiff || (volDiff == minVolumeDiff && shapeDiff < minShapeDiff)) {
                    minVolumeDiff = volDiff;
                    minShapeDiff = shapeDiff;
                    bestGrid[0] = z;
                    bestGrid[1] = y;
                    bestGrid[2] = x;
                }
            }
        }
        
        return bestGrid;
    }
	
	static < T extends RealType< T > & NativeType< T >> Img< T > makeCachedCellImg(final T type, final int nEdge, final double minAmp, final double maxAmp)
	{
		final long[] dims = new long[] {nEdge, nEdge, nEdge};
		final ReadOnlyCachedCellImgFactory factory = new ReadOnlyCachedCellImgFactory(
				ReadOnlyCachedCellImgOptions.options().cellDimensions( 32, 32, 32 ) );
		double period = nEdge*0.5 + Math.random() *  nEdge*0.5;
		final Img< T > cellimg = factory.create( dims, type, cell -> {
			Cursor< T > cursor = cell.localizingCursor();
			final double [] pos = new double[3];
			while(cursor.hasNext())
			{
				cursor.fwd();
				cursor.localize( pos );
				double val = gyroid(pos, period, minAmp, maxAmp);
				cursor.get().setReal( val ); 
			}
			//Thread.sleep( 80 );
		});

		return cellimg;
	}
	
	static double gyroid(final double [] pos, final double period, final double minAmp, final double maxAmp) 
	{
		double w = 2.0 * Math.PI / period;

		
		double g =   Math.sin(pos[0] * w ) * Math.cos(pos[1] * w) 
				   + Math.sin(pos[1] * w ) * Math.cos(pos[2] * w ) 
				   + Math.sin(pos[2] * w ) * Math.cos(pos[0] * w );
		g = Math.pow((Math.tanh( g ) + 1) * 0.5, 7);
		return g  * (maxAmp - minAmp) + minAmp;
		}
}
