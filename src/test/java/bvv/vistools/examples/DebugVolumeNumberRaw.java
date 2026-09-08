package bvv.vistools.examples;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import bdv.BigDataViewer;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.transformation.TransformedSource;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvv.vistools.Bvv;
import bvv.vistools.BvvFunctions;
import bvv.vistools.BvvOptions;
import mpicbg.spim.data.SpimDataException;


public class DebugVolumeNumberRaw
{
	public static void main( final String[] args )
	{
		final String xmlFilename = "/home/eugene/Desktop/projects/bvv/20260908_volume_test/10x10x10.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}		

		final List<SourceAndConverter<?>> sources = new ArrayList<>();
		BigDataViewer.initSetups( spimData, new ArrayList<>(), sources );


		Source<?> source = sources.get(0).getSpimSource();
		final Bvv bvv = BvvFunctions.show(BvvOptions.options().frameTitle( "Test max number of volumes" ));
		int nMaxVolumesToTry = 15;
		int[] bestGrid = findOptimalGridDimensions(nMaxVolumesToTry);
		int nx = bestGrid[0];
        int ny = bestGrid[1];
        double spacingX = 10 * 1.2;
        double spacingY = 10 * 1.2;
        double spacingZ = 10 * 1.2;
		for (int i = 0; i < nMaxVolumesToTry; i++) {
            int gridX = i % nx;
            int gridY = (i / nx) % ny;
            int gridZ = i / (nx * ny);

            double px = gridX * spacingX;
            double py = gridY * spacingY;
            double pz = gridZ * spacingZ;
            final AffineTransform3D t = new AffineTransform3D();
            t.translate( px, py, pz );
            TransformedSource<?> transformedSource = new TransformedSource<>(source);
            transformedSource.setIncrementalTransform(t);
            BvvFunctions.show( transformedSource, Bvv.options().addTo( bvv ));       
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
}
