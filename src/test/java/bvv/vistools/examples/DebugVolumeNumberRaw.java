package bvv.vistools.examples;

import java.util.List;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvv.vistools.Bvv;
import bvv.vistools.BvvFunctions;
import bvv.vistools.BvvSource;
import bvv.vistools.BvvStackSource;
import mpicbg.spim.data.SpimDataException;

public class DebugVolumeNumberRaw
{
	public static void main( final String[] args )
	{
		final String xmlFilename = "/home/eugene/Desktop/projects/BigTrace/BigTrace_data/t1-head.xml";
		SpimDataMinimal spimData = null;
		try {
			spimData = new XmlIoSpimDataMinimal().load( xmlFilename );
		} catch (SpimDataException e) {
			e.printStackTrace();
		}		
		List< BvvStackSource< ? > > sources = BvvFunctions.show( spimData );
		final BvvSource source = sources.get(0);
		for(int i = 0; i < 15; i++)
		{
			BvvFunctions.show( spimData, Bvv.options().addTo( source ));
			System.out.println( "Volume " + Integer.toString( i+2 ));
		}
	}
}
