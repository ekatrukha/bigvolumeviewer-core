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
	
	// 1. Calculate tile index on RAW pos (do NOT clamp with max(0.0) first!)
	vec3 tileIndexBase = floor( pos / cacheBlockSize );
	
	ivec3 localQ = ivec3( tileIndexBase - lutOffset );

	// 2. Strict bounds check: now correctly catches negative pos (< 0) and outer bounds (>= lutSize)
	if ( any( lessThan( localQ, ivec3( 0 ) ) ) || any( greaterThanEqual( localQ, ivec3( lutSize ) ) ) )
	    return 0.0;
	
	ivec3 globalQ = ivec3( localQ.x, localQ.y, localQ.z + cacheLutZOffset );	
	uvec4 lutv = texelFetch( globalCacheLut, globalQ, 0 );
	
	vec3 B0 = vec3( lutv.xyz ) * paddedBlockSize + cachePadOffset;
	vec3 sj = blockScales[ lutv.w ];
	
	// 3. Coarse tile origin alignment for multiscale levels (sj > 1)
	vec3 tileIndexCoarse = floor( pos / ( cacheBlockSize * sj ) );
	vec3 relativePos = pos - tileIndexCoarse * ( cacheBlockSize * sj );
	
	// 4. Clamp continuous position within block boundaries to prevent trilinear filtering bleeding
	vec3 localCachePos = clamp( relativePos * sj, vec3( 0.0 ), cacheBlockSize );
	vec3 c0 = ( B0 + localCachePos + 0.5 ) / cacheSize[cacheType];

	return texture( u_Caches[cacheType], c0 ).r;
}