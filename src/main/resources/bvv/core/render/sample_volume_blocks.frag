uniform mat4 im;
uniform vec3 sourcemin;
uniform vec3 sourcemax;

void intersectBoundingBox( vec4 wfront, vec4 wback, out float tnear, out float tfar )
{
	vec4 mfront = im * wfront;
	vec4 mback = im * wback;
	intersectBox( mfront.xyz, (mback - mfront).xyz, sourcemin - 0.5, sourcemax + 0.5, tnear, tfar );
}

uniform vec3 blockScales[ NUM_BLOCK_SCALES ];
uniform vec3 lutOffset;
uniform vec3 lutSize; // Local volume LUT size (in tiles)
uniform int cacheType;
uniform int cacheLutZOffset;

float sampleVolume( vec4 wpos )
{
	vec3 pos = (im * wpos).xyz;
	
	// Clamp position to non-negative coordinates
	vec3 qPos = max( vec3( 0.0 ), pos );
	
	// Initial lookup using base resolution tile grid
	vec3 tileIndexBase = floor( qPos / cacheBlockSize );
	
	ivec3 localQ = ivec3( tileIndexBase - lutOffset );

	//check that it remains inside
	if ( any( lessThan( localQ, ivec3( 0 ) ) ) || any( greaterThanEqual( localQ, ivec3( lutSize ) ) ) )
	    return 0.0;
	
	ivec3 globalQ = ivec3( localQ.x, localQ.y, localQ.z + cacheLutZOffset );	
	uvec4 lutv = texelFetch( globalCacheLut, globalQ, 0 );
	
	vec3 B0 = vec3( lutv.xyz ) * paddedBlockSize + cachePadOffset;
	vec3 sj = blockScales[ lutv.w ];
	
	// Correct coarse tile origin alignment for sj > 1
	vec3 tileIndexCoarse = floor( qPos / ( cacheBlockSize * sj ) );
	vec3 relativePos = pos - tileIndexCoarse * ( cacheBlockSize * sj );
	
	// Calculate normalized cache coordinate
	vec3 c0 = ( B0 + relativePos * sj + 0.5 ) / cacheSize[cacheType];
	return texture( u_Caches[cacheType], c0 ).r;
}